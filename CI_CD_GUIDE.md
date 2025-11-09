# CI/CD Pipeline Guide

This guide explains the Continuous Integration and Continuous Deployment (CI/CD) pipeline for the Snipper application using GitHub Actions.

## Overview

The CI/CD pipeline consists of four main workflows:

1. **Backend CI** - Tests and builds the Spring Boot backend
2. **Frontend CI** - Tests and builds the React frontend
3. **Docker Build** - Builds and pushes Docker images
4. **Deploy** - Deploys to Kubernetes cluster

## Workflows

### 1. Backend CI (`backend-ci.yml`)

**Triggers:**
- Push to `main` or `develop` branches (backend changes)
- Pull requests to `main` or `develop` (backend changes)

**Jobs:**

#### Test Job
- Sets up MySQL service container
- Runs Maven tests with coverage
- Generates test reports
- Uploads coverage to Codecov

#### Build Job
- Builds the application with Maven
- Creates JAR artifact
- Uploads artifact for later use

#### Code Quality Job
- Runs SonarCloud analysis
- Checks code quality metrics
- Reports issues and technical debt

**Required Secrets:**
- `SONAR_TOKEN` - SonarCloud authentication token

### 2. Frontend CI (`frontend-ci.yml`)

**Triggers:**
- Push to `main` or `develop` branches (frontend changes)
- Pull requests to `main` or `develop` (frontend changes)

**Jobs:**

#### Test Job
- Installs npm dependencies
- Runs ESLint for code linting
- Executes Jest tests with coverage
- Uploads coverage to Codecov

#### Build Job
- Builds production React application
- Optimizes bundle size
- Uploads build artifact

#### Code Quality Job
- Runs SonarCloud analysis for frontend
- Checks code quality and security

**Required Secrets:**
- `SONAR_TOKEN` - SonarCloud authentication token

### 3. Docker Build (`docker-build.yml`)

**Triggers:**
- Push to `main` branch
- Git tags (e.g., `v1.0.0`)
- Manual workflow dispatch

**Jobs:**

#### Build Backend Image
- Builds multi-platform Docker image (amd64, arm64)
- Pushes to GitHub Container Registry
- Tags with version, branch, and SHA

#### Build Frontend Image
- Builds multi-platform Docker image
- Pushes to GitHub Container Registry
- Tags appropriately

#### Security Scan
- Scans images with Trivy
- Uploads vulnerability reports
- Fails on critical vulnerabilities

**Image Tags:**
- `latest` - Latest main branch build
- `v1.0.0` - Semantic version tags
- `main-abc123` - Branch with commit SHA
- `staging` - Staging environment

### 4. Deploy (`deploy.yml`)

**Triggers:**
- Successful completion of Docker Build workflow
- Manual workflow dispatch with environment selection

**Jobs:**

#### Deploy to Kubernetes
- Configures kubectl with cluster credentials
- Creates/updates namespace
- Deploys ConfigMaps and Secrets
- Deploys MySQL StatefulSet
- Updates Backend deployment
- Updates Frontend deployment
- Applies HPA configuration
- Runs smoke tests
- Sends deployment notifications

**Required Secrets:**
- `KUBE_CONFIG` - Base64 encoded kubeconfig file
- `MYSQL_ROOT_PASSWORD` - MySQL root password
- `MYSQL_USER` - MySQL username
- `MYSQL_PASSWORD` - MySQL password
- `JWT_SECRET` - JWT signing secret
- `SLACK_WEBHOOK` - Slack webhook URL (optional)

## Setup Instructions

### 1. Configure GitHub Secrets

Navigate to your repository → Settings → Secrets and variables → Actions

Add the following secrets:

```bash
# SonarCloud
SONAR_TOKEN=your-sonarcloud-token

# Kubernetes
KUBE_CONFIG=base64-encoded-kubeconfig

# Database
MYSQL_ROOT_PASSWORD=strong-root-password
MYSQL_USER=snipper_user
MYSQL_PASSWORD=strong-user-password

# Application
JWT_SECRET=your-jwt-secret-min-256-bits

# Notifications (optional)
SLACK_WEBHOOK=https://hooks.slack.com/services/YOUR/WEBHOOK/URL
```

### 2. Generate kubeconfig

```bash
# Get your kubeconfig
cat ~/.kube/config | base64 -w 0

# Or for specific context
kubectl config view --minify --flatten | base64 -w 0
```

Add the output as `KUBE_CONFIG` secret.

### 3. Configure SonarCloud

1. Go to [SonarCloud](https://sonarcloud.io)
2. Import your repository
3. Generate a token
4. Add token as `SONAR_TOKEN` secret
5. Update organization in workflow files

### 4. Configure Container Registry

The workflows use GitHub Container Registry (ghcr.io) by default.

**Permissions:**
- Ensure GitHub Actions has write access to packages
- Repository → Settings → Actions → General → Workflow permissions
- Select "Read and write permissions"

### 5. Test Workflows Locally

Use [act](https://github.com/nektos/act) to test workflows locally:

```bash
# Install act
brew install act  # macOS
choco install act  # Windows

# Run backend CI
act -W .github/workflows/backend-ci.yml

# Run with secrets
act -W .github/workflows/backend-ci.yml --secret-file .secrets
```

## Workflow Execution

### Automatic Triggers

1. **Push to main/develop:**
   ```bash
   git push origin main
   ```
   - Triggers Backend CI and/or Frontend CI
   - If main branch, triggers Docker Build
   - Docker Build success triggers Deploy

2. **Create Pull Request:**
   ```bash
   gh pr create --base main --head feature-branch
   ```
   - Triggers CI workflows for changed components
   - Shows status checks in PR

3. **Create Release Tag:**
   ```bash
   git tag -a v1.0.0 -m "Release v1.0.0"
   git push origin v1.0.0
   ```
   - Triggers Docker Build with version tags

### Manual Triggers

1. **Manual Deployment:**
   - Go to Actions → Deploy to Kubernetes
   - Click "Run workflow"
   - Select environment (staging/production)
   - Click "Run workflow"

2. **Manual Docker Build:**
   - Go to Actions → Docker Build and Push
   - Click "Run workflow"
   - Select branch
   - Click "Run workflow"

## Monitoring Workflows

### View Workflow Runs

```bash
# Using GitHub CLI
gh run list
gh run view <run-id>
gh run watch <run-id>
```

### Check Logs

```bash
# View logs for specific run
gh run view <run-id> --log

# View logs for specific job
gh run view <run-id> --log --job=<job-id>
```

### Workflow Status Badge

Add to README.md:

```markdown
![Backend CI](https://github.com/username/repo/workflows/Backend%20CI/badge.svg)
![Frontend CI](https://github.com/username/repo/workflows/Frontend%20CI/badge.svg)
![Docker Build](https://github.com/username/repo/workflows/Docker%20Build%20and%20Push/badge.svg)
```

## Deployment Environments

### Staging Environment

- **Namespace:** `snipper-staging`
- **Trigger:** Automatic on main branch
- **Image Tag:** `staging`
- **Purpose:** Pre-production testing

### Production Environment

- **Namespace:** `snipper-prod`
- **Trigger:** Manual approval required
- **Image Tag:** `latest` or version tag
- **Purpose:** Live production environment

## Rollback Procedures

### Rollback Deployment

```bash
# Rollback to previous version
kubectl rollout undo deployment/backend -n snipper-prod
kubectl rollout undo deployment/frontend -n snipper-prod

# Rollback to specific revision
kubectl rollout undo deployment/backend --to-revision=2 -n snipper-prod

# Check rollout history
kubectl rollout history deployment/backend -n snipper-prod
```

### Rollback via GitHub Actions

1. Go to Actions → Deploy to Kubernetes
2. Find successful previous deployment
3. Click "Re-run all jobs"

## Troubleshooting

### CI Failures

#### Backend Tests Failing

```bash
# Run tests locally
cd backend
mvn clean test

# Check MySQL connection
docker run -d -p 3306:3306 -e MYSQL_ROOT_PASSWORD=rootpassword mysql:8.0
mvn test
```

#### Frontend Tests Failing

```bash
# Run tests locally
cd frontend
npm test

# Run with coverage
npm test -- --coverage
```

### Build Failures

#### Docker Build Failing

```bash
# Test Docker build locally
docker build -t test-backend ./backend
docker build -t test-frontend ./frontend

# Check build logs
docker build --progress=plain -t test-backend ./backend
```

#### Out of Memory

Increase Docker memory in workflow:

```yaml
- name: Build with Maven
  run: mvn clean package -DskipTests
  env:
    MAVEN_OPTS: "-Xmx2048m"
```

### Deployment Failures

#### kubectl Connection Issues

```bash
# Verify kubeconfig
echo $KUBE_CONFIG | base64 -d > /tmp/config
kubectl --kubeconfig=/tmp/config get nodes

# Test connection
kubectl cluster-info
```

#### Pod Not Starting

```bash
# Check pod status
kubectl get pods -n snipper-prod
kubectl describe pod <pod-name> -n snipper-prod
kubectl logs <pod-name> -n snipper-prod

# Check events
kubectl get events -n snipper-prod --sort-by='.lastTimestamp'
```

#### Image Pull Errors

```bash
# Verify image exists
docker pull ghcr.io/username/repo/backend:latest

# Check image pull secret
kubectl get secrets -n snipper-prod
```

## Best Practices

1. **Branch Protection**
   - Require status checks to pass
   - Require pull request reviews
   - Require up-to-date branches

2. **Secrets Management**
   - Rotate secrets regularly
   - Use environment-specific secrets
   - Never commit secrets to repository

3. **Testing**
   - Maintain high test coverage (>80%)
   - Run tests before pushing
   - Fix failing tests immediately

4. **Deployment**
   - Deploy to staging first
   - Run smoke tests after deployment
   - Monitor application after deployment
   - Have rollback plan ready

5. **Monitoring**
   - Set up alerts for workflow failures
   - Monitor deployment success rate
   - Track deployment frequency
   - Review failed workflows promptly

6. **Documentation**
   - Document workflow changes
   - Keep secrets list updated
   - Document deployment procedures
   - Maintain runbooks for common issues

## Performance Optimization

### Cache Dependencies

Already implemented in workflows:
- Maven dependencies cached
- npm dependencies cached
- Docker layer caching enabled

### Parallel Execution

Workflows run jobs in parallel when possible:
- Test, Build, and Code Quality run in parallel
- Backend and Frontend workflows run independently

### Conditional Execution

Workflows only run when relevant files change:
```yaml
paths:
  - 'backend/**'
  - '.github/workflows/backend-ci.yml'
```

## Security

### Dependency Scanning

- Trivy scans Docker images
- Dependabot updates dependencies
- SonarCloud checks for vulnerabilities

### Secret Scanning

GitHub automatically scans for exposed secrets.

### Code Signing

Consider signing Docker images:

```yaml
- name: Sign image
  uses: sigstore/cosign-installer@main
- run: cosign sign ghcr.io/username/repo/backend:latest
```

## Metrics and Analytics

Track these metrics:
- Build success rate
- Average build time
- Deployment frequency
- Mean time to recovery (MTTR)
- Change failure rate

Use GitHub Insights → Actions to view metrics.

## Additional Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Docker Build Push Action](https://github.com/docker/build-push-action)
- [kubectl Documentation](https://kubernetes.io/docs/reference/kubectl/)
- [SonarCloud Documentation](https://docs.sonarcloud.io/)
