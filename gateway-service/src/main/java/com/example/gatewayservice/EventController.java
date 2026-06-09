package com.example.gatewayservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/events")
public class EventController {
    private final EventGatewayService service;
    private final RestClient accountClient;

    public EventController(EventGatewayService service, 
                           RestClient.Builder restClientBuilder,
                           @Value("${account.service.url:http://localhost:8081}") String accountServiceUrl) {
        this.service = service;
        this.accountClient = restClientBuilder.baseUrl(accountServiceUrl).build();
    }

    @PostMapping
    public ResponseEntity<Event> submitEvent(@RequestBody Event event) {
        try {
            Event result = service.processEvent(event);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            if ("Account Service Unavailable".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            }
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEvent(@PathVariable String id) {
        return service.getEventById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<Event> listEvents(@RequestParam String account) {
        return service.listEventsByAccount(account);
    }

    @GetMapping("/balance")
    public ResponseEntity<?> getBalance(@RequestParam String accountId) {
        try {
            return accountClient.get()
                    .uri("/accounts/{accountId}/balance", accountId)
                    .retrieve()
                    .toEntity(Map.class);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Account Service is unreachable"));
        }
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}