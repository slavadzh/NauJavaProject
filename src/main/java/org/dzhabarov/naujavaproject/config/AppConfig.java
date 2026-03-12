package org.dzhabarov.naujavaproject.config;

import jakarta.annotation.PostConstruct;
import org.dzhabarov.naujavaproject.entity.Book;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class AppConfig {

    @Value("${app.name}")
    private String appName;

    @Value("${app.version}")
    private String appVersion;

    @Bean
    public List<Book> bookContainer() {
        return new ArrayList<>();
    }

    @PostConstruct
    public void init() {
        System.out.println("Application: " + appName);
        System.out.println("Version: " + appVersion);
    }
}