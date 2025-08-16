# Cache Service

A Spring Boot REST API cache service with **Redis caching** and **MariaDB persistence** using Docker Compose.

## Architecture

- **Redis**: Primary cache layer for fast data access (TTL and persistent entries)
- **MariaDB**: Persistent storage and fallback when Redis is unavailable
- **Spring Cache Abstraction**: Declarative caching with automatic fallback

## Quick Start

1. **Start Redis and MariaDB:**
   ```bash
   docker-compose up -d
   ```

2. **Run the application:**
   ```bash
   ./gradlew bootRun
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

## Redis Connection

### Connection Details
- **Host:** localhost
- **Port:** 6379
- **Password:** none (development setup)
- **Database:** 0 (default)

### Connect via Docker
```bash
# Access Redis CLI through container
docker exec -it cache-service-redis redis-cli

# Connect to specific database
docker exec -it cache-service-redis redis-cli -n 0

# Execute single command
docker exec -it cache-service-redis redis-cli KEYS "*"
```

### Connect via Command Line
```bash
# Local redis-cli (if installed)
redis-cli -h localhost -p 6379

# Connect to specific database
redis-cli -h localhost -p 6379 -n 0
```

### GUI Redis Tools
- **RedisInsight** (free, official Redis GUI)
- **Redis Desktop Manager** 
- **Medis** (macOS)
- **Another Redis Desktop Manager** (cross-platform)

**Connection settings:**
- Host: `localhost`
- Port: `6379`
- Auth: none
- Name: `cache-service`

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

### Docker Services
```bash
# Start all services (Redis + MariaDB)
docker-compose up -d

# Start specific service
docker-compose up -d redis
docker-compose up -d mariadb

# Stop all services
docker-compose down

# Stop and remove volumes (data reset)
docker-compose down -v

# View logs
docker-compose logs redis
docker-compose logs mariadb
docker-compose logs -f  # Follow logs for all services

# Check service status
docker-compose ps

# Restart services
docker-compose restart redis
docker-compose restart mariadb
```

### Application Build
```bash
# Build application
./gradlew build

# Run tests
./gradlew test

# Clean build
./gradlew clean build

# Run application
./gradlew bootRun
```

### Quick Debug Commands
```bash
# Check Redis connectivity
docker exec cache-service-redis redis-cli ping

# Check MariaDB connectivity  
docker exec cache-service-mariadb mysqladmin ping -h localhost

# View current cache keys
docker exec cache-service-redis redis-cli KEYS "*"

# View database tables
docker exec -it cache-service-mariadb mysql -u cache_user -p cache_service -e "SHOW TABLES;"
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

## Redis Management

### View Cache Data
```bash
# Connect to Redis first, then:

# View all cache keys
KEYS "*"

# View keys by pattern
KEYS "cache-entries::*"  # Spring cache entries
KEYS "ttl:*"             # TTL cache entries

# Get specific values
GET "cache-entries::mykey"
GET "ttl:session123"

# Check TTL for keys
TTL "ttl:session123"     # Time to live in seconds
PTTL "ttl:session123"    # Time to live in milliseconds

# Get key information
TYPE "cache-entries::mykey"
EXISTS "ttl:session123"
```

### Cache Manipulation
```bash
# Manually set cache values
SET "ttl:test" "manual_value" EX 300    # With 5-minute expiry
SET "cache-entries::test" "persistent"  # Without expiry

# Delete specific keys
DEL "ttl:session123"
DEL "cache-entries::oldkey"

# Delete by pattern (be careful!)
EVAL "return redis.call('del', unpack(redis.call('keys', ARGV[1])))" 0 "ttl:*"

# Set expiration on existing key
EXPIRE "cache-entries::temp" 300  # 5 minutes
```

### Monitor Cache Performance
```bash
# Real-time monitoring
MONITOR  # Shows all Redis commands in real-time (Ctrl+C to stop)

# Get Redis info
INFO memory     # Memory usage
INFO clients    # Client connections
INFO stats      # General statistics
INFO keyspace   # Database statistics

# Check specific database
INFO keyspace
```

### Cache Maintenance
```bash
# Clear specific cache regions
FLUSHDB         # Clear current database (0)
FLUSHALL        # Clear all databases (use with caution!)

# Clear by pattern (safer)
# TTL cache only
EVAL "return redis.call('del', unpack(redis.call('keys', ARGV[1])))" 0 "ttl:*"

# Spring cache only  
EVAL "return redis.call('del', unpack(redis.call('keys', ARGV[1])))" 0 "cache-entries::*"

# Memory usage optimization
MEMORY USAGE "cache-entries::mykey"  # Check key memory usage
```

### Useful Docker Commands
```bash
# Redis container logs
docker-compose logs redis

# Redis container statistics
docker exec cache-service-redis redis-cli INFO stats

# Backup Redis data
docker exec cache-service-redis redis-cli --rdb /data/backup.rdb

# Check Redis configuration
docker exec cache-service-redis redis-cli CONFIG GET "*"
```

## Troubleshooting

### Redis Connection Issues
1. **Check Redis container status:**
   ```bash
   docker-compose ps redis
   docker exec cache-service-redis redis-cli ping
   ```

2. **Verify Redis port availability:**
   ```bash
   netstat -an | grep 6379
   lsof -i :6379  # macOS/Linux
   ```

3. **Check Redis logs:**
   ```bash
   docker-compose logs redis
   docker-compose logs -f redis  # Follow logs
   ```

4. **Test Redis connectivity:**
   ```bash
   # From host
   redis-cli -h localhost -p 6379 ping
   
   # From container
   docker exec cache-service-redis redis-cli ping
   ```

### Database Connection Issues
1. **Check MariaDB container status:**
   ```bash
   docker-compose ps mariadb
   docker exec cache-service-mariadb mysqladmin ping -h localhost
   ```

2. **Verify MariaDB port availability:**
   ```bash
   netstat -an | grep 3306
   ```

3. **Check MariaDB logs:**
   ```bash
   docker-compose logs mariadb
   ```

### Application Issues
1. **Check service health:**
   ```bash
   docker-compose ps  # All services status
   ```

2. **Check application logs for cache errors:**
   ```bash
   # Look for Redis connection failures
   grep -i "redis" application.log
   
   # Look for database connection issues  
   grep -i "mysql\|mariadb" application.log
   ```

3. **Verify cache functionality:**
   ```bash
   # Test Redis cache directly
   docker exec cache-service-redis redis-cli KEYS "*"
   
   # Test database directly
   docker exec -it cache-service-mariadb mysql -u cache_user -p cache_service -e "SELECT COUNT(*) FROM cache_entries;"
   ```

### Cache Performance Issues
1. **Monitor Redis memory usage:**
   ```bash
   docker exec cache-service-redis redis-cli INFO memory
   ```

2. **Check cache hit/miss ratios:**
   ```bash
   docker exec cache-service-redis redis-cli INFO stats
   ```

3. **Monitor slow Redis commands:**
   ```bash
   docker exec cache-service-redis redis-cli SLOWLOG GET 10
   ```

### Port Conflicts
**Redis port 6379 conflict:**
```yaml
# Edit docker-compose.yml
redis:
  ports:
    - "6380:6379"  # Use different host port
```

**MariaDB port 3306 conflict:**
```yaml
# Edit docker-compose.yml  
mariadb:
  ports:
    - "3307:3306"  # Use different host port
```

Then update connection strings accordingly.

### Data Consistency Issues
1. **Check for cache/DB sync issues:**
   ```bash
   # Compare Redis keys with DB entries
   docker exec cache-service-redis redis-cli KEYS "cache-entries::*"
   docker exec -it cache-service-mariadb mysql -u cache_user -p cache_service -e "SELECT cache_key FROM cache_entries;"
   ```

2. **Clear cache and force DB reload:**
   ```bash
   # Clear Redis cache
   docker exec cache-service-redis redis-cli FLUSHDB
   
   # Restart application to reload from DB
   # (Application will rebuild cache from database)
   ```

## Project Structure

```
├── src/main/java/com/cacheservice/
│   ├── CacheServiceApplication.java     # Main Spring Boot application
│   ├── config/CacheConfig.java          # Redis cache abstraction config
│   ├── controller/CacheController.java  # REST API endpoints
│   ├── service/CacheService.java        # Hybrid cache logic (Redis + DB)
│   ├── repository/CacheRepository.java  # MariaDB data access layer
│   └── entity/CacheEntry.java          # JPA entity for persistent storage
├── src/main/resources/
│   └── application.yml                  # App config (Redis + MariaDB)
├── docker-compose.yml                   # Redis + MariaDB containers
└── README.md                           # This documentation
```

## Cache Architecture Details

### Cache Strategy
- **TTL entries**: `ttl:{key}` → Direct Redis storage with expiration
- **Persistent entries**: `cache-entries::{key}` → Spring cache (Redis) + DB storage
- **Fallback**: MariaDB when Redis is unavailable

### Data Flow
1. **PUT with TTL**: Store in DB + Redis (`ttl:{key}`)
2. **PUT without TTL**: Store in DB + Redis (`cache-entries::{key}`)
3. **GET**: Check `ttl:{key}` → Check `cache-entries::{key}` → Query DB → Cache result
4. **DELETE**: Remove from both Redis patterns + DB

### Key Components
- **CacheConfig**: Spring cache abstraction with Redis backend
- **CacheService**: Hybrid cache logic with automatic fallback
- **RedisTemplate**: Direct Redis operations for TTL cache
- **Spring Cache**: Declarative caching for persistent entries