package ru.alfabank.interview.api.tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.alfabank.interview.api.dto.CreateUserRequest;
import ru.alfabank.interview.api.dto.User;
import ru.alfabank.interview.api.testdata.UserRequests;

import static org.assertj.core.api.Assertions.assertThat;

class UserBusinessFlowTest extends BaseApiTest {

    @Test
    @DisplayName("Created user can be loaded by id")
    void shouldCreateUserAndLoadItById() {
        CreateUserRequest request = UserRequests.validUser();

        User loadedUser = userFlow.createUserAndLoadIt(request);

        assertThat(loadedUser)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(new User(0, request.firstName(), request.lastName(), request.email()));
    }
}
