package ru.alfabank.interview.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Post(
        int userId,
        int id,
        String title,
        String body
) {
}
