package ru.alfabank.interview.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateUserResponse(
        int id,
        String firstName,
        String lastName,
        String email
) {
}
