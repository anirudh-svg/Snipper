# Monitoring and Health Checks Guide

This guide explains the monitoring and health check capabilities of the Snipper application.

## Health Check Endpoints

### Backend Health Checks

The Spring Boot backend exposes several health check endpoints via Spring Boot Actuator:

#### 1. General Health Check
```bash
curl http://localhost:9090/actuator/health
```

Response:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP"
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

#### 2. Liveness Probe
```bash
curl http://localhost:9090/actuator/health/liveness
```

Used by Kubernetes to determine if the application should be restarted.

#### 3. Readiness Probe
```bash
curl http://localhost:9090/actuator/health/readiness
```

Used by Kubernetes to determine if the application is ready to accept traffic.

#### 4. Application Info
```bash
curl http://localhost:9090/actuator/info
```

Response:
```json
{
  "app": {
    "name": "Snipper Backend API",
    "description": "Code snippet sharing platform backend",
    "version": "1.0.0"
  }
}
```

#### 5. Metrics
```bash
curl http://localhost:9090/actuator/metrics
```

Available metrics:
- `jvm.memory.used`
- `jvm.memory.max`
- `jvm.threads.live`
- `http.server.requests`
- `system.cpu.usage`
- `process.uptime`

Get specific metric:
```bash
curl http://localhost:9090/actuator/metrics/http.server.requests
```

#### 6. Prometheus Metrics
```bash
curl http://localhost:9090/actuator/prometheus
```

Exports metrics in Prometheus format for scraping.

### Frontend Health Checks

The Nginx-based frontend has a simple health check:

```bash
curl http://localhost:3000/
```

Returns HTTP 200 if the server is running.

## Docker Health Checks

### Backend Container
```bash
# Check health status
docker ps

# View health check logs
docker inspect --format='{{json .State.Health}}' snipper-backend
```

### Frontend Container
```bash
# Check health status
docker ps

# View health check logs
docker inspect --format='{{json .State.Health}}' snipper-frontend
```

### MySQL Container
```bash
# Check health status
docker ps

# View health check logs
docker inspect --format='{{json .State.Health}}' snipper-mysql
```

## Kubernetes Health Checks

### Check Pod Health
```bash
# Get pod status
kubectl get pods -n snipper

# Describe pod to see health check details
kubectl describe pod <pod-name> -n snipper

# View health check events
kubectl get events -n snipper --field-selector involvedObject.name=<pod-name>
```

### Liveness Probe Configuration

Backend liveness probe (from `backend-deployment.yaml`):
```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 9090
  initialDelaySeconds: 60
  periodSeconds: 10
  timeoutSeconds: 5
  failureThreshold: 3
```

### Readiness Probe Configuration

Backend readiness probe:
```yaml
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 9090
  initialDelaySeconds: 30
  periodSeconds: 5
  timeoutSeconds: 3
  failureThreshold: 3
```

## Monitoring with Prometheus

### Setup Prometheus

1. **Install Prometheus in Kubernetes:**

```bash
# Add Prometheus Helm repository
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

# Install Prometheus
helm install prometheus prometheus-community/prometheus -n monitoring --create-namespace
```

2. **Configure ServiceMonitor for Backend:**

```yaml
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: backend-metrics
  namespace: snipper
spec:
  selector:
    matchLabels:
      app: backend
  endpoints:
  - port: http
    path: /actuator/prometheus
    interval: 30s
```

3. **Access Prometheus UI:**

```bash
kubectl port-forward -n monitoring svc/prometheus-server 9090:80
```

Visit: http://localhost:9090

### Key Metrics to Monitor

#### Application Metrics
- `http_server_requests_seconds_count` - Total HTTP requests
- `http_server_requests_seconds_sum` - Total time spent processing requests
- `jvm_memory_used_bytes` - JVM memory usage
- `jvm_threads_live_threads` - Active threads
- `process_cpu_usage` - CPU usage

#### Database Metrics
- `hikaricp_connections_active` - Active database connections
- `hikaricp_connections_idle` - Idle database connections
- `hikaricp_connections_pending` - Pending connection requests

#### System Metrics
- `system_cpu_usage` - System CPU usage
- `system_load_average_1m` - System load average
- `disk_free_bytes` - Available disk space

### Sample Prometheus Queries

```promql
# Request rate (requests per second)
rate(http_server_requests_seconds_count[5m])

# Average response time
rate(http_server_requests_seconds_sum[5m]) / rate(http_server_requests_seconds_count[5m])

# Error rate (5xx responses)
rate(http_server_requests_seconds_count{status=~"5.."}[5m])

# Memory usage percentage
(jvm_memory_used_bytes / jvm_memory_max_bytes) * 100

# Active database connections
hikaricp_connections_active
```

## Monitoring with Grafana

### Setup Grafana

1. **Install Grafana:**

```bash
helm install grafana grafana/grafana -n monitoring
```

2. **Get Grafana admin password:**

```bash
kubectl get secret --namespace monitoring grafana -o jsonpath="{.data.admin-password}" | base64 --decode
```

3. **Access Grafana:**

```bash
kubectl port-forward -n monitoring svc/grafana 3000:80
```

Visit: http://localhost:3000

### Configure Prometheus Data Source

1. Go to Configuration → Data Sources
2. Add Prometheus
3. URL: `http://prometheus-server.monitoring.svc.cluster.local`
4. Save & Test

### Import Dashboards

Import these dashboard IDs:
- **4701** - JVM (Micrometer)
- **6756** - Spring Boot Statistics
- **11378** - Spring Boot Observability

### Custom Dashboard Panels

#### Request Rate
```promql
sum(rate(http_server_requests_seconds_count[5m])) by (uri)
```

#### Response Time (95th percentile)
```promql
histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[5m])) by (le, uri))
```

#### Error Rate
```promql
sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) / sum(rate(http_server_requests_seconds_count[5m]))
```

## Logging

### Application Logs

#### View Logs in Docker
```bash
# Backend logs
docker logs -f snipper-backend

# Frontend logs
docker logs -f snipper-frontend

# MySQL logs
docker logs -f snipper-mysql
```

#### View Logs in Kubernetes
```bash
# Backend logs
kubectl logs -f deployment/backend -n snipper

# Frontend logs
kubectl logs -f deployment/frontend -n snipper

# MySQL logs
kubectl logs -f statefulset/mysql -n snipper

# Follow logs from all pods
kubectl logs -f -l app=backend -n snipper
```

### Centralized Logging with ELK Stack

1. **Install Elasticsearch, Logstash, Kibana:**

```bash
helm repo add elastic https://helm.elastic.co
helm install elasticsearch elastic/elasticsearch -n logging --create-namespace
helm install kibana elastic/kibana -n logging
helm install filebeat elastic/filebeat -n logging
```

2. **Configure Filebeat to collect logs:**

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: filebeat-config
  namespace: logging
data:
  filebeat.yml: |
    filebeat.inputs:
    - type: container
      paths:
        - /var/log/containers/*.log
      processors:
        - add_kubernetes_metadata:
            host: ${NODE_NAME}
            matchers:
            - logs_path:
                logs_path: "/var/log/containers/"
    
    output.elasticsearch:
      hosts: ['${ELASTICSEARCH_HOST:elasticsearch}:${ELASTICSEARCH_PORT:9200}']
```

3. **Access Kibana:**

```bash
kubectl port-forward -n logging svc/kibana-kibana 5601:5601
```

Visit: http://localhost:5601

## Alerting

### Prometheus Alerting Rules

Create alert rules in `prometheus-alerts.yaml`:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: prometheus-alerts
  namespace: monitoring
data:
  alerts.yml: |
    groups:
    - name: snipper-alerts
      rules:
      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.05
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "High error rate detected"
          description: "Error rate is {{ $value }} requests/sec"
      
      - alert: HighMemoryUsage
        expr: (jvm_memory_used_bytes / jvm_memory_max_bytes) > 0.9
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High memory usage"
          description: "Memory usage is {{ $value }}%"
      
      - alert: PodDown
        expr: up{job="backend"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Backend pod is down"
          description: "Backend pod has been down for more than 1 minute"
```

### Configure Alertmanager

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: alertmanager-config
  namespace: monitoring
data:
  alertmanager.yml: |
    global:
      resolve_timeout: 5m
    
    route:
      group_by: ['alertname', 'cluster']
      group_wait: 10s
      group_interval: 10s
      repeat_interval: 12h
      receiver: 'email'
    
    receivers:
    - name: 'email'
      email_configs:
      - to: 'alerts@example.com'
        from: 'alertmanager@example.com'
        smarthost: 'smtp.example.com:587'
        auth_username: 'alertmanager@example.com'
        auth_password: 'password'
```

## Performance Monitoring

### Application Performance Metrics

Monitor these key performance indicators:

1. **Response Time**
   - Target: < 200ms for 95th percentile
   - Alert: > 500ms

2. **Throughput**
   - Monitor requests per second
   - Track by endpoint

3. **Error Rate**
   - Target: < 1%
   - Alert: > 5%

4. **Database Connection Pool**
   - Monitor active connections
   - Alert if pool exhaustion

5. **Memory Usage**
   - Target: < 80%
   - Alert: > 90%

6. **CPU Usage**
   - Target: < 70%
   - Alert: > 85%

### Load Testing

Use tools like Apache JMeter or k6 for load testing:

```bash
# Install k6
brew install k6  # macOS
choco install k6  # Windows

# Run load test
k6 run load-test.js
```

Sample k6 script:
```javascript
import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
  stages: [
    { duration: '2m', target: 100 },
    { duration: '5m', target: 100 },
    { duration: '2m', target: 0 },
  ],
};

export default function () {
  let res = http.get('http://localhost:9090/actuator/health');
  check(res, { 'status was 200': (r) => r.status == 200 });
  sleep(1);
}
```

## Troubleshooting

### High Memory Usage

```bash
# Get heap dump
kubectl exec -it deployment/backend -n snipper -- jcmd 1 GC.heap_dump /tmp/heap.hprof

# Copy heap dump locally
kubectl cp snipper/backend-pod:/tmp/heap.hprof ./heap.hprof

# Analyze with tools like Eclipse MAT or VisualVM
```

### Slow Queries

Enable slow query logging in MySQL:

```sql
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2;
SET GLOBAL slow_query_log_file = '/var/log/mysql/slow-query.log';
```

### Connection Pool Issues

Check HikariCP metrics:
```bash
curl http://localhost:9090/actuator/metrics/hikaricp.connections.active
curl http://localhost:9090/actuator/metrics/hikaricp.connections.pending
```

## Best Practices

1. **Set up alerts for critical metrics**
2. **Monitor trends, not just current values**
3. **Use dashboards for quick overview**
4. **Set up log aggregation early**
5. **Test health checks regularly**
6. **Document baseline performance metrics**
7. **Review metrics during deployments**
8. **Set up automated performance testing**
9. **Monitor business metrics (user signups, snippet creation)**
10. **Regular capacity planning based on metrics**
