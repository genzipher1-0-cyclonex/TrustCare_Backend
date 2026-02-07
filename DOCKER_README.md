# Trust Care - Docker Setup Guide

## Overview
This project includes Docker configuration for both the Spring Boot backend and PostgreSQL database.

## Prerequisites
- Docker Desktop installed on your system
- Docker Compose (included with Docker Desktop)

## Files Created
- `Dockerfile` - Multi-stage build for the Spring Boot backend
- `docker-compose.yml` - Orchestrates both backend and database containers
- `.dockerignore` - Excludes unnecessary files from Docker builds

## Quick Start

### Option 1: Using Docker Compose (Recommended)

1. **Start all services** (database + backend):
   ```bash
   docker-compose up -d
   ```

2. **View logs**:
   ```bash
   docker-compose logs -f
   ```

3. **Stop all services**:
   ```bash
   docker-compose down
   ```

4. **Stop and remove volumes** (deletes database data):
   ```bash
   docker-compose down -v
   ```

### Option 2: Build and Run Backend Only

1. **Build the Docker image**:
   ```bash
   docker build -t trust-care-backend .
   ```

2. **Run the container**:
   ```bash
   docker run -p 8080:8080 trust-care-backend
   ```

## Configuration

### Database Connection
The application is configured to use environment variables for database connection:

- **URL**: `jdbc:postgresql://postgres:5432/trust_care_db`
- **Username**: `trust_care_user`
- **Password**: `trust_care_password`

These can be modified in the `docker-compose.yml` file.

### Ports
- **Backend**: http://localhost:8080
- **PostgreSQL**: localhost:5432

## Useful Commands

### View running containers:
```bash
docker-compose ps
```

### Access backend container shell:
```bash
docker exec -it trust_care_backend sh
```

### Access PostgreSQL shell:
```bash
docker exec -it trust_care_db psql -U trust_care_user -d trust_care_db
```

### Rebuild and restart:
```bash
docker-compose up -d --build
```

### View backend logs only:
```bash
docker-compose logs -f backend
```

### View database logs only:
```bash
docker-compose logs -f postgres
```

## Troubleshooting

### Backend won't start
- Check if PostgreSQL is healthy: `docker-compose ps`
- View logs: `docker-compose logs backend`

### Port already in use
- Change ports in `docker-compose.yml`
- Or stop the conflicting service

### Database connection issues
- Ensure PostgreSQL is running: `docker-compose ps postgres`
- Check credentials in `docker-compose.yml`

## Development Tips

- The backend uses a multi-stage build to optimize image size
- Maven dependencies are cached for faster rebuilds
- Database data persists in a Docker volume named `postgres_data`
- The application runs as a non-root user for security
