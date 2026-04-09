package org.dzhabarov.naujavaproject.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthUITest {

    private WebDriver driver;
    private final String baseUrl = "http://localhost:8080";

    @BeforeEach
    public void setUp() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void testLoginAndLogout() throws InterruptedException {
        driver.get(baseUrl + "/login");

        WebElement usernameInput = driver.findElement(By.name("name"));
        WebElement passwordInput = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//button[text()='Войти']"));

        usernameInput.sendKeys("name");
        passwordInput.sendKeys("name");
        loginButton.click();

        Thread.sleep(1000);
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("/books"));

        driver.get(baseUrl + "/logout");

        Thread.sleep(1000);
        String logoutUrl = driver.getCurrentUrl();
        assertTrue(logoutUrl.contains("/login"));
    }
}