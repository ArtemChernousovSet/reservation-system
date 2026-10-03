# Reservation System

REST API for managing room reservations, built with Java 21 and Spring Boot.

The project focuses on backend fundamentals used in real applications: database-backed authentication, session-based security, ownership and role-based authorization, persistence with PostgreSQL, reservation conflict checks, validation, and automated service and security integration tests.

> **Project status:** Authorization for `USER` and `ADMIN` roles and Spring Security integration tests are implemented. API documentation is planned next.

## Features

- User registration and login
- Password encoding with Spring Security
- Session-based authentication using `SecurityContext` and `JSESSIONID`
- Role-based authorization for `USER` and `ADMIN`
- Create reservations for the authenticated user with server-controlled ownership and initial status
- View, update, and cancel owned reservations as a regular user
- Prevent regular users from accessing reservations owned by other users
- Allow administrators to view, update, and cancel reservations across users
- Allow administrators to cancel approved reservations
- Restrict reservation search and approval endpoints to `ADMIN`
- Validate reservation date ranges and reject start dates in the past
- Reservation availability and date-conflict checks
- Search and pagination support for reservations
- Centralized exception handling
- Service unit tests and Spring Security integration tests with JUnit 5, Mockito, and MockMvc

## Tech Stack

- Java 21
- Spring Boot 4
- Spring Web MVC
- Spring Security
- Spring Data JPA / Hibernate
- PostgreSQL
- Maven
- JUnit 5
- Mockito
- Docker for local PostgreSQL development

## Architecture

The application follows a conventional layered backend structure:

```text
HTTP Request
    ↓
Spring Security
    ↓
Controller
    ↓
Service
    ↓
Repository
    ↓
PostgreSQL
```

Authentication, reservation ownership, and role checks are kept separate from client input. The client does not choose the reservation owner or initial status.

For reservation operations that support administrative access, the service applies ownership and role checks:

```text
Authenticated request
        ↓
Authentication.getName()
        ↓
username
        ↓
UserRepository
        ↓
current user + role
        ↓
is owner OR ADMIN?
      ↓        ↓
     yes       no
      ↓         ↓
    allow   403 Forbidden
```

When a reservation is created, the server assigns the authenticated user's ID and sets the reservation status to `PENDING`. When an administrator updates another user's reservation, the original owner is preserved.

## API

### Authentication

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/user/register` | Public | Register a new user with the `USER` role |
| `POST` | `/user/login` | Public | Authenticate and create an HTTP session |

### Reservations

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/reservation` | Authenticated | Create a reservation for the current user |
| `GET` | `/reservation/all` | Authenticated | Get reservations owned by the current user |
| `GET` | `/reservation/{id}` | Owner or Admin | Get a reservation when owned by the user or accessed by an admin |
| `PUT` | `/reservation/{id}` | Owner or Admin | Update a `PENDING` reservation while preserving its owner |
| `DELETE` | `/reservation/{id}/cancel` | Owner or Admin | Cancel a reservation; admins may also cancel approved reservations |
| `POST` | `/reservation/availability/check` | Authenticated | Check whether a room is available for a date range |
| `GET` | `/reservation` | Admin | Search reservations with filters and pagination |
| `POST` | `/reservation/{id}/approve` | Admin | Approve a pending reservation after a conflict check |

Regular users can work only with their own reservations. Administrative search and approval are restricted at the Spring Security layer, while ownership-sensitive operations also enforce authorization in the service layer.

## Authentication

Login uses Spring Security's `AuthenticationManager`. After successful authentication, the application stores the authenticated `Authentication` object in a `SecurityContext` and persists it through `HttpSessionSecurityContextRepository`.

Subsequent requests are associated with the authenticated user through the session cookie (`JSESSIONID`).

Passwords are stored using Spring Security's `DelegatingPasswordEncoder`.

New registrations are assigned the `USER` role by the server; clients cannot register themselves as `ADMIN`.

## Running Locally

### Requirements

- Java 21
- Docker or a local PostgreSQL installation

Start PostgreSQL with Docker, for example:

```bash
docker run --name reservation-postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres
```

Set the database credentials expected by `application.properties`:

```bash
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
```

Run the application:

```bash
./mvnw spring-boot:run
```

The API is available at:

```text
http://localhost:8080
```

## API Usage Example

Register a user:

```bash
curl -X POST http://localhost:8080/user/register \
  -H "Content-Type: application/json" \
  -d '{"username":"artem","password":"password"}'
```

Login and save the session cookie:

```bash
curl -X POST http://localhost:8080/user/login \
  -H "Content-Type: application/json" \
  -c cookies.txt \
  -d '{"username":"artem","password":"password"}'
```

Create a reservation using that session:

```bash
curl -X POST http://localhost:8080/reservation \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "roomId": 5,
    "startDate": "2030-01-20",
    "endDate": "2030-01-22"
  }'
```

The client does not send `userId` or `status`; both are controlled by the server. Reservation start dates cannot be in the past, and `endDate` must be after `startDate`.

## Testing

Run the test suite with:

```bash
./mvnw test
```

The current service tests cover successful flows and failure cases including:

- reservation creation and date validation
- ownership checks for reading, updating, and cancelling reservations
- administrative access to reservations owned by other users
- preservation of reservation ownership during admin updates
- admin cancellation of approved reservations
- missing users and reservations
- invalid reservation status transitions
- search pagination behavior
- reservation approval and availability conflicts

Spring Security integration tests verify:

- unauthenticated requests to protected endpoints return `401 Unauthorized`
- authenticated `USER` access to admin-only endpoints returns `403 Forbidden`
- authenticated `ADMIN` access to admin-only endpoints succeeds
- authenticated regular users can access endpoints protected by `.authenticated()`

## Error Handling

The API uses a centralized `GlobalExceptionHandler` for application-level errors, including:

- `400 Bad Request` — invalid input or invalid reservation state
- `401 Unauthorized` — invalid credentials or missing authentication
- `403 Forbidden` — ownership or role-based authorization failure
- `404 Not Found` — user or reservation does not exist
- `500 Internal Server Error` — unexpected server errors

## Roadmap

- Add OpenAPI / Swagger documentation
- Review CSRF strategy before production-style browser usage
