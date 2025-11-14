package com.example;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;

@SpringBootApplication
public class BankApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankApplication.class, args);
    }

    // Database Initialization: Calls createTableAndInitialData() after startup
    @Bean
    public CommandLineRunner initDatabase(AccountRepository repository) {
        return args -> {
            repository.createTableAndInitialData();
            System.out.println("Backend: Database initialized with transfer accounts.");
        };
    }
    
    // CORS Configuration (Essential for React to connect to Java)
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("*") 
                        .allowedMethods("GET", "POST");
            }
        };
    }

    /**
     * Defines the DataSource (database connection) for H2.
     */
    @Bean
    public DataSource dataSource() {
        return new DriverManagerDataSource(
            "jdbc:h2:mem:bankdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE", 
            "sa", 
            ""
        );
    }
}