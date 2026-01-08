# Demo Application Guide

## Overview

This guide provides instructions for running the Request Management Service locally with demo data and testing the complete workflow.

---

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- Docker and Docker Compose
- Git

---

## Quick Start

### 1. Start Infrastructure Services

```bash
cd construction/unit_1_request_management
docker-compose up -d
```

This starts:
- PostgreSQL (port 5432)
- Kafka (port 9092)
- Zookeeper (port 2181)
- Redis (port 6379)
- Prometheus (port 9090)
- Grafana (port 3000)

### 2. Build the Application

```bash
mvn clean install
```

### 3. Run the Application

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

The application will start on `http://localhost:8080`

### 4. Verify Application is Running

```bash
curl http://localhost:8080/api/v1/health/live
```

Expected response:
```json
{
  "status": "UP",
  "message": "Service is alive"
}
```

---

## Demo Data

When the application starts with the `dev` profile, it automatically initializes:

### Access Types
1. **OS** - Operating System access
2. **WebApp** - Web application access
3. **Database** - Database access

### Sample Requests
Three sample requests are created in Draft status for demonstration.

---

## API Documentation

### Swagger UI
Access the interactive API documentation at:
```
http://localhost:8080/swagger-ui.html
```

### OpenAPI JSON
```
http://localhost:8080/v3/api-docs
```

---

## Demo Scenarios

### Scenario 1: Create and Submit a Request

**Step 1: Create a Request**
```bash
curl -X POST http://localhost:8080/api/v1/requests \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -d '{
    "requestorId": "550e8400-e29b-41d4-a716-446655440000",
    "accessType": "OS",
    "systemName": "Production Server",
    "justification": "Need access for deployment and maintenance of microservices"
  }'
```

**Step 2: Get Request Details**
```bash
curl -X GET http://localhost:8080/api/v1/requests/{requestId} \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

**Step 3: Submit Request**
```bash
curl -X PUT http://localhost:8080/api/v1/requests/{requestId}/submit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -d '{
    "headOfOfficeId": "550e8400-e29b-41d4-a716-446655440001"
  }'
```

### Scenario 2: Approve Request Through Workflow

**Step 1: Head of Office Approves**
```bash
curl -X PUT http://localhost:8080/api/v1/requests/{requestId}/approve \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -d '{
    "approverId": "550e8400-e29b-41d4-a716-446655440001",
    "approvalType": "HeadOfOffice",
    "comments": "Approved for review"
  }'
```

**Step 2: Reviewer Endorses**
```bash
curl -X PUT http://localhost:8080/api/v1/requests/{requestId}/endorse \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -d '{
    "reviewerId": "550e8400-e29b-41d4-a716-446655440002",
    "comments": "Endorsed for final approval"
  }'
```

**Step 3: Head Approves**
```bash
curl -X PUT http://localhost:8080/api/v1/requests/{requestId}/approve \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -d '{
    "approverId": "550e8400-e29b-41d4-a716-446655440003",
    "approvalType": "Head",
    "comments": "Finally approved"
  }'
```

**Step 4: Admin Implements**
```bash
curl -X PUT http://localhost:8080/api/v1/requests/{requestId}/implement \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -d '{
    "implementerId": "550e8400-e29b-41d4-a716-446655440004",
    "notes": "Access granted successfully"
  }'
```

### Scenario 3: Decline Request

```bash
curl -X PUT http://localhost:8080/api/v1/requests/{requestId}/decline \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -d '{
    "declinerId": "550e8400-e29b-41d4-a716-446655440001",
    "reason": "Does not meet security requirements"
  }'
```

### Scenario 4: Return Request to Reviewer

```bash
curl -X PUT http://localhost:8080/api/v1/requests/{requestId}/return \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -d '{
    "returnerId": "550e8400-e29b-41d4-a716-446655440003",
    "reason": "Need more information about the access requirements"
  }'
```

### Scenario 5: Search Requests

**Advanced Search**
```bash
curl -X GET "http://localhost:8080/api/v1/search/requests?query=Linux&status=Draft&page=0&size=20" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

**Pending for Approver**
```bash
curl -X GET "http://localhost:8080/api/v1/search/pending-for-approver/{approverId}" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

**By Requestor**
```bash
curl -X GET "http://localhost:8080/api/v1/search/by-requestor/{requestorId}" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

**Overdue Requests**
```bash
curl -X GET "http://localhost:8080/api/v1/search/overdue" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

---

## Monitoring

### Prometheus
Access Prometheus at: `http://localhost:9090`

Available metrics:
- `http_requests_total` - Total HTTP requests
- `http_request_duration_seconds` - Request duration
- `jvm_memory_used_bytes` - JVM memory usage
- `process_cpu_usage` - CPU usage

### Grafana
Access Grafana at: `http://localhost:3000`
- Default username: `admin`
- Default password: `admin`

Add Prometheus as a data source:
1. Go to Configuration → Data Sources
2. Click "Add data source"
3. Select Prometheus
4. Set URL to `http://prometheus:9090`
5. Click "Save & Test"

---

## Health Checks

### Liveness Probe
```bash
curl http://localhost:8080/api/v1/health/live
```

### Readiness Probe
```bash
curl http://localhost:8080/api/v1/health/ready
```

### Detailed Health
```bash
curl http://localhost:8080/api/v1/health/detailed
```

---

## Database Access

### Connect to PostgreSQL
```bash
psql -h localhost -U postgres -d request_management
```

Password: `bir*1234`

### Useful Queries

**List all requests:**
```sql
SELECT * FROM access_requests;
```

**List all access types:**
```sql
SELECT * FROM access_types;
```

**View request approvals:**
```sql
SELECT * FROM request_approvals;
```

**View request history:**
```sql
SELECT * FROM request_history;
```

---

## Kafka Topics

### View Topics
```bash
docker exec request-management-kafka kafka-topics --list --bootstrap-server localhost:9092
```

### View Messages
```bash
docker exec request-management-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic request.events \
  --from-beginning
```

---

## Logs

### Application Logs
```bash
tail -f logs/application.log
```

### Docker Logs
```bash
docker-compose logs -f request-management-app
```

---

## Troubleshooting

### Port Already in Use
If a port is already in use, modify `docker-compose.yml`:
```yaml
ports:
  - "5433:5432"  # Change 5432 to 5433
```

### Database Connection Error
Ensure PostgreSQL is running:
```bash
docker-compose ps
```

### Kafka Connection Error
Ensure Kafka and Zookeeper are running:
```bash
docker-compose logs kafka
docker-compose logs zookeeper
```

### Application Won't Start
Check logs:
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev" 2>&1 | tail -50
```

---

## Cleanup

### Stop All Services
```bash
docker-compose down
```

### Remove All Data
```bash
docker-compose down -v
```

---

## Next Steps

1. Review the API documentation at `http://localhost:8080/swagger-ui.html`
2. Test the demo scenarios above
3. Monitor the application using Prometheus and Grafana
4. Review logs and metrics
5. Proceed to Phase 8: Documentation & Finalization

---

## Support

For issues or questions:
1. Check the logs
2. Review the API documentation
3. Verify all services are running: `docker-compose ps`
4. Check database connectivity
5. Verify Kafka is operational
