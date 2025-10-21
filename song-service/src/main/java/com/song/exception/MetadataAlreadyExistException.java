package com.song.exception;


public class MetadataAlreadyExistException extends RuntimeException {
    public MetadataAlreadyExistException(String message) {
        super(message);
    }
}