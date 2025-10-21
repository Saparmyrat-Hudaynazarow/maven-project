package com.resource.service;

import com.resource.client.SongMetadataClient;
import com.resource.dto.ResourceDto;
import com.resource.dto.SongMetadataDto;
import com.resource.entity.Resource;
import com.resource.exception.InvalidMp3Exception;
import com.resource.exception.ResourceNotFoundException;
import com.resource.repository.ResourceRepository;
import com.resource.util.Utility;

import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceService {
    private static final String SUPPORTED_CONTENT_TYPE = "audio/mpeg";
    private static final int MAX_IDS_LENGTH = 200;
    private static final String METADATA_EXTRACTION_ERROR = "Failed to extract MP3 metadata";

    private final ResourceRepository resourceRepository;
    private final SongMetadataClient songMetadataClient;


    @Transactional
    public Map<String, Integer> saveResource(ResourceDto request) {
        validateMp3Request(request);

        Resource resource = toEntity(request);
        Resource savedResource = resourceRepository.saveAndFlush(resource);

        log.info("Resource saved with ID: {}", savedResource.getId());

        Metadata metadata = extractMetadata(savedResource.getContent());
        SongMetadataDto songMetadata = buildSongMetadataRequest(savedResource.getId(), metadata);

        log.debug("Prepared SongMetadataCreateRequest: {}", songMetadata);
        songMetadataClient.createSongMetadata(songMetadata);
        log.info("Song metadata successfully created in song-service");
        return Map.of("id", savedResource.getId());
    }

    @Transactional(readOnly = true)
    public ResourceDto getResource(String sid) {
        log.info("Fetching resource with ID: {}", sid);

        int id = Utility.parseAndValidateId(sid);

        Resource resource = resourceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Resource with ID=" + id + " not found"));

        log.debug("Resource found with byte length: {}", resource.getContent().length);

        Tika tika = new Tika();
        String detectedType = tika.detect(resource.getContent());
        return new ResourceDto(resource.getContent(), detectedType);
    }

    @Transactional
    public Map<String, List<Integer>> deleteByIds(String ids) {
        List<Integer> idList = Utility.validateAndParse(ids, MAX_IDS_LENGTH);

        if (idList.isEmpty()) {
            return Map.of("ids", Collections.emptyList());
        }

        List<Integer> existingIds = resourceRepository.findExistingIds(idList);

        if (existingIds.isEmpty()) {
            log.warn("No matching resources found for deletion");
            return Map.of("ids", Collections.emptyList());
        }

        resourceRepository.deleteAllByIdInBatch(existingIds);
        log.info("Deleted resources with IDs: {}", existingIds);

        Map<String, List<Integer>> deletedSongIds = songMetadataClient.deleteSongMetadata(existingIds);
        log.info("Deleted corresponding Song metadata IDs: {}", deletedSongIds.values());

        return Map.of("ids", existingIds);
    }


    private void validateMp3Request(ResourceDto request) {
        if (request == null) {
            throw new InvalidMp3Exception("Request cannot be null");
        }

        if (!SUPPORTED_CONTENT_TYPE.equalsIgnoreCase(request.contentType())) {
            throw new InvalidMp3Exception(
                "Invalid content type: " + request.contentType() + ". Only MP3 files are allowed");
        }

        if (request.data() == null || request.data().length == 0) {
            throw new InvalidMp3Exception("Empty or invalid MP3 file");
        }
    }

    private Metadata extractMetadata(byte[] content) {
        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        ParseContext parseContext = new ParseContext();
        Mp3Parser mp3Parser = new Mp3Parser();

        try (InputStream inputStream = new ByteArrayInputStream(content)) {
            mp3Parser.parse(inputStream, handler, metadata, parseContext);
            return metadata;
        } catch (Exception e) {
            log.error(METADATA_EXTRACTION_ERROR, e);
            throw new RuntimeException(METADATA_EXTRACTION_ERROR, e);
        }
    }

    private SongMetadataDto buildSongMetadataRequest(Integer resourceId, Metadata metadata) {
        return new SongMetadataDto(
            resourceId,
            metadata.get("dc:title"),
            metadata.get("xmpDM:artist"),
            metadata.get("xmpDM:album"),
            Utility.formatDuration(metadata.get("xmpDM:duration")),
            metadata.get("xmpDM:releaseDate")
        );
    }

    private Resource toEntity(ResourceDto request) {
        Resource resource = new Resource();
        resource.setContent(request.data());
        return resource;
    }
}
