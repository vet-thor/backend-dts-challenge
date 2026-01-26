 # Tracking System 1.0 - Authenticated Task Management
This is a task management app built for the HMCTS DTS Developer Challenge. It helps caseworkers stay on top of their daily tasks with a simple, easy-to-use interface and a reliable backend that keeps everything running smoothly.

---

## Table of Contents

- [Feature](#feature)
- [Setup](#setup)
- [Authentication Flow](#authentication-flow)
- [API Documentation](#api-documentation)
- [Database Configuration](#database-configuration)
- [Testing](#testing)
- [Advanced Options](#advanced-options)
- [Localisation](#localisation)

---

## Feature

- JWT authentication for secure access
- User-based task filtering
- Updated unit tests for auth flows
- data.sql for demo users and tasks
- Role-based access control
- Localisation Support: Language files added for English [UK, US], French and Spanish

---

## Setup

**Requirements:**

- Java 17+
- Maven

**Run the application:**

```bash
mvn spring-boot:run
```

**Access Points:**

- API:

[`http://localhost:8081/api/v2/case`](http://localhost:8081/api/v2/case)

- Auth: 
[`http://localhost:8081/api/v2/auth`](http://localhost:8081/api/v2/auth)

- H2 Console:

[`http://localhost:8081/h2-console`](http://localhost:8081/h2-console)  
JDBC URL: `jdbc:h2:mem:caseworkerTestDB`  
*(Credentials in `application.properties`)*


---

## Authentication Flow
| Username | Password | Role |
|----------|----------|------|
| staff | pass123 | ROLE_STAFF |
| member | pass123 | ROLE_STAFF |
| user | pass123 | ROLE_USER |

Login Endpoint: POST /api/v2/auth/

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

## API Documentation

Swagger UI:

[`http://localhost:8081/swagger-ui/index.html`](http://localhost:8081/swagger-ui/index.html)

| Method | Endpoint | Description                     |
|--------|------------------|---------------------------------|
| POST   | `/api/v2/auth/` | Login and Recieve JWT           |
| POST   | `/api/v2/case`   | Create new case (auth required) |
| GET    | `/api/v2/case`  | List all cases  (auth required) |
| GET    | `/api/v2/case/{id}` | Get case by ID (auth required)  |
| PUT    | `/api/v2/case/{id}` | Update case (auth required)     |
| DELETE | `/api/v2/case/{id}` | Delete case (auth required)    |
 

---

## Testing

Run all tests:

```bash
mvn test
```

Includes:
1. Auth controller test
2. Token validation
3. Task access per user
---

## ️Database Configuration

- H2 Console:

[`http://localhost:8081/h2-console`](http://localhost:8081/h2-console)
- JDBC URL: `jdbc:h2:mem:caseworkerTestDB`  
  *(Credentials match `.env` or `application.properties`)*

---


## Advanced Options

###  Switch to PostgreSQL

Update `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/caseworkerTestDB
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.username=postgres
spring.datasource.password=yourpassword
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

Or use environment variables:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/caseworkerTestDB
export SPRING_DATASOURCE_DRIVER=org.postgresql.Driver
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=yourpassword
export SPRING_DATABASE_PLATFORM=org.hibernate.dialect.PostgreSQLDialect
```

Uncomment the following in `application.properties` for development/debugging:

```properties

# SQL Logging Configuration
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

Add PostgreSQL dependency to `pom.xml` file:

```xml
<dependency>
  <groupId>org.postgresql</groupId>
  <artifactId>postgresql</artifactId>
</dependency>
```
---

## Localisation

The app supports dynamic language switching via message bundles. The default language is English (GB). 
The following languages are supported English, French and Spanish

Usage
```http
GET /api/v2/case?locale=en-gb
```

Supported Locale Values:

| Locale | Language | Region         |
|--------|----------|----------------|
| en-gb  | English  | United Kingdom |
| en-us  | English  | United State   |
| fr-fr  | French   | France         |
| es-es  | Spanish  | Spain          |

---
