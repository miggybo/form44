# Developer Guide

## Overview

This guide provides comprehensive information for developers working on the Request Management Service.

---

## Project Structure

```
construction/unit_1_request_management/
├── src/
│   ├── main/
│   │   ├── java/com/accessrequest/
│   │   │   ├── api/                    # REST Controllers & Exception Handling
│   │   │   │   ├── controller/         # REST Controllers
│   │   │   │   ├── exception/          # Exception Handlers & DTOs
│   │   │   │   └── interceptor/        # HTTP Interceptors
│   │   │   ├── application/            # Application Services & DTOs
│   │   │   │   ├── dto/                # Data Transfer Objects
│   │   │   │   └── service/            # Application Services
│   │   │   ├── domain/                 # Domain Model (DDD)
│   │   │   │   ├── aggregate/          # Aggregate Roots
│   │   │   │   ├── entity/             # Domain Entities
│   │   │   │   ├── event/              # Domain Events
│   │   │   │   ├── factory/            # Domain Factories
│   │   │   │   ├── policy/             # Domain Policies
│   │   │   │   ├── repository/         # Repository Interfaces
│   │   │   │   ├── service/            # Domain Services
│   │   │   │   ├── specification/      # Query Specifications
│   │   │   │   └── valueobject/        # Value Objects
│   │   │   ├── infrastructure/         # Infrastructure Layer
│   │   │   │   ├── client/             # External Service Clients
│   │   │   │   ├── event/              # Event Publishing/Listening
│   │   │   │   ├── exception/          # Custom Exceptions
│   │   │   │   └── persistence/        # JPA Entities & Repositories
│   │   │   ├── config/                 # Spring Configuration
│   │   │   └── demo/                   # Demo Data Initialization
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       ├── application-prod.properties
│   │       ├── logback-spring.xml
│   │       └── db/changelog/           # Liquibase Migrations
│   └── test/
│       ├── java/com/accessrequest/
│       │   ├── api/                    # API Tests
│       │   ├── application/            # Application Service Tests
│       │   └── domain/                 # Domain Tests
│       └── resources/
│           └── application-test.properties
├── pom.xml
├── docker-compose.yml
├── prometheus.yml
└── README.md
```

---

## Architecture

### Layered Architecture

```
┌─────────────────────────────────────────┐
│ Presentation Layer (REST API)           │
│ - Controllers                           │
│ - Exception Handlers                    │
│ - Interceptors                          │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│ Application Layer                       │
│ - Application Services                  │
│ - DTOs                                  │
│ - Transaction Management                │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│ Domain Layer (DDD)                      │
│ - Aggregates                            │
│ - Entities                              │
│ - Value Objects                         │
│ - Domain Services                       │
│ - Domain Events                         │
│ - Policies                              │
│ - Specifications                        │
│ - Factories                             │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│ Infrastructure Layer                    │
│ - JPA Repositories                      │
│ - Event Bus (Kafka)                     │
│ - External Clients                      │
│ - Database Access                       │
│ - Security                              │
└─────────────────────────────────────────┘
```

---

## Key Concepts

### Domain-Driven Design (DDD)

The project follows DDD principles:

1. **Aggregates**: Request and AccessType are aggregate roots
2. **Entities**: RequestApproval, RequestHistory, RequestDocument
3. **Value Objects**: RequestId, RequestStatus, RequestJustification
4. **Domain Services**: RequestWorkflowService, RequestRoutingService
5. **Domain Events**: RequestCreated, RequestSubmitted, RequestApproved, etc.
6. **Repositories**: RequestRepository, AccessTypeRepository
7. **Specifications**: Query objects for complex queries

### Event-Driven Architecture

- Domain events are published when aggregates change state
- Events are persisted to Kafka for asynchronous processing
- External services consume events and react accordingly
- Ensures loose coupling between services

### Request Workflow

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
Implement by Admin → Implemented (Terminal)

Alternative paths:
- Decline at any stage → Declined (Terminal)
- Return from PendingFinalApproval → ReturnedToReviewer
```

---

## Adding New Features

### 1. Add a New Request Endpoint

**Step 1: Create a DTO**
```java
@Data
@Builder
public class MyRequestDTO {
    private UUID id;
    private String name;
    // ... other fields
}
```

**Step 2: Add Controller Method**
```java
@PostMapping("/my-endpoint")
@PreAuthorize("hasRole('EMPLOYEE')")
public ResponseEntity<MyRequestDTO> myEndpoint(@Valid @RequestBody MyRequest request) {
    // Implementation
}
```

**Step 3: Add Application Service Method**
```java
@Transactional
public MyRequestDTO myOperation(MyRequest request) {
    // Implementation
}
```

**Step 4: Add Domain Logic**
```java
public class Request {
    public void myDomainOperation() {
        // Validate invariants
        // Update state
        // Publish events
    }
}
```

### 2. Add a New Domain Event

**Step 1: Create Event Class**
```java
@Data
@Builder
public class MyEvent extends DomainEvent {
    private RequestId requestId;
    private String details;
}
```

**Step 2: Publish Event from Aggregate**
```java
public class Request {
    public void myOperation() {
        // ... business logic
        this.domainEvents.add(new MyEvent(...));
    }
}
```

**Step 3: Handle Event**
```java
@Component
public class MyEventHandler implements DomainEventListener {
    @Override
    public void handle(DomainEvent event) {
        if (event instanceof MyEvent) {
            // Handle event
        }
    }
}
```

### 3. Add a New Query/Search

**Step 1: Create Specification**
```java
public class MySpecification implements Specification<Request> {
    @Override
    public Predicate toPredicate(Root<Request> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        // Build predicate
    }
}
```

**Step 2: Add Repository Method**
```java
public interface RequestRepository {
    Page<Request> findAll(Specification<Request> spec, Pageable pageable);
}
```

**Step 3: Add Application Service Method**
```java
public Page<RequestDTO> mySearch(SearchCriteria criteria, Pageable pageable) {
    Specification<Request> spec = new MySpecification(criteria);
    return requestRepository.findAll(spec, pageable);
}
```

**Step 4: Add Controller Endpoint**
```java
@GetMapping("/my-search")
public ResponseEntity<Page<RequestDTO>> mySearch(@RequestParam String criteria) {
    Page<RequestDTO> results = requestApplicationService.mySearch(criteria, pageable);
    return ResponseEntity.ok(results);
}
```

---

## Testing

### Unit Tests

Test domain logic in isolation:
```java
@Test
void testDomainBehavior() {
    // Arrange
    Request request = createTestRequest();
    
    // Act
    request.submit(headOfOfficeId);
    
    // Assert
    assertEquals(RequestStatus.PENDING_INITIAL_APPROVAL, request.getStatus());
}
```

### Integration Tests

Test API endpoints with MockMvc:
```java
@SpringBootTest
@AutoConfigureMockMvc
class RequestControllerTest {
    @Test
    void testCreateRequest() throws Exception {
        mockMvc.perform(post("/api/v1/requests")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }
}
```

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=RequestControllerTest

# Run with coverage
mvn test jacoco:report
```

---

## Database Migrations

### Adding a New Table

**Step 1: Create Liquibase Changelog**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                   http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.1.xsd">

    <changeSet id="001-create-my-table" author="developer">
        <createTable tableName="my_table">
            <column name="id" type="UUID" defaultValueComputed="gen_random_uuid()">
                <constraints primaryKey="true"/>
            </column>
            <column name="name" type="VARCHAR(255)">
                <constraints nullable="false"/>
            </column>
        </createTable>
    </changeSet>
</databaseChangeLog>
```

**Step 2: Add to Master Changelog**
```xml
<include file="db/changelog/008-create-my-table.xml"/>
```

**Step 3: Run Migration**
```bash
mvn liquibase:update
```

---

## Configuration

### Application Properties

**Development** (`application-dev.properties`):
```properties
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
logging.level.root=INFO
```

**Production** (`application-prod.properties`):
```properties
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
logging.level.root=WARN
```

### Environment Variables

```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=request_management
DB_USER=postgres
DB_PASSWORD=bir*1234

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# JWT
JWT_SECRET=your-secret-key
JWT_EXPIRATION=1800000
```

---

## Logging

### Log Levels

- **DEBUG**: Detailed information for debugging
- **INFO**: General informational messages
- **WARN**: Warning messages for potential issues
- **ERROR**: Error messages for failures

### Viewing Logs

```bash
# Application logs
tail -f logs/application.log

# Docker logs
docker-compose logs -f postgres
docker-compose logs -f kafka
```

---

## Performance Optimization

### Database Indexing

Indexes are created on frequently queried fields:
- `request_id`
- `requestor_id`
- `status`
- `created_at`

### Caching

Redis caching is configured for:
- Access types
- Request details
- User information

### Query Optimization

- Use specifications for complex queries
- Implement pagination for large result sets
- Use database indexes effectively

---

## Security

### Authentication

- JWT tokens are used for authentication
- Tokens expire after 30 minutes
- Bearer token scheme in Authorization header

### Authorization

- Role-based access control (RBAC)
- Supported roles: EMPLOYEE, HEAD_OF_OFFICE, REVIEWER, HEAD, ADMIN
- Method-level authorization with `@PreAuthorize`

### Input Validation

- All request bodies are validated with `@Valid`
- Custom validators for domain constraints
- Field-level validation with detailed error messages

---

## Deployment

### Build Docker Image

```bash
mvn clean package
docker build -t request-management-service:1.0.0 .
```

### Run in Docker

```bash
docker run -p 8080:8080 \
  -e DB_HOST=postgres \
  -e KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
  request-management-service:1.0.0
```

### Kubernetes Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: request-management-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: request-management-service
  template:
    metadata:
      labels:
        app: request-management-service
    spec:
      containers:
      - name: request-management-service
        image: request-management-service:1.0.0
        ports:
        - containerPort: 8080
        livenessProbe:
          httpGet:
            path: /api/v1/health/live
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /api/v1/health/ready
            port: 8080
          initialDelaySeconds: 20
          periodSeconds: 5
```

---

## Troubleshooting

### Common Issues

**Issue**: Database connection error
**Solution**: Verify PostgreSQL is running and credentials are correct

**Issue**: Kafka connection error
**Solution**: Verify Kafka and Zookeeper are running

**Issue**: JWT token expired
**Solution**: Generate a new token

**Issue**: Permission denied error
**Solution**: Verify user has required role

---

## Best Practices

1. **Follow DDD principles**: Keep domain logic in aggregates
2. **Use transactions**: Mark service methods with `@Transactional`
3. **Publish events**: Emit domain events for state changes
4. **Validate input**: Use `@Valid` and custom validators
5. **Log appropriately**: Use appropriate log levels
6. **Write tests**: Aim for 80%+ code coverage
7. **Document code**: Add Javadoc comments
8. **Handle exceptions**: Use custom exceptions and global handlers

---

## Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Domain-Driven Design](https://martinfowler.com/bliki/DomainDrivenDesign.html)
- [Event-Driven Architecture](https://martinfowler.com/articles/201701-event-driven.html)
- [Kafka Documentation](https://kafka.apache.org/documentation/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
