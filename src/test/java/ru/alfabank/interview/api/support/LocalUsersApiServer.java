package ru.alfabank.interview.api.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import ru.alfabank.interview.api.dto.CreateUserRequest;
import ru.alfabank.interview.api.dto.ErrorResponse;
import ru.alfabank.interview.api.dto.User;
import ru.alfabank.interview.api.dto.UsersResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public final class LocalUsersApiServer {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final HttpServer server;
    private final ExecutorService executorService;
    private final Map<Integer, User> users = new ConcurrentHashMap<>();
    private final AtomicInteger idSequence = new AtomicInteger(100);

    private LocalUsersApiServer(HttpServer server, ExecutorService executorService) {
        this.server = server;
        this.executorService = executorService;
        seedUsers();
        this.server.createContext("/api/users", this::handleUsers);
    }

    public static LocalUsersApiServer start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        server.setExecutor(executorService);

        LocalUsersApiServer apiServer = new LocalUsersApiServer(server, executorService);
        server.start();
        return apiServer;
    }

    public int port() {
        return server.getAddress().getPort();
    }

    public void stop() {
        server.stop(0);
        executorService.shutdownNow();
    }

    private void seedUsers() {
        users.put(1, new User(1, "Ivan", "Petrov", "ivan.petrov@example.com"));
        users.put(2, new User(2, "Anna", "Sidorova", "anna.sidorova@example.com"));
    }

    private void handleUsers(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if ("GET".equals(method) && "/api/users".equals(path)) {
                writeJson(exchange, 200, new UsersResponse(users.values().stream().toList()));
                return;
            }

            if ("GET".equals(method) && path.matches("/api/users/\\d+")) {
                handleGetUser(exchange, path);
                return;
            }

            if ("POST".equals(method) && "/api/users".equals(path)) {
                handleCreateUser(exchange);
                return;
            }

            writeJson(exchange, 404, new ErrorResponse("NOT_FOUND", "Route not found"));
        } finally {
            exchange.close();
        }
    }

    private void handleGetUser(HttpExchange exchange, String path) throws IOException {
        int id = Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));
        User user = users.get(id);

        if (user == null) {
            writeJson(exchange, 404, new ErrorResponse("USER_NOT_FOUND", "User was not found"));
            return;
        }

        writeJson(exchange, 200, user);
    }

    private void handleCreateUser(HttpExchange exchange) throws IOException {
        CreateUserRequest request = readJson(exchange.getRequestBody(), CreateUserRequest.class);

        if (isBlank(request.firstName()) || isBlank(request.lastName()) || isBlank(request.email())) {
            writeJson(exchange, 400, new ErrorResponse("VALIDATION_ERROR", "firstName, lastName and email are required"));
            return;
        }

        int id = idSequence.incrementAndGet();
        User user = new User(id, request.firstName(), request.lastName(), request.email());
        users.put(id, user);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", user.id());
        response.put("firstName", user.firstName());
        response.put("lastName", user.lastName());
        response.put("email", user.email());

        writeJson(exchange, 201, response);
    }

    private <T> T readJson(InputStream body, Class<T> type) throws IOException {
        return MAPPER.readValue(body, type);
    }

    private void writeJson(HttpExchange exchange, int statusCode, Object responseBody) throws IOException {
        byte[] body = MAPPER.writeValueAsString(responseBody).getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, body.length);

        try (OutputStream responseStream = exchange.getResponseBody()) {
            responseStream.write(body);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
