package ru.alfabank.interview.api.client;

import ru.alfabank.interview.api.config.TestConfig;
import ru.alfabank.interview.api.dto.CreatePostRequest;
import ru.alfabank.interview.api.dto.CreatePostResponse;
import ru.alfabank.interview.api.dto.Post;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class PostApiClient extends BaseApiClient {
    private static final String POSTS_PATH = "/posts";

    public PostApiClient(TestConfig config) {
        super(config);
    }

    public Post getPost(int id) {
        return get(POSTS_PATH + "/" + id)
                .then()
                .statusCode(200)
                .extract()
                .as(Post.class);
    }

    public List<Post> getPostsByUserId(int userId) {
        Post[] posts = get(POSTS_PATH, Map.of("userId", userId))
                .then()
                .statusCode(200)
                .extract()
                .as(Post[].class);

        return Arrays.asList(posts);
    }

    public CreatePostResponse createPost(CreatePostRequest request) {
        return post(POSTS_PATH, request)
                .then()
                .statusCode(201)
                .extract()
                .as(CreatePostResponse.class);
    }
}
