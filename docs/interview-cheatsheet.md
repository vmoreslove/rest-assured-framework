# Шпаргалка по проекту

## REST Assured

Базовый паттерн:

```java
given()
    .contentType(ContentType.JSON)
    .body(request)
.when()
    .post("/api/users")
.then()
    .statusCode(201);
```

В проекте этот код спрятан в `BaseApiClient`, чтобы тесты не дублировали HTTP-настройки.

## Локальный API и внешний API

В проекте есть два типа API-тестов:

- локальные тесты поднимают `LocalUsersApiServer` внутри Java-процесса;
- внешний тест `ExternalJsonPlaceholderTest` ходит в `https://jsonplaceholder.typicode.com`.

Команда для внешнего API:

```bash
mvn test -Pexternal -Dtest=ExternalJsonPlaceholderTest
```

Почему внешний тест выключен по умолчанию:

> Внешний API зависит от сети и доступности стороннего сервиса. Чтобы основной CI был стабильным, такие тесты лучше запускать отдельным профилем как smoke/integration suite.

## HTTP вопросы

- `200 OK` - успешное получение или операция без отдельного статуса.
- `201 Created` - ресурс создан.
- `400 Bad Request` - ошибка валидации входных данных.
- `401 Unauthorized` - нет или неверная аутентификация.
- `403 Forbidden` - аутентификация есть, прав нет.
- `404 Not Found` - ресурс или маршрут не найден.
- `409 Conflict` - конфликт состояния, например дубль уникального значения.
- `500` - ошибка сервера.

## Backend, API и REST

Backend - это вся серверная часть приложения: бизнес-логика, БД, интеграции, очереди, обработчики, фоновые задачи.

API - это контракт взаимодействия с backend: endpoints, HTTP methods, request, response, status codes, headers.

Пример:

```text
Frontend
  -> POST /api/payments
  -> API endpoint
  -> backend logic
  -> database / Kafka / other services
```

API-тест обычно проверяет внешний контракт:

```text
request -> status code -> response body -> headers
```

Backend/integration тест может проверять глубже:

```text
request -> response -> запись в БД -> событие в брокере -> audit log
```

Короткий ответ:

> API - это входная точка и контракт общения с backend. Backend шире: это вся серверная логика за API, включая БД, интеграции, очереди и обработку данных.

## Auth в API

Authentication и authorization - разные вещи:

| Термин | Что значит | Пример |
|---|---|---|
| Authentication | Кто ты? | Пользователь ввел логин/пароль и получил token |
| Authorization | Что тебе можно? | User может смотреть свои счета, admin может смотреть всех пользователей |

Коротко:

```text
Authentication = идентификация
Authorization = проверка прав
```

### Basic Auth

Логин и пароль передаются в header в base64:

```http
Authorization: Basic base64(username:password)
```

Пример в REST Assured:

```java
given()
    .auth().basic("user", "password")
.when()
    .get("/api/profile")
.then()
    .statusCode(200);
```

Basic Auth чаще встречается в простых internal tools/services, но для пользовательских систем обычно используют token-based auth.

### Bearer Token

Самый частый вариант в REST API.

Сначала клиент логинится:

```http
POST /api/login
```

Получает token:

```json
{
  "token": "eyJhbGciOi..."
}
```

Потом отправляет token в header:

```http
Authorization: Bearer eyJhbGciOi...
```

Пример в REST Assured:

```java
given()
    .header("Authorization", "Bearer " + token)
.when()
    .get("/api/profile")
.then()
    .statusCode(200);
```

### JWT

JWT - частый формат bearer token. Он состоит из трех частей:

```text
header.payload.signature
```

В payload могут быть данные:

```json
{
  "userId": 123,
  "role": "ADMIN",
  "exp": 1770000000
}
```

JWT обычно подписан, чтобы backend мог проверить, что token не подделали.

### OAuth 2.0

OAuth 2.0 - это не просто token, а протокол выдачи access token.

Часто используется, когда есть:

```text
authorization server
resource server
client application
```

Примеры: вход через Google, корпоративный SSO, Keycloak.

### Session + Cookie

В web-приложениях часто бывает session-based auth.

После логина backend возвращает cookie:

```http
Set-Cookie: SESSION=abc123
```

Дальше браузер автоматически отправляет:

```http
Cookie: SESSION=abc123
```

В UI-тестах это обычно работает через браузер. В API-тестах cookie можно передавать вручную.

### Коды ответов auth

| Код | Значение |
|---|---|
| `401 Unauthorized` | Нет token, token неправильный или истек |
| `403 Forbidden` | Пользователь авторизован, но прав недостаточно |
| `200/201` | Доступ разрешен |
| `302` | В web может быть redirect на login page |

Главная разница:

```text
401 = система не знает/не приняла пользователя
403 = пользователя знает, но действие запрещено
```

### Что проверять в автотестах

Positive:

```text
valid token -> 200
```

Negative:

```text
no token -> 401
invalid token -> 401
expired token -> 401
valid token without required role -> 403
```

Как добавить auth в framework:

```java
new RequestSpecBuilder()
    .setBaseUri(config.baseUri().toString())
    .addHeader("Authorization", "Bearer " + token)
    .setContentType(ContentType.JSON)
    .build();
```

Ответ для интервью:

> Authentication отвечает на вопрос "кто пользователь", authorization - "что ему разрешено". В API часто используют Bearer token/JWT: сначала клиент получает token через login/SSO, потом передает его в header `Authorization`. В тестах я бы проверял positive case с валидным token, negative cases без token, с invalid/expired token и с валидным token без нужной роли. Важно различать `401` и `403`: `401` - нет валидной аутентификации, `403` - аутентификация есть, но прав недостаточно.

## REST и брокеры сообщений

REST - синхронное взаимодействие:

```text
Service A -> HTTP request -> Service B
Service A <- HTTP response <- Service B
```

Клиент ждет ответ сразу. REST удобен, когда нужен immediate response: получить пользователя, создать заявку, проверить статус, получить список продуктов.

Kafka/RabbitMQ - асинхронное взаимодействие:

```text
Service A -> event/message -> broker
Service B -> consumes message later
```

Отправитель не обязан ждать обработку получателем. Брокер полезен, когда нужна развязка сервисов, retry, гарантированная доставка, обработка пиков нагрузки или несколько потребителей одного события.

Пример:

```text
Order service -> ORDER_CREATED -> Kafka
Warehouse service -> reserves item
Notification service -> sends email/SMS
Analytics service -> stores event
```

Можно ли обойтись REST без брокера:

> Да, если сценарий простой и синхронный: отправили request и сразу получили response. Но если процесс долгий, событийный, зависит от нескольких сервисов или требует надежной доставки, лучше использовать Kafka/RabbitMQ.

Ответ для интервью:

> REST и брокеры не заменяют друг друга полностью. REST подходит для request-response взаимодействия, а Kafka/RabbitMQ - для асинхронных событий, развязки сервисов, ретраев и обработки сообщений несколькими consumer'ами.

## Java вопросы

- `record` удобен для immutable DTO.
- `abstract class` может хранить состояние и реализацию, interface описывает контракт.
- `ArrayList` быстрый по индексу, `LinkedList` редко нужен в автотестах.
- `HashMap` не гарантирует порядок, `LinkedHashMap` сохраняет порядок вставки.
- Checked exceptions нужно обрабатывать или пробрасывать, unchecked обычно говорят об ошибке программирования или входных данных.

## Что сказать про framework from scratch

> С нуля я бы начал не с большого абстрактного фреймворка, а с минимального каркаса: конфигурация окружений, базовый клиент, конкретные API-клиенты, DTO, test data builders, flow-слой и отчетность. Дальше расширял бы по боли проекта: авторизация, БД, очереди, параллельность, Docker/Testcontainers, Allure и интеграция с CI/CD.

## Мини-задачи для тренировки

- Написать метод, который из списка пользователей вернет пользователя по email.
- Проверить, что все email уникальны.
- Добавить обработку `404 USER_NOT_FOUND`.
- Добавить endpoint `/api/login` и негативный тест на неверный пароль.
- Написать SQL-запрос: найти пользователей с количеством заказов больше 3.
