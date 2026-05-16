package ru.alfabank.interview.api.tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.alfabank.interview.api.dto.ErrorResponse;
import ru.alfabank.interview.api.dto.UsersResponse;
import ru.alfabank.interview.api.testdata.UserRequests;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.assertj.core.api.Assertions.assertThat;

class UserApiContractTest extends BaseApiTest {

    @Test
    @DisplayName("GET /api/users returns users matching JSON schema")
    void shouldReturnUsersBySchema() {
        userApiClient.getUsersRaw()
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("users-schema.json"));
    }

    @Test
    @DisplayName("GET /api/users returns non-empty list with valid emails")
    void shouldReturnUsers() {
        UsersResponse response = userApiClient.getUsers();

        assertThat(response.data())
                .isNotEmpty()
                .allSatisfy(user -> assertThat(user.email()).contains("@"));
    }

    @Test
    @DisplayName("POST /api/users validates required fields")
    void shouldValidateRequiredFields() {
        ErrorResponse error = userApiClient.createUserAndExpectValidationError(UserRequests.userWithoutEmail());

        assertThat(error.message()).contains("email");
    }
}
