# Expense Tracker Backend

A secure Spring Boot REST API for tracking expenses with JWT authentication, refresh tokens, role-based access, filtering, pagination, Swagger docs, H2 for local development, and PostgreSQL-ready deployment support.

## Features

- User registration and login with BCrypt password hashing
- JWT access tokens plus refresh-token rotation
- Logout by revoking refresh tokens
- Expense CRUD with per-user ownership checks
- Expense filtering by category and date range
- Pagination and sorting for expense listing
- Monthly summary with category breakdown
- Role-based access with an admin-only users endpoint
- Global exception handling and request validation
- Swagger UI / OpenAPI docs
- Docker and Docker Compose support
- H2 development setup and PostgreSQL profile

## Tech Stack

- Java 17
- Spring Boot 3.5
- Spring Web
- Spring Data JPA
- Spring Security
- JWT (`jjwt`)
- Hibernate Validator
- H2 Database
- PostgreSQL
- springdoc OpenAPI
- Maven
- JUnit 5 / MockMvc

## Project Structure

```text
src/main/java/com/malkeet/ExpenseTracker
├── config
├── controller
├── dto
├── entity
├── exception
├── repository
├── security
└── service
```

## Authentication

The API uses:

- Short-lived access tokens for authenticated requests
- Refresh tokens stored in the database
- Token rotation on refresh
- Refresh-token revocation on logout

The first registered user becomes `ADMIN`. All later users are registered as `USER`.

## Main Endpoints

### Auth

- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/refresh`
- `POST /auth/logout`

### Expenses

- `POST /expenses`
- `GET /expenses`
- `GET /expenses/{id}`
- `PUT /expenses/{id}`
- `DELETE /expenses/{id}`
- `GET /expenses/summary/monthly`

### Admin

- `GET /admin/users`

## Example Expense Query

```http
GET /expenses?page=0&size=10&sortBy=date&sortDir=desc&category=FOOD&startDate=2026-04-01&endDate=2026-04-30
Authorization: Bearer <access-token>
```

## Swagger

After starting the app, open:

- `http://localhost:8080/swagger-ui.html`

## Local Run

### Option 1: Maven Wrapper

```powershell
.\mvnw.cmd spring-boot:run
```

### Option 2: Installed Maven

```powershell
mvn spring-boot:run
```

## H2 Console

- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:expensetracker`
- Username: `sa`
- Password: blank

## PostgreSQL Profile

Run with the PostgreSQL profile:

```powershell
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

Environment variables used by the PostgreSQL profile:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

## Docker

Build and run everything with PostgreSQL:

```powershell
docker-compose up --build
```

App URL:

- `http://localhost:8080`

Swagger URL:

- `http://localhost:8080/swagger-ui.html`

## Testing

Run tests with:

```powershell
mvn test
```

Current automated coverage includes:

- Spring context boot test
- Register/login flow
- Refresh/logout flow
- Expense create/list/summary flow
- Admin endpoint role protection
- Unauthorized access checks

## Sample Auth Response

```json
{
  "token": "jwt-access-token",
  "refreshToken": "refresh-token",
  "tokenType": "Bearer",
  "role": "USER",
  "name": "Malkeet",
  "email": "malkeet@example.com"
}
```

## Notes

- Access tokens are stateless JWTs.
- Logout revokes the refresh token used for future renewals.
- Expense data is always scoped to the authenticated user.
- The admin endpoint is available only to users with the `ADMIN` role.

## Next Possible Improvements

- Redis-backed token/session management
- Email verification / password reset
- CSV export
- Rate limiting
- CI pipeline
- Deployment to Render, Railway, or AWS
