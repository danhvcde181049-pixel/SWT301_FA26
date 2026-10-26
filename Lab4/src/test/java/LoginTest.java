
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Login Tests")
public class LoginTest {

    static WebDriver driver;
    static WebDriverWait wait;

    @BeforeAll
    static void setUp() {
        WebDriverManager.chromedriver().setup();

        driver = new ChromeDriver();
        driver.manage().window().maximize();

        wait = new WebDriverWait(
                driver, Duration.ofSeconds(10));
    }

    @Test
    @DisplayName("Login successfully with valid credentials")
    void testLoginSuccess() {
        driver.get("https://the-internet.herokuapp.com/login");

        driver.findElement(By.id("username"))
                .sendKeys("tomsmith");

        driver.findElement(By.id("password"))
                .sendKeys("SuperSecretPassword!");

        driver.findElement(
                        By.cssSelector("button[type='submit']"))
                .click();

        WebElement successMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector(".flash.success")));

        assertTrue(
                successMsg.getText().contains(
                        "You logged into a secure area!"),
                "Login success message was not displayed");
    }

    @Test
    @DisplayName("Login fails with invalid credentials")
    void testLoginFail() {
        driver.get("https://the-internet.herokuapp.com/login");

        driver.findElement(By.id("username"))
                .sendKeys("invalid");

        driver.findElement(By.id("password"))
                .sendKeys("wrongpassword");

        driver.findElement(
                        By.cssSelector("button[type='submit']"))
                .click();

        WebElement errorMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector(".flash.error")));

        assertTrue(
                errorMsg.getText().contains(
                        "Your username is invalid!"),
                "Login error message was not displayed");
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
