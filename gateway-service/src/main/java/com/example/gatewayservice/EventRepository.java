package com.example.gatewayservice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, String> {
    List<Event> findByAccountIdOrderByEventTimestampAsc(String accountId);
}
