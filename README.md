# Booking System

A full-stack appointment booking system for a beauty studio.

The application lets clients view the studio portfolio and price list, choose an available time slot, and submit an appointment request. Administrators can manage time slots, portfolio images, prices, and bookings from a protected admin panel.

## Features

### Client
- View portfolio and services
- View available appointment slots
- Book an available time slot
- Provide name, phone, selected service and comment
- Receive a booking confirmation message
- Contact the studio through WhatsApp, Instagram or phone

### Administrator
- Secure admin login with Spring Security
- Add and remove available time slots
- View bookings with pagination
- Cancel bookings and release the slot
- Manage portfolio images
- Manage the price list
- Receive Telegram notifications for new and cancelled bookings

## Booking Flow

```text
Client
  ↓
Select available slot
  ↓
Enter contact details
  ↓
Server locks the slot
  ↓
Check that the slot is still free
  ↓
Create booking
  ↓
Mark slot as booked
  ↓
Send Telegram notification
```

The booking operation uses a database pessimistic lock and a unique database constraint on the booking slot to reduce the risk of double booking.

## Architecture

The current application uses a classic Spring layered architecture:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
PostgreSQL
```

Main packages:

```text
com.nailstudio
├── config
├── controller
├── model
├── repository
└── service
```

The project is intentionally being evolved incrementally toward a more production-oriented architecture with DTOs, validation, domain-specific exceptions, stronger testing and a REST API.

## Tech Stack

### Backend
- Java 17
- Spring Boot
- Spring MVC
- Spring Data JPA
- Spring Security
- Hibernate
- Maven
- Lombok

### Frontend
- Thymeleaf
- HTML5
- CSS3
- JavaScript
- Responsive UI
- PWA support

### Database
- PostgreSQL

### Integrations
- Telegram Bot API

## Project Structure

```text
src/
├── main/
│   ├── java/com/nailstudio/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── model/
│   │   ├── repository/
│   │   └── service/
│   └── resources/
│       ├── static/
│       ├── templates/
│       └── application.properties
└── test/
```

## Configuration

Do not commit passwords, bot tokens or other secrets.

Copy the example configuration and provide your own values through environment variables:

```text
src/main/resources/application.properties.example
```

Required environment variables for a local setup:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
ADMIN_USERS
```

Optional Telegram configuration:

```text
TELEGRAM_BOT_TOKEN
TELEGRAM_ADMIN_CHAT_IDS
```

Example:

```properties
DB_URL=jdbc:postgresql://localhost:5432/nailstudio
DB_USERNAME=postgres
DB_PASSWORD=your_password
ADMIN_USERS=admin:strong_password
TELEGRAM_BOT_TOKEN=your_bot_token
TELEGRAM_ADMIN_CHAT_IDS=your_chat_id
```

For security, the Telegram bot token previously stored in the repository must be revoked and replaced before using the application in production.

## Running Locally

### Requirements

- Java 17+
- PostgreSQL
- Maven

### Database

Create a PostgreSQL database:

```sql
CREATE DATABASE nailstudio;
```

Set the required environment variables and start the application:

Linux/macOS:

```bash
./mvnw spring-boot:run
```

Windows:

```bash
mvnw.cmd spring-boot:run
```

The application starts on:

```text
http://localhost:8080
```

Admin panel:

```text
http://localhost:8080/login
```

## Security Notes

- Admin routes are protected by Spring Security.
- Admin passwords are encoded with BCrypt.
- Database credentials are read from environment variables.
- Telegram credentials are read from environment variables.
- Booking slots are locked during the booking transaction.
- Production deployments should use HTTPS and a managed secret store.

## Roadmap

The next development stages are planned around real backend engineering practices:

- DTOs and request/response models
- Bean Validation
- Domain-specific exceptions
- Centralized error handling
- Cleaner service boundaries
- Booking status lifecycle
- Customer accounts and booking history
- Specialist management
- Working schedules and automatic availability calculation
- REST API
- OpenAPI / Swagger
- Unit and integration tests
- Docker / Docker Compose
- CI/CD with GitHub Actions
- Database migrations with Flyway
- Production deployment

## Author

**Aktilek Korganbek**

Java Backend Developer

GitHub: https://github.com/devtilek
