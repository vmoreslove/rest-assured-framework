package ru.alfabank.interview.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record User(
        int id,
        String firstName,
        String lastName,
        String email
) {
}
