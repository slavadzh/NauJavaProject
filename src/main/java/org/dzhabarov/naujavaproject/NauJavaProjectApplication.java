package org.dzhabarov.naujavaproject;

import org.dzhabarov.naujavaproject.console.CommandProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Scanner;

@SpringBootApplication
public class NauJavaProjectApplication implements CommandLineRunner {

    @Autowired
    private CommandProcessor commandProcessor;

    public static void main(String[] args) {
        SpringApplication.run(NauJavaProjectApplication.class, args);
    }

    @Override
    public void run(String[] args) {

        Scanner scanner = new Scanner(System.in);

        while (true) {

            System.out.print("> ");

            String input = scanner.nextLine();

            if ("exit".equalsIgnoreCase(input)) {
                break;
            }

            commandProcessor.processCommand(input);
        }
    }

}
