package lab2.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("AccountService")
class AccountServiceTest {

    private static final String USER = "alice_01";
    private static final String EMAIL = "alice@example.com";
    private static final String PASS = "Secret@123";
    private static final String WRONG = "Wrong@123";

    private static final String PHONE = "0912345678";

    private static final LocalDate DOB =
            LocalDate.now().minusYears(20);

    private static final LocalDate CHILD_DOB =
            LocalDate.now().minusYears(17);

    private AccountService service;

    // =========================================================
    // COMMON
    // =========================================================

    @BeforeEach
    void setUp() {
        service = new AccountService();
    }

    private void registerDefault() {

        ResultCode result = service.register(
                USER,
                EMAIL,
                PASS,
                PASS,
                DOB,
                PHONE
        );

        assertEquals(ResultCode.SUCCESS, result);
    }

    private Account account() {

        return service.findByUsername(USER)
                .orElseThrow();
    }

    private void failLogin(int times) {

        for (int i = 0; i < times; i++) {
            service.login(USER, WRONG);
        }
    }

    // =========================================================
    // REGISTER
    // =========================================================

    @Nested
    @DisplayName("register()")
    class Register {

        @Test
        void register_ValidData_ReturnsSuccess() {

            ResultCode result = service.register(
                    USER,
                    EMAIL,
                    PASS,
                    PASS,
                    DOB,
                    PHONE
            );

            assertEquals(ResultCode.SUCCESS, result);

            Account account = account();

            assertNotNull(account);
            assertEquals(USER, account.getUsername());
            assertEquals(EMAIL, account.getEmail());
            assertEquals(DOB, account.getDateOfBirth());
            assertEquals(PHONE, account.getPhone());

            assertEquals(
                    AccountStatus.ACTIVE,
                    account.getStatus()
            );

            assertEquals(
                    0,
                    account.getFailedAttempts()
            );

            assertFalse(
                    account.isLocked()
            );

            assertNotEquals(
                    PASS,
                    account.getCurrentPasswordHash()
            );

            assertNotNull(account.getSalt());
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("invalidInputs")
        void register_InvalidInputs_ReturnsExpected(
                String description,
                String username,
                String email,
                String password,
                String confirmPassword,
                LocalDate dob,
                String phone,
                ResultCode expected) {

            ResultCode result = service.register(
                    username,
                    email,
                    password,
                    confirmPassword,
                    dob,
                    phone
            );

            assertEquals(expected, result);
        }

        static Stream<Arguments> invalidInputs() {

            return Stream.of(

                    Arguments.of(
                            "blank username",
                            "",
                            EMAIL,
                            PASS,
                            PASS,
                            DOB,
                            PHONE,
                            ResultCode.INVALID_INPUT
                    ),

                    Arguments.of(
                            "blank email",
                            USER,
                            "",
                            PASS,
                            PASS,
                            DOB,
                            PHONE,
                            ResultCode.INVALID_INPUT
                    ),

                    Arguments.of(
                            "blank password",
                            USER,
                            EMAIL,
                            "",
                            "",
                            DOB,
                            PHONE,
                            ResultCode.INVALID_INPUT
                    ),

                    Arguments.of(
                            "null date of birth",
                            USER,
                            EMAIL,
                            PASS,
                            PASS,
                            null,
                            PHONE,
                            ResultCode.INVALID_INPUT
                    ),

                    Arguments.of(
                            "future date of birth",
                            USER,
                            EMAIL,
                            PASS,
                            PASS,
                            LocalDate.now().plusDays(1),
                            PHONE,
                            ResultCode.INVALID_INPUT
                    ),

                    Arguments.of(
                            "invalid username",
                            "1alice",
                            EMAIL,
                            PASS,
                            PASS,
                            DOB,
                            PHONE,
                            ResultCode.INVALID_USERNAME
                    ),

                    Arguments.of(
                            "invalid email",
                            USER,
                            "bad",
                            PASS,
                            PASS,
                            DOB,
                            PHONE,
                            ResultCode.INVALID_EMAIL
                    ),

                    Arguments.of(
                            "weak password",
                            USER,
                            EMAIL,
                            "weak",
                            "weak",
                            DOB,
                            PHONE,
                            ResultCode.WEAK_PASSWORD
                    ),

                    Arguments.of(
                            "password mismatch",
                            USER,
                            EMAIL,
                            PASS,
                            "Other@123",
                            DOB,
                            PHONE,
                            ResultCode.PASSWORD_MISMATCH
                    ),

                    Arguments.of(
                            "underage",
                            USER,
                            EMAIL,
                            PASS,
                            PASS,
                            CHILD_DOB,
                            PHONE,
                            ResultCode.UNDERAGE
                    ),

                    Arguments.of(
                            "invalid phone",
                            USER,
                            EMAIL,
                            PASS,
                            PASS,
                            DOB,
                            "123",
                            ResultCode.INVALID_PHONE
                    )
            );
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" "})
        void register_InvalidUsername_ReturnsInvalidInput(
                String username) {

            assertEquals(
                    ResultCode.INVALID_INPUT,
                    service.register(
                            username,
                            EMAIL,
                            PASS,
                            PASS,
                            DOB,
                            PHONE
                    )
            );
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" "})
        void register_InvalidEmail_ReturnsInvalidInput(
                String email) {

            assertEquals(
                    ResultCode.INVALID_INPUT,
                    service.register(
                            USER,
                            email,
                            PASS,
                            PASS,
                            DOB,
                            PHONE
                    )
            );
        }

        @Test
        void register_PhoneNull_IsAllowed() {

            assertEquals(
                    ResultCode.SUCCESS,
                    service.register(
                            USER,
                            EMAIL,
                            PASS,
                            PASS,
                            DOB,
                            null
                    )
            );
        }

        @Test
        void register_PhoneEmpty_IsAllowed() {

            assertEquals(
                    ResultCode.SUCCESS,
                    service.register(
                            USER,
                            EMAIL,
                            PASS,
                            PASS,
                            DOB,
                            ""
                    )
            );
        }

        @Test
        void register_DuplicateUsername_CaseInsensitive() {

            assertEquals(
                    ResultCode.SUCCESS,
                    service.register(
                            USER,
                            EMAIL,
                            PASS,
                            PASS,
                            DOB,
                            PHONE
                    )
            );

            assertEquals(
                    ResultCode.DUPLICATE_USERNAME,
                    service.register(
                            "ALICE_01",
                            "other@example.com",
                            PASS,
                            PASS,
                            DOB,
                            PHONE
                    )
            );
        }

        @Test
        void register_DuplicateEmail_CaseInsensitive() {

            assertEquals(
                    ResultCode.SUCCESS,
                    service.register(
                            USER,
                            EMAIL,
                            PASS,
                            PASS,
                            DOB,
                            PHONE
                    )
            );

            assertEquals(
                    ResultCode.DUPLICATE_EMAIL,
                    service.register(
                            "bob_01",
                            "ALICE@EXAMPLE.COM",
                            PASS,
                            PASS,
                            DOB,
                            PHONE
                    )
            );
        }

        @Test
        void register_FailedValidation_DoesNotCreateAccount() {

            ResultCode result = service.register(
                    "1alice",
                    EMAIL,
                    PASS,
                    PASS,
                    DOB,
                    PHONE
            );

            assertEquals(
                    ResultCode.INVALID_USERNAME,
                    result
            );

            assertTrue(
                    service.findByUsername("1alice").isEmpty()
            );
        }
    }

    // =========================================================
    // LOGIN
    // =========================================================

    @Nested
    @DisplayName("login()")
    class Login {

        @BeforeEach
        void registerUser() {
            registerDefault();
        }

        // -----------------------------------------------------
        // Rule 6
        // Correct login
        // -----------------------------------------------------

        @Test
        void login_CorrectCredentials_Success() {

            ResultCode result =
                    service.login(USER, PASS);

            assertEquals(
                    ResultCode.SUCCESS,
                    result
            );

            assertEquals(
                    0,
                    account().getFailedAttempts()
            );

            assertFalse(
                    service.isLocked(USER)
            );
        }

        // -----------------------------------------------------
        // Invalid input
        // -----------------------------------------------------

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" "})
        void login_UsernameNullEmptyBlank_ReturnsInvalidInput(
                String username) {

            assertEquals(
                    ResultCode.INVALID_INPUT,
                    service.login(username, PASS)
            );

            assertEquals(
                    0,
                    account().getFailedAttempts()
            );
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" "})
        void login_PasswordNullEmptyBlank_ReturnsInvalidInput(
                String password) {

            assertEquals(
                    ResultCode.INVALID_INPUT,
                    service.login(USER, password)
            );

            assertEquals(
                    0,
                    account().getFailedAttempts()
            );
        }

        // -----------------------------------------------------
        // Username case insensitive
        // -----------------------------------------------------

        @ParameterizedTest
        @ValueSource(strings = {
                "alice_01",
                "ALICE_01",
                "Alice_01"
        })
        void login_UsernameIgnoreCase_Success(
                String username) {

            assertEquals(
                    ResultCode.SUCCESS,
                    service.login(username, PASS)
            );
        }

        // -----------------------------------------------------
        // Password case sensitive
        // -----------------------------------------------------

        @ParameterizedTest
        @ValueSource(strings = {
                "secret@123",
                "SECRET@123"
        })
        void login_PasswordCaseSensitive_ReturnsInvalidCredentials(
                String password) {

            assertEquals(
                    ResultCode.INVALID_CREDENTIALS,
                    service.login(USER, password)
            );
        }

        // -----------------------------------------------------
        // Rule 1
        // Unknown user
        // -----------------------------------------------------

        @Test
        void login_UnknownUserAndWrongPassword_ReturnSameCode() {

            assertEquals(
                    ResultCode.INVALID_CREDENTIALS,
                    service.login("nobody_1", PASS)
            );

            assertEquals(
                    ResultCode.INVALID_CREDENTIALS,
                    service.login(USER, WRONG)
            );
        }

        // -----------------------------------------------------
        // Rule 4
        // Wrong password 1 -> 4
        // -----------------------------------------------------

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 4})
        void login_WrongPasswordLessThan5Times_IncrementsCounter(
                int times) {

            failLogin(times - 1);

            ResultCode result =
                    service.login(USER, WRONG);

            assertEquals(
                    ResultCode.INVALID_CREDENTIALS,
                    result
            );

            assertEquals(
                    times,
                    account().getFailedAttempts()
            );

            assertFalse(
                    service.isLocked(USER)
            );
        }

        // -----------------------------------------------------
        // Rule 5
        // Wrong password 5th time
        // -----------------------------------------------------

        @Test
        void login_WrongPassword5thTime_LocksAccount() {

            failLogin(4);

            ResultCode result =
                    service.login(USER, WRONG);

            assertEquals(
                    ResultCode.ACCOUNT_LOCKED,
                    result
            );

            assertEquals(
                    5,
                    account().getFailedAttempts()
            );

            assertTrue(
                    service.isLocked(USER)
            );
        }

        // -----------------------------------------------------
        // Rule 3
        // Already locked
        // -----------------------------------------------------

        @ParameterizedTest
        @ValueSource(strings = {
                PASS,
                WRONG
        })
        void login_WhileLocked_RejectsWithoutIncrement(
                String password) {

            failLogin(5);

            assertEquals(
                    ResultCode.ACCOUNT_LOCKED,
                    service.login(USER, password)
            );

            assertEquals(
                    5,
                    account().getFailedAttempts()
            );

            assertTrue(
                    service.isLocked(USER)
            );
        }

        // -----------------------------------------------------
        // Boundary 4 / 5 / 6
        // -----------------------------------------------------

        @ParameterizedTest
        @CsvSource({
                "4, SUCCESS, false",
                "5, ACCOUNT_LOCKED, true",
                "6, ACCOUNT_LOCKED, true"
        })
        void login_CorrectPasswordAfterNFailures(
                int failures,
                ResultCode expected,
                boolean locked) {

            failLogin(failures);

            ResultCode result =
                    service.login(USER, PASS);

            assertEquals(
                    expected,
                    result
            );

            assertEquals(
                    locked,
                    service.isLocked(USER)
            );
        }

        // -----------------------------------------------------
        // Successful login resets counter
        // -----------------------------------------------------

        @Test
        void login_SuccessAfterFailures_ResetsCounter() {

            failLogin(3);

            assertEquals(
                    3,
                    account().getFailedAttempts()
            );

            assertEquals(
                    ResultCode.SUCCESS,
                    service.login(USER, PASS)
            );

            assertEquals(
                    0,
                    account().getFailedAttempts()
            );

            assertFalse(
                    service.isLocked(USER)
            );
        }

        // -----------------------------------------------------
        // Admin unlock
        // -----------------------------------------------------

        @Test
        void login_AfterAdminUnlock_CounterRestartsAndCanLogin() {

            failLogin(5);

            assertTrue(
                    service.isLocked(USER)
            );

            assertEquals(
                    ResultCode.SUCCESS,
                    service.unlockAccount(USER)
            );

            assertFalse(
                    service.isLocked(USER)
            );

            assertEquals(
                    0,
                    account().getFailedAttempts()
            );

            assertEquals(
                    ResultCode.INVALID_CREDENTIALS,
                    service.login(USER, WRONG)
            );

            assertEquals(
                    1,
                    account().getFailedAttempts()
            );

            assertEquals(
                    ResultCode.SUCCESS,
                    service.login(USER, PASS)
            );

            assertEquals(
                    0,
                    account().getFailedAttempts()
            );
        }

        // -----------------------------------------------------
        // Unlock unknown user
        // -----------------------------------------------------

        @Test
        void unlock_UnknownUser_ReturnsUserNotFound() {

            assertEquals(
                    ResultCode.USER_NOT_FOUND,
                    service.unlockAccount("nobody_1")
            );
        }
    }
}