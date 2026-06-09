package com.example.accountservice;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "account_transaction") // Renamed to avoid conflict with SQL keyword
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String accountId;
    @Column(unique = true) // Ensure idempotency by eventId
    private String eventId;
    private TransactionType type;
    private BigDecimal amount;
    private String currency;
    private Instant eventTimestamp;

    public enum TransactionType {
        CREDIT, DEBIT
    }
}
