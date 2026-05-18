# Booking System — Fullstack Application

Система бронирования рабочих мест с ролевой моделью доступа (USER / MANAGER / ADMIN), JWT-аутентификацией, управлением контрактами и аналитикой.

---

## Стек технологий

| Слой | Технология |
|------|-----------|
| Backend | Java 17, Spring Boot 3.4.2, Spring Security, Spring Data JPA |
| База данных | PostgreSQL + Flyway (миграции) |
| Аутентификация | JWT (jjwt 0.11.5) |
| Маппинг | MapStruct 1.5.5 |
| Frontend | Angular 20, TypeScript 5.8, RxJS 7.8 |
| Сборка backend | Maven |
| Сборка frontend | Angular CLI |

---

## Требования

- **Java** 17+
- **Maven** 3.8+
- **Node.js** 18+ и **npm** 9+
- **PostgreSQL** 14+
- **Angular CLI** 20+ (глобально: `npm install -g @angular/cli`)

---

## Настройка базы данных

1. Создайте базу данных и пользователя в PostgreSQL:

```sql
CREATE DATABASE booking_db;
CREATE USER postgres WITH PASSWORD 'qwerty123';
GRANT ALL PRIVILEGES ON DATABASE booking_db TO postgres;
```

> По умолчанию приложение ожидает: хост `localhost:5432`, БД `booking_db`, логин `postgres`, пароль `qwerty123`.
> Для изменения — отредактируйте `src/main/resources/application.yml`.

Миграции Flyway применятся автоматически при старте приложения.

---

## Запуск backend

```bash
# В корне проекта (там где находится pom.xml)
mvn clean install
mvn spring-boot:run
```

Backend запустится на `http://localhost:8080`.

Для запуска тестов:

```bash
mvn test
```

---

## Запуск frontend

```bash
cd frontend

# Установка зависимостей (если node_modules ещё нет)
npm install

# Запуск dev-сервера
npm start
```

Frontend запустится на `http://localhost:4200` и автоматически откроется в браузере.

> Backend должен быть запущен до старта frontend — все API-запросы идут на `http://localhost:8080`.

---

## Структура проекта

```
├── src/
│   ├── main/
│   │   ├── java/booking/
│   │   │   ├── config/          # SecurityConfig, CorsConfig
│   │   │   ├── controller/      # REST-контроллеры
│   │   │   ├── dto/             # Request/Response DTO, MapStruct маппперы
│   │   │   ├── entity/          # JPA-сущности
│   │   │   ├── enums/           # Role, PaymentStatus, PaymentMethod
│   │   │   ├── exception/       # GlobalExceptionHandler
│   │   │   ├── repo/            # Spring Data репозитории
│   │   │   ├── security/        # JWT-фильтр, утилиты
│   │   │   └── service/         # Бизнес-логика
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/    # SQL-миграции Flyway
│   └── test/                    # Unit-тесты (JUnit 5 + Mockito)
├── frontend/
│   └── src/app/
│       ├── components/          # Login, Register, Home, Admin, Manager,
│       │                        #   Workplaces, Bookings, Profile
│       ├── guards/              # authGuard, roleGuard
│       ├── interceptors/        # JWT auth interceptor
│       ├── models/              # TypeScript-интерфейсы
│       └── services/            # AuthService, BookingService
└── pom.xml
```

---

## Роли и доступ

| Роль | Возможности |
|------|------------|
| `ROLE_USER` | Регистрация, просмотр рабочих мест, создание/отмена бронирований, личный профиль |
| `ROLE_MANAGER` | Всё выше + просмотр всех бронирований, управление рабочими местами и услугами |
| `ROLE_ADMIN` | Полный доступ + управление пользователями, менеджерами, аналитика доходов |

---

## Основные API-эндпоинты

| Метод | Путь | Доступ |
|-------|------|--------|
| POST | `/api/auth/registration` | Публичный |
| POST | `/api/auth/login` | Публичный |
| GET | `/api/locations` | Публичный |
| GET | `/api/workplaces/{locationId}` | Публичный |
| POST | `/api/bookings` | ROLE_USER |
| GET | `/api/bookings/my` | Аутентифицированный |
| POST | `/api/bookings/{id}/cancel` | ROLE_USER, ROLE_ADMIN |
| GET | `/api/bookings` | ROLE_ADMIN, ROLE_MANAGER |
| GET | `/api/admin/stats/summary` | ROLE_ADMIN |
| POST | `/api/admin/managers` | ROLE_ADMIN |

---

## Конфигурация (`application.yml`)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/booking_db
    username: postgres
    password: qwerty123

jwt:
  secret: <base64-encoded-secret>
```

> **Важно:** JWT-секрет в репозитории предназначен только для разработки. Перед деплоем замените его на случайный Base64-строку длиной не менее 64 байт.

---

## Сборка frontend для production

```bash
cd frontend
npm run build
```

Собранные файлы появятся в `frontend/dist/booking-frontend/`. Их можно раздавать через любой статический сервер (nginx, Apache) или встроить в jar Spring Boot.
