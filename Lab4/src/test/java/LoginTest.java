
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Login Tests")
public class LoginTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String LOGIN_URL =
            "https://the-internet.herokuapp.com/login";

    private static final By USERNAME_FIELD = By.id("username");
    private static final By PASSWORD_FIELD = By.id("password");
    private static final By LOGIN_BUTTON = By.cssSelector("button.radius");
    private static final By FLASH_MESSAGE = By.id("flash");

    @BeforeAll
    static void setUp() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--incognito");
        options.addArguments("--disable-notifications");

        options.setExperimentalOption(
                "prefs",
                Map.of(
                        "profile.password_manager_leak_detection", false,
                        "credentials_enable_service", false,
                        "profile.password_manager_enabled", false
                )
        );

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();

        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }


    @Test
    @Order(1)
    @DisplayName("Login with valid username and password")
    void testLoginSuccess() {
        driver.get(LOGIN_URL);

        enterCredentials("tomsmith", "SuperSecretPassword!");
        clickLogin();

        String message = getFlashMessage();

        assertTrue(
                message.contains("You logged into a secure area!"),
                "Expected successful login, but got: " + message
        );
    }


    @Test
    @Order(2)
    @DisplayName("Login with invalid username and password")
    void testLoginFail() {
        driver.get(LOGIN_URL);

        enterCredentials("wronguser", "wrongpassword");
        clickLogin();

        String message = getFlashMessage();

        assertTrue(
                message.toLowerCase().contains("invalid"),
                "Expected login error, but got: " + message
        );
    }


    @ParameterizedTest(
            name = "[{index}] username=''{0}'' password=''{1}'' -> {2}"
    )
    @Order(3)
    @CsvSource({
            "tomsmith, SuperSecretPassword!, success",
            "wronguser, SuperSecretPassword!, error",
            "tomsmith, wrongpassword, error",
            "'', '', error"
    })
    @DisplayName("Multiple login attempts using CsvSource")
    void testLoginWithMultipleParameters(
            String username,
            String password,
            String expectedResult
    ) {
        driver.get(LOGIN_URL);

        username = (username == null) ? "" : username.trim();
        password = (password == null) ? "" : password.trim();

        enterCredentials(username, password);
        clickLogin();

        String message = getFlashMessage();

        if ("success".equals(expectedResult)) {
            assertTrue(
                    message.contains("You logged into a secure area!"),
                    "Expected successful login, but got: " + message
            );
        } else {
            assertTrue(
                    message.toLowerCase().contains("invalid"),
                    "Expected login error, but got: " + message
            );
        }
    }


    private static void enterCredentials(
            String username,
            String password
    ) {
        WebElement usernameField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        USERNAME_FIELD
                )
        );

        usernameField.clear();
        usernameField.sendKeys(username);

        WebElement passwordField = driver.findElement(PASSWORD_FIELD);
        passwordField.clear();
        passwordField.sendKeys(password);
    }


    private static void clickLogin() {
        wait.until(
                ExpectedConditions.elementToBeClickable(LOGIN_BUTTON)
        ).click();
    }


    private static String getFlashMessage() {
        WebElement message = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        FLASH_MESSAGE
                )
        );

        return message.getText().trim();
    }


    @ParameterizedTest(name = "CSV [{index}]: {0} / {1} -> {2}")
    @Order(4)
    @CsvFileSource(
            resources = "/login-data.csv",
            numLinesToSkip = 1
    )
    @DisplayName("Login with data from external CSV file")
    void testLoginWithCSV(
            String username,
            String password,
            String expectedResult
    ) {
        driver.get(LOGIN_URL);

        username = (username == null) ? "" : username.trim();
        password = (password == null) ? "" : password.trim();

        enterCredentials(username, password);
        clickLogin();

        String message = getFlashMessage();

        if ("success".equals(expectedResult)) {
            assertTrue(
                    message.contains("You logged into a secure area!"),
                    "Expected successful login, but got: " + message
            );
        } else {
            assertTrue(
                    message.toLowerCase().contains("invalid"),
                    "Expected login error, but got: " + message
            );
        }
    }


    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
