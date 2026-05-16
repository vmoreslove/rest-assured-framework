package ru.alfabank.interview.api.tests;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import ru.alfabank.interview.api.client.PostApiClient;
import ru.alfabank.interview.api.config.TestConfig;
import ru.alfabank.interview.api.dto.CreatePostRequest;
import ru.alfabank.interview.api.dto.CreatePostResponse;
import ru.alfabank.interview.api.dto.Post;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("external")
@EnabledIfSystemProperty(named = "externalApi", matches = "true")
class ExternalJsonPlaceholderTest {
    private static PostApiClient postApiClient;

    @BeforeAll
    static void setUpExternalApiClient() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        postApiClient = new PostApiClient(TestConfig.jsonPlaceholder());
    }

    @Test
    @DisplayName("GET /posts/1 returns existing post from external API")
    void shouldGetExistingPost() {
        Post post = postApiClient.getPost(1);

        assertThat(post.id()).isEqualTo(1);
        assertThat(post.userId()).isPositive();
        assertThat(post.title()).isNotBlank();
        assertThat(post.body()).isNotBlank();
    }

    @Test
    @DisplayName("GET /posts?userId=1 returns only posts by requested user")
    void shouldFilterPostsByUserId() {
        List<Post> posts = postApiClient.getPostsByUserId(1);

        assertThat(posts)
                .isNotEmpty()
                .allSatisfy(post -> assertThat(post.userId()).isEqualTo(1));
    }

    @Test
    @DisplayName("POST /posts creates fake resource in external API")
    void shouldCreateFakePost() {
        CreatePostRequest request = new CreatePostRequest(
                1,
                "REST Assured interview post",
                "External smoke test for JSONPlaceholder"
        );

        CreatePostResponse response = postApiClient.createPost(request);

        assertThat(response.id()).isPositive();
        assertThat(response.title()).isEqualTo(request.title());
        assertThat(response.body()).isEqualTo(request.body());
        assertThat(response.userId()).isEqualTo(request.userId());
    }
}
