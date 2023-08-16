# Microservices Design Patterns

1. **Service Registry**

   **Problem:** In microservices network locations, number of instances changes dynamically due to features like auto-scaling.

   **Solution:** Service registry, which is a database of services mapped to their instances and their locations. Service instances are registered with the service registry on startup and deregistered on shutdown.

   In **client-side discovery**, the responsibility of service discovery is placed on the client or consumer of the service. Each client is responsible for querying the _service registry_ or _discovery server_ to locate the appropriate instances of a service.

   In **server-side discovery**, a central component (service registry or discovery server) maintains the registry of available services and their instances. Clients (consumers) send requests to the discovery server to find the appropriate service instance.

2. **API Gateway**

   **Problem:** How do client of microservices-based applications access the individual services?

   **Solution:** As the number of services in the architecture increases, it becomes challenging to manage the APIs and handle requests from external clients. The **API Gateway** design pattern is an architectural approach used in microservices and distributed systems to _centralize_ and manage the entry point for client requests. It acts as a single point of entry for all API calls, providing various features that improve security, scalability, performance, and overall management of the APIs exposed by the underlying services.

   The pattern is similar to the _facade pattern_ from object-oriented design, but it is part of a distributed system _reverse proxy_ or _gateway routing_. It is similar to the facade pattern of Object-Oriented Design, so it provides a single entry point to the APIs with encapsulating the underlying system architecture.

3. **Circuit Breaker**

   **Problem:** When one service synchronously invokes another there is always the possibility that the other service is unavailable or is exhibiting such high latency. This might lead to resource exhaustion, which would make the calling service unable to handle other requests. The failure of one service can potentially cascade to other services throughout the application.

   **Solution:** When the number of consecutive failures crosses a threshold, the circuit breaker trips, and for the duration of a timeout period all attempts to invoke the remote service will fail immediately. After the timeout expires the circuit breaker allows a limited number of test requests to pass through. If those requests succeed the circuit breaker resumes normal operation. Otherwise, if there is a failure the timeout period begins again.
