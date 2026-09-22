package traltb.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class AccountServiceTest {

    private AccountService service;

    @BeforeEach
    void setUp() {
        service = new AccountService();
    }



    @ParameterizedTest(name = "Email hợp lệ: {0}")
    @ValueSource(strings = {
            "john@example.com",
            "alice.b@mail.co.uk",
            "carol_99@domain.io"
    })
    @DisplayName("isValidEmail trả về true với email đúng định dạng")
    void isValidEmail_ValidEmails_ReturnsTrue(String email) {

        // Act
        boolean result = service.isValidEmail(email);

        // Assert
        assertTrue(result);
    }



    @ParameterizedTest(name = "Email không hợp lệ: \"{0}\"")
    @CsvSource(value = {
            "bobmail.com",
            "missing@dot",
            "'@nodomain.com'",
            "' '",
            "NULL"
    }, nullValues = "NULL")
    @DisplayName("isValidEmail trả về false với email sai định dạng / null")
    void isValidEmail_InvalidEmails_ReturnsFalse(String email) {

        // Act
        boolean result = service.isValidEmail(email);

        // Assert
        assertFalse(result);
    }



    @ParameterizedTest(name = "Row {index}: ({0},{1},{2}) → {3}")
    @CsvFileSource(
            resources = "/test-data.csv",
            numLinesToSkip = 1
    )
    @DisplayName("registerAccount với dữ liệu từ test-data.csv")
    void registerAccount_FromCsv(
            String username,
            String password,
            String email,
            boolean expected) {


        boolean actual =
                service.registerAccount(username, password, email);


        assertEquals(
                expected,
                actual,
                () -> String.format(
                        "(%s,%s,%s) phải trả về %s",
                        username,
                        password,
                        email,
                        expected
                )
        );
    }



    @Test
    @DisplayName("registerAccount: password = 6 ký tự → false")
    void registerAccount_PasswordExactly6_ReturnsFalse() {


        String username = "bob";
        String password = "abcdef";
        String email = "bob@mail.com";


        boolean actual =
                service.registerAccount(
                        username,
                        password,
                        email
                );


        assertFalse(actual, "password phải > 6 ký tự");
    }



    @Test
    @DisplayName("registerAccount: password = 7 ký tự → true")
    void registerAccount_PasswordExactly7_ReturnsTrue() {


        String username = "bob";
        String password = "abcdefg";
        String email = "bob@mail.com";


        boolean actual =
                service.registerAccount(
                        username,
                        password,
                        email
                );


        assertTrue(actual);
    }



    @Test
    @DisplayName("registerAccount: tất cả tham số null → false")
    void registerAccount_AllNull_ReturnsFalse() {


        boolean actual =
                service.registerAccount(
                        null,
                        null,
                        null
                );


        assertFalse(actual);
    }
}