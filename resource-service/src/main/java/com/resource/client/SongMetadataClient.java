package com.resource.client;

import com.resource.dto.SongMetadataDto;
import com.resource.exception.InvalidMp3Exception;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SongMetadataClient {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String baseUrl;

    public SongMetadataClient(@Value("${song-service.base-url}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void createSongMetadata(SongMetadataDto songMetadataDto) {
        String url = baseUrl + "/songs";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SongMetadataDto> entity = new HttpEntity<>(songMetadataDto, headers);

        try {
            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
        } catch (HttpClientErrorException.BadRequest ex) {
            // 400 - Invalid MP3 case
            throw new InvalidMp3Exception("Invalid Mp3");
        } catch (RestClientException ex) {
            // Other HTTP or connection errors
            throw new RuntimeException("SongMetadata Service failed: " + ex.getMessage(), ex);
        }
    }

    public Map<String, List<Integer>> deleteSongMetadata(List<Integer> ids) {
        String csv = ids.stream()
            .map(String::valueOf)
            .collect(Collectors.joining(","));

        String url = baseUrl + "/songs?id=" + csv;

        try {
            ResponseEntity<Map<String, List<Integer>>> response = restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                null,
                new ParameterizedTypeReference<>() {}
            );
            return response.getBody();
        } catch (RestClientException ex) {
            throw new RuntimeException("Failed to delete song metadata: " + ex.getMessage(), ex);
        }
    }
}
