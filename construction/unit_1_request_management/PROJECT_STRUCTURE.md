# Request Management Service - Project Structure

## Directory Layout

```
construction/unit_1_request_management/
├── pom.xml                                    # Maven configuration
├── PROJECT_STRUCTURE.md                       # This file
├── PHASE_1_COMPLETION_SUMMARY.md             # Phase 1 completion details
│
├── src/
│   ├── main/
│   │   ├── java/com/accessrequest/
│   │   │   ├── RequestManagementServiceApplication.java  # Main Spring Boot app
│   │   │   │
│   │   │   ├── api/                          # REST Controllers & DTOs (Phase 5)
│   │   │   │   ├── controller/
│   │   │   │   │   ├── RequestController.java
│   │   │   │   │   ├── AccessTypeController.java
│   │   │   │   │   └── SearchController.java
│   │   │   │   ├── dto/
│   │   │   │   │   ├── CreateRequestRequest.java
│   │   │   │   │   ├── RequestDTO.java
│   │   │   │   │   ├── ApprovalDTO.java
│   │   │   │   │   ├── AccessTypeDTO.java
│   │   │   │   │   └── ... (other DTOs)
│   │   │   │   └── exception/
│   │   │   │       ├── GlobalExceptionHandler.java
│   │   │   │       └── ... (custom exceptions)
│   │   │   │
│   │   │   ├── application/                  # Application Services (Phase 4)
│   │   │   │   ├── service/
│   │   │   │   │   ├── RequestApplicationService.java
│   │   │   │   │   ├── RequestApprovalApplicationService.java
│   │   │   │   │   └── AccessTypeApplicationService.java
│   │   │   │   ├── command/
│   │   │   │   │   ├── CreateRequestCommand.java
│   │   │   │   │   ├── SubmitRequestCommand.java
│   │   │   │   │   ├── ApproveRequestCommand.java
│   │   │   │   │   └── ... (other commands)
│   │   │   │   └── query/
│   │   │   │       ├── GetRequestQuery.java
│   │   │   │       ├── ListRequestsQuery.java
│   │   │   │       └── ... (other queries)
│   │   │   │
│   │   │   ├── domain/                       # Domain Layer (Phase 2)
│   │   │   │   ├── aggregate/
│   │   │   │   │   ├── Request.java          # Request Aggregate Root
│   │   │   │   │   ├── AccessType.java       # AccessType Aggregate Root
│   │   │   │   │   └── ... (other aggregates)
│   │   │   │   ├── entity/
│   │   │   │   │   ├── RequestApproval.java
│   │   │   │   │   ├── RequestHistory.java
│   │   │   │   │   ├── RequestDocument.java
│   │   │   │   │   └── AccessTypeRouting.java
│   │   │   │   ├── valueobject/
│   │   │   │   │   ├── RequestId.java
│   │   │   │   │   ├── RequestStatus.java
│   │   │   │   │   ├── RequestJustification.java
│   │   │   │   │   ├── ApprovalComment.java
│   │   │   │   │   ├── AccessTypeId.java
│   │   │   │   │   ├── AccessTypeName.java
│   │   │   │   │   └── ... (other value objects)
│   │   │   │   ├── service/
│   │   │   │   │   ├── RequestWorkflowService.java
│   │   │   │   │   ├── RequestRoutingService.java
│   │   │   │   │   └── AccessTypeRoutingService.java
│   │   │   │   ├── policy/
│   │   │   │   │   ├── RequestApprovalPolicy.java
│   │   │   │   │   ├── RequestDeclinePolicy.java
│   │   │   │   │   ├── RequestRoutingPolicy.java
│   │   │   │   │   └── AccessTypeRoutingPolicy.java
│   │   │   │   ├── factory/
│   │   │   │   │   ├── RequestFactory.java
│   │   │   │   │   ├── AccessTypeFactory.java
│   │   │   │   │   └── RequestHistoryFactory.java
│   │   │   │   ├── event/
│   │   │   │   │   ├── DomainEvent.java      # Base event class
│   │   │   │   │   ├── RequestCreated.java
│   │   │   │   │   ├── RequestSubmitted.java
│   │   │   │   │   ├── RequestApprovedByHeadOfOffice.java
│   │   │   │   │   ├── RequestEndorsed.java
│   │   │   │   │   ├── RequestFinallyApproved.java
│   │   │   │   │   ├── RequestDeclined.java
│   │   │   │   │   ├── RequestReturned.java
│   │   │   │   │   ├── RequestImplemented.java
│   │   │   │   │   ├── AccessTypeAdded.java
│   │   │   │   │   └── AccessTypeRoutingConfigured.java
│   │   │   │   ├── specification/
│   │   │   │   │   ├── RequestsByStatusSpecification.java
│   │   │   │   │   ├── RequestsByRequestorSpecification.java
│   │   │   │   │   ├── PendingRequestsForApproverSpecification.java
│   │   │   │   │   ├── RequestsByDateRangeSpecification.java
│   │   │   │   │   ├── RequestsByAccessTypeSpecification.java
│   │   │   │   │   └── OverdueRequestsSpecification.java
│   │   │   │   └── repository/
│   │   │   │       ├── RequestRepository.java
│   │   │   │       └── AccessTypeRepository.java
│   │   │   │
│   │   │   ├── infrastructure/               # Infrastructure Layer (Phase 3)
│   │   │   │   ├── persistence/
│   │   │   │   │   ├── jpa/
│   │   │   │   │   │   ├── RequestJpaEntity.java
│   │   │   │   │   │   ├── RequestApprovalJpaEntity.java
│   │   │   │   │   │   ├── RequestHistoryJpaEntity.java
│   │   │   │   │   │   ├── RequestDocumentJpaEntity.java
│   │   │   │   │   │   ├── AccessTypeJpaEntity.java
│   │   │   │   │   │   └── AccessTypeRoutingJpaEntity.java
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   ├── RequestJpaRepository.java
│   │   │   │   │   │   ├── RequestRepositoryImpl.java
│   │   │   │   │   │   ├── AccessTypeJpaRepository.java
│   │   │   │   │   │   └── AccessTypeRepositoryImpl.java
│   │   │   │   │   └── mapper/
│   │   │   │   │       ├── RequestMapper.java
│   │   │   │   │       └── AccessTypeMapper.java
│   │   │   │   ├── event/
│   │   │   │   │   ├── DomainEventPublisher.java
│   │   │   │   │   ├── KafkaEventPublisher.java
│   │   │   │   │   ├── DomainEventListener.java
│   │   │   │   │   ├── DocumentUploadedEventHandler.java
│   │   │   │   │   └── DocumentDeletedEventHandler.java
│   │   │   │   ├── client/
│   │   │   │   │   ├── AdministrationServiceClient.java
│   │   │   │   │   ├── DocumentManagementServiceClient.java
│   │   │   │   │   └── NotificationServiceClient.java
│   │   │   │   └── exception/
│   │   │   │       ├── ApplicationException.java
│   │   │   │       ├── ValidationException.java
│   │   │   │       ├── BusinessRuleException.java
│   │   │   │       ├── ResourceNotFoundException.java
│   │   │   │       └── ... (other exceptions)
│   │   │   │
│   │   │   └── config/                       # Spring Configuration (Phase 1)
│   │   │       ├── SecurityConfig.java
│   │   │       ├── KafkaConfig.java
│   │   │       ├── CacheConfig.java
│   │   │       └── OpenApiConfig.java
│   │   │
│   │   └── resources/
│   │       ├── application.properties         # Default configuration
│   │       ├── application-dev.properties    # Development profile
│   │       ├── application-prod.properties   # Production profile
│   │       ├── logback-spring.xml            # Logging configuration
│   │       └── db/
│   │           └── changelog/
│   │               ├── db.changelog-master.xml
│   │               ├── 001-create-access-types-table.xml
│   │               ├── 002-create-access-requests-table.xml
│   │               ├── 003-create-request-approvals-table.xml
│   │               ├── 004-create-request-history-table.xml
│   │               ├── 005-create-request-documents-table.xml
│   │               ├── 006-create-access-type-routing-table.xml
│   │               └── 007-create-indexes.xml
│   │
│   └── test/
│       ├── java/com/accessrequest/
│       │   ├── api/                          # API Layer Tests (Phase 6)
│       │   │   └── controller/
│       │   │       ├── RequestControllerTest.java
│       │   │       ├── AccessTypeControllerTest.java
│       │   │       └── SearchControllerTest.java
│       │   ├── application/                  # Application Layer Tests (Phase 6)
│       │   │   └── service/
│       │   │       ├── RequestApplicationServiceTest.java
│       │   │       ├── RequestApprovalApplicationServiceTest.java
│       │   │       └── AccessTypeApplicationServiceTest.java
│       │   ├── domain/                       # Domain Layer Tests (Phase 6)
│       │   │   ├── aggregate/
│       │   │   │   ├── RequestAggregateTest.java
│       │   │   │   └── AccessTypeAggregateTest.java
│       │   │   ├── valueobject/
│       │   │   │   ├── RequestStatusTest.java
│       │   │   │   └── RequestJustificationTest.java
│       │   │   ├── service/
│       │   │   │   ├── RequestWorkflowServiceTest.java
│       │   │   │   └── RequestRoutingServiceTest.java
│       │   │   ├── policy/
│       │   │   │   ├── RequestApprovalPolicyTest.java
│       │   │   │   └── RequestDeclinePolicyTest.java
│       │   │   └── factory/
│       │   │       ├── RequestFactoryTest.java
│       │   │       └── AccessTypeFactoryTest.java
│       │   └── infrastructure/               # Infrastructure Tests (Phase 6)
│       │       ├── persistence/
│       │       │   ├── RequestRepositoryTest.java
│       │       │   └── AccessTypeRepositoryTest.java
│       │       ├── event/
│       │       │   ├── KafkaEventPublisherTest.java
│       │       │   └── DocumentUploadedEventHandlerTest.java
│       │       └── client/
│       │           ├── AdministrationServiceClientTest.java
│       │           └── DocumentManagementServiceClientTest.java
│       │
│       └── resources/
│           └── application-test.properties   # Test configuration
│
└── docker/                                   # Docker files (Phase 7)
    ├── Dockerfile
    └── docker-compose.yml
```

## Layer Descriptions

### API Layer (Phase 5)
- REST Controllers for handling HTTP requests
- DTOs for request/response serialization
- Global exception handler
- Input validation

### Application Layer (Phase 4)
- Application Services orchestrating domain logic
- Commands for write operations
- Queries for read operations
- Transaction management

### Domain Layer (Phase 2)
- Aggregates (Request, AccessType)
- Entities (RequestApproval, RequestHistory, etc.)
- Value Objects (RequestId, RequestStatus, etc.)
- Domain Services
- Policies
- Factories
- Domain Events
- Specifications

### Infrastructure Layer (Phase 3)
- JPA Entities mapping to database
- Repositories for persistence
- Event Publisher/Listener (Kafka)
- External Service Clients
- Exception handling

### Configuration (Phase 1)
- Spring Security
- Kafka
- Redis Caching
- OpenAPI/Swagger

## Implementation Phases

1. **Phase 1** ✅ - Project Setup & Infrastructure (COMPLETED)
2. **Phase 2** - Domain Layer Implementation
3. **Phase 3** - Infrastructure Layer Implementation
4. **Phase 4** - Application Layer Implementation
5. **Phase 5** - API Layer Implementation
6. **Phase 6** - Testing Implementation
7. **Phase 7** - Demo Application
8. **Phase 8** - Documentation & Finalization

## Key Technologies

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

## Build & Run

### Build
```bash
mvn clean install
```

### Run (Development)
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

### Run (Production)
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
```

### Run Tests
```bash
mvn test
```

### Build Docker Image
```bash
mvn clean package
docker build -t request-management-service:1.0.0 .
```

## API Endpoints (Phase 5)

### Request Management
- `POST /api/v1/requests` - Create request
- `GET /api/v1/requests/{requestId}` - Get request details
- `GET /api/v1/requests` - List requests
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

## Notes

- All files follow Java naming conventions
- Package structure follows domain-driven design principles
- Each layer is independent and testable
- Configuration is externalized and environment-specific
- Database schema is version-controlled via Liquibase
- Events are published to Kafka for asynchronous processing
- Caching is implemented for performance optimization
- Security is enforced via JWT authentication and RBAC
