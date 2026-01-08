# Quick Start Guide - Request Management Service

## Prerequisites

- Java 17 or higher
- Maven 3.8.0 or higher
- PostgreSQL 17
- Kafka (with Zookeeper)
- Redis
- Docker & Docker Compose (optional, for containerized setup)

## Local Development Setup

### 1. Start Infrastructure Services

#### Option A: Using Docker Compose (Recommended)

Create a `docker-compose.yml` file in the project root:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:17-alpine
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: bir*1234
      POSTGRES_DB: access_request_db
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    depends_on:
      - zookeeper
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    ports:
      - "9092:9092"

  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
    ports:
      - "2181:2181"

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

volumes:
  postgres_data:
```

Start services:
```bash
docker-compose up -d
```

#### Option B: Manual Installation

1. **PostgreSQL**:
   ```bash
   # Create database
   createdb -U postgres access_request_db
   ```

2. **Kafka**: Follow [Kafka Quick Start](https://kafka.apache.org/quickstart)

3. **Redis**: Follow [Redis Installation](https://redis.io/docs/getting-started/installation/)

### 2. Build the Project

```bash
cd construction/unit_1_request_management
mvn clean install
```

### 3. Run the Application

#### Development Mode
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

#### Production Mode
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
```

The application will start on `http://localhost:8080`

### 4. Verify Installation

#### Health Check
```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{
  "status": "UP"
}
```

#### Swagger UI
Open browser: `http://localhost:8080/swagger-ui.html`

#### Prometheus Metrics
```bash
curl http://localhost:8080/actuator/prometheus
```

## Running Tests

### All Tests
```bash
mvn test
```

### Specific Test Class
```bash
mvn test -Dtest=RequestApplicationServiceTest
```

### With Coverage Report
```bash
mvn test jacoco:report
```

## Project Structure

```
src/
├── main/
│   ├── java/com/accessrequest/
│   │   ├── api/              # REST Controllers & DTOs
│   │   ├── application/      # Application Services
│   │   ├── domain/           # Domain Model
│   │   ├── infrastructure/   # Repositories, Event Bus
│   │   └── config/           # Spring Configuration
│   └── resources/
│       ├── application.properties
│       ├── logback-spring.xml
│       └── db/changelog/     # Database migrations
└── test/
    ├── java/com/accessrequest/
    └── resources/
```

## API Endpoints

### Create Request
```bash
curl -X POST http://localhost:8080/api/v1/requests \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -d '{
    "requestorId": "550e8400-e29b-41d4-a716-446655440000",
    "accessType": "OS",
    "systemName": "Linux Server",
    "justification": "Need access for development work",
    "documents": []
  }'
```

### Get Request
```bash
curl http://localhost:8080/api/v1/requests/{requestId} \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

### List Requests
```bash
curl "http://localhost:8080/api/v1/requests?status=Draft&page=1&size=20" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

### Submit Request
```bash
curl -X PUT http://localhost:8080/api/v1/requests/{requestId}/submit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -d '{
    "headOfOfficeId": "550e8400-e29b-41d4-a716-446655440001"
  }'
```

### Approve Request
```bash
curl -X PUT http://localhost:8080/api/v1/requests/{requestId}/approve \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -d '{
    "approverId": "550e8400-e29b-41d4-a716-446655440001",
    "approvalType": "HeadOfOffice",
    "comments": "Approved"
  }'
```

## Configuration

### Environment Variables (Production)

```bash
# Database
export DATABASE_URL=jdbc:postgresql://db-host:5432/access_request_db
export DATABASE_USER=postgres
export DATABASE_PASSWORD=your-secure-password

# Kafka
export KAFKA_BOOTSTRAP_SERVERS=kafka-host:9092

# Redis
export REDIS_HOST=redis-host
export REDIS_PORT=6379

# JWT
export JWT_SECRET=your-secret-key
export JWT_EXPIRATION=1800000

# External Services
export ADMIN_SERVICE_URL=http://admin-service:8080
export DOC_SERVICE_URL=http://doc-service:8080
export NOTIFICATION_SERVICE_URL=http://notification-service:8080
```

### Application Properties

Edit `src/main/resources/application.properties` for default configuration.

## Troubleshooting

### Database Connection Error
```
Error: Connection refused
```
**Solution**: Ensure PostgreSQL is running on localhost:5432

### Kafka Connection Error
```
Error: Failed to connect to Kafka broker
```
**Solution**: Ensure Kafka is running on localhost:9092

### Redis Connection Error
```
Error: Failed to connect to Redis
```
**Solution**: Ensure Redis is running on localhost:6379

### Port Already in Use
```
Error: Address already in use
```
**Solution**: Change port in `application.properties`:
```properties
server.port=8081
```

### Database Migration Error
```
Error: Liquibase migration failed
```
**Solution**: Check database connection and ensure database exists

## Development Workflow

1. **Create a feature branch**:
   ```bash
   git checkout -b feature/your-feature
   ```

2. **Make changes** in appropriate layer (api, application, domain, infrastructure)

3. **Write tests** for your changes

4. **Run tests**:
   ```bash
   mvn test
   ```

5. **Build project**:
   ```bash
   mvn clean install
   ```

6. **Commit and push**:
   ```bash
   git add .
   git commit -m "Add your feature"
   git push origin feature/your-feature
   ```

## Useful Commands

### Clean Build
```bash
mvn clean install
```

### Skip Tests
```bash
mvn clean install -DskipTests
```

### Run Specific Profile
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

### Generate Javadoc
```bash
mvn javadoc:javadoc
```

### Check Dependencies
```bash
mvn dependency:tree
```

### Format Code
```bash
mvn spotless:apply
```

## Docker Build & Run

### Build Docker Image
```bash
mvn clean package
docker build -t request-management-service:1.0.0 .
```

### Run Docker Container
```bash
docker run -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://postgres:5432/access_request_db \
  -e DATABASE_USER=postgres \
  -e DATABASE_PASSWORD=bir*1234 \
  -e KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
  -e REDIS_HOST=redis \
  request-management-service:1.0.0
```

### Docker Compose (Full Stack)
```bash
docker-compose up -d
```

## Monitoring

### Prometheus Metrics
```
http://localhost:8080/actuator/prometheus
```

### Health Check
```
http://localhost:8080/actuator/health
```

### Swagger UI
```
http://localhost:8080/swagger-ui.html
```

## Documentation

- **API Documentation**: See `PHASE_1_COMPLETION_SUMMARY.md`
- **Project Structure**: See `PROJECT_STRUCTURE.md`
- **Logical Design**: See `../logical_design.md`
- **Implementation Plan**: See `../../plan.md`

## Support

For issues or questions:
1. Check the troubleshooting section above
2. Review the logical design document
3. Check application logs: `logs/spring.log`
4. Review test cases for usage examples

## Next Steps

After successful setup:
1. Review the API documentation in Swagger UI
2. Create sample requests using the API endpoints
3. Monitor the application using Prometheus metrics
4. Proceed with Phase 2: Domain Layer Implementation
