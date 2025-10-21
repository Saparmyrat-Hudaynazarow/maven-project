package com.song.service;

import com.song.dto.SongMetadataRequest;
import com.song.dto.SongMetadataResponse;
import com.song.entity.SongMetadata;
import com.song.exception.MetadataValidationException;
import com.song.exception.ResourceNotFoundException;
import com.song.exception.MetadataAlreadyExistException;
import com.song.repository.SongMetadataRepository;
import com.song.util.Utility;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SongMetadataService {

    private static final int MAX_IDS_LENGTH = 200;

    private final SongMetadataRepository songMetadataRepository;
    private final Validator validator;


    @Transactional
    public Map<String, Integer> createSong(final SongMetadataRequest request) {
        log.debug("Creating song metadata: {}", request);

        Map<String, String> validationErrors = validateRequest(request);
        if (!validationErrors.isEmpty()) {
            throw new MetadataValidationException("Validation failed", validationErrors);
        }

        if (songMetadataRepository.findById(request.id()).isPresent()) {
            throw new MetadataAlreadyExistException(
                String.format("Metadata for resource ID=%d already exists", request.id()));
        }

        SongMetadata saved = songMetadataRepository.save(toEntity(request));

        log.info("Song metadata successfully created: id={}", saved.getId());
        return Map.of("id", saved.getId());
    }

    @Transactional(readOnly = true)
    public SongMetadataResponse getSongMetadata(final String songId) {
        final int id = Utility.parseAndValidateId(songId);

        SongMetadata songMetadata = songMetadataRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                String.format("Song metadata with ID=%d not found", id)));

        log.debug("Fetched song metadata: {}", songMetadata);
        return toResponse(songMetadata);
    }

    @Transactional
    public Map<String, List<Integer>> deleteByIds(final String csvIds) {
        final List<Integer> idList = Utility.validateAndParse(csvIds, MAX_IDS_LENGTH);
        final List<Integer> existingIds = songMetadataRepository.findExistingIds(idList);

        if (!existingIds.isEmpty()) {
            songMetadataRepository.deleteAllByIdInBatch(existingIds);
            log.info("Deleted song metadata records: {}", existingIds);
        } else {
            log.warn("No existing metadata found for provided IDs: {}", idList);
        }

        return Map.of("ids", existingIds);
    }

    private Map<String, String> validateRequest(final SongMetadataRequest request) {
        final Set<ConstraintViolation<SongMetadataRequest>> violations = validator.validate(request);

        final Map<String, String> errorMap = new LinkedHashMap<>();
        for (ConstraintViolation<SongMetadataRequest> violation : violations) {
            errorMap.put(violation.getPropertyPath().toString(), violation.getMessage());
        }
        return errorMap;
    }


    private SongMetadata toEntity(final SongMetadataRequest request) {
        return SongMetadata.builder()
            .id(request.id())
            .name(request.name())
            .artist(request.artist())
            .album(request.album())
            .duration(request.duration())
            .year(request.year())
            .build();
    }

    private SongMetadataResponse toResponse(final SongMetadata entity) {
        return new SongMetadataResponse(
            entity.getId(),
            entity.getName(),
            entity.getArtist(),
            entity.getAlbum(),
            entity.getDuration(),
            entity.getYear()
        );
    }
}
