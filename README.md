# Event Ledger System

A distributed system composed of two microservices to process financial transaction events.

## Architecture
- **Event Gateway API**: Entry point for transaction events. Handles validation, idempotency, and stores event records.
- **Account Service**: Manages account balances and transaction history.

## Features
- **Idempotency**: Prevents duplicate processing of the same `eventId`.
- **Out-of-order tolerance**: Displays events chronologically based on `eventTimestamp`.
- **Observability**: 
    - Structured JSON logging with Trace IDs.
    - Health checks and custom metrics (`gateway.events.processed`).
- **Resiliency**: Circuit Breaker implemented in Gateway to handle Account Service failures.

## Setup & Run

### Using Docker Compose (Preferred)
```bash
docker-compose up --build
```

### Manual Run
1. Start Account Service:
   ```bash
   cd account-service
   ./mvnw spring-boot:run
   ```
2. Start Event Gateway Service:
   ```bash
   cd gateway-service
   ./mvnw spring-boot:run
   ```

## Testing
Run tests using:
```bash
./mvnw test
```
(Tests cover idempotency, out-of-order processing, and resiliency behavior)

## Resiliency Pattern Choice: Circuit Breaker
I chose the **Circuit Breaker** pattern because it prevents the Gateway from repeatedly calling the Account Service when it is known to be failing. This saves resources and allows the Gateway to fail fast or provide fallback behavior (like returning `503 Service Unavailable`) until the Account Service recovers.
