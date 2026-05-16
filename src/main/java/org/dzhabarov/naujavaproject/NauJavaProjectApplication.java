package org.dzhabarov.naujavaproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Точка входа веб-приложения «Система управления библиотекой»
 */
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class NauJavaProjectApplication {

  /** Запускает Spring Boot приложение */
  public static void main(String[] args) {
    SpringApplication.run(NauJavaProjectApplication.class, args);
  }
}
