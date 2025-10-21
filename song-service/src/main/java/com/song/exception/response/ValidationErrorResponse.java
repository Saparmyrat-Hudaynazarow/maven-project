package com.song.exception.response;

import java.util.Map;

public record ValidationErrorResponse(String errorMessage, Map<String, String> details, String errorCode) {
}