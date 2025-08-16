# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Spring Boot cache service with MariaDB persistence. Provides REST API for key-value caching with TTL support.

## Development Commands

```bash
# Build the application
./gradlew build

# Run the application
./gradlew bootRun

# Run tests
./gradlew test

# Run tests continuously
./gradlew test --continuous

# Clean build
./gradlew clean build

# Generate wrapper (if needed)
gradle wrapper
```

## Database Setup

### Docker Setup (Recommended)
```bash
# Start MariaDB container
docker-compose up -d

# Stop containers
docker-compose down

# View logs
docker-compose logs mariadb

# Run application with Docker profile
./gradlew bootRun --args='--spring.profiles.active=docker'
```

### Manual MariaDB Setup (Alternative)
```sql
CREATE DATABASE cache_service;
CREATE USER 'cache_user'@'localhost' IDENTIFIED BY 'cache_password';
GRANT ALL PRIVILEGES ON cache_service.* TO 'cache_user'@'localhost';
FLUSH PRIVILEGES;
```

## Architecture

**Spring Boot application** with layered architecture:
- **Controller Layer**: REST endpoints (`/api/cache`)
- **Service Layer**: Business logic with TTL management and cleanup
- **Repository Layer**: JPA repositories with custom queries
- **Entity Layer**: JPA entities with MariaDB persistence

**Key Features**:
- TTL-based expiration with automatic cleanup (5-minute intervals)
- MariaDB persistence with JPA/Hibernate
- RESTful API for cache operations (GET, POST, DELETE)
- Lombok for reduced boilerplate

## API Endpoints

```
GET    /api/cache/{key}        - Retrieve cache entry
POST   /api/cache/{key}        - Store cache entry (with optional TTL)
DELETE /api/cache/{key}        - Delete cache entry  
GET    /api/cache/{key}/exists - Check if key exists
```

## Project Structure

```
src/main/java/com/cacheservice/
├── CacheServiceApplication.java     - Main Spring Boot class
├── controller/CacheController.java  - REST API endpoints
├── service/CacheService.java        - Business logic & cleanup
├── repository/CacheRepository.java  - Data access layer
└── entity/CacheEntry.java          - JPA entity
```