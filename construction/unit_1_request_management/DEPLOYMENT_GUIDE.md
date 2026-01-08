# Deployment Guide

## Overview

This guide provides comprehensive instructions for deploying the Request Management Service to various environments.

---

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- Docker and Docker Compose
- Kubernetes cluster (for K8s deployment)
- kubectl CLI (for K8s deployment)

---

## Build Process

### 1. Build JAR File

```bash
cd construction/unit_1_request_management
mvn clean package -DskipTests
```

Output: `target/request-management-service-1.0.0.jar`

### 2. Build Docker Image

**Option A: Using Dockerfile**
```bash
docker build -t request-management-service:1.0.0 .
```

**Option B: Using Maven Plugin**
```bash
mvn clean package docker:build
```

### 3. Push to Registry

```bash
# Tag image
docker tag request-management-service:1.0.0 myregistry.azurecr.io/request-management-service:1.0.0

# Login to registry
docker login myregistry.azurecr.io

# Push image
docker push myregistry.azurecr.io/request-management-service:1.0.0
```

---

## Local Deployment

### Using Docker Compose

**Step 1: Start Infrastructure**
```bash
docker-compose up -d
```

**Step 2: Build Application Image**
```bash
docker build -t request-management-service:latest .
```

**Step 3: Run Application**
```bash
docker run -d \
  --name request-management-app \
  --network request-management-network \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e DB_HOST=postgres \
  -e DB_PORT=5432 \
  -e DB_NAME=request_management \
  -e DB_USER=postgres \
  -e DB_PASSWORD=bir*1234 \
  -e KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
  -e REDIS_HOST=redis \
  -e REDIS_PORT=6379 \
  request-management-service:latest
```

**Step 4: Verify Deployment**
```bash
curl http://localhost:8080/api/v1/health/live
```

---

## Cloud Deployment

### Azure Container Instances (ACI)

**Step 1: Create Resource Group**
```bash
az group create --name request-management-rg --location eastus
```

**Step 2: Create Container Registry**
```bash
az acr create --resource-group request-management-rg \
  --name requestmgmtregistry --sku Basic
```

**Step 3: Build and Push Image**
```bash
az acr build --registry requestmgmtregistry \
  --image request-management-service:1.0.0 .
```

**Step 4: Deploy Container**
```bash
az container create \
  --resource-group request-management-rg \
  --name request-management-app \
  --image requestmgmtregistry.azurecr.io/request-management-service:1.0.0 \
  --cpu 2 --memory 4 \
  --registry-login-server requestmgmtregistry.azurecr.io \
  --registry-username <username> \
  --registry-password <password> \
  --environment-variables \
    SPRING_PROFILES_ACTIVE=prod \
    DB_HOST=<db-host> \
    KAFKA_BOOTSTRAP_SERVERS=<kafka-host>:9092
```

### AWS Elastic Container Service (ECS)

**Step 1: Create ECR Repository**
```bash
aws ecr create-repository --repository-name request-management-service
```

**Step 2: Build and Push Image**
```bash
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com

docker tag request-management-service:1.0.0 <account-id>.dkr.ecr.us-east-1.amazonaws.com/request-management-service:1.0.0

docker push <account-id>.dkr.ecr.us-east-1.amazonaws.com/request-management-service:1.0.0
```

**Step 3: Create ECS Task Definition**
```json
{
  "family": "request-management-service",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "2048",
  "memory": "4096",
  "containerDefinitions": [
    {
      "name": "request-management-service",
      "image": "<account-id>.dkr.ecr.us-east-1.amazonaws.com/request-management-service:1.0.0",
      "portMappings": [
        {
          "containerPort": 8080,
          "hostPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SPRING_PROFILES_ACTIVE",
          "value": "prod"
        }
      ]
    }
  ]
}
```

---

## Kubernetes Deployment

### Prerequisites

- Kubernetes cluster running
- kubectl configured
- Helm (optional)

### Step 1: Create Namespace

```bash
kubectl create namespace request-management
```

### Step 2: Create ConfigMap

```bash
kubectl create configmap request-management-config \
  --from-literal=SPRING_PROFILES_ACTIVE=prod \
  --from-literal=DB_HOST=postgres.request-management.svc.cluster.local \
  --from-literal=KAFKA_BOOTSTRAP_SERVERS=kafka.request-management.svc.cluster.local:9092 \
  -n request-management
```

### Step 3: Create Secret

```bash
kubectl create secret generic request-management-secret \
  --from-literal=DB_PASSWORD=bir*1234 \
  --from-literal=JWT_SECRET=your-secret-key \
  -n request-management
```

### Step 4: Deploy PostgreSQL

```yaml
apiVersion: v1
kind: Service
metadata:
  name: postgres
  namespace: request-management
spec:
  ports:
  - port: 5432
  selector:
    app: postgres
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: postgres
  namespace: request-management
spec:
  replicas: 1
  selector:
    matchLabels:
      app: postgres
  template:
    metadata:
      labels:
        app: postgres
    spec:
      containers:
      - name: postgres
        image: postgres:17-alpine
        ports:
        - containerPort: 5432
        env:
        - name: POSTGRES_DB
          value: request_management
        - name: POSTGRES_USER
          value: postgres
        - name: POSTGRES_PASSWORD
          valueFrom:
            secretKeyRef:
              name: request-management-secret
              key: DB_PASSWORD
        volumeMounts:
        - name: postgres-storage
          mountPath: /var/lib/postgresql/data
      volumes:
      - name: postgres-storage
        emptyDir: {}
```

### Step 5: Deploy Kafka

```yaml
apiVersion: v1
kind: Service
metadata:
  name: kafka
  namespace: request-management
spec:
  ports:
  - port: 9092
  selector:
    app: kafka
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: kafka
  namespace: request-management
spec:
  replicas: 1
  selector:
    matchLabels:
      app: kafka
  template:
    metadata:
      labels:
        app: kafka
    spec:
      containers:
      - name: kafka
        image: confluentinc/cp-kafka:7.5.0
        ports:
        - containerPort: 9092
        env:
        - name: KAFKA_BROKER_ID
          value: "1"
        - name: KAFKA_ZOOKEEPER_CONNECT
          value: zookeeper:2181
        - name: KAFKA_ADVERTISED_LISTENERS
          value: PLAINTEXT://kafka:9092
```

### Step 6: Deploy Application

```yaml
apiVersion: v1
kind: Service
metadata:
  name: request-management-service
  namespace: request-management
spec:
  type: LoadBalancer
  ports:
  - port: 80
    targetPort: 8080
  selector:
    app: request-management-service
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: request-management-service
  namespace: request-management
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
        image: myregistry.azurecr.io/request-management-service:1.0.0
        ports:
        - containerPort: 8080
        envFrom:
        - configMapRef:
            name: request-management-config
        - secretRef:
            name: request-management-secret
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
        resources:
          requests:
            memory: "2Gi"
            cpu: "1000m"
          limits:
            memory: "4Gi"
            cpu: "2000m"
```

### Step 7: Deploy with kubectl

```bash
kubectl apply -f deployment.yaml -n request-management
```

### Step 8: Verify Deployment

```bash
# Check pods
kubectl get pods -n request-management

# Check services
kubectl get svc -n request-management

# Check logs
kubectl logs -f deployment/request-management-service -n request-management

# Port forward for testing
kubectl port-forward svc/request-management-service 8080:80 -n request-management
```

---

## Database Migration

### Automatic Migration

Liquibase migrations run automatically on application startup:
```properties
spring.liquibase.enabled=true
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.xml
```

### Manual Migration

```bash
mvn liquibase:update -Dspring.profiles.active=prod
```

### Rollback

```bash
mvn liquibase:rollback -Dliquibase.rollbackCount=1
```

---

## Monitoring & Logging

### Prometheus Metrics

Metrics are exposed at: `/actuator/prometheus`

Configure Prometheus scrape config:
```yaml
scrape_configs:
  - job_name: 'request-management-service'
    static_configs:
      - targets: ['localhost:8080']
    metrics_path: '/actuator/prometheus'
```

### Grafana Dashboards

1. Add Prometheus as data source
2. Import dashboard JSON
3. Configure alerts

### Centralized Logging

Configure ELK Stack:
```properties
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
logging.file.name=logs/application.log
```

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

---

## Scaling

### Horizontal Scaling

```bash
# Kubernetes
kubectl scale deployment request-management-service --replicas=5 -n request-management

# Docker Compose
docker-compose up -d --scale app=3
```

### Load Balancing

- Kubernetes: Service with LoadBalancer type
- Docker: Use nginx reverse proxy
- Cloud: Use cloud provider's load balancer

---

## Backup & Recovery

### Database Backup

```bash
# PostgreSQL backup
pg_dump -h localhost -U postgres request_management > backup.sql

# Restore
psql -h localhost -U postgres request_management < backup.sql
```

### Kafka Backup

Kafka topics are replicated across brokers. Configure replication factor:
```properties
num.network.threads=8
num.io.threads=8
```

---

## Troubleshooting

### Application Won't Start

1. Check logs: `docker logs request-management-app`
2. Verify database connection
3. Verify Kafka connection
4. Check configuration

### High Memory Usage

1. Increase JVM heap: `-Xmx4g`
2. Enable garbage collection logging
3. Profile application

### Slow Queries

1. Check database indexes
2. Enable query logging
3. Analyze slow queries
4. Optimize queries

---

## Rollback Procedure

### Docker

```bash
# Stop current version
docker stop request-management-app

# Run previous version
docker run -d --name request-management-app \
  request-management-service:0.9.0
```

### Kubernetes

```bash
# Rollback deployment
kubectl rollout undo deployment/request-management-service -n request-management

# Check rollout history
kubectl rollout history deployment/request-management-service -n request-management
```

---

## Performance Tuning

### JVM Tuning

```bash
java -Xms2g -Xmx4g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -jar request-management-service.jar
```

### Database Tuning

```sql
-- Analyze query performance
EXPLAIN ANALYZE SELECT * FROM access_requests WHERE status = 'Draft';

-- Create indexes
CREATE INDEX idx_request_status ON access_requests(status);
CREATE INDEX idx_request_created_at ON access_requests(created_at);
```

### Caching

Configure Redis for optimal performance:
```properties
spring.redis.timeout=2000
spring.cache.redis.time-to-live=300000
```

---

## Security Hardening

### Network Security

- Use VPC/Private networks
- Configure firewall rules
- Enable SSL/TLS

### Application Security

- Rotate JWT secrets regularly
- Use strong passwords
- Enable audit logging
- Regular security updates

### Data Security

- Encrypt sensitive data
- Use database encryption
- Regular backups
- Access control

---

## Compliance

### Audit Logging

All operations are logged with:
- Timestamp
- User ID
- Action
- Result
- IP Address

### Data Retention

Configure retention policies:
```properties
logging.file.max-history=30
logging.file.max-size=10MB
```

---

## Support

For deployment issues:
1. Check logs
2. Verify configuration
3. Check resource availability
4. Review monitoring metrics
5. Contact support team
