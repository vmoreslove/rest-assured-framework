package ru.alfabank.interview.api.dto;

public record CreatePostRequest(
        int userId,
        String title,
        String body
) {
}
