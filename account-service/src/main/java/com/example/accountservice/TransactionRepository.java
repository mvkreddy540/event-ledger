package com.example.accountservice;
import org.springframework.data.jpa.repository.JpaRepository;
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    boolean existsByEventId(String eventId);
}
