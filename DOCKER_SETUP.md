# Docker Setup Guide

This guide explains how to run the Snipper application using Docker and Docker Compose.

## Prerequisites

- Docker (version 20.10 or higher)
- Docker Compose (version 2.0 or higher)

## Quick Start

### 1. Build and Run All Services

```bash
docker-compose up --build
```

This command will:
- Build the backend Spring Boot application
- Build the frontend React application
- Start MySQL database
- Start all services with proper networking

### 2. Access the Application

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:9090
- **MySQL**: localhost:3306

### 3. Stop All Services

```bash
docker-compose down
```

### 4. Stop and Remove Volumes

```bash
docker-compose down -v
```

## Individual Service Commands

### Build Individual Services

```bash
# Build backend only
docker-compose build backend

# Build frontend only
docker-compose build frontend
```

### Run Individual Services

```bash
# Start only MySQL
docker-compose up mysql

# Start backend (requires MySQL)
docker-compose up backend

# Start frontend
docker-compose up frontend
```

## Environment Variables

### Backend Environment Variables

- `SPRING_PROFILES_ACTIVE`: Active Spring profile (default: dev)
- `SPRING_DATASOURCE_URL`: MySQL connection URL
- `SPRING_DATASOURCE_USERNAME`: Database username
- `SPRING_DATASOURCE_PASSWORD`: Database password
- `JWT_SECRET`: Secret key for JWT token generation
- `JWT_EXPIRATION`: JWT token expiration time in milliseconds

### Frontend Environment Variables

The frontend is configured to proxy API requests to the backend at `http://backend:9090`.

## Health Checks

All services include health checks:

- **MySQL**: Checks database connectivity
- **Backend**: Checks Spring Boot Actuator health endpoint
- **Frontend**: Checks Nginx server availability

## Volumes

- `mysql_data`: Persists MySQL database data

## Networks

All services run on the `snipper-network` bridge network for inter-service communication.

## Troubleshooting

### Backend fails to connect to MySQL

Wait for MySQL to be fully initialized. The backend has a health check dependency on MySQL.

### Frontend cannot reach backend

Ensure all services are running:
```bash
docker-compose ps
```

### View logs

```bash
# All services
docker-compose logs

# Specific service
docker-compose logs backend
docker-compose logs frontend
docker-compose logs mysql

# Follow logs
docker-compose logs -f backend
```

### Rebuild from scratch

```bash
docker-compose down -v
docker-compose build --no-cache
docker-compose up
```

## Production Considerations

For production deployment:

1. Change default passwords in `docker-compose.yml`
2. Use environment-specific configuration files
3. Set up proper JWT secret key
4. Configure SSL/TLS certificates
5. Use Docker secrets for sensitive data
6. Set up proper logging and monitoring
7. Configure resource limits for containers

## Development Mode

For development with hot-reload:

1. Run MySQL only via Docker:
   ```bash
   docker-compose up mysql
   ```

2. Run backend locally:
   ```bash
   cd backend
   mvn spring-boot:run
   ```

3. Run frontend locally:
   ```bash
   cd frontend
   npm start
   ```
