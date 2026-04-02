# Expense Tracker Codebase Study Guide

This file explains the entire codebase in a student-friendly way.

Use it for:

- understanding the project from scratch
- revision before interviews or presentations
- quickly remembering how the layers connect
- learning how Spring Boot backend projects are structured

---

## 1. Project Goal

This project is a backend API for an Expense Tracker application.

It allows users to:

- register and log in
- receive JWT access tokens
- use refresh tokens to get new access tokens
- create, update, view, filter, and delete their own expenses
- see a monthly summary of expenses

It also supports:

- role-based access
- an admin-only endpoint
- interactive API docs
- H2 for development
- PostgreSQL profile for deployment
- Docker support

---

## 2. High-Level Architecture

The project follows layered architecture:

`Controller -> Service -> Repository -> Database`

Each layer has one main responsibility:

- `Controller`: accepts HTTP requests and returns HTTP responses
- `Service`: contains business logic
- `Repository`: talks to the database using Spring Data JPA
- `Entity`: defines database tables as Java classes
- `DTO`: defines request and response payloads
- `Security`: handles authentication and authorization
- `Exception`: handles errors in a clean and centralized way
- `Config`: contains application configuration beans

This separation makes the code easier to read, test, and maintain.

---

## 3. Main Folders and Why They Exist

### `src/main/java/com/malkeet/ExpenseTracker/config`

Contains configuration classes.

- `AppConfig.java`
  - creates beans like `BCryptPasswordEncoder`
- `SecurityConfig.java`
  - configures Spring Security
- `ApiDocsConfig.java`
  - configures interactive API documentation
- `OpenApiConfig.java`
  - now only a placeholder comment from an earlier rename

### `controller`

Entry point for API requests.

- `AuthController.java`
  - register, login, refresh, logout
- `ExpenseController.java`
  - expense CRUD, filtering, monthly summary
- `AdminController.java`
  - admin-only endpoint to list users

### `service`

Defines the service contracts.

- `AuthService.java`
- `ExpenseService.java`
- `UserService.java`

### `service/impl`

Contains real business logic.

- `AuthServiceImpl.java`
- `ExpenseServiceImpl.java`
- `UserServiceImpl.java`

### `repository`

Database access layer.

- `UserRepository.java`
- `ExpenseRepository.java`
- `RefreshTokenRepository.java`

### `entity`

Database models.

- `BaseEntity.java`
- `User.java`
- `Expense.java`
- `RefreshToken.java`

### `entity/enums`

Enums used in entities.

- `ExpenseCategory.java`
- `Role.java`

### `dto/request`

Objects used when client sends data to API.

- `RegisterRequest`
- `LoginRequest`
- `RefreshTokenRequest`
- `LogoutRequest`
- `ExpenseCreateRequest`
- `ExpenseUpdateRequest`

### `dto/response`

Objects returned by the API.

- `AuthResponse`
- `ExpenseResponse`
- `MonthlySummaryResponse`
- `MessageResponse`
- `PagedResponse<T>`
- `UserResponseDTO`

### `security`

Authentication and authorization logic.

- `SecurityConfig`
- `JwtService`
- `JwtAuthenticationFilter`
- `CustomUserDetailsService`

### `exception`

Centralized error handling.

- `GlobalExceptionHandler`
- `ApiException`
- `BadRequestException`
- `ConflictException`
- `NotFoundException`

---

## 4. Startup Flow: What Happens When the App Starts

When the application starts, Spring Boot scans the package and creates beans.

Main startup class:

- `ExpenseTrackerApplication.java`

Important beans created during startup:

- `BCryptPasswordEncoder` from `AppConfig`
- `SecurityFilterChain` from `SecurityConfig`
- `AuthenticationManager`
- `DaoAuthenticationProvider`
- `JwtAuthenticationFilter`
- `CustomUserDetailsService`
- repositories for JPA
- services and controllers

Database tables are created automatically because JPA/Hibernate is enabled.

The base config comes from:

- `src/main/resources/application.properties`

Optional PostgreSQL override comes from:

- `src/main/resources/application-postgres.properties`

---

## 5. Database Design

### `BaseEntity`

Purpose:

- common fields reused by other entities

Fields:

- `id`
- `createdAt`
- `updatedAt`

Why it exists:

- avoids repeating common fields in every entity

How it works:

- `@PrePersist` sets timestamps when record is first created
- `@PreUpdate` updates timestamp when record changes

### `User`

Represents an application user.

Important fields:

- `email`
- `password`
- `name`
- `role`

Relations:

- one user can have many expenses
- one user can have many refresh tokens

### `Expense`

Represents one expense entry.

Important fields:

- `title`
- `description`
- `amount`
- `category`
- `date`
- `user`

Relation:

- many expenses belong to one user

### `RefreshToken`

Represents a stored refresh token for a user.

Important fields:

- `token`
- `expiresAt`
- `revoked`
- `user`

Why store refresh tokens in DB:

- allows logout
- allows rotation
- allows invalidation

---

## 6. Security Flow

This is one of the most important parts of the project.

### Main Security Classes

#### `SecurityConfig`

This class defines how Spring Security should behave.

Responsibilities:

- disables CSRF for stateless API use
- allows public access to:
  - `/auth/**`
  - `/h2-console/**`
  - docs endpoints
- protects everything else
- sets session mode to stateless
- adds `JwtAuthenticationFilter` before the default username/password filter

Why stateless:

- JWT-based APIs do not store session state on server

#### `JwtService`

Purpose:

- create JWT tokens
- read username from token
- check whether token is valid

Important methods:

- `generateToken(subject)`
- `extractUsername(token)`
- `isTokenValid(token, expectedUsername)`

Why subject is email:

- email uniquely identifies the user in this project

#### `CustomUserDetailsService`

Purpose:

- loads a user from database for Spring Security

How it works:

- receives username, which is email here
- finds user in `UserRepository`
- converts user into Spring Security `UserDetails`
- assigns role using `roles(user.getRole().name())`

#### `JwtAuthenticationFilter`

Purpose:

- checks every protected request for `Authorization: Bearer <token>`

Flow:

1. read `Authorization` header
2. if header is missing or not `Bearer`, continue filter chain
3. extract token
4. get username from token using `JwtService`
5. load user details from DB
6. validate token
7. if valid, place authenticated user into `SecurityContextHolder`
8. request continues as authenticated

Why this matters:

- without this filter, protected endpoints would not know who the user is

---

## 7. Authentication Flow

Authentication logic lives mainly in:

- `AuthController`
- `AuthServiceImpl`
- `JwtService`
- `RefreshTokenRepository`

### Register Flow

Request:

- `POST /auth/register`

Path:

1. `AuthController.register()`
2. `AuthServiceImpl.register()`
3. validate email uniqueness
4. validate password == confirmPassword
5. create `User`
6. encode password using BCrypt
7. assign role:
   - first user becomes `ADMIN`
   - others become `USER`
8. save user
9. create refresh token
10. generate access token
11. return `AuthResponse`

### Login Flow

Request:

- `POST /auth/login`

Path:

1. `AuthController.login()`
2. `AuthServiceImpl.login()`
3. `AuthenticationManager` checks credentials
4. fetch user from DB
5. create new refresh token
6. generate access token
7. return `AuthResponse`

### Refresh Flow

Request:

- `POST /auth/refresh`

Path:

1. receive refresh token
2. find token in DB
3. check if revoked or expired
4. revoke old refresh token
5. create new refresh token
6. generate new access token
7. return new `AuthResponse`

This is called refresh-token rotation.

Why rotation is good:

- if old refresh token leaks, it becomes useless after refresh

### Logout Flow

Request:

- `POST /auth/logout`

Path:

1. find refresh token in DB
2. set `revoked = true`
3. save token
4. return success message

Why logout works here:

- because refresh tokens are stored in database

---

## 8. Expense Flow

This logic mainly lives in:

- `ExpenseController`
- `ExpenseServiceImpl`
- `ExpenseRepository`

### Create Expense

Endpoint:

- `POST /expenses`

Flow:

1. request hits `ExpenseController.createExpense()`
2. `Principal` gives logged-in user email
3. `ExpenseServiceImpl.addExpense()` runs
4. user is fetched from DB
5. expense object is created
6. fields are copied from request
7. expense is linked to user
8. saved using repository
9. returned as `ExpenseResponse`

### List Expenses

Endpoint:

- `GET /expenses`

Supports:

- category filter
- date range filter
- pagination
- sorting

Flow:

1. controller receives query parameters
2. service finds current user
3. service builds JPA `Specification`
4. specification always includes user ownership
5. optional filters are added:
   - category
   - date range
6. pageable object is created
7. repository runs paginated query
8. results are converted to `PagedResponse<ExpenseResponse>`

Why this is better than filtering in Java:

- database does the work
- faster
- scalable
- safer for multi-user data

### Get Expense By ID

Endpoint:

- `GET /expenses/{id}`

Important security detail:

- service uses `findByIdAndUserId()`
- so a user cannot read another user’s expense

### Update Expense

Endpoint:

- `PUT /expenses/{id}`

Flow:

1. find expense by id + current user id
2. update fields
3. save
4. return updated response

### Delete Expense

Endpoint:

- `DELETE /expenses/{id}`

Flow:

1. find expense belonging to current user
2. delete it

### Monthly Summary

Endpoint:

- `GET /expenses/summary/monthly`

Flow:

1. service receives year and month
2. builds date range for full month
3. fetches all user expenses in that range
4. calculates:
   - total amount
   - total number of expenses
   - category-wise totals
5. returns `MonthlySummaryResponse`

---

## 9. Admin Flow

Admin logic uses:

- `AdminController`
- `UserServiceImpl`

Endpoint:

- `GET /admin/users`

Protection:

- `@PreAuthorize("hasRole('ADMIN')")`

Flow:

1. request enters controller
2. Spring Security checks user role
3. if role is `ADMIN`, method runs
4. `UserServiceImpl.getAllUsers()` fetches users
5. users are converted to `UserResponseDTO`
6. result returned

Why DTO is used:

- avoids exposing full entity directly
- hides password

---

## 10. Repositories: How Data Access Works

### `UserRepository`

Used for:

- checking email existence
- finding user by email
- counting admins

### `ExpenseRepository`

Used for:

- find expense by owner
- find by filters
- paginated search using `JpaSpecificationExecutor`
- safe lookup by expense id and user id

### `RefreshTokenRepository`

Used for:

- lookup refresh token by token value
- revoke tokens

---

## 11. DTOs: Why They Exist

DTO means Data Transfer Object.

These classes define what enters and leaves the API.

Why DTOs are important:

- avoid exposing full database entities
- allow validation
- keep API stable even if entity changes

### Request DTOs

- `RegisterRequest`
  - name, email, password, confirmPassword
- `LoginRequest`
  - email, password
- `RefreshTokenRequest`
  - refreshToken
- `LogoutRequest`
  - refreshToken
- `ExpenseCreateRequest`
  - title, description, amount, date, category
- `ExpenseUpdateRequest`
  - same structure as create

### Response DTOs

- `AuthResponse`
  - access token, refresh token, token type, role, name, email
- `ExpenseResponse`
  - expense details
- `MonthlySummaryResponse`
  - month, total, count, category breakdown
- `MessageResponse`
  - simple success message
- `PagedResponse<T>`
  - content, page info, total counts
- `UserResponseDTO`
  - id, name, email, role

---

## 12. Exception Handling

Main class:

- `GlobalExceptionHandler`

Purpose:

- keeps controller code clean
- returns meaningful HTTP responses
- centralizes error formatting

Custom exception hierarchy:

- `ApiException`
- `BadRequestException`
- `ConflictException`
- `NotFoundException`

Examples:

- duplicate email -> `409 Conflict`
- invalid refresh token -> `400 Bad Request`
- missing expense -> `404 Not Found`

Why this is good:

- easy to debug
- API responses are consistent
- business errors are separated from system errors

---

## 13. Configuration and Connectivity

This section answers: when, where, why, and how things connect.

### How HTTP request reaches business logic

`Client -> Controller -> Service -> Repository -> Database`

### How security connects

`Request -> JwtAuthenticationFilter -> SecurityContext -> Controller`

### How authentication manager connects

`AuthServiceImpl -> AuthenticationManager -> DaoAuthenticationProvider -> CustomUserDetailsService -> UserRepository`

### How JWT connects to user

`JwtService token subject = email`

Then:

- filter extracts email from token
- `CustomUserDetailsService` loads matching user

### How refresh token connects

`AuthServiceImpl -> RefreshTokenRepository -> RefreshToken entity -> User`

### How expense ownership is enforced

Current user email is taken from `Principal`.

Then:

- service fetches user by email
- repository queries include user id

This prevents cross-user access.

---

## 14. Important “Why” Questions

### Why use JWT?

- stateless authentication
- easy for frontend/mobile clients
- no server session needed

### Why also use refresh tokens?

- access tokens should be short-lived
- refresh tokens let users continue safely without logging in again

### Why store refresh tokens in DB?

- allows logout
- allows revocation
- allows token rotation

### Why use `Principal` in controllers?

- it gives the currently authenticated username
- here that username is the user email

### Why use `Specification` for expense listing?

- dynamic filters
- cleaner than writing many query methods
- supports pagination and sorting naturally

### Why first user becomes admin?

- simple bootstrapping for project demo
- avoids separate manual admin seeding step

---

## 15. End-to-End Connectivity Examples

### Example A: Login

1. client sends email and password to `/auth/login`
2. controller receives request DTO
3. service calls authentication manager
4. authentication provider uses user details service
5. user details service loads user from `UserRepository`
6. password encoder verifies password
7. service creates refresh token in DB
8. service creates access token using `JwtService`
9. response goes back with both tokens

### Example B: Create Expense

1. client sends request with bearer token
2. JWT filter validates token
3. security context is filled
4. controller receives request
5. `Principal` gives email
6. service finds `User`
7. service creates `Expense`
8. repository saves it
9. response DTO is returned

### Example C: Access Admin Endpoint

1. client sends bearer token
2. JWT filter authenticates user
3. Spring checks `@PreAuthorize("hasRole('ADMIN')")`
4. if role is admin, method runs
5. otherwise `403 Forbidden`

---

## 16. Files You Should Read First as a Student

Recommended reading order:

1. `ExpenseTrackerApplication.java`
2. `SecurityConfig.java`
3. `JwtAuthenticationFilter.java`
4. `JwtService.java`
5. `AuthController.java`
6. `AuthServiceImpl.java`
7. `ExpenseController.java`
8. `ExpenseServiceImpl.java`
9. `User.java`
10. `Expense.java`
11. `RefreshToken.java`
12. repositories
13. DTOs
14. `GlobalExceptionHandler.java`

This order helps you understand the project from outside to inside.

---

## 17. Revision Notes

### Quick Revision: Security

- `SecurityConfig` protects routes
- `JwtAuthenticationFilter` reads token
- `JwtService` creates and validates token
- `CustomUserDetailsService` loads user from DB

### Quick Revision: Auth

- register -> save user -> create refresh token -> generate access token
- login -> authenticate -> create refresh token -> generate access token
- refresh -> validate DB token -> revoke old -> create new -> issue new access token
- logout -> revoke refresh token

### Quick Revision: Expense

- controller takes request
- service gets current user
- repository works only on that user’s data
- list endpoint supports filtering + paging + sorting
- summary endpoint calculates month totals

### Quick Revision: Admin

- first registered user becomes `ADMIN`
- admin endpoint is protected by role annotation

---

## 18. Things a New Student Should Practice Here

If you want to learn from this project, try these exercises:

- add a new expense category
- add validation for title length
- add search by title
- add a CSV export endpoint
- add password reset flow
- add unit tests for `AuthServiceImpl`
- seed admin user in a better way
- add rate limiting

These are good revision tasks because they touch multiple layers.

---

## 19. Final Summary in One Paragraph

This codebase is a layered Spring Boot backend where controllers receive API requests, services handle business logic, repositories talk to the database, entities define stored data, DTOs control input/output, and the security layer uses JWT access tokens plus database-backed refresh tokens to protect routes. Every expense is connected to a user, every protected request is authenticated by the JWT filter, admin routes are enforced by roles, and exception handling keeps responses clean and consistent.

---

## 20. If You Forget Everything, Remember This

- Controllers handle requests
- Services contain logic
- Repositories access DB
- JWT filter authenticates requests
- `Principal` gives current user email
- Expenses always belong to one user
- Refresh tokens are stored in DB
- First user becomes admin
- Admin endpoint is role protected
- Expense list uses filtering + pagination + sorting

