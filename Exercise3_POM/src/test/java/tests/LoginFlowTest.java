package tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import pages.LoginPage;
import pages.SecureAreaPage;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("End-to-end login and logout flow")
class LoginFlowTest extends BaseTest {

    @Test
    @Tag("smoke")
    @DisplayName("User can login and then logout")
    void loginThenLogout() {

        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigate();

        SecureAreaPage secure = loginPage.loginAs(
                "tomsmith",
                "SuperSecretPassword!"
        );

        assertAll("Secure area",
                () -> assertEquals(
                        "Secure Area",
                        secure.getHeading()
                ),
                () -> assertTrue(
                        secure.getFlashMessage()
                                .contains("You logged into a secure area!")
                )
        );

        LoginPage returnedLoginPage = secure.logout();

        assertTrue(
                returnedLoginPage.isAt(),
                "User should return to the login page after logout"
        );

        assertTrue(
                returnedLoginPage.getFlashMessage()
                        .contains("You logged out of the secure area!")
        );
    }
}