package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private final AccountRepository repository;

    @Autowired
    public TransactionService(AccountRepository repository) {
        this.repository = repository;
    }

    // New Feature: Service method for creating an account
    public int createNewAccount(String name, double initialDeposit) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Account name cannot be empty.");
        }
        if (initialDeposit < 100.00) {
            // Business rule: require minimum deposit of $100
            throw new IllegalArgumentException("Minimum initial deposit is $100.00.");
        }
        return repository.create(name, initialDeposit);
    }

    /**
     * Executes a money transfer using Spring's @Transactional management.
     */
    @Transactional
    public String transferFunds(int fromAccountId, int toAccountId, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive.");
        }
        
        // 1. Check if source account exists and has sufficient funds
        Account sourceAccount = repository.findById(fromAccountId)
            .orElseThrow(() -> new IllegalArgumentException("Source account not found: " + fromAccountId));

        if (sourceAccount.balance() < amount) {
            throw new IllegalStateException("Insufficient funds in account: " + fromAccountId);
        }

        // 2. Check if target account exists
        repository.findById(toAccountId)
            .orElseThrow(() -> new IllegalArgumentException("Target account not found: " + toAccountId));

        // 3. Perform withdrawal
        repository.updateBalance(fromAccountId, sourceAccount.balance() - amount);
        
        // 4. Perform deposit
        Account targetAccount = repository.findById(toAccountId).get(); 
        repository.updateBalance(toAccountId, targetAccount.balance() + amount);
        
        return "Transfer successful.";
    }
}