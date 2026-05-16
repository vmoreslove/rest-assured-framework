package ru.alfabank.interview.api.tests;

import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import ru.alfabank.interview.api.client.UserApiClient;
import ru.alfabank.interview.api.config.TestConfig;
import ru.alfabank.interview.api.flow.UserFlow;
import ru.alfabank.interview.api.support.LocalUsersApiServer;

import java.io.IOException;

public abstract class BaseApiTest {
    protected static LocalUsersApiServer localApiServer;
    protected static UserApiClient userApiClient;
    protected static UserFlow userFlow;

    @BeforeAll
    static void startLocalApi() throws IOException {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        localApiServer = LocalUsersApiServer.start();
        TestConfig config = TestConfig.local(localApiServer.port());

        userApiClient = new UserApiClient(config);
        userFlow = new UserFlow(userApiClient);
    }

    @AfterAll
    static void stopLocalApi() {
        if (localApiServer != null) {
            localApiServer.stop();
        }
    }
}
