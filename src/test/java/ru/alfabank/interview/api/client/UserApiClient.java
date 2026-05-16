package ru.alfabank.interview.api.client;

import io.restassured.response.Response;
import ru.alfabank.interview.api.config.TestConfig;
import ru.alfabank.interview.api.dto.CreateUserRequest;
import ru.alfabank.interview.api.dto.CreateUserResponse;
import ru.alfabank.interview.api.dto.ErrorResponse;
import ru.alfabank.interview.api.dto.User;
import ru.alfabank.interview.api.dto.UsersResponse;

import static org.assertj.core.api.Assertions.assertThat;

public final class UserApiClient extends BaseApiClient {
    private static final String USERS_PATH = "/api/users";

    public UserApiClient(TestConfig config) {
        super(config);
    }

    public Response getUsersRaw() {
        return get(USERS_PATH);
    }

    public UsersResponse getUsers() {
        return getUsersRaw()
                .then()
                .statusCode(200)
                .extract()
                .as(UsersResponse.class);
    }

    public User getUser(int id) {
        return get(USERS_PATH + "/" + id)
                .then()
                .statusCode(200)
                .extract()
                .as(User.class);
    }

    public CreateUserResponse createUser(CreateUserRequest request) {
        return post(USERS_PATH, request)
                .then()
                .statusCode(201)
                .extract()
                .as(CreateUserResponse.class);
    }

    public ErrorResponse createUserAndExpectValidationError(CreateUserRequest request) {
        ErrorResponse error = post(USERS_PATH, request)
                .then()
                .statusCode(400)
                .extract()
                .as(ErrorResponse.class);

        assertThat(error.code()).isEqualTo("VALIDATION_ERROR");
        return error;
    }
}
