Employee Management System

A comprehensive Employee Management REST API built with Spring Boot 3.
This system provides secure employee, department, salary, and document management with JWT-based authentication, role-based authorization, Swagger API documentation, Docker support, and fully tested services (Mockito & Testcontainers).

🚀 Features
🔐 Security

JWT Authentication & Authorization

Role-based access control (ADMIN, HR, MANAGER)

Spring Security Filter Chain

Custom JWT authentication filter

BCrypt password encoding

👥 User Management

Create, update, delete users

Generate JWT tokens

Search users by username or role

Count total users

🧑‍💼 Employee Management

Full CRUD operations

Advanced employee search (name, department, status, age range)

Pagination & sorting

Salary-based filtering

Employee statistics:

Total employees

Active employee percentage

New employees in last 30 days

Employee age calculation

🏢 Department Management

Create, update, delete departments

Search by name

Employee count per department

Department statistics:

Min / Max / Avg salary

Employee count

Position count

Yearly statistics

Manager-based department filtering

💰 Salary Management

Salary CRUD

Bonus management

Salary history per employee

Salary statistics:

Min / Max / Avg salary

Top 10 highest salaries

Top 10 highest bonuses

Monthly salary statistics

Department & date-range based queries

PDF salary report generation

📁 Employee Document Management

Upload employee documents

Categorize documents

Download documents

Delete documents

File storage inside Docker container

📚 API Documentation

Swagger UI (OpenAPI 3)

JWT authentication supported in Swagger

🛠 Tech Stack
Category	Technology
Language	Java 17
Framework	Spring Boot 3.2.5
Security	Spring Security + JWT
Database	PostgreSQL
ORM	Spring Data JPA (Hibernate)
Validation	Jakarta Validation
Documentation	SpringDoc OpenAPI
Testing	JUnit 5, Mockito, Testcontainers
PDF	OpenPDF
Build Tool	Gradle
Containerization	Docker
🔑 Authentication & Authorization
JWT Flow

User logs in via /api/users/generateToken

JWT token is returned

Token must be sent in header:

Authorization: Bearer <JWT_TOKEN>

Roles

ADMIN – Full access

HR – Employee & salary access

MANAGER – Read-only employee & department stats

📦 API Endpoints (Overview)
Users

POST /api/users

POST /api/users/generateToken

GET /api/users

GET /api/users/by-username

GET /api/users/by-role

DELETE /api/users/{id}

Employees

POST /api/employees/save

PUT /api/employees/{id}

GET /api/employees

GET /api/employees/advanced-search

GET /api/employees/paging

DELETE /api/employees/{id}

Departments

POST /api/departments/create

PUT /api/departments/update/{id}

GET /api/departments/all

GET /api/departments/{id}/stats

DELETE /api/departments/delete/{id}

Salaries

POST /api/salaries

PUT /api/salaries/{id}/bonus

GET /api/salaries/top10

GET /api/salaries/monthly-stats

DELETE /api/salaries/{id}

Documents

POST /api/employees/{employeeId}/documents/upload

GET /api/employees/{employeeId}/documents/list

GET /api/employees/{employeeId}/documents/file/{documentId}/download

DELETE /api/employees/{employeeId}/documents/{documentId}

🧪 Testing

Unit Tests

Service layer tested using Mockito

Controllers tested with mocked dependencies

Integration Tests

PostgreSQL via Testcontainers

Real database behavior validation

Transaction rollback after each test

🐳 Docker Support
Build Image
docker build -t employee-management .

Run Container
docker run -p 8080:8080 employee-management

File Upload Directory
/app/uploads

⚙ Configuration
application.yml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/employee_management
    username: mohirdev
    password: 123

jwt:
  secret: verylongsecretkey-shouldbeatleast32characters!!

📖 Swagger UI

Access API documentation at:

http://localhost:8080/swagger-ui/index.html


JWT authentication is fully supported inside Swagger UI.

👨‍💻 Author

Ruslan
Java Backend Developer
📧 javabackenddeveloperbro@gmail.com
