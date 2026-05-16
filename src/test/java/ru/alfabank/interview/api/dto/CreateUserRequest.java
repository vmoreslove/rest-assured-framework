package ru.alfabank.interview.api.dto;

public record CreateUserRequest(
        String firstName,
        String lastName,
        String email
) {
}
