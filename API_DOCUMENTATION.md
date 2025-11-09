# API Documentation

Complete API reference for the Snipper backend REST API.

## Base URL

```
Development: http://localhost:9090/api
Production: https://api.snipper.example.com/api
```

## Authentication

Most endpoints require JWT authentication. Include the token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

## Response Format

### Success Response

```json
{
  "data": { ... },
  "timestamp": "2024-01-01T12:00:00Z"
}
```

### Error Response

```json
{
  "timestamp": "2024-01-01T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/snippets"
}
```

## Authentication Endpoints

### Register User

Create a new user account.

**Endpoint:** `POST /api/auth/register`

**Request Body:**
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "fullName": "John Doe"
}
```

**Response:** `201 Created`
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer",
  "user": {
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com"
  }
}
```

**Errors:**
- `400` - Validation error (username/email already exists)
- `500` - Server error

### Login

Authenticate and receive JWT tokens.

**Endpoint:** `POST /api/auth/login`

**Request Body:**
```json
{
  "username": "johndoe",
  "password": "SecurePass123!"
}
```

**Response:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer",
  "user": {
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com"
  }
}
```

**Errors:**
- `401` - Invalid credentials
- `500` - Server error

### Refresh Token

Get a new access token using refresh token.

**Endpoint:** `POST /api/auth/refresh`

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

**Response:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer",
  "user": {
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com"
  }
}
```

**Errors:**
- `401` - Invalid or expired refresh token
- `500` - Server error

## Snippet Endpoints

### Get All Snippets

Retrieve paginated list of snippets.

**Endpoint:** `GET /api/snippets`

**Query Parameters:**
- `page` (optional) - Page number (default: 0)
- `size` (optional) - Page size (default: 20)
- `sort` (optional) - Sort field (default: createdAt)
- `language` (optional) - Filter by language
- `visibility` (optional) - Filter by visibility (PUBLIC/PRIVATE)
- `tags` (optional) - Filter by tag

**Example:**
```
GET /api/snippets?page=0&size=10&language=javascript&visibility=PUBLIC
```

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "title": "React Hook Example",
      "description": "Custom React hook for API calls",
      "language": "javascript",
      "visibility": "PUBLIC",
      "tags": ["react", "hooks", "javascript"],
      "authorUsername": "johndoe",
      "createdAt": "2024-01-01T12:00:00Z",
      "updatedAt": "2024-01-01T12:00:00Z"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 100,
  "totalPages": 10
}
```

**Errors:**
- `500` - Server error

### Get Snippet by ID

Retrieve a specific snippet.

**Endpoint:** `GET /api/snippets/{id}`

**Response:** `200 OK`
```json
{
  "id": 1,
  "title": "React Hook Example",
  "description": "Custom React hook for API calls",
  "content": "import { useState, useEffect } from 'react';\n\nfunction useApi(url) {\n  ...\n}",
  "language": "javascript",
  "visibility": "PUBLIC",
  "tags": ["react", "hooks", "javascript"],
  "authorUsername": "johndoe",
  "createdAt": "2024-01-01T12:00:00Z",
  "updatedAt": "2024-01-01T12:00:00Z"
}
```

**Errors:**
- `404` - Snippet not found
- `403` - Access denied (private snippet)
- `500` - Server error

### Create Snippet

Create a new code snippet.

**Endpoint:** `POST /api/snippets`

**Authentication:** Required

**Request Body:**
```json
{
  "title": "React Hook Example",
  "description": "Custom React hook for API calls",
  "content": "import { useState, useEffect } from 'react';\n\nfunction useApi(url) {\n  ...\n}",
  "language": "javascript",
  "visibility": "PUBLIC",
  "tags": ["react", "hooks", "javascript"]
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "title": "React Hook Example",
  "description": "Custom React hook for API calls",
  "content": "import { useState, useEffect } from 'react';\n\nfunction useApi(url) {\n  ...\n}",
  "language": "javascript",
  "visibility": "PUBLIC",
  "tags": ["react", "hooks", "javascript"],
  "authorUsername": "johndoe",
  "createdAt": "2024-01-01T12:00:00Z",
  "updatedAt": "2024-01-01T12:00:00Z"
}
```

**Errors:**
- `400` - Validation error
- `401` - Unauthorized
- `500` - Server error

### Update Snippet

Update an existing snippet.

**Endpoint:** `PUT /api/snippets/{id}`

**Authentication:** Required (must be owner)

**Request Body:**
```json
{
  "title": "Updated React Hook Example",
  "description": "Updated description",
  "content": "// Updated code",
  "language": "javascript",
  "visibility": "PRIVATE",
  "tags": ["react", "hooks"]
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "title": "Updated React Hook Example",
  "description": "Updated description",
  "content": "// Updated code",
  "language": "javascript",
  "visibility": "PRIVATE",
  "tags": ["react", "hooks"],
  "authorUsername": "johndoe",
  "createdAt": "2024-01-01T12:00:00Z",
  "updatedAt": "2024-01-02T12:00:00Z"
}
```

**Errors:**
- `400` - Validation error
- `401` - Unauthorized
- `403` - Not the owner
- `404` - Snippet not found
- `500` - Server error

### Delete Snippet

Delete a snippet.

**Endpoint:** `DELETE /api/snippets/{id}`

**Authentication:** Required (must be owner)

**Response:** `204 No Content`

**Errors:**
- `401` - Unauthorized
- `403` - Not the owner
- `404` - Snippet not found
- `500` - Server error

### Search Snippets

Search snippets by title, description, or tags.

**Endpoint:** `GET /api/snippets/search`

**Query Parameters:**
- `q` (required) - Search query
- `page` (optional) - Page number (default: 0)
- `size` (optional) - Page size (default: 20)
- `language` (optional) - Filter by language
- `visibility` (optional) - Filter by visibility

**Example:**
```
GET /api/snippets/search?q=react&language=javascript
```

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "title": "React Hook Example",
      "description": "Custom React hook for API calls",
      "language": "javascript",
      "visibility": "PUBLIC",
      "tags": ["react", "hooks", "javascript"],
      "authorUsername": "johndoe",
      "createdAt": "2024-01-01T12:00:00Z",
      "updatedAt": "2024-01-01T12:00:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 5,
  "totalPages": 1
}
```

**Errors:**
- `400` - Invalid query
- `500` - Server error

## User Endpoints

### Get Current User Profile

Get the authenticated user's profile.

**Endpoint:** `GET /api/users/profile`

**Authentication:** Required

**Response:** `200 OK`
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "fullName": "John Doe",
  "createdAt": "2024-01-01T12:00:00Z",
  "updatedAt": "2024-01-01T12:00:00Z"
}
```

**Errors:**
- `401` - Unauthorized
- `500` - Server error

### Update User Profile

Update the authenticated user's profile.

**Endpoint:** `PUT /api/users/profile`

**Authentication:** Required

**Request Body:**
```json
{
  "username": "johndoe",
  "email": "john.doe@example.com",
  "fullName": "John Doe Jr."
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john.doe@example.com",
  "fullName": "John Doe Jr.",
  "createdAt": "2024-01-01T12:00:00Z",
  "updatedAt": "2024-01-02T12:00:00Z"
}
```

**Errors:**
- `400` - Validation error
- `401` - Unauthorized
- `500` - Server error

### Get User's Snippets

Get snippets created by a specific user.

**Endpoint:** `GET /api/users/{username}/snippets`

**Query Parameters:**
- `page` (optional) - Page number (default: 0)
- `size` (optional) - Page size (default: 20)
- `sort` (optional) - Sort field (default: createdAt)

**Example:**
```
GET /api/users/johndoe/snippets?page=0&size=10
```

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "title": "React Hook Example",
      "description": "Custom React hook for API calls",
      "language": "javascript",
      "visibility": "PUBLIC",
      "tags": ["react", "hooks", "javascript"],
      "authorUsername": "johndoe",
      "createdAt": "2024-01-01T12:00:00Z",
      "updatedAt": "2024-01-01T12:00:00Z"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 25,
  "totalPages": 3
}
```

**Errors:**
- `404` - User not found
- `500` - Server error

## Health Check Endpoints

### Application Health

Check application health status.

**Endpoint:** `GET /actuator/health`

**Response:** `200 OK`
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

### Liveness Probe

Kubernetes liveness probe endpoint.

**Endpoint:** `GET /actuator/health/liveness`

**Response:** `200 OK`
```json
{
  "status": "UP"
}
```

### Readiness Probe

Kubernetes readiness probe endpoint.

**Endpoint:** `GET /actuator/health/readiness`

**Response:** `200 OK`
```json
{
  "status": "UP"
}
```

### Application Metrics

Get application metrics.

**Endpoint:** `GET /actuator/metrics`

**Response:** `200 OK`
```json
{
  "names": [
    "jvm.memory.used",
    "jvm.memory.max",
    "http.server.requests",
    "system.cpu.usage",
    "process.uptime"
  ]
}
```

### Specific Metric

Get a specific metric.

**Endpoint:** `GET /actuator/metrics/{metricName}`

**Example:**
```
GET /actuator/metrics/http.server.requests
```

**Response:** `200 OK`
```json
{
  "name": "http.server.requests",
  "measurements": [
    {
      "statistic": "COUNT",
      "value": 1234
    },
    {
      "statistic": "TOTAL_TIME",
      "value": 45.678
    }
  ]
}
```

## Rate Limiting

API rate limits (future implementation):
- Authenticated users: 1000 requests/hour
- Unauthenticated users: 100 requests/hour

Rate limit headers:
```
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 999
X-RateLimit-Reset: 1640995200
```

## Pagination

All list endpoints support pagination with these parameters:
- `page` - Page number (0-indexed)
- `size` - Items per page (max: 100)
- `sort` - Sort field and direction (e.g., `createdAt,desc`)

Response includes pagination metadata:
```json
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5,
  "first": true,
  "last": false
}
```

## Error Codes

| Code | Description |
|------|-------------|
| 200 | Success |
| 201 | Created |
| 204 | No Content |
| 400 | Bad Request - Validation error |
| 401 | Unauthorized - Authentication required |
| 403 | Forbidden - Insufficient permissions |
| 404 | Not Found - Resource doesn't exist |
| 409 | Conflict - Resource already exists |
| 429 | Too Many Requests - Rate limit exceeded |
| 500 | Internal Server Error |
| 503 | Service Unavailable |

## Examples

### cURL Examples

**Register:**
```bash
curl -X POST http://localhost:9090/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "SecurePass123!",
    "fullName": "John Doe"
  }'
```

**Login:**
```bash
curl -X POST http://localhost:9090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "SecurePass123!"
  }'
```

**Create Snippet:**
```bash
curl -X POST http://localhost:9090/api/snippets \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "title": "Hello World",
    "description": "Simple hello world",
    "content": "console.log(\"Hello World\");",
    "language": "javascript",
    "visibility": "PUBLIC",
    "tags": ["javascript", "tutorial"]
  }'
```

**Get Snippets:**
```bash
curl http://localhost:9090/api/snippets?page=0&size=10
```

### JavaScript/Axios Examples

**Login:**
```javascript
const response = await axios.post('/api/auth/login', {
  username: 'johndoe',
  password: 'SecurePass123!'
});

const { token } = response.data;
localStorage.setItem('token', token);
```

**Create Snippet:**
```javascript
const response = await axios.post('/api/snippets', {
  title: 'Hello World',
  description: 'Simple hello world',
  content: 'console.log("Hello World");',
  language: 'javascript',
  visibility: 'PUBLIC',
  tags: ['javascript', 'tutorial']
}, {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
```

**Get Snippets:**
```javascript
const response = await axios.get('/api/snippets', {
  params: {
    page: 0,
    size: 10,
    language: 'javascript'
  }
});

const snippets = response.data.content;
```

## Postman Collection

Import the Postman collection for easy API testing:

[Download Postman Collection](./postman_collection.json)

## OpenAPI/Swagger

Access the interactive API documentation:

```
http://localhost:9090/swagger-ui.html
```

(Note: Swagger UI needs to be configured in the backend)
