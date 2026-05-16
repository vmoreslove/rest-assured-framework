package ru.alfabank.interview.api.config;

import java.net.URI;

public final class TestConfig {
    private final String env;
    private final URI baseUri;

    private TestConfig(String env, URI baseUri) {
        this.env = env;
        this.baseUri = baseUri;
    }

    public static TestConfig local(int port) {
        return new TestConfig("local", URI.create("http://localhost:" + port));
    }

    public static TestConfig jsonPlaceholder() {
        String baseUrl = System.getProperty("externalBaseUrl", "https://jsonplaceholder.typicode.com");
        return new TestConfig("external", URI.create(baseUrl));
    }

    public static TestConfig fromSystemProperties() {
        String env = System.getProperty("env", "local");
        String baseUrl = System.getProperty("baseUrl", "http://localhost:8080");
        return new TestConfig(env, URI.create(baseUrl));
    }

    public String env() {
        return env;
    }

    public URI baseUri() {
        return baseUri;
    }
}
