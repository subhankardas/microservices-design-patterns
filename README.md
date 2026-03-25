# Microservices Design Patterns

Learning distributed systems design through practical implementations in Go, Java, and Spring Boot.

---

## 🎯 Problems Solved in Production Systems

In high-scale, distributed environments, microservices face unique challenges regarding availability, communication, and resource management. The patterns implemented in this repository address the following critical production issues:

### 1. Rate Limiter Pattern

- **Resource Exhaustion & DDoS Mitigation:** Prevents a sudden spike in traffic—whether malicious or accidental—from overwhelming backend services, preventing system crashes.
- **Noisy Neighbor Isolation:** In multi-tenant systems, it ensures that one highly active user or client does not consume all available network bandwidth or CPU, keeping performance stable for everyone else.
- **API Monetization & Quotas:** Allows systems to enforce usage limits based on subscription tiers (e.g., limiting a free tier to 100 requests/minute).

### 2. Circuit Breaker Pattern

- **Preventing Cascading Failures:** Stops a single failing downstream service from dragging down the entire system by tying up threads, memory, and database connection pools.
- **Failing Fast:** Instead of forcing clients to wait for network timeouts when a service is struggling, the circuit opens and rejects requests immediately, allowing the struggling service time to recover.
- **Graceful Degradation:** Provides a hook to return fallback data (like a cached response or a default value) when a dependent service is unavailable, maintaining a better user experience.

### 3. Retry with Exponential Backoff

- **Handling Transient Failures:** Automatically retries failed requests to downstream services, which is effective for temporary issues like network glitches, brief service unavailability, or request throttling.
- **Avoiding Thundering Herd:** Implements an exponential backoff strategy—increasing the delay between retries—to prevent a "thundering herd" scenario where multiple clients repeatedly hammer a recovering service, causing it to fail again.
- **Improving System Resilience:** Increases the overall stability and fault tolerance of the system by gracefully handling intermittent failures without manual intervention.

---

## ✅ Completed Work

- [x] **Rate Limiter Pattern**
  - Go implementation with token bucket algorithm
  - Pure Java implementation
  - Spring Boot with Redis distributed rate limiting
- [x] **Circuit Breaker**
  - Go implementation of count-based circuit breaker using a Finite State Machine (FSM)
  - Spring Boot with Resilience4j and Prometheus monitoring
- [x] **Retry with Exponential Backoff**
  - Go implementation
  - Spring Boot with retry and exponential backoff

## 🚧 Planned Work

- [ ] Bulkhead (Isolation)
- [ ] Service Mesh / API Gateway
- [ ] Saga Pattern (Distributed Transactions)
- [ ] Event Sourcing
- [ ] CQRS (Command Query Responsibility Segregation)
- [ ] Cache-Aside Pattern
- [ ] Timeout Pattern
- [ ] Load Balancer
- [ ] Strangler Fig Pattern
- [ ] Blue-Green Deployment

## 📚 Documentation

- [Spring Boot Rate Limiter](patterns/rate-limiter/spring/distributed-rate-limiting/README.md)
- [Spring Boot Circuit Breaker (Resilience4j)](patterns/circuit-breaker/spring/circuit-breaker-resilience4j/README.md)
- [Spring Boot Retry with Exponential Backoff](patterns/retry-with-backoff/spring/retry-exponential-backoff/README.md)
