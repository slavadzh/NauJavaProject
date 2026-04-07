package org.dzhabarov.naujavaproject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class NauJavaProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(NauJavaProjectApplication.class, args);
    }

}
