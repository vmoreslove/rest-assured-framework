# Live Coding Playbook

Цель этого файла - быстро вспомнить, как действовать на лайв-кодинге по API automation project.
Держи рядом, но не заучивай дословно: главное - спокойно объяснять ход мыслей.

## Базовый алгоритм

1. Уточнить задачу.
2. Найти похожий пример в проекте.
3. Добавить DTO, если нужен новый request/response.
4. Добавить метод в API client.
5. Если сценарий состоит из нескольких API-вызовов, добавить метод во flow.
6. Написать тест.
7. Запустить конкретный тест.
8. Если упало, прочитать статус-код, body и stacktrace.

Фраза вслух:

> Сначала я посмотрю существующий паттерн в проекте, чтобы не писать новый стиль. Потом добавлю минимальный client method и покрою его тестом.

## Где что лежит

```text
src/test/java/ru/alfabank/interview/api
├── client      # HTTP methods and endpoint clients
├── config      # baseUrl/env config
├── dto         # request/response models
├── flow        # multi-step business actions
├── support     # local test server
├── testdata    # test data factories
└── tests       # JUnit tests
```

## Задача 1. Добавить DELETE /api/users/{id}

Что сказать:

> Я добавлю endpoint в локальный mock server, потом метод в UserApiClient и проверю сценарий create-delete-get.

### 1. Добавить delete в BaseApiClient

Файл:

```text
src/test/java/ru/alfabank/interview/api/client/BaseApiClient.java
```

Шаблон:

```java
protected Response delete(String path) {
    return given(requestSpecification)
            .when()
            .delete(path)
            .then()
            .log().ifValidationFails()
            .extract()
            .response();
}
```

### 2. Добавить обработку DELETE в LocalUsersApiServer

Файл:

```text
src/test/java/ru/alfabank/interview/api/support/LocalUsersApiServer.java
```

В `handleUsers` добавить ветку:

```java
if ("DELETE".equals(method) && path.matches("/api/users/\\d+")) {
    handleDeleteUser(exchange, path);
    return;
}
```

Метод:

```java
private void handleDeleteUser(HttpExchange exchange, String path) throws IOException {
    int id = Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));
    User removedUser = users.remove(id);

    if (removedUser == null) {
        writeJson(exchange, 404, new ErrorResponse("USER_NOT_FOUND", "User was not found"));
        return;
    }

    exchange.sendResponseHeaders(204, -1);
}
```

### 3. Добавить методы в UserApiClient

Файл:

```text
src/test/java/ru/alfabank/interview/api/client/UserApiClient.java
```

Шаблон:

```java
public void deleteUser(int id) {
    delete(USERS_PATH + "/" + id)
            .then()
            .statusCode(204);
}

public ErrorResponse getUserAndExpectNotFound(int id) {
    return get(USERS_PATH + "/" + id)
            .then()
            .statusCode(404)
            .extract()
            .as(ErrorResponse.class);
}
```

### 4. Добавить тест

Файл:

```text
src/test/java/ru/alfabank/interview/api/tests/UserBusinessFlowTest.java
```

Шаблон:

```java
@Test
@DisplayName("Deleted user cannot be loaded by id")
void shouldDeleteUser() {
    CreateUserRequest request = UserRequests.validUser();
    User createdUser = userFlow.createUserAndLoadIt(request);

    userApiClient.deleteUser(createdUser.id());

    ErrorResponse error = userApiClient.getUserAndExpectNotFound(createdUser.id());
    assertThat(error.code()).isEqualTo("USER_NOT_FOUND");
}
```

Если забываешь импорт:

```java
import ru.alfabank.interview.api.dto.ErrorResponse;
```

## Задача 2. Добавить negative test на invalid email

Что сказать:

> Это validation test. Я добавлю тестовые данные и проверю 400 с ожидаемым error code/message.

### 1. Добавить test data

Файл:

```text
src/test/java/ru/alfabank/interview/api/testdata/UserRequests.java
```

Шаблон:

```java
public static CreateUserRequest userWithInvalidEmail() {
    return new CreateUserRequest("Oleg", "Kopylov", "invalid-email");
}
```

### 2. Если сервер пока не валидирует email

Файл:

```text
src/test/java/ru/alfabank/interview/api/support/LocalUsersApiServer.java
```

В `handleCreateUser` после required-проверки:

```java
if (!request.email().contains("@")) {
    writeJson(exchange, 400, new ErrorResponse("VALIDATION_ERROR", "email should be valid"));
    return;
}
```

### 3. Добавить тест

Файл:

```text
src/test/java/ru/alfabank/interview/api/tests/UserApiContractTest.java
```

Шаблон:

```java
@Test
@DisplayName("POST /api/users validates email format")
void shouldValidateEmailFormat() {
    ErrorResponse error = userApiClient.createUserAndExpectValidationError(UserRequests.userWithInvalidEmail());

    assertThat(error.message()).contains("email");
}
```

## Задача 3. Добавить query param test

Что сказать:

> Для query params я не буду собирать строку руками. В REST Assured есть `queryParams`, это меньше ошибок с encoding.

В проекте уже есть пример:

```text
src/test/java/ru/alfabank/interview/api/client/PostApiClient.java
```

Шаблон:

```java
get(POSTS_PATH, Map.of("userId", userId))
        .then()
        .statusCode(200)
        .extract()
        .as(Post[].class);
```

Проверка списка:

```java
assertThat(posts)
        .isNotEmpty()
        .allSatisfy(post -> assertThat(post.userId()).isEqualTo(1));
```

## Задача 4. Добавить DTO

Что сказать:

> Для response я использую DTO, потому что это типизированный контракт. Если API вернет неожиданную структуру, это будет проще отловить и поддерживать.

Шаблон:

```java
package ru.alfabank.interview.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SomeResponse(
        int id,
        String name
) {
}
```

Почему `@JsonIgnoreProperties(ignoreUnknown = true)`:

> Если API добавит новое поле, тест не упадет только из-за расширения response. Мы проверяем важные для сценария поля.

## Задача 5. Найти пользователя по email

Шаблон:

```java
User user = userApiClient.getUsers().data().stream()
        .filter(item -> item.email().equals("ivan.petrov@example.com"))
        .findFirst()
        .orElseThrow(() -> new AssertionError("User was not found by email"));

assertThat(user.id()).isPositive();
```

Что сказать:

> Здесь я использую stream, потому что у меня уже есть типизированный список пользователей.

## Задача 6. Проверить уникальность email

Шаблон:

```java
List<String> emails = userApiClient.getUsers().data().stream()
        .map(User::email)
        .toList();

assertThat(emails).doesNotHaveDuplicates();
```

Нужный импорт:

```java
import java.util.List;
```

## Если тест упал

Сначала смотреть:

1. status code;
2. response body;
3. request URL;
4. stacktrace;
5. setup в `@BeforeAll`.

Фраза вслух:

> Сначала я проверю фактический status code и body. Если API вернул не то, что я ожидал, поправлю либо ожидание, либо подготовку данных.

## Что говорить, пока пишешь код

Хорошие фразы:

- "Сначала добавлю минимальный счастливый путь, потом negative scenario."
- "Я вынесу HTTP-вызов в client layer, чтобы тест не зависел от деталей REST Assured."
- "DTO нужен, чтобы работать с response типизированно."
- "Этот тест зависит от внешней сети, поэтому я бы держал его в отдельном профиле."
- "Для локального CI лучше оставить стабильный mock/test server."

## Быстрые ответы на вопросы интервьюера

**Почему не писать REST Assured прямо в тесте?**

> Можно, но при росте проекта появится дублирование baseUrl, headers, auth и endpoint paths. Client layer делает тесты короче и упрощает поддержку.

**Почему внешний API выключен по умолчанию?**

> Потому что он зависит от сети и стороннего сервиса. Такие тесты лучше запускать отдельно как smoke/integration suite.

**Что такое flow layer?**

> Это слой бизнес-действий поверх API-клиентов. Например, создать пользователя и загрузить его по id.

**Что такое BDD-style DSL в REST Assured?**

> Это цепочка `given / when / then`: given - подготовка запроса, when - HTTP-действие, then - проверка ответа.

## Мини-план на лайв-кодинг

Перед стартом:

1. Открыть `README.md`.
2. Открыть `BaseApiClient`.
3. Открыть похожий client/test.
4. Запустить существующий тест, чтобы показать зеленую базу.
5. Делать маленькие изменения и часто запускать конкретный тест.

Если завис:

> Я на секунду сверю с существующим паттерном в проекте, чтобы сделать в том же стиле.
