# Spring Boot Authorization Server

A Spring Boot application implementing OAuth2 authorization server with role-based access control.

## Features

- OAuth2 Authorization Server with JWT support
- Role-based access control (USER and ADMIN roles)
- PostgreSQL database with Flyway migrations
- Sample CRUD API with role-based restrictions

## Prerequisites

- Java 17
- Maven
- PostgreSQL 12+

## Setup

1. Create PostgreSQL database:
```sql
CREATE DATABASE authdb;
```

2. Configure database connection in `application.yml` if needed:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/authdb
    username: postgres
    password: postgres
```

3. Build and run the application:
```bash
mvn clean install
mvn spring-boot:run
```

## Default Users

- Admin user:
  - Username: admin
  - Password: admin123

- Regular user:
  - Username: user
  - Password: user123

## API Endpoints

### OAuth2 Endpoints
- Authorization: `http://localhost:8080/oauth2/authorize`
- Token: `http://localhost:8080/oauth2/token`

### Product API
- GET `/api/products` - List all products (USER, ADMIN)
- GET `/api/products/{id}` - Get product by ID (USER, ADMIN)
- POST `/api/products` - Create product (ADMIN only)
- PUT `/api/products/{id}` - Update product (ADMIN only)
- DELETE `/api/products/{id}` - Delete product (ADMIN only)

## Security

The application uses:
- OAuth2 with JWT tokens
- BCrypt password encryption
- Role-based access control
- PostgreSQL for data persistence
- Flyway for database migrations 