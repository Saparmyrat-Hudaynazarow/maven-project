package com.song.controller;

import com.song.dto.SongMetadataRequest;
import com.song.dto.SongMetadataResponse;
import com.song.service.SongMetadataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/songs")
@RequiredArgsConstructor
public class SongMetadataController {

    private final SongMetadataService songMetadataService;

    @PostMapping
    public ResponseEntity<Map<String, Integer>> createSongMetadata(@RequestBody SongMetadataRequest songMetadataRequest) {
        log.info("SongCreateRequest: {}", songMetadataRequest);
       return ResponseEntity.ok(songMetadataService.createSong(songMetadataRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SongMetadataResponse> getSongMetadata(@PathVariable("id") String id) {
        return ResponseEntity.ok(songMetadataService.getSongMetadata(id));
    }

    @DeleteMapping
    public ResponseEntity<Map<String, List<Integer>>> deleteSongMetadata(@RequestParam("id") String csvIds) {
        return ResponseEntity.ok(songMetadataService.deleteByIds(csvIds));
    }
}