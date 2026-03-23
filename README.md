 # Tracking System 1.0 - Authenticated Task Management
This is a professional task management app built for the HMCTS DTS Developer Challenge. It helps caseworkers stay on top of their daily tasks with a simple, easy to use interface and a reliable backend that keeps everything running smoothly.

---

## Table of Contents

- [Key features](#key-features)
- [Setup](#setup)
- [Authentication Flow](#authentication-flow)
- [Architecture and Infrastructure](#architecture-and-infrastructure)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Localisation](#localisation)

---

## Key features
- Integrated Valkey 9.0.1 (the latest open-source Redis fork)
- Native Spring Boot support for automatic Docker infrastructure startup.
- Implemented Testcontainers for isolated, reliable integration tests against real service instances.
- Stateless JWT Authentication with Role Based Access Control (RBAC).
---

## Setup

**Prerequisites:**
Docker must be running before starting the application or running tests. The app uses Spring Boot Docker Compose support and Testcontainers to manage the infrastructure automatically.
- Java 17+
- Maven
- Docker Desktop

**Run the application:**

```bash
mvn spring-boot:run
```
**What happens behind the scenes?**
- Orchestration: Spring Boot detects your compose.yaml file and automatically tells Docker to pull and start PostgreSQL 16 and Valkey 9.0.1 containers.

- Dynamic Injection: Spring intercepts the random ports assigned by Docker and pushes those values directly into the application's memory while it's starting up.

- Zero Config: Because of this automatic discovery, you don't need to manually write database URLs or passwords in application.properties. The app connects to the services automatically.

**Access Points:**

- API:

[`http://localhost:8081/api/v1/tasks`](http://localhost:8081/api/v2/case)

- Auth: 
[`http://localhost:8081/api/v1/auth`](http://localhost:8081/api/v2/auth)

---

## Authentication Flow
| Username | Password | Role |
|----------|----------|------|
| staff | pass123 | ROLE_STAFF |
| member | pass123 | ROLE_STAFF |
| user | pass123 | ROLE_USER |

Login Endpoint: POST /api/v1/auth

Request:
```payload
{
  "username": "staff",
  "password": "pass123"
}
```
Response:
```payload
{
    "message": "User logged in successfully",
    "status": "OK",
    "data": {
        "accessToken": <token>,
        "tokenType": "Bearer "
    }
}
```
Use this token in the Authorization header for all protected endpoints:
```header
Authorization: Bearer <token>
```
---

## Architecture and Infrastructure

The system has been engineered for scalability and reliability:

- Persistence: PostgreSQL handles all relational data (Users, Tasks, Roles).

- Caching: Valkey 9.0.1 provides the caching layer, ensuring high speed access to frequently requested task filters.

- Security: Stateless JWT based authentication with Role Based Access Control (RBAC).
---
## API Documentation

Swagger UI:

[`http://localhost:8081/swagger-ui/index.html`](http://localhost:8081/swagger-ui/index.html)

| Method | Endpoint             | Description                     |
|--------|----------------------|---------------------------------|
| POST   | `/api/v1/auth/`      | Login and Recieve JWT           |
| POST   | `/api/v1/tasks`      | Create new case (auth required) |
| GET    | `/api/v1/tasks`      | List all cases  (auth required) |
| GET    | `/api/v1/tasks/{id}` | Get case by ID (auth required)  |
| PUT    | `/api/v1/tasks/{id}` | Update case (auth required)     |
| DELETE | `/api/v1/tasks/{id}` | Delete case (auth required)    |
 

---

## Testing
We use Testcontainers to run integration tests against real instances of Postgres and Valkey. This ensures that the code behaves exactly the same in the test environment as it does in production.

Run all tests:
```bash
mvn test
```

Developer Note: We utilize @ServiceConnection in our test configurations. This allows the JUnit tests to dynamically discover the container ports, preventing "Port already in use" errors during parallel CI/CD runs.

---

## Localisation

The app supports dynamic language switching via message bundles. The default language is English (GB). 
The following languages are supported English, French and Spanish

Usage
```http
GET /api/v1/tasks?locale=en-gb
```

Supported Locale Values:

| Locale | Language | Region         |
|--------|----------|----------------|
| en-gb  | English  | United Kingdom |
| en-us  | English  | United State   |
| fr-fr  | French   | France         |
| es-es  | Spanish  | Spain          |

---
