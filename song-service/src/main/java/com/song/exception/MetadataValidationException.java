package com.song.exception;

import java.util.Map;

import lombok.Getter;

@Getter
public class MetadataValidationException extends RuntimeException {
    private final Map<String,String> details;

    public MetadataValidationException(String message, Map<String,String> details) {
        super(message);
        this.details=details;
    }
}