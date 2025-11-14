package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class BankController {

    private final TransactionService transactionService;
    private final AccountRepository accountRepository;

    @Autowired
    public BankController(TransactionService transactionService, AccountRepository accountRepository) {
        this.transactionService = transactionService;
        this.accountRepository = accountRepository;
    }

    // Endpoint to get all accounts and their balances
    @GetMapping("/accounts")
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    // New Feature: Endpoint to create a new account
    @PostMapping("/accounts")
    public ResponseEntity<?> createAccount(@RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            double initialDeposit = ((Number) request.get("initialDeposit")).doubleValue();
            
            int newAccountId = transactionService.createNewAccount(name, initialDeposit);
            
            return new ResponseEntity<>(
                Map.of("message", "Account created successfully.", "accountId", newAccountId), 
                HttpStatus.CREATED
            );
        } catch (IllegalArgumentException e) {
            // Handle business logic errors (e.g., minimum deposit)
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    // Existing: Endpoint to execute the transfer
    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestBody Map<String, Object> request) {
        try {
            int from = (Integer) request.get("fromAccountId");
            int to = (Integer) request.get("toAccountId");
            double amount = ((Number) request.get("amount")).doubleValue(); 

            String result = transactionService.transferFunds(from, to, amount);
            return ResponseEntity.ok(Map.of("message", result));
            
        } catch (IllegalStateException | IllegalArgumentException e) {
            // Handle business logic errors (Insufficient funds, Account not found)
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
             // Handle unexpected server errors
            return ResponseEntity.internalServerError().body(Map.of("error", "An unexpected server error occurred during transfer."));
        }
    }
}
