# Infrastructure Layer Guide

## Overview

The infrastructure layer provides technical implementation for the domain model, including database persistence, event handling, and external service integration.

## Directory Structure

```
src/main/java/com/accessrequest/infrastructure/
├── persistence/
│   ├── jpa/
│   │   ├── RequestJpaEntity.java
│   │   ├── RequestApprovalJpaEntity.java
│   │   ├── RequestHistoryJpaEntity.java
│   │   ├── RequestDocumentJpaEntity.java
│   │   ├── AccessTypeJpaEntity.java
│   │   └── AccessTypeRoutingJpaEntity.java
│   ├── repository/
│   │   ├── RequestJpaRepository.java
│   │   ├── RequestRepositoryImpl.java
│   │   ├── AccessTypeJpaRepository.java
│   │   └── AccessTypeRepositoryImpl.java
│   └── mapper/
│       ├── RequestMapper.java
│       └── AccessTypeMapper.java
├── event/
│   ├── DomainEventPublisher.java
│   ├── KafkaEventPublisher.java
│   ├── DomainEventListener.java
│   └── KafkaEventListener.java
├── client/
│   ├── AdministrationServiceClient.java
│   ├── DocumentManagementServiceClient.java
│   └── NotificationServiceClient.java
├── exception/
│   ├── ApplicationException.java
│   ├── ResourceNotFoundException.java
│   ├── BusinessRuleException.java
│   ├── ValidationException.java
│   ├── ConflictException.java
│   └── GlobalExceptionHandler.java
└── config/
    └── RestTemplateConfig.java
```

## Persistence Layer

### JPA Entities

JPA entities map domain aggregates to database tables. They use Lombok for boilerplate reduction and include comprehensive indexing.

**Key Features**:
- Automatic timestamp management (@CreationTimestamp, @UpdateTimestamp)
- Lazy loading for relationships
- Cascade operations for aggregates
- Comprehensive indexing for performance
- Enum support for status fields

**Example Usage**:
```java
// Saving a request
RequestJpaEntity entity = mapper.toDomain(request);
jpaRepository.save(entity);

// Querying requests
Page<RequestJpaEntity> page = jpaRepository.findByStatus(status, pageable);
```

### Spring Data JPA Repositories

Spring Data JPA repositories provide automatic CRUD operations and custom query methods.

**RequestJpaRepository Methods**:
- `findByRequestorId()` - Find requests by requestor
- `findByStatus()` - Find requests by status
- `findByAccessTypeId()` - Find requests by access type
- `findByDateRange()` - Find requests in date range
- `findPendingForApprover()` - Find pending requests for approver
- `searchBySystemName()` - Search by system name
- `findOverdueRequests()` - Find overdue requests
- `countByStatus()` - Count requests by status

**AccessTypeJpaRepository Methods**:
- `findByName()` - Find by name
- `findByRoutingRole()` - Find by routing role
- `findDefaultByRoutingRole()` - Find default for role
- `existsByName()` - Check if exists
- `existsByNameExcludingId()` - Check if exists (excluding ID)

### Domain Repository Implementations

Domain repository implementations bridge the domain and infrastructure layers, converting between domain aggregates and JPA entities.

**Key Responsibilities**:
- Convert domain aggregates to/from JPA entities
- Implement domain repository interfaces
- Provide logging for audit trail
- Handle pagination and specifications

**Example Usage**:
```java
// Saving a domain aggregate
requestRepository.save(request);

// Finding a domain aggregate
Optional<Request> request = requestRepository.findById(requestId);

// Querying with pagination
Page<Request> page = requestRepository.findAll(pageable);
```

### Mappers

Mappers provide bidirectional conversion between domain aggregates and JPA entities.

**RequestMapper**:
- Converts Request aggregate to/from RequestJpaEntity
- Maps RequestStatus enum values
- Handles all request fields and relationships

**AccessTypeMapper**:
- Converts AccessType aggregate to/from AccessTypeJpaEntity
- Maps value objects (AccessTypeId, AccessTypeName, AccessTypeDescription)

**Example Usage**:
```java
// Domain to JPA
RequestJpaEntity entity = mapper.toDomain(request);

// JPA to Domain
Request request = mapper.toDomain(entity);
```

## Event Layer

### Event Publisher

The event publisher publishes domain events to Kafka for asynchronous processing.

**DomainEventPublisher Interface**:
```java
void publish(DomainEvent event);
void publishAll(List<DomainEvent> events);
```

**KafkaEventPublisher Implementation**:
- Publishes to `request.events` topic
- JSON serialization using ObjectMapper
- Message headers for event type routing
- Exception handling with EventPublishingException

**Example Usage**:
```java
// Publish single event
eventPublisher.publish(new RequestCreated(...));

// Publish multiple events
eventPublisher.publishAll(request.getDomainEvents());
```

### Event Listener

The event listener consumes domain events from Kafka and routes them to appropriate handlers.

**DomainEventListener Interface**:
```java
void handle(DomainEvent event);
String getEventType();
```

**KafkaEventListener Implementation**:
- Listens to `request.events` topic
- Routes events based on type
- Handles all 10 domain event types
- Error handling and logging

**Supported Events**:
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

## External Service Clients

### Administration Service Client

Provides integration with Administration Service for user and role information.

**Methods**:
- `getHeadOfOfficeForRequestor(requestorId)` - Get Head of Office
- `getReviewerForAccessType(accessType)` - Get Reviewer
- `getSmdHeadId()` - Get SMD/RDC Head
- `getAdministratorForAccessType(accessType)` - Get Administrator
- `getUserById(userId)` - Get user details
- `hasRole(userId, role)` - Check user role

**Configuration**:
```properties
external.services.administration.url=http://localhost:8081
```

### Document Management Service Client

Provides integration with Document Management Service for document information.

**Methods**:
- `getDocumentMetadata(documentId)` - Get document details
- `listDocumentsForRequest(requestId)` - List request documents
- `validateDocumentExists(documentId)` - Validate document

**Configuration**:
```properties
external.services.document-management.url=http://localhost:8082
```

### Notification Service Client

Provides integration with Notification Service for sending notifications.

**Methods**:
- `sendNotification(notification)` - Send notification
- `sendEmailNotification(email, subject, body)` - Send email
- `sendSmsNotification(phone, message)` - Send SMS

**Configuration**:
```properties
external.services.notification.url=http://localhost:8083
```

## Exception Handling

### Exception Hierarchy

```
ApplicationException (base)
├── ResourceNotFoundException (404)
├── BusinessRuleException (400)
├── ValidationException (400)
└── ConflictException (409)
```

### Global Exception Handler

The global exception handler (@ControllerAdvice) converts exceptions to standardized HTTP responses.

**Handled Exceptions**:
- ApplicationException (all subtypes)
- MethodArgumentNotValidException
- Generic Exception

**Error Response Format**:
```json
{
  "code": "ERROR_CODE",
  "message": "Human-readable message",
  "details": {
    "field": "error details"
  },
  "timestamp": "2025-01-08T10:30:00Z",
  "path": "/api/v1/requests"
}
```

### Exception Usage

**ResourceNotFoundException**:
```java
if (request == null) {
    throw new ResourceNotFoundException("Request", requestId.toString());
}
```

**BusinessRuleException**:
```java
if (!policy.canApprove(request, approverId)) {
    throw new BusinessRuleException("Request cannot be approved in current state");
}
```

**ValidationException**:
```java
ValidationException ex = new ValidationException("Validation failed");
ex.addFieldError("justification", "Justification is required");
throw ex;
```

**ConflictException**:
```java
if (accessTypeRepository.existsByName(name)) {
    throw new ConflictException("Access type with name already exists");
}
```

## Configuration

### RestTemplate Configuration

RestTemplate is configured with timeouts for external service calls.

**Configuration**:
- Connect Timeout: 5 seconds
- Read Timeout: 10 seconds

**Usage**:
```java
@Autowired
private RestTemplate restTemplate;

// Used by external service clients
```

## Integration Points

### With Domain Layer
- Domain repositories interface with repository implementations
- Mappers convert between domain and persistence layers
- Event publisher publishes domain events

### With Application Layer
- Application services use domain repositories
- Application services use event publisher
- Application services use external service clients
- Application services handle exceptions

### With Database
- JPA entities map to database tables
- Spring Data JPA repositories execute queries
- Liquibase migrations create schema

### With Kafka
- Event publisher sends to Kafka topic
- Event listener consumes from Kafka topic
- Consumer group: request-management-service

## Best Practices

1. **Always use domain repositories**, not JPA repositories directly
2. **Use mappers** for converting between layers
3. **Handle exceptions** appropriately with structured exception types
4. **Log important operations** for audit trail
5. **Use pagination** for large result sets
6. **Configure timeouts** for external service calls
7. **Implement idempotency** for event handlers
8. **Use lazy loading** for relationships to improve performance

## Testing

### Unit Tests
- Mapper tests for domain/persistence conversion
- Exception tests for error handling

### Integration Tests
- Repository tests with TestContainers PostgreSQL
- Event publisher/listener tests
- External service client tests (with mocking)
- Exception handler tests

### Example Test
```java
@SpringBootTest
@Testcontainers
class RequestRepositoryTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>();
    
    @Test
    void testSaveAndFind() {
        // Arrange
        Request request = RequestFactory.createRequest(...);
        
        // Act
        requestRepository.save(request);
        Optional<Request> found = requestRepository.findById(request.getRequestId());
        
        // Assert
        assertTrue(found.isPresent());
        assertEquals(request.getRequestId(), found.get().getRequestId());
    }
}
```

## Troubleshooting

### Database Connection Issues
- Check PostgreSQL is running on localhost:5432
- Verify credentials in application.properties
- Check HikariCP connection pool settings

### Kafka Issues
- Check Kafka broker is running on localhost:9092
- Verify topic `request.events` exists
- Check consumer group `request-management-service`

### External Service Issues
- Check service URLs in application.properties
- Verify services are running on configured ports
- Check timeout settings (5s connect, 10s read)

### Mapping Issues
- Verify domain and JPA entity fields match
- Check enum mappings are complete
- Ensure null handling is correct

---

**Last Updated**: January 8, 2025
**Phase**: 3 - Infrastructure Layer
**Status**: Complete
