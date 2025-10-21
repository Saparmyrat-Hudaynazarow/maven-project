package com.resource.controller;

import com.resource.dto.ResourceDto;
import com.resource.service.ResourceService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @PostMapping
    public ResponseEntity<Map<String, Integer>> uploadResource(@RequestBody(required = false) byte[] data,
                                                               @RequestHeader(value = HttpHeaders.CONTENT_TYPE,
                                                                   required = false) String contentType) {
        ResourceDto resourceCreateRequest = new ResourceDto(data, contentType);
        return ResponseEntity.ok(resourceService.saveResource(resourceCreateRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getResource(@PathVariable("id") String id) {
        ResourceDto resource = resourceService.getResource(id);
        return ResponseEntity.ok().contentType(MediaType.valueOf(resource.contentType()))
            .body(resource.data());
    }

    @DeleteMapping
    public ResponseEntity<Map<String, List<Integer>>> deleteResources(@RequestParam("id") String ids) {
        return ResponseEntity.ok(resourceService.deleteByIds(ids));
    }
}
