💰 Expense Tracker Backend (Spring Boot)

A secure, production-ready REST API for managing user expenses.

Built using modern Spring Boot architecture with JWT authentication, clean layered design, validation, and global exception handling.

Designed as an SDE1-level backend portfolio project.

🚀 Tech Stack
Backend

- Java 17
- Spring Boot 3+
- Spring Web
- Spring Data JPA (Hibernate)
- Spring Security + JWT
- Lombok
- Hibernate Validator
- ModelMapper (optional enhancement)
- Database
- H2 (Development)
- Tools
- Maven
- GitHub Codespaces
- Postman
- Git
- Bash / Terminal

🏗 Architecture

Follows Clean Layered Architecture

Controller → Service → Repository → Database
Key Principles

- Separation of concerns
- DTO-based communication
- Global exception handling
- JWT-based stateless authentication
- Validation at request layer
- Proper HTTP status codes

📂 Project Structure
src/main/java/com/expensetracker/

  config/
    AppConfig.java

  controller/
    AuthController.java
    ExpenseController.java

  service/
    AuthService.java
    ExpenseService.java

  service/impl/
    AuthServiceImpl.java
    ExpenseServiceImpl.java

  repository/
    UserRepository.java
    ExpenseRepository.java

  entity/
    User.java
    Expense.java
    BaseEntity.java   (NEW – common fields)

  dto/
    request/
      RegisterRequest.java
      LoginRequest.java
      ExpenseCreateRequest.java
      ExpenseUpdateRequest.java
    response/
      AuthResponse.java
      ExpenseResponse.java
      MonthlySummaryResponse.java

  security/
    SecurityConfig.java
    JwtService.java
    JwtAuthenticationFilter.java

  exception/
    GlobalExceptionHandler.java
    CustomException.java

  util/
    MapperUtil.java
🧱 Database Design
User

- id (Long)
- name
- email (unique)
- password (BCrypt)
- createdAt
- updatedAt
- Expense
- id (Long)
- title
- description
- amount
- category (ENUM)
- date
- user (ManyToOne)
- createdAt
- updatedAt

🔐 Authentication Flow (Improved)

- User registers
- Password stored using BCrypt
- User logs in
- JWT token generated
- Token sent in:
- Authorization: Bearer <token>
- All expense endpoints secured

📌 API Endpoints (Enhanced)
🔐 Authentication
Method	Endpoint	Description
POST	/auth/register	Register new user
POST	/auth/login	Login & receive JWT
💵 Expense APIs (Protected)
Method	Endpoint	Description
POST	/expenses	Create expense
GET	/expenses	Get all expenses (paginated)
GET	/expenses/{id}	Get expense by ID
PUT	/expenses/{id}	Update expense
DELETE	/expenses/{id}	Delete expense
GET	/expenses/summary/monthly	Monthly analytics
🔍 Advanced Features Added

✅ Pagination & Sorting
GET /expenses?page=0&size=10&sort=date,desc

✅ Filtering
GET /expenses?category=FOOD

GET /expenses?startDate=2025-01-01&endDate=2025-01-31
✅ Monthly Summary Response Example
{
  "month": "January",
  "totalAmount": 1200.50,
  "totalExpenses": 18,
  "categoryBreakdown": {
    "FOOD": 400,
    "TRAVEL": 300,
    "BILLS": 500
  }
}

⚙️ How to Run (GitHub Codespaces)
Step 1
chmod +x mvnw
Step 2
./mvnw clean install
Step 3
./mvnw spring-boot:run

Open:

http://localhost:8080
🛢 H2 Console

URL:
/h2-console

JDBC:
jdbc:h2:mem:testdb

Username:
sa

Password:
(blank)

---

# 🧪 Testing

- Postman Collection included
- Unit tests using JUnit + Mockito
- Integration testing with @SpringBootTest

---

# 📈 Project Roadmap (Improved)

## Phase 1 – Core
- Entities & Repositories
- Basic CRUD
- DTO mapping

## Phase 2 – Security
- Spring Security
- JWT
- Password encryption

## Phase 3 – Advanced Features
- Pagination
- Filtering
- Analytics summary
- Role-based access (ADMIN)

## Phase 4 – Production Ready
- PostgreSQL migration
- Docker support
- Swagger/OpenAPI docs
- Deployment (Render / Railway / AWS)

---

# 🐳 Optional Enhancements (Highly Recommended)

- Add Dockerfile
- Add docker-compose.yml
- Add OpenAPI (Swagger UI)
- Add Refresh Token mechanism
- Add Expense Export (CSV)
- Add Rate limiting
- Add Caching (Redis)

---

# 🎯 Resume Version (Short Description)

> Built a secure Expense Tracker REST API using Spring Boot, JPA, and JWT authentication.  
> Implemented layered architecture, DTO mapping, validation, pagination, filtering, and monthly analytics.  
> Secured endpoints using Spring Security with stateless JWT authentication.

---

# 👨‍💻 Author

**Malkeet Singh Nanra**  
Backend Developer (Java + Spring Boot)  
Transitioning from Cloud Support → SDE1  
