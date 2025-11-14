package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AccountRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public AccountRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // New Feature: Insert a new account
    public int create(String name, double initialBalance) {
        // Use a simple random number for the ID for the H2 in-memory DB
        int newId = (int) (Math.random() * 89999) + 10000;
        String sql = "INSERT INTO accounts (id, name, balance) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, newId, name, initialBalance);
        return newId;
    }
    
    // Existing: Finds an account by its ID.
    public Optional<Account> findById(int id) {
        String sql = "SELECT id, name, balance FROM accounts WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, (rs, rowNum) -> 
                new Account(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("balance")
                ), id
            ));
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    // Existing: Updates the balance of an account.
    public int updateBalance(int id, double newBalance) {
        String sql = "UPDATE accounts SET balance = ? WHERE id = ?";
        return jdbcTemplate.update(sql, newBalance, id);
    }
    
    // Existing: Gets all accounts.
     public List<Account> findAll() {
        String sql = "SELECT id, name, balance FROM accounts";
        return jdbcTemplate.query(sql, (rs, rowNum) -> 
            new Account(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getDouble("balance")
            )
        );
    }
    
    // Setup method (called from BankApplication)
    public void createTableAndInitialData() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS accounts IF EXISTS");
        jdbcTemplate.execute(
            "CREATE TABLE accounts (" +
            "id INT PRIMARY KEY, name VARCHAR(100), balance DOUBLE NOT NULL)"
        );
        // Initial data for testing transfers
        jdbcTemplate.update("INSERT INTO accounts (id, name, balance) VALUES (101, 'Alice Smith', 1000.00)");
        jdbcTemplate.update("INSERT INTO accounts (id, name, balance) VALUES (102, 'Bob Johnson', 500.00)");
    }
}