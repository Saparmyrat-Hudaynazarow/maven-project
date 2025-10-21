package com.resource.util;

import com.resource.exception.ValidationException;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public final class Utility {
    private static final String DEFAULT_DURATION = "00:00";
    private static final Pattern ID_PATTERN = Pattern.compile("^[0-9]+$");
    private static final Pattern CSV_PATTERN = Pattern.compile("^[0-9]+(?:,[0-9]+)*$");

    private Utility() {
        // prevent instantiation
    }

    public static String formatDuration(String durationInSeconds) {
        if (durationInSeconds == null || durationInSeconds.isBlank()) {
            return DEFAULT_DURATION;
        }

        try {
            double totalSeconds = Double.parseDouble(durationInSeconds);
            Duration duration = Duration.ofSeconds((long) Math.floor(totalSeconds));

            long minutes = duration.toMinutes();
            long seconds = duration.minusMinutes(minutes).getSeconds();

            return String.format("%02d:%02d", minutes, seconds);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid duration format: " + durationInSeconds, e);
        }
    }

    public static int parseAndValidateId(String id) {
        if (id == null || !ID_PATTERN.matcher(id).matches()) {
            throw new ValidationException(
                String.format("Invalid ID '%s'. Must be a positive integer.", id));
        }

        try {
            int parsed = Integer.parseInt(id);
            if (parsed <= 0) {
                throw new ValidationException(
                    String.format("Invalid ID '%s'. Must be greater than zero.", id));
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new ValidationException(
                String.format("Invalid ID '%s'. Value is too large for 32-bit integer.", id));
        }
    }

    public static List<Integer> validateAndParse(String csv, int maxLength) {
        if (csv == null || csv.isBlank()) {
            throw new ValidationException("CSV string cannot be null or blank");
        }

        if (csv.length() > maxLength) {
            throw new ValidationException(
                String.format("CSV string too long: %d chars (max %d)", csv.length(), maxLength));
        }

        if (!CSV_PATTERN.matcher(csv).matches()) {
            throw new ValidationException("Invalid CSV format. Expected comma-separated positive integers.");
        }

        List<Integer> idList = Arrays.stream(csv.split(","))
            .map(String::trim)
            .map(Utility::parseAndValidateId)
            .distinct()
            .toList();

        if (idList.isEmpty()) {
            throw new ValidationException("CSV string must contain at least one valid ID");
        }

        return idList;
    }
}
