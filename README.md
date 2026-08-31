# Simpatico CRM - Phase 1: Architecture & Project Foundation

Simpatico CRM is a Spring Boot application designed to support a wholesale liquidation lead-generation business (Simpatico Liquidations).

## Technology Stack
- **Java**: 17
- **Framework**: Spring Boot 3.3.3
- **Database**: PostgreSQL (Production/Dev), H2 In-Memory (Testing)
- **Build System**: Maven

## Architecture & Package Structure
The application follows a clean layered architecture:
`controller → service → repository → database`

Packages under `com.simpatico.crm`:
- `config`: Configuration classes
- `controller`: REST Controllers
- `dto`: Data Transfer Objects (DTOs) for request/response payloads
- `entity`: JPA Entities representing tables
- `exception`: Global and custom exception handling classes
- `mapper`: Converters between entities and DTOs
- `repository`: Data access interfaces
- `service`: Business logic interfaces and implementations

## Prerequisites
- Java 17 SDK installed
- Maven 3.x installed
- A running PostgreSQL database (for dev/production execution)

## Database Configuration
The application separates settings based on active profiles:
- **Default/Production**: Requires environment variables for PostgreSQL connection details:
  - `SPRING_DATASOURCE_URL` (e.g. `jdbc:postgresql://host:port/database_name`)
  - `SPRING_DATASOURCE_USERNAME`
  - `SPRING_DATASOURCE_PASSWORD`
- **Development** (`dev` profile): Uses local PostgreSQL settings with fallback defaults:
  - Default URL: `jdbc:postgresql://localhost:5432/simpatico_crm`
  - Default Username: `postgres`
  - Default Password: `postgres`
  - Table auto-generation: Set to `update`.
- **Testing** (`test` profile): Automatically uses an in-memory H2 database. There is no need for a running PostgreSQL database during test execution.

## Getting Started

### 1. Build the Application
To compile and package the application, run:
```bash
mvn clean package
```

### 2. Run Automated Tests
To run all tests (which run using the in-memory H2 database under the `test` profile):
```bash
mvn clean test
```

### 3. Run the Application Locally
To run the application with the development profile, ensure you have set up your environment variables if your local PostgreSQL details differ from the defaults:

```bash
# Optional: customize database URL and credentials if different from localhost defaults
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/simpatico_crm
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=your_password

mvn spring-boot:run
```

Once running, the server is available on port `8080`.

### 4. Health Check Endpoint
To verify the application is running, call the health endpoint:
```bash
curl -i http://localhost:8080/api/health
```

Expected Response:
```json
{
  "status": "UP",
  "message": "Simpatico CRM API is running"
}
```
