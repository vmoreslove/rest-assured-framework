package ru.alfabank.interview.api.client;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import ru.alfabank.interview.api.config.TestConfig;

import static io.restassured.RestAssured.given;

public abstract class BaseApiClient {
    private final RequestSpecification requestSpecification;

    protected BaseApiClient(TestConfig config) {
        this.requestSpecification = new RequestSpecBuilder()
                .setBaseUri(config.baseUri().toString())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .log(LogDetail.METHOD)
                .log(LogDetail.URI)
                .build();
    }

    protected Response get(String path) {
        return given(requestSpecification)
                .when()
                .get(path)
                .then()
                .log().ifValidationFails()
                .extract()
                .response();
    }

    protected Response post(String path, Object body) {
        return given(requestSpecification)
                .body(body)
                .when()
                .post(path)
                .then()
                .log().ifValidationFails()
                .extract()
                .response();
    }
}
