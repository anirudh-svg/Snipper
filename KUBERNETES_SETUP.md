# Kubernetes Setup Guide

This guide explains how to deploy the Snipper application to a Kubernetes cluster.

## Prerequisites

- Kubernetes cluster (v1.24+)
- kubectl configured to access your cluster
- Docker images built and pushed to a registry (or available locally for Minikube)

## Quick Start

### 1. Build Docker Images

```bash
# Build backend image
docker build -t snipper-backend:latest ./backend

# Build frontend image
docker build -t snipper-frontend:latest ./frontend
```

### 2. Deploy to Kubernetes

```bash
# Apply all manifests in order
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/mysql-pvc.yaml
kubectl apply -f k8s/mysql-statefulset.yaml
kubectl apply -f k8s/backend-deployment.yaml
kubectl apply -f k8s/frontend-deployment.yaml
kubectl apply -f k8s/hpa.yaml
```

Or apply all at once:

```bash
kubectl apply -f k8s/
```

### 3. Verify Deployment

```bash
# Check all resources
kubectl get all -n snipper

# Check pods status
kubectl get pods -n snipper

# Check services
kubectl get svc -n snipper
```

### 4. Access the Application

```bash
# Get frontend service external IP
kubectl get svc frontend-service -n snipper

# For Minikube
minikube service frontend-service -n snipper
```

## Detailed Setup

### Namespace

Creates an isolated namespace for the application:

```bash
kubectl apply -f k8s/namespace.yaml
```

### ConfigMap

Stores non-sensitive configuration:

```bash
kubectl apply -f k8s/configmap.yaml
```

### Secrets

**Important**: Update secrets before deploying to production!

```bash
# Create secrets manually (recommended for production)
kubectl create secret generic snipper-secret \
  --from-literal=MYSQL_ROOT_PASSWORD=<strong-password> \
  --from-literal=MYSQL_USER=snipper_user \
  --from-literal=MYSQL_PASSWORD=<strong-password> \
  --from-literal=SPRING_DATASOURCE_USERNAME=snipper_user \
  --from-literal=SPRING_DATASOURCE_PASSWORD=<strong-password> \
  --from-literal=JWT_SECRET=<strong-jwt-secret-min-256-bits> \
  -n snipper

# Or apply the default (NOT for production)
kubectl apply -f k8s/secret.yaml
```

### MySQL Database

Deploy MySQL with persistent storage:

```bash
kubectl apply -f k8s/mysql-pvc.yaml
kubectl apply -f k8s/mysql-statefulset.yaml

# Wait for MySQL to be ready
kubectl wait --for=condition=ready pod -l app=mysql -n snipper --timeout=300s
```

### Backend Service

Deploy Spring Boot backend:

```bash
kubectl apply -f k8s/backend-deployment.yaml

# Wait for backend to be ready
kubectl wait --for=condition=ready pod -l app=backend -n snipper --timeout=300s
```

### Frontend Service

Deploy React frontend:

```bash
kubectl apply -f k8s/frontend-deployment.yaml
```

### Horizontal Pod Autoscaler

Enable auto-scaling based on CPU/memory:

```bash
kubectl apply -f k8s/hpa.yaml

# Check HPA status
kubectl get hpa -n snipper
```

## Monitoring and Debugging

### View Logs

```bash
# Backend logs
kubectl logs -f deployment/backend -n snipper

# Frontend logs
kubectl logs -f deployment/frontend -n snipper

# MySQL logs
kubectl logs -f statefulset/mysql -n snipper
```

### Execute Commands in Pods

```bash
# Access backend pod
kubectl exec -it deployment/backend -n snipper -- /bin/sh

# Access MySQL
kubectl exec -it statefulset/mysql -n snipper -- mysql -u root -p
```

### Port Forwarding (for testing)

```bash
# Forward backend port
kubectl port-forward svc/backend-service 9090:9090 -n snipper

# Forward frontend port
kubectl port-forward svc/frontend-service 8080:80 -n snipper
```

### Check Resource Usage

```bash
# Pod resource usage
kubectl top pods -n snipper

# Node resource usage
kubectl top nodes
```

## Scaling

### Manual Scaling

```bash
# Scale backend
kubectl scale deployment backend --replicas=5 -n snipper

# Scale frontend
kubectl scale deployment frontend --replicas=3 -n snipper
```

### Auto-scaling

HPA automatically scales based on CPU/memory usage (configured in `hpa.yaml`).

## Updates and Rollouts

### Update Application

```bash
# Update backend image
kubectl set image deployment/backend backend=snipper-backend:v2 -n snipper

# Update frontend image
kubectl set image deployment/frontend frontend=snipper-frontend:v2 -n snipper
```

### Rollout Status

```bash
# Check rollout status
kubectl rollout status deployment/backend -n snipper
kubectl rollout status deployment/frontend -n snipper
```

### Rollback

```bash
# Rollback to previous version
kubectl rollout undo deployment/backend -n snipper
kubectl rollout undo deployment/frontend -n snipper
```

## Cleanup

### Delete All Resources

```bash
# Delete all resources in namespace
kubectl delete namespace snipper

# Or delete individual resources
kubectl delete -f k8s/
```

### Delete Persistent Data

```bash
# Delete PVC (this will delete MySQL data!)
kubectl delete pvc mysql-pvc -n snipper
```

## Production Considerations

### 1. Image Registry

Push images to a container registry:

```bash
# Tag images
docker tag snipper-backend:latest your-registry/snipper-backend:v1.0.0
docker tag snipper-frontend:latest your-registry/snipper-frontend:v1.0.0

# Push images
docker push your-registry/snipper-backend:v1.0.0
docker push your-registry/snipper-frontend:v1.0.0

# Update deployment manifests with registry URLs
```

### 2. Secrets Management

- Use external secrets management (e.g., HashiCorp Vault, AWS Secrets Manager)
- Never commit secrets to version control
- Rotate secrets regularly

### 3. Ingress Controller

For production, use an Ingress controller instead of LoadBalancer:

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: snipper-ingress
  namespace: snipper
  annotations:
    kubernetes.io/ingress.class: nginx
    cert-manager.io/cluster-issuer: letsencrypt-prod
spec:
  tls:
  - hosts:
    - snipper.example.com
    secretName: snipper-tls
  rules:
  - host: snipper.example.com
    http:
      paths:
      - path: /api
        pathType: Prefix
        backend:
          service:
            name: backend-service
            port:
              number: 9090
      - path: /
        pathType: Prefix
        backend:
          service:
            name: frontend-service
            port:
              number: 80
```

### 4. Monitoring

Set up monitoring with:
- Prometheus for metrics
- Grafana for visualization
- ELK/EFK stack for logging

### 5. Backup Strategy

- Regular MySQL backups
- Backup PersistentVolumes
- Disaster recovery plan

### 6. Resource Limits

Adjust resource requests/limits based on actual usage:

```bash
# Monitor resource usage
kubectl top pods -n snipper --containers
```

### 7. Network Policies

Implement network policies for security:

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: backend-network-policy
  namespace: snipper
spec:
  podSelector:
    matchLabels:
      app: backend
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - podSelector:
        matchLabels:
          app: frontend
    ports:
    - protocol: TCP
      port: 9090
  egress:
  - to:
    - podSelector:
        matchLabels:
          app: mysql
    ports:
    - protocol: TCP
      port: 3306
```

## Troubleshooting

### Pods Not Starting

```bash
# Describe pod to see events
kubectl describe pod <pod-name> -n snipper

# Check logs
kubectl logs <pod-name> -n snipper
```

### Database Connection Issues

```bash
# Test MySQL connectivity
kubectl run -it --rm debug --image=mysql:8.0 --restart=Never -n snipper -- \
  mysql -h mysql-service -u snipper_user -p

# Check MySQL service
kubectl get svc mysql-service -n snipper
```

### Image Pull Errors

```bash
# Check image pull secrets
kubectl get secrets -n snipper

# Create image pull secret if needed
kubectl create secret docker-registry regcred \
  --docker-server=<your-registry> \
  --docker-username=<username> \
  --docker-password=<password> \
  -n snipper
```

### HPA Not Scaling

```bash
# Check metrics server
kubectl get apiservice v1beta1.metrics.k8s.io

# Install metrics server if needed
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
```

## Minikube Specific

### Start Minikube

```bash
minikube start --cpus=4 --memory=8192

# Enable metrics server
minikube addons enable metrics-server
```

### Load Images to Minikube

```bash
# Load images directly
minikube image load snipper-backend:latest
minikube image load snipper-frontend:latest

# Or use Minikube's Docker daemon
eval $(minikube docker-env)
docker build -t snipper-backend:latest ./backend
docker build -t snipper-frontend:latest ./frontend
```

### Access Services

```bash
# Get service URL
minikube service frontend-service -n snipper --url

# Open in browser
minikube service frontend-service -n snipper
```
