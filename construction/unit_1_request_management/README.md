# Request Management Service - Access Request Processing System (Unit 1)

## Overview

The Request Management Service is the core domain of the Access Request Processing System. It manages the complete lifecycle of access requests from creation through implementation, orchestrating a complex multi-level approval workflow involving Head of Office, SMD/RDC Reviewer, SMD/RDC Head, and System/Database Administrators.

## Key Features

- **Request Lifecycle Management**: Create, submit, approve, decline, and implement access requests
- **Multi-Level Approval Workflow**: Orchestrate complex approval chains with multiple stakeholders
- **Event-Driven Architecture**: Asynchronous communication via Kafka message queue
- **Domain-Driven Design**: Rich domain model with aggregates, entities, and value objects
- **RESTful API**: Comprehensive REST API for request operations
- **JWT Authentication**: Secure authentication with JWT tokens
- **Role-Based Access Control**: Fine-grained authorization based on user roles
- **Caching**: Redis-based caching for improved performance
- **Monitoring**: Prometheus metrics and Grafana dashboards
- **API Documentation**: Swagger/OpenAPI 3.0 documentation

## Technology Stack

- **Language**: Java 17+
- **Framework**: Spring Boot 3.2.0
- **Database**: PostgreSQL 17
- **Message Queue**: Kafka
- **Caching**: Redis
- **ORM**: Hibernate/JPA
- **Migrations**: Liquibase
- **Security**: JWT, Spring Security
- **API Documentation**: Swagger/OpenAPI 3.0
- **Monitoring**: Micrometer/Prometheus
- **Testing**: JUnit 5, Mockito, TestContainers
- **Build Tool**: Maven

## Project Structure

```
src/
├── main/
│   ├── java/com/accessrequest/
│   │   ├── api/              # REST Controllers & DTOs
│   │   ├── application/      # Application Services
│   │   ├── domain/           # Domain Model (Aggregates, Entities, Value Objects)
│   │   ├── infrastructure/   # Repositories, Event Bus, External Clients
│   │   └── config/           # Spring Configuration
│   └── resources/
│       ├── application.properties
│       ├── logback-spring.xml
│       └── db/changelog/     # Database migrations
└── test/
    ├── java/com/accessrequest/
    └── resources/
```

See `PROJECT_STRUCTURE.md` for detailed structure.

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.8.0+
- PostgreSQL 17
- Kafka
- Redis
- Docker & Docker Compose (optional)

### Setup & Run

1. **Start Infrastructure**:
   ```bash
   docker-compose up -d
   ```

2. **Build Project**:
   ```bash
   mvn clean install
   ```

3. **Run Application**:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
   ```

4. **Access Application**:
   - API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - Health: http://localhost:8080/actuator/health
   - Metrics: http://localhost:8080/actuator/prometheus

See `QUICK_START.md` for detailed setup instructions.

## API Endpoints

### Request Management
- `POST /api/v1/requests` - Create request
- `GET /api/v1/requests/{requestId}` - Get request details
- `GET /api/v1/requests` - List requests with filters
- `PUT /api/v1/requests/{requestId}/submit` - Submit request
- `PUT /api/v1/requests/{requestId}/approve` - Approve request
- `PUT /api/v1/requests/{requestId}/decline` - Decline request
- `PUT /api/v1/requests/{requestId}/endorse` - Endorse request
- `PUT /api/v1/requests/{requestId}/return` - Return request
- `PUT /api/v1/requests/{requestId}/implement` - Implement request
- `GET /api/v1/requests/{requestId}/history` - Get request history

### Access Type Management
- `GET /api/v1/access-types` - List access types
- `POST /api/v1/access-types` - Create access type
- `PUT /api/v1/access-types/{accessTypeId}/routing` - Update routing

### Search
- `GET /api/v1/requests/search` - Search requests

### Health & Monitoring
- `GET /actuator/health` - Health check
- `GET /actuator/prometheus` - Prometheus metrics
- `GET /swagger-ui.html` - Swagger UI
- `GET /api-docs` - OpenAPI JSON

## Request Lifecycle

```
Draft
  ↓
Submit → PendingInitialApproval
  ↓
Approve by Head of Office → PendingReview
  ↓
Endorse by Reviewer → PendingFinalApproval
  ↓
Approve by Head → Approved
  ↓
Implement by Administrator → Implemented

Alternative paths:
- Decline at any stage → Declined
- Return from Head → ReturnedToReviewer
```

## Configuration

### Development
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

### Production
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
```

### Environment Variables (Production)
```bash
DATABASE_URL=jdbc:postgresql://db-host:5432/access_request_db
DATABASE_USER=postgres
DATABASE_PASSWORD=your-password
KAFKA_BOOTSTRAP_SERVERS=kafka-host:9092
REDIS_HOST=redis-host
JWT_SECRET=your-secret-key
JWT_EXPIRATION=1800000
```

See `application.properties` and `application-dev.properties` for all configuration options.

## Database Schema

The service uses PostgreSQL with the following tables:
- `access_types` - Access type definitions
- `access_requests` - Request records
- `request_approvals` - Approval tracking
- `request_history` - Audit trail
- `request_documents` - Document references
- `access_type_routing` - Routing configuration

Database migrations are managed by Liquibase and run automatically on startup.

## Event-Driven Architecture

The service publishes domain events to Kafka for asynchronous processing:

**Published Events**:
- RequestCreated
- RequestSubmitted
- RequestApprovedByHeadOfOffice
- RequestEndorsed
- RequestFinallyApproved
- RequestDeclined
- RequestReturned
- RequestImplemented
- AccessTypeAdded
- AccessTypeRoutingConfigured

**Consumed Events**:
- DocumentUploaded (from Document Management Service)
- DocumentDeleted (from Document Management Service)

## Security

- **Authentication**: JWT tokens
- **Authorization**: Role-Based Access Control (RBAC)
- **Encryption**: HTTPS/TLS for transport, AES-256 for sensitive data
- **Audit Logging**: All user actions logged

## Monitoring & Observability

- **Metrics**: Prometheus metrics exposed at `/actuator/prometheus`
- **Health Checks**: Liveness and readiness probes
- **Logging**: Structured logging with SLF4J/Logback
- **Tracing**: Request tracing with correlation IDs

## Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn test -Dtest=*IntegrationTest
```

### Test Coverage
```bash
mvn test jacoco:report
```

Target coverage: 80%+

## Development

### Build
```bash
mvn clean install
```

### Run Tests
```bash
mvn test
```

### Format Code
```bash
mvn spotless:apply
```

### Generate Javadoc
```bash
mvn javadoc:javadoc
```

## Docker

### Build Image
```bash
mvn clean package
docker build -t request-management-service:1.0.0 .
```

### Run Container
```bash
docker run -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://postgres:5432/access_request_db \
  -e DATABASE_USER=postgres \
  -e DATABASE_PASSWORD=bir*1234 \
  -e KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
  -e REDIS_HOST=redis \
  request-management-service:1.0.0
```

### Docker Compose
```bash
docker-compose up -d
```

## Implementation Phases

1. **Phase 1** ✅ - Project Setup & Infrastructure (COMPLETED)
   - Maven configuration
   - Spring Boot setup
   - Database schema
   - Kafka configuration
   - Security configuration
   - Caching setup
   - API documentation

2. **Phase 2** - Domain Layer Implementation
   - Value Objects
   - Domain Entities
   - Aggregates
   - Domain Services
   - Policies & Factories
   - Domain Events
   - Specifications

3. **Phase 3** - Infrastructure Layer Implementation
   - JPA Entities
   - Repositories
   - Event Bus
   - External Service Clients

4. **Phase 4** - Application Layer Implementation
   - DTOs
   - Application Services
   - Command Handlers

5. **Phase 5** - API Layer Implementation
   - REST Controllers
   - Input Validation
   - Error Handling

6. **Phase 6** - Testing Implementation
   - Unit Tests
   - Integration Tests
   - E2E Tests

7. **Phase 7** - Demo Application
   - Docker Compose
   - Sample Data
   - Postman Collection

8. **Phase 8** - Documentation & Finalization
   - API Documentation
   - Developer Guide
   - Deployment Guide

## Documentation

- **Quick Start**: See `QUICK_START.md`
- **Project Structure**: See `PROJECT_STRUCTURE.md`
- **Phase 1 Summary**: See `PHASE_1_COMPLETION_SUMMARY.md`
- **Logical Design**: See `../logical_design.md`
- **Implementation Plan**: See `../../plan.md`

## Troubleshooting

### Database Connection Error
Ensure PostgreSQL is running on localhost:5432 with credentials:
- Username: postgres
- Password: bir*1234

### Kafka Connection Error
Ensure Kafka is running on localhost:9092

### Redis Connection Error
Ensure Redis is running on localhost:6379

See `QUICK_START.md` for more troubleshooting tips.

## Support

For issues or questions:
1. Check the troubleshooting section
2. Review the logical design document
3. Check application logs
4. Review test cases for usage examples

## License

Apache License 2.0

## Contact

Development Team: dev@example.com

---

**Status**: Phase 1 Complete ✅ - Ready for Phase 2 Implementation
