# Cache Service

A Spring Boot REST API cache service with MariaDB persistence and Docker Compose setup.

## Quick Start

1. **Start the database:**
   ```bash
   docker-compose up -d
   ```

2. **Run the application:**
   ```bash
   ./gradlew bootRun --args='--spring.profiles.active=docker'
   ```

3. **Test the API:**
   ```bash
   # Store a value
   curl -X POST localhost:8080/api/cache/mykey \
     -H "Content-Type: application/json" \
     -d '{"value": "Hello World", "ttl": 300}'
   
   # Retrieve a value
   curl localhost:8080/api/cache/mykey
   ```

## Database Connection

### Connection Details
- **Host:** localhost
- **Port:** 3306
- **Database:** cache_service
- **Username:** cache_user
- **Password:** cache_password
- **Root Password:** root_password

### Connect via Command Line
```bash
# As application user
mysql -h localhost -P 3306 -u cache_user -p cache_service

# As root user (for admin tasks)
mysql -h localhost -P 3306 -u root -p
```

### Connect via Docker
```bash
# Direct container access
docker exec -it cache-service-mariadb mysql -u cache_user -p cache_service

# Root access
docker exec -it cache-service-mariadb mysql -u root -p
```

### GUI Database Tools
Use any MySQL-compatible client:
- **DBeaver** (free, cross-platform)
- **MySQL Workbench** 
- **phpMyAdmin**
- **Sequel Pro** (macOS)

**Connection settings:**
- Host: `localhost`
- Port: `3306` 
- Database: `cache_service`
- Username: `cache_user`
- Password: `cache_password`

## API Endpoints

| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|--------------|
| POST | `/api/cache/{key}` | Store cache entry | `{"value": "data", "ttl": 300}` |
| GET | `/api/cache/{key}` | Retrieve cache entry | - |
| DELETE | `/api/cache/{key}` | Delete cache entry | - |
| GET | `/api/cache/{key}/exists` | Check if key exists | - |

### Examples

```bash
# Store with TTL (expires in 5 minutes)
curl -X POST localhost:8080/api/cache/session123 \
  -H "Content-Type: application/json" \
  -d '{"value": "user_data", "ttl": 300}'

# Store without expiration
curl -X POST localhost:8080/api/cache/config \
  -H "Content-Type: application/json" \
  -d '{"value": "app_settings"}'

# Retrieve
curl localhost:8080/api/cache/session123

# Check existence
curl localhost:8080/api/cache/session123/exists

# Delete
curl -X DELETE localhost:8080/api/cache/session123
```

## Development Commands

```bash
# Start database only
docker-compose up -d

# Stop database
docker-compose down

# View database logs
docker-compose logs mariadb

# Build application
./gradlew build

# Run tests
./gradlew test

# Clean build
./gradlew clean build
```

## Database Management

### View Cache Data
```sql
-- Connect to database first, then:
SELECT * FROM cache_entries;
SELECT * FROM cache_entries WHERE expires_at > NOW();
```

### Manual Cleanup
```sql
-- Remove expired entries
DELETE FROM cache_entries WHERE expires_at < NOW();
```

### Reset Database
```bash
# Stop containers and remove volumes
docker-compose down -v

# Start fresh
docker-compose up -d
```

## Troubleshooting

### Database Connection Issues
1. Check container status: `docker-compose ps`
2. Verify port availability: `netstat -an | grep 3306`
3. Check logs: `docker-compose logs mariadb`

### Application Issues
1. Ensure Docker profile is active: `--spring.profiles.active=docker`
2. Check database connectivity from application logs
3. Verify MariaDB container is healthy

### Port Conflicts
If port 3306 is already in use:
```yaml
# Edit docker-compose.yml
ports:
  - "3307:3306"  # Use different host port
```

Then update connection strings to use port 3307.

## Project Structure

```
├── src/main/java/com/cacheservice/
│   ├── CacheServiceApplication.java
│   ├── controller/CacheController.java
│   ├── service/CacheService.java
│   ├── repository/CacheRepository.java
│   └── entity/CacheEntry.java
├── src/main/resources/
│   ├── application.yml
│   └── application-docker.yml
├── docker-compose.yml
└── docker/mariadb/init/01-init.sql
```