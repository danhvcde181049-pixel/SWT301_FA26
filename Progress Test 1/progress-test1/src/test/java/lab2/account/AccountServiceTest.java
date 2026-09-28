package lab2.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("AccountService")
class AccountServiceTest {

    private AccountService service;

    @BeforeEach
    void setUp() {
        service = new AccountService();
    }

    @Nested
    @DisplayName("Register")
    class Register {

        @Test
        @DisplayName("Đăng ký thành công")
        void register_ValidData_ReturnsSuccess() {
            ResultCode result = service.register(
                    "alice01",
                    "Alice@Example.com",
                    "Secret@123",
                    "Secret@123",
                    LocalDate.of(2000, 1, 1),
                    "0912345678"
            );

            assertEquals(ResultCode.SUCCESS, result);

            Account account = service.findByUsername("alice01").orElseThrow();

            assertEquals("alice01", account.getUsername());
            assertEquals("alice@example.com", account.getEmail());
            assertEquals(AccountStatus.ACTIVE, account.getStatus());
            assertEquals(0, account.getFailedAttempts());
            assertFalse(account.isLocked());

            assertNotNull(account.getSalt());
            assertNotNull(account.getCurrentPasswordHash());
            assertNotEquals("Secret@123", account.getCurrentPasswordHash());
            assertEquals(64, account.getCurrentPasswordHash().length());
        }

        @ParameterizedTest(name = "[{index}] {2}")
        @MethodSource("invalidRegisterInputs")
        void register_InvalidInputs_ReturnsExpected(
                String username,
                String email,
                String password,
                String confirmPassword,
                LocalDate dob,
                String phone,
                ResultCode expected,
                String description) {

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

        static Stream<Arguments> invalidRegisterInputs() {
            LocalDate adultDob = LocalDate.now().minusYears(20);
            LocalDate minorDob = LocalDate.now().minusYears(17);

            return Stream.of(
                    Arguments.of(
                            "",
                            "alice@example.com",
                            "Secret@123",
                            "Secret@123",
                            adultDob,
                            "0912345678",
                            ResultCode.INVALID_INPUT,
                            "username rỗng"
                    ),
                    Arguments.of(
                            "alice01",
                            "",
                            "Secret@123",
                            "Secret@123",
                            adultDob,
                            "0912345678",
                            ResultCode.INVALID_INPUT,
                            "email rỗng"
                    ),
                    Arguments.of(
                            "alice01",
                            "alice@example.com",
                            "",
                            "",
                            adultDob,
                            "0912345678",
                            ResultCode.INVALID_INPUT,
                            "password rỗng"
                    ),
                    Arguments.of(
                            "ab",
                            "alice@example.com",
                            "Secret@123",
                            "Secret@123",
                            adultDob,
                            "0912345678",
                            ResultCode.INVALID_USERNAME,
                            "username không hợp lệ"
                    ),
                    Arguments.of(
                            "alice01",
                            "invalid-email",
                            "Secret@123",
                            "Secret@123",
                            adultDob,
                            "0912345678",
                            ResultCode.INVALID_EMAIL,
                            "email không hợp lệ"
                    ),
                    Arguments.of(
                            "alice01",
                            "alice@example.com",
                            "weakpass",
                            "weakpass",
                            adultDob,
                            "0912345678",
                            ResultCode.WEAK_PASSWORD,
                            "password yếu"
                    ),
                    Arguments.of(
                            "alice01",
                            "alice@example.com",
                            "Secret@123",
                            "Different@123",
                            adultDob,
                            "0912345678",
                            ResultCode.PASSWORD_MISMATCH,
                            "password không khớp"
                    ),
                    Arguments.of(
                            "alice01",
                            "alice@example.com",
                            "Secret@123",
                            "Secret@123",
                            minorDob,
                            "0912345678",
                            ResultCode.UNDERAGE,
                            "chưa đủ 18 tuổi"
                    ),
                    Arguments.of(
                            "alice01",
                            "alice@example.com",
                            "Secret@123",
                            "Secret@123",
                            adultDob,
                            "0123456789",
                            ResultCode.INVALID_PHONE,
                            "phone không hợp lệ"
                    )
            );
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" "})
        void register_BlankUsername_ReturnsInvalidInput(String username) {
            ResultCode result = service.register(
                    username,
                    "alice@example.com",
                    "Secret@123",
                    "Secret@123",
                    LocalDate.now().minusYears(20),
                    "0912345678"
            );

            assertEquals(ResultCode.INVALID_INPUT, result);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" "})
        void register_BlankEmail_ReturnsInvalidInput(String email) {
            ResultCode result = service.register(
                    "alice01",
                    email,
                    "Secret@123",
                    "Secret@123",
                    LocalDate.now().minusYears(20),
                    "0912345678"
            );

            assertEquals(ResultCode.INVALID_INPUT, result);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" "})
        void register_BlankPassword_ReturnsInvalidInput(String password) {
            ResultCode result = service.register(
                    "alice01",
                    "alice@example.com",
                    password,
                    password,
                    LocalDate.now().minusYears(20),
                    "0912345678"
            );

            assertEquals(ResultCode.INVALID_INPUT, result);
        }

        @ParameterizedTest
        @ValueSource(strings = {"Alice01", "ALICE01"})
        void register_DuplicateUsername_CaseInsensitive_ReturnsDuplicate(String username) {
            service.register(
                    "alice01",
                    "alice@example.com",
                    "Secret@123",
                    "Secret@123",
                    LocalDate.now().minusYears(20),
                    "0912345678"
            );

            ResultCode result = service.register(
                    username,
                    "other@example.com",
                    "Secret@123",
                    "Secret@123",
                    LocalDate.now().minusYears(20),
                    "0912345679"
            );

            assertEquals(ResultCode.DUPLICATE_USERNAME, result);
        }

        @ParameterizedTest
        @ValueSource(strings = {"ALICE@EXAMPLE.COM", "Alice@Example.Com"})
        void register_DuplicateEmail_CaseInsensitive_ReturnsDuplicate(String email) {
            service.register(
                    "alice01",
                    "alice@example.com",
                    "Secret@123",
                    "Secret@123",
                    LocalDate.now().minusYears(20),
                    "0912345678"
            );

            ResultCode result = service.register(
                    "bob01",
                    email,
                    "Secret@123",
                    "Secret@123",
                    LocalDate.now().minusYears(20),
                    "0912345679"
            );

            assertEquals(ResultCode.DUPLICATE_EMAIL, result);
        }

        @ParameterizedTest
        @CsvSource({
                "17, 0, UNDERAGE",
                "18, 0, SUCCESS"
        })
        void register_AgeBoundary(int yearsAgo, int plusDays, ResultCode expected) {
            LocalDate dob = LocalDate.now()
                    .minusYears(yearsAgo)
                    .plusDays(plusDays);

            ResultCode result = service.register(
                    "user" + yearsAgo + plusDays,
                    "user" + yearsAgo + plusDays + "@example.com",
                    "Secret@123",
                    "Secret@123",
                    dob,
                    "0912345678"
            );

            assertEquals(expected, result);
        }

        @Test
        @DisplayName("Thứ tự ưu tiên: invalid input trước username")
        void register_Priority_InvalidInputBeforeUsername() {
            ResultCode result = service.register(
                    "",
                    "",
                    "",
                    "",
                    null,
                    null
            );

            assertEquals(ResultCode.INVALID_INPUT, result);
        }

        @Test
        @DisplayName("Thứ tự ưu tiên: username trước email")
        void register_Priority_UsernameBeforeEmail() {
            ResultCode result = service.register(
                    "ab",
                    "invalid-email",
                    "Secret@123",
                    "Secret@123",
                    LocalDate.now().minusYears(20),
                    "0912345678"
            );

            assertEquals(ResultCode.INVALID_USERNAME, result);
        }

        @Test
        @DisplayName("Thứ tự ưu tiên: email trước password")
        void register_Priority_EmailBeforePassword() {
            ResultCode result = service.register(
                    "alice01",
                    "invalid-email",
                    "weak",
                    "weak",
                    LocalDate.now().minusYears(20),
                    "0912345678"
            );

            assertEquals(ResultCode.INVALID_EMAIL, result);
        }
    }
}