package com.example.accountservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {
    private final AccountRepository accountRepo;
    private final TransactionRepository transRepo;

    @Transactional
    public void applyTransaction(Transaction tx) {
        if (transRepo.existsByEventId(tx.getEventId())) {
            log.info("Transaction {} already applied, skipping.", tx.getEventId());
            return;
        }

        Account account = accountRepo.findById(tx.getAccountId())
                .orElseGet(() -> accountRepo.save(new Account(tx.getAccountId(), BigDecimal.ZERO)));

        if (tx.getType() == Transaction.TransactionType.CREDIT) {
            account.setBalance(account.getBalance().add(tx.getAmount()));
        } else {
            account.setBalance(account.getBalance().subtract(tx.getAmount()));
        }

        accountRepo.save(account);
        transRepo.save(tx);
        log.info("Applied {} of {} to account {}. New balance: {}", 
            tx.getType(), tx.getAmount(), tx.getAccountId(), account.getBalance());
    }

    public BigDecimal getBalance(String accountId) {
        return accountRepo.findById(accountId)
                .map(Account::getBalance)
                .orElse(BigDecimal.ZERO);
    }

    public Optional<Account> getAccountDetails(String accountId) {
        return accountRepo.findById(accountId);
    }
}
