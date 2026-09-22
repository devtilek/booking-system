# Booking System

Online booking system for a nail studio and beauty services.

The project allows clients to view available services, choose a specialist, select a convenient date and time, and create a booking. Administrators can manage services, specialists, schedules, and bookings.

## About the Project

This project is a full-stack booking system designed for a real nail studio workflow.

The main goal is to replace manual appointment management through Instagram messages, phone calls, or messengers with a centralized web application.

### Main Features

* User registration and authentication
* Role-based access
* Service management
* Specialist management
* Working schedule management
* Available time slot calculation
* Appointment booking
* Appointment cancellation
* Booking status management
* Admin panel
* PostgreSQL database
* Form validation
* Spring Security
* Server-side rendering with Thymeleaf

## User Roles

### Client

A client can:

* Register an account
* Log in
* View available services
* View specialists
* Select a service
* Select a date and time
* Create an appointment
* View personal appointments
* Cancel an appointment

### Administrator

An administrator can:

* Manage users
* Create, update and delete services
* Manage specialists
* Configure working schedules
* View all appointments
* Change appointment statuses
* Manage the booking system

## Tech Stack

### Backend

* Java 17
* Spring Boot
* Spring MVC
* Spring Data JPA
* Spring Security
* Hibernate
* Maven
* Lombok

### Frontend

* Thymeleaf
* HTML5
* CSS3
* JavaScript

### Database

* PostgreSQL

### Tools

* IntelliJ IDEA
* Git
* GitHub
* Postman

## Architecture

The application follows a layered architecture:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

Main project structure:

```text
src/main/java/com/booking
│
├── controller
├── service
├── repository
├── entity
├── dto
├── mapper
├── security
├── exception
└── config
```

## Booking Flow

The main booking process:

```text
Client
   ↓
Select service
   ↓
Select specialist
   ↓
Select date
   ↓
System checks availability
   ↓
Select available time
   ↓
Create booking
   ↓
Booking confirmed
```

The system must prevent double booking of the same specialist for the same time slot.

## Booking Statuses

A booking can have different statuses:

```text
PENDING
CONFIRMED
COMPLETED
CANCELLED
```

Example lifecycle:

```text
PENDING → CONFIRMED → COMPLETED

PENDING → CANCELLED
CONFIRMED → CANCELLED
```

## Database

The application uses PostgreSQL as the primary database.

The main entities are planned around:

```text
User
Service
Specialist
Schedule
Booking
```

Relationships:

```text
User
  │
  └── Booking

Specialist
  │
  ├── Schedule
  └── Booking

Service
  │
  └── Booking
```

## Security

Spring Security is used for authentication and authorization.

The application provides different access levels for:

```text
CLIENT
ADMIN
```

Protected resources are available only to authenticated users, while administrative operations are restricted to administrators.

## Validation

The application validates user input before processing requests.

Examples:

* Required fields
* Valid email format
* Valid phone number
* Valid booking date
* Valid booking time
* Correct service selection
* Correct specialist selection

## Error Handling

The application uses centralized exception handling for common errors.

Examples:

```text
UserNotFoundException
BookingNotFoundException
ServiceNotFoundException
SpecialistNotFoundException
TimeSlotUnavailableException
```

The goal is to return clear and predictable responses when an operation cannot be completed.

## Running the Project

### Requirements

Make sure you have installed:

* Java 17+
* Maven
* PostgreSQL
* Git

### Clone the repository

```bash
git clone https://github.com/devtilek/booking-system.git
```

```bash
cd booking-system
```

### Database

Create a PostgreSQL database:

```sql
CREATE DATABASE booking_system;
```

Configure the database connection in:

```text
src/main/resources/application.properties
```

Example:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/booking_system
spring.datasource.username=postgres
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### Run the application

Using Maven:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

The application will start on:

```text
http://localhost:8080
```

## Future Improvements

Planned improvements include:

* REST API
* Swagger / OpenAPI documentation
* Online payment integration
* Email notifications
* Telegram notifications
* Calendar integration
* Image upload for services and specialists
* Advanced admin dashboard
* Booking history
* Statistics and analytics
* Docker support
* Unit and integration tests
* CI/CD with GitHub Actions

## Project Goals

This project is created as a real-world portfolio project to demonstrate practical backend development skills.

The project focuses on:

* Spring Boot development
* Database design
* Authentication and authorization
* REST principles
* Business logic
* Clean architecture
* Exception handling
* Validation
* Git/GitHub workflow
* Testing
* Production-oriented development

## Author

**Aktilek Korganbek**

Java Backend Developer

GitHub: `https://github.com/devtilek`
