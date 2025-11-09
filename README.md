# Snipper - Code Snippet Sharing Platform

A production-ready code snippet sharing platform built with Spring Boot, React, Docker, and Kubernetes. Share, discover, and manage code snippets with Markdown support and syntax highlighting.

![Backend CI](https://github.com/username/snipper/workflows/Backend%20CI/badge.svg)
![Frontend CI](https://github.com/username/snipper/workflows/Frontend%20CI/badge.svg)
![Docker Build](https://github.com/username/snipper/workflows/Docker%20Build%20and%20Push/badge.svg)

## Features

- **Secure Authentication** - JWT-based authentication with refresh tokens
- **Markdown Support** - Rich text editing with live Markdown preview
- **Syntax Highlighting** - Support for 20+ programming languages via Prism.js
- **Search & Filter** - Full-text search with language and tag filtering
- **User Dashboard** - Manage snippets with statistics and activity tracking
- **Public/Private Snippets** - Control snippet visibility
- **Responsive Design** - Mobile-friendly interface
- **Containerized** - Docker and Docker Compose ready
- **Kubernetes Ready** - Production-grade K8s manifests
- **CI/CD Pipeline** - Automated testing and deployment with GitHub Actions

## Tech Stack

### Backend
- **Framework:** Spring Boot 3.2.0
- **Language:** Java 17
- **Database:** MySQL 8.0
- **Security:** Spring Security + JWT
- **ORM:** Spring Data JPA + Hibernate
- **Migration:** Flyway
- **Testing:** JUnit 5, Mockito
- **Build:** Maven

### Frontend
- **Framework:** React 18
- **Routing:** React Router v6
- **HTTP Client:** Axios
- **Styling:** CSS Modules
- **Markdown:** React-Markdown
- **Syntax Highlighting:** Prism.js
- **Build:** Create React App

### Infrastructure
- **Containerization:** Docker, Docker Compose
- **Orchestration:** Kubernetes
- **CI/CD:** GitHub Actions
- **Monitoring:** Spring Boot Actuator, Prometheus
- **Web Server:** Nginx (frontend)

## Quick Start

### Prerequisites

- Java 17+
- Node.js 18+
- MySQL 8.0+
- Docker & Docker Compose (optional)
- kubectl & Minikube/K8s cluster (optional)

### Local Development

#### 1. Clone the Repository

```bash
git clone https://github.com/username/snipper.git
cd snipper
```

#### 2. Start MySQL

```bash
docker run -d \
  --name snipper-mysql \
  -e MYSQL_ROOT_PASSWORD=rootpassword \
  -e MYSQL_DATABASE=snipper \
  -e MYSQL_USER=snipper_user \
  -e MYSQL_PASSWORD=snipper_password \
  -p 3306:3306 \
  mysql:8.0
```

#### 3. Run Backend

```bash
cd backend
mvn spring-boot:run
```

Backend will start on http://localhost:9090

#### 4. Run Frontend

```bash
cd frontend
npm install
npm start
```

Frontend will start on http://localhost:3000

### Using Docker Compose

```bash
# Build and start all services
docker-compose up --build

# Access the application
# Frontend: http://localhost:3000
# Backend: http://localhost:9090
# MySQL: localhost:3306
```

See [DOCKER_SETUP.md](DOCKER_SETUP.md) for detailed Docker instructions.

### Using Kubernetes

```bash
# Apply all manifests
kubectl apply -f k8s/

# Check deployment status
kubectl get pods -n snipper

# Access the application
kubectl get svc frontend-service -n snipper
```

See [KUBERNETES_SETUP.md](KUBERNETES_SETUP.md) for detailed Kubernetes instructions.

## Project Structure

```
snipper/
├── backend/                    # Spring Boot backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/snipper/
│   │   │   │   ├── config/           # Configuration classes
│   │   │   │   ├── controller/       # REST controllers
│   │   │   │   ├── dto/              # Data Transfer Objects
│   │   │   │   ├── exception/        # Exception handling
│   │   │   │   ├── health/           # Health indicators
│   │   │   │   ├── model/            # JPA entities
│   │   │   │   ├── repository/       # Data repositories
│   │   │   │   ├── security/         # Security configuration
│   │   │   │   └── service/          # Business logic
│   │   │   └── resources/
│   │   │       ├── db/migration/     # Flyway migrations
│   │   │       └── application*.yml  # Configuration files
│   │   └── test/                     # Unit & integration tests
│   ├── Dockerfile
│   └── pom.xml
│
├── frontend/                   # React frontend
│   ├── public/
│   ├── src/
│   │   ├── components/              # React components
│   │   │   ├── auth/                # Authentication components
│   │   │   ├── common/              # Shared components
│   │   │   ├── layout/              # Layout components
│   │   │   └── snippet/             # Snippet components
│   │   ├── contexts/                # React contexts
│   │   ├── pages/                   # Page components
│   │   ├── services/                # API services
│   │   └── App.js
│   ├── Dockerfile
│   ├── nginx.conf
│   └── package.json
│
├── k8s/                        # Kubernetes manifests
│   ├── namespace.yaml
│   ├── configmap.yaml
│   ├── secret.yaml
│   ├── mysql-*.yaml
│   ├── backend-deployment.yaml
│   ├── frontend-deployment.yaml
│   └── hpa.yaml
│
├── .github/workflows/          # CI/CD pipelines
│   ├── backend-ci.yml
│   ├── frontend-ci.yml
│   ├── docker-build.yml
│   └── deploy.yml
│
├── docker-compose.yml
└── README.md
```

## API Documentation

### Authentication Endpoints

```
POST   /api/auth/register    - Register new user
POST   /api/auth/login       - Login user
POST   /api/auth/refresh     - Refresh JWT token
```

### Snippet Endpoints

```
GET    /api/snippets         - Get all snippets (paginated)
GET    /api/snippets/{id}    - Get snippet by ID
POST   /api/snippets         - Create new snippet
PUT    /api/snippets/{id}    - Update snippet
DELETE /api/snippets/{id}    - Delete snippet
GET    /api/snippets/search  - Search snippets
```

### User Endpoints

```
GET    /api/users/profile              - Get current user profile
PUT    /api/users/profile              - Update user profile
GET    /api/users/{username}/snippets  - Get user's snippets
```

### Health Check Endpoints

```
GET    /actuator/health           - Application health status
GET    /actuator/health/liveness  - Liveness probe
GET    /actuator/health/readiness - Readiness probe
GET    /actuator/metrics          - Application metrics
GET    /actuator/prometheus       - Prometheus metrics
```

## Configuration

### Backend Configuration

Edit `backend/src/main/resources/application-dev.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/snipper
    username: snipper_user
    password: snipper_password
  
  security:
    jwt:
      secret: your-secret-key-min-256-bits
      expiration: 86400000  # 24 hours
```

### Frontend Configuration

Edit `frontend/package.json`:

```json
{
  "proxy": "http://localhost:9090"
}
```

Or set environment variable:
```bash
REACT_APP_API_URL=http://localhost:9090
```

## Testing

### Backend Tests

```bash
cd backend

# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UserServiceTest

# Run with coverage
mvn test jacoco:report
```

### Frontend Tests

```bash
cd frontend

# Run all tests
npm test

# Run with coverage
npm test -- --coverage

# Run specific test
npm test -- SnippetEditor.test.js
```

## Deployment

### Docker Deployment

See [DOCKER_SETUP.md](DOCKER_SETUP.md) for complete Docker deployment guide.

### Kubernetes Deployment

See [KUBERNETES_SETUP.md](KUBERNETES_SETUP.md) for complete Kubernetes deployment guide.

### CI/CD Pipeline

See [CI_CD_GUIDE.md](CI_CD_GUIDE.md) for CI/CD pipeline documentation.

## Monitoring

### Health Checks

```bash
# Backend health
curl http://localhost:9090/actuator/health

# Frontend health
curl http://localhost:3000/
```

### Metrics

```bash
# Application metrics
curl http://localhost:9090/actuator/metrics

# Prometheus metrics
curl http://localhost:9090/actuator/prometheus
```

See [MONITORING_GUIDE.md](MONITORING_GUIDE.md) for complete monitoring setup.

## Development

### Backend Development

```bash
cd backend

# Run in development mode
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Build JAR
mvn clean package

# Skip tests
mvn clean package -DskipTests
```

### Frontend Development

```bash
cd frontend

# Install dependencies
npm install

# Start development server
npm start

# Build for production
npm run build

# Run linter
npm run lint

# Fix linting issues
npm run lint:fix
```

### Database Migrations

Flyway migrations are in `backend/src/main/resources/db/migration/`

```bash
# Migrations run automatically on startup

# To create a new migration:
# 1. Create file: V{version}__Description.sql
# 2. Example: V4__Add_user_avatar.sql
```

## Troubleshooting

### Backend Issues

**Port already in use:**
```bash
# Find process using port 9090
lsof -i :9090  # macOS/Linux
netstat -ano | findstr :9090  # Windows

# Kill the process
kill -9 <PID>  # macOS/Linux
taskkill /PID <PID> /F  # Windows
```

**Database connection failed:**
```bash
# Check MySQL is running
docker ps | grep mysql

# Check connection
mysql -h localhost -u snipper_user -p
```

### Frontend Issues

**Module not found:**
```bash
# Clear cache and reinstall
rm -rf node_modules package-lock.json
npm install
```

**Build fails:**
```bash
# Clear build cache
rm -rf build
npm run build
```

### Docker Issues

**Container won't start:**
```bash
# Check logs
docker logs snipper-backend
docker logs snipper-frontend
docker logs snipper-mysql

# Restart containers
docker-compose restart
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style

- **Backend:** Follow Java code conventions, use Spring Boot best practices
- **Frontend:** Follow Airbnb React/JavaScript style guide
- **Commits:** Use conventional commits format

### Pull Request Process

1. Update documentation for any new features
2. Add tests for new functionality
3. Ensure all tests pass
4. Update the README if needed
5. Request review from maintainers

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Spring Boot team for the excellent framework
- React team for the amazing library
- Prism.js for syntax highlighting
- React-Markdown for Markdown rendering
- All contributors and users of this project

## Support

- Email: support@snipper.example.com
- Issues: [GitHub Issues](https://github.com/username/snipper/issues)
- Documentation: [Wiki](https://github.com/username/snipper/wiki)
- Discussions: [GitHub Discussions](https://github.com/username/snipper/discussions)

## Roadmap

- [ ] Code collaboration features
- [ ] Snippet versioning
- [ ] Comments and discussions
- [ ] Snippet collections/folders
- [ ] API rate limiting
- [ ] OAuth2 social login
- [ ] Dark mode
- [ ] Mobile app
- [ ] VS Code extension
- [ ] CLI tool

## Authors

- **Your Name** - *Initial work* - [YourGitHub](https://github.com/yourusername)

See also the list of [contributors](https://github.com/username/snipper/contributors) who participated in this project.

---

Made by the Snipper team
