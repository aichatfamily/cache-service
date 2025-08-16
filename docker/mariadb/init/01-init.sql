-- Initial setup for cache service database
-- This script runs when the container is first created

-- Grant additional privileges if needed
GRANT ALL PRIVILEGES ON cache_service.* TO 'cache_user'@'%';
FLUSH PRIVILEGES;

-- Set timezone
SET time_zone = '+00:00';

-- Optional: Create some initial configuration
-- ALTER DATABASE cache_service CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;