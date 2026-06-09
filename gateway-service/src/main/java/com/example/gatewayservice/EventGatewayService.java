package com.example.gatewayservice;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class EventGatewayService {
    private final EventRepository eventRepo;
    private final RestClient restClient;
    private final Counter eventCounter;

    public EventGatewayService(EventRepository eventRepo, 
                               RestClient.Builder restClientBuilder,
                               @Value("${account.service.url:http://localhost:8081}") String accountServiceUrl,
                               MeterRegistry meterRegistry) {
        this.eventRepo = eventRepo;
        this.restClient = restClientBuilder
                .baseUrl(accountServiceUrl)
                .build();
        this.eventCounter = Counter.builder("gateway.events.processed")
                .description("Number of events processed by the gateway")
                .register(meterRegistry);
    }

    public Event processEvent(Event event) {
        // Idempotency check
        Optional<Event> existing = eventRepo.findById(event.getEventId());
        if (existing.isPresent()) {
            log.info("Duplicate eventId: {}. Returning original.", event.getEventId());
            return existing.get();
        }

        // Validate
        validateEvent(event);

        // Store first
        Event saved = eventRepo.save(event);
        
        // Increment metric
        eventCounter.increment();

        // Notify Account Service (Sync call with Circuit Breaker)
        notifyAccountService(saved);

        return saved;
    }

    private void validateEvent(Event event) {
        if (event.getAmount() == null || event.getAmount().doubleValue() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        if (!List.of("CREDIT", "DEBIT").contains(event.getType())) {
            throw new IllegalArgumentException("Type must be CREDIT or DEBIT");
        }
    }

    @CircuitBreaker(name = "accountService", fallbackMethod = "fallbackAccountService")
    public void notifyAccountService(Event event) {
        restClient.post()
                .uri("/accounts/{accountId}/transactions", event.getAccountId())
                .contentType(MediaType.APPLICATION_JSON)
                .body(event)
                .retrieve()
                .toBodilessEntity();
    }

    public void fallbackAccountService(Event event, Throwable t) {
        log.error("Account service unavailable for event {}: {}", event.getEventId(), t.getMessage());
        throw new RuntimeException("Account Service Unavailable");
    }

    public List<Event> listEventsByAccount(String accountId) {
        return eventRepo.findByAccountIdOrderByEventTimestampAsc(accountId);
    }

    public Optional<Event> getEventById(String id) {
        return eventRepo.findById(id);
    }
}