# Система управления банковскими картами

REST API для управления банковскими картами на Spring Boot.

## Технологии

- Java 21
- Spring Boot 3.5.14
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL
- Liquibase (миграции БД)
- Docker Compose
- Swagger (OpenAPI 3.1)
- JUnit 5 + Mockito

## Быстрый старт

### 1. Клонировать репозиторий

```git clone <https://github.com/Woody-rn/bank-rest.git>```

### 2. Запуск через Docker

```docker-compose up -d```

### 3. Запуск без Docker
   Нужен запущенный PostgreSQL


## Тестовые пользователи

|Логин|Пароль|Роль|
|---|---|---|
|admin|admin123|ADMIN|
|user|user123|USER|

## API

### Авторизация

|Метод|URL|Описание|
|---|---|---|
|POST|`/api/auth/login`|Вход, получение JWT токена|

### Пользователь (`/api/user`)

|Метод|URL|Описание|
|---|---|---|
|GET|`/cards`|Мои карты (фильтр `?status=ACTIVE`)|
|GET|`/cards/{id}`|Карта по ID|
|GET|`/cards/{id}/balance`|Баланс карты|
|POST|`/cards/{id}/block-request`|Запрос на блокировку|
|POST|`/transfers`|Перевод между своими картами|
|GET|`/transfers?cardId=`|История переводов|

### Администратор (`/api/admin`)

|Метод|URL|Описание|
|---|---|---|
|POST|`/cards`|Создать карту|
|GET|`/cards`|Все карты|
|GET|`/cards/{id}`|Карта по ID|
|PUT|`/cards/{id}/block`|Заблокировать карту|
|PUT|`/cards/{id}/activate`|Активировать карту|
|DELETE|`/cards/{id}`|Удалить карту (мягкое удаление)|
|POST|`/users`|Создать пользователя|
|GET|`/users`|Все пользователи|
|GET|`/users/{id}`|Пользователь по ID|
|DELETE|`/users/{id}`|Отключить пользователя|
|GET|`/block-requests`|Заявки на блокировку|
|PUT|`/block-requests/{id}/approve`|Одобрить заявку|
|PUT|`/block-requests/{id}/reject`|Отклонить заявку|

## Аутентификация

1. `POST /api/auth/login` с логином и паролем

2. Скопировать токен из ответа

3. Добавить заголовок: `Authorization: Bearer <токен>`


## Документация API

- Swagger UI: `http://localhost:8080/swagger-ui.html`

- OpenAPI JSON: `http://localhost:8080/v3/api-docs`