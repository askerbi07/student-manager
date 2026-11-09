# 🎓 Student Manager (Spring Boot REST API)

Учебный проект, переросший в полноценное коммерческое приложение для управления данными студентов. Проект демонстрирует современные подходы к разработке на Java.

## 🚀 Технологический стек
*   **Язык:** Java 17 (используются `record` для неизменяемых моделей)
*   **Фреймворк:** Spring Boot 3.2.x (Spring Web, Spring JDBC)
*   **База данных:** SQLite (встроенная, не требует установки сервера)
*   **Тестирование:** JUnit 5, MockMvc (22+ успешных теста)
*   **Документация:** Swagger UI (OpenAPI 3)
*   **Сборка:** Maven (с настроенным `maven-shade-plugin` для сборки исполняемого JAR)
*   **Логирование:** SLF4J + Logback

## 🏗️ Архитектура проекта
Проект построен по паттерну **MVC / Layered Architecture**:
*   `controller` — REST-контроллеры для обработки HTTP-запросов.
*   `service` — Бизнес-логика приложения (`StudentService`).
*   `repository` — Слой доступа к данным (`StudentDatabaseRepository`).
*   `model` — Доменные модели данных (`Student`).
*   `collection` — Кастомная реализация коллекции на массивах (`CustomList`).
*   `strategy` — Реализация паттерна "Стратегия" для сортировки.

## ⚙️ Как запустить локально

1. Клонируйте репозиторий:
   ```bash
   git clone https://github.com/askerbi07/student-manager.git