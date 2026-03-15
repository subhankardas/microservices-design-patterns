# Distributed Rate Limiting with Spring Boot

A Spring Boot application demonstrating distributed rate limiting using Redis and Bucket4j for microservices architecture.

## Features

- **Distributed Rate Limiting**: Uses Redis as a distributed store to share rate limit state across multiple instances
- **Token Bucket Algorithm**: Implements token bucket rate limiting via Bucket4j library
- **Configurable Limits**: Currently set to 10 requests per minute per client
- **Client Identification**: Supports API key-based or IP-based client identification
- **HTTP Headers**: Returns rate limit information in response headers
- **Logging**: Includes logging for monitoring rate limit events

## Prerequisites

- Java 21 or higher
- Maven 3.6+
- Redis server (can be run via Docker Compose from the infra directory)

## Setup

1. **Clone the repository** (if applicable) and navigate to the project directory:

   ```bash
   cd patterns/rate-limiter/spring/distributed-rate-limiting
   ```

2. **Start Redis** (using Docker Compose from the infra directory):

   ```bash
   cd ../../../infra
   docker-compose up -d redis
   ```

3. **Configure Redis connection** in `src/main/resources/application.yml`:
   ```yaml
   spring:
     data:
       redis:
         host: localhost
         port: 6379
         password: password # Update if different
   ```

## Running the Application

1. **Build the project**:

   ```bash
   mvn clean compile
   ```

2. **Run the application**:

   ```bash
   mvn spring-boot:run
   ```

   The application will start on `http://localhost:8080`

## Testing the Rate Limiter

### Test Endpoint

- **URL**: `GET /hello`
- **Response**: `Hello, World!`

### Rate Limiting Behavior

- Send multiple requests to `/hello` to observe rate limiting
- Use `X-API-KEY` header for client identification, or requests will be identified by IP address
- Response headers include:
  - `X-Rate-Limit-Remaining`: Number of remaining requests
  - `X-Rate-Limit-Retry-After-Seconds`: Wait time when limit exceeded

### Example Requests

```bash
# Test normal request
curl -X GET http://localhost:8080/hello

# Test with API key
curl -X GET http://localhost:8080/hello -H "X-API-KEY: 1234"

# Exceed rate limit (send 15+ requests quickly)
for i in {1..15}; do curl -s http://localhost:8080/hello; done
```

## Architecture

### Components

- **RateLimitFilter**: Servlet filter that intercepts requests and applies rate limiting
- **RateLimitingService**: Service that manages Bucket4j buckets stored in Redis
- **RateLimiterConfig**: Configuration for Redis client and Bucket4j proxy manager
- **HelloController**: Simple controller for testing rate limiting

### How It Works

1. Each incoming request is intercepted by `RateLimitFilter`
2. Client is identified via API key header or IP address
3. A unique bucket is resolved/retrieved from Redis for the client
4. Token consumption is attempted; if successful, request proceeds
5. If rate limit exceeded, HTTP 429 (Too Many Requests) is returned

## Configuration

### Rate Limits

Modify `RateLimitingService.getConfigSupplierForUser()` to change rate limits:

```java
private static final int REQUESTS_PER_MINUTE = 10;  // Change this value

return () -> BucketConfiguration.builder()
        .addLimit(limit -> limit
                .capacity(REQUESTS_PER_MINUTE)
                .refillGreedy(REQUESTS_PER_MINUTE, Duration.ofMinutes(1)))
        .build();
```

### Client Identification

Update `RateLimitFilter.doFilterInternal()` to customize client identification logic.

## Technologies Used

- **Spring Boot 4.0.3**: Framework for building the application
- **Bucket4j 8.10.1**: Rate limiting library
- **Redis**: Distributed data store for rate limit state
- **Lettuce**: Redis client for Java
- **Maven**: Build tool

## Testing

Run the tests:

```bash
mvn test
```

Tests include:

- Context loading test
- Controller endpoint test

## Monitoring

Check application logs for rate limiting events:

- INFO: Allowed requests with remaining tokens
- WARN: Blocked requests with retry time

## Troubleshooting

- **Redis Connection Issues**: Ensure Redis is running and accessible
- **Rate Limits Not Working**: Check Redis connectivity and configuration
- **Build Failures**: Ensure Java 21 and Maven are properly installed

## License

This project is for educational purposes demonstrating distributed rate limiting patterns.
