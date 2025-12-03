package com.example.githubanalyzer.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableAutoConfiguration
@EntityScan(basePackages = "com.example.githubanalyzer.entity")
@EnableJpaRepositories(basePackages = "com.example.githubanalyzer.repository")
@EnableTransactionManagement
public class TestConfig {
    // This is a configuration class for tests
    // It enables JPA repositories and entity scanning without loading the main application
}