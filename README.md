# rest-assured-framework

Repository: https://github.com/vmoreslove/rest-assured-framework

Небольшой проект для подготовки к техническому интервью Fullstack QA / Java QA.
Он показывает не просто `given().when().then()`, а типовую структуру API automation framework:

- REST Assured + JUnit 5 + AssertJ;
- DTO на Java records;
- слой API-клиентов;
- flow-слой для бизнес-сценариев;
- тестовые данные отдельно от тестов;
- JSON Schema contract check;
- локальный mock API на JDK `HttpServer`;
- отдельные smoke-тесты внешнего API JSONPlaceholder;
- GitHub Actions pipeline.

## Зачем локальный API

Тесты не зависят от внешних сервисов, VPN и интернета. При запуске `mvn test` поднимается локальный HTTP-сервер на свободном порту, REST Assured ходит в него как в обычный backend, после тестов сервер закрывается.

Это удобно для собеседования: можно открыть код, объяснить архитектуру и сразу показать зеленый прогон.

## Быстрый запуск

Нужны Java 17 и Maven.

```bash
mvn clean test
```

Запуск конкретного тестового класса:

```bash
mvn test -Dtest=UserApiContractTest
```

Запуск тестов во внешний публичный API:

```bash
mvn test -Pexternal -Dtest=ExternalJsonPlaceholderTest
```

По умолчанию внешний тестовый класс выключен. Это сделано специально: обычный `mvn test` не должен падать из-за интернета, DNS или временной недоступности публичного сервиса.

## Структура

```text
src/test/java/ru/alfabank/interview/api
├── client      # BaseApiClient и конкретные API-клиенты
├── config      # конфигурация окружений
├── dto         # request/response модели
├── flow        # бизнес-сценарии поверх API-клиентов
├── support     # локальный HTTP mock server
├── testdata    # фабрики тестовых данных
└── tests       # JUnit тесты
```

## Как рассказывать на интервью

Короткий вариант:

> Это небольшой API automation framework на Java. Я разделил его на несколько слоев: `client` инкапсулирует HTTP-вызовы и статус-коды, `dto` описывает контракт данных, `flow` собирает бизнес-сценарии, а тесты остаются короткими и читаемыми. Для стабильности демо тесты ходят в локальный mock API, поэтому не зависят от внешнего сервиса. В CI добавлен простой pipeline с Java 17 и `mvn clean test`.

Что подчеркнуть:

- `BaseApiClient` хранит общую REST Assured спецификацию: base URI, JSON headers, логирование.
- `UserApiClient` скрывает детали эндпоинтов и возвращает типизированные DTO.
- `PostApiClient` показывает работу с реальным внешним REST API.
- `UserFlow` показывает слой бизнес-действий: создать пользователя и загрузить его по id.
- `UserApiContractTest` проверяет HTTP-контракт, JSON Schema и валидацию.
- `UserBusinessFlowTest` проверяет пользовательский сценарий.
- `ExternalJsonPlaceholderTest` проверяет внешний API: GET ресурса, фильтрацию query param и POST fake resource.

Про внешний API можно сказать так:

> Я специально разделил локальные и внешние тесты. Локальные тесты стабильны и подходят для обычного CI, а внешний набор запускается отдельным Maven profile `external`. Это снижает flaky-риск: если публичный сервис или сеть временно недоступны, основной test suite не ломается.

## Что можно добавить в live coding

1. Добавить `DELETE /api/users/{id}` в `LocalUsersApiServer`.
2. Добавить метод `deleteUser(int id)` в `UserApiClient`.
3. Написать тест: создать пользователя, удалить, проверить `404` при повторном получении.
4. Добавить negative test на создание пользователя с невалидным email.
5. Вынести логирование request/response в отдельный filter.
6. Добавить внешний DELETE smoke test и объяснить, почему JSONPlaceholder не сохраняет изменения.

## Частые вопросы

**Почему DTO, а не JsonPath в каждом тесте?**

DTO лучше показывают контракт API, дают типизацию и делают тесты читаемее. `JsonPath` полезен для точечных проверок, но если весь проект на строковых путях, его сложнее поддерживать.

**Зачем flow-слой?**

Чтобы не размазывать бизнес-шаги по тестам. Тест должен отвечать на вопрос "что проверяем", а не содержать всю механику подготовки данных.

**Что бы ты улучшил в enterprise-проекте?**

Добавил бы Allure, Testcontainers/WireMock, параллельный запуск, retries только для технических сбоев, интеграцию с test management, секреты через CI variables, расширенную конфигурацию окружений и контрактные проверки через OpenAPI.
