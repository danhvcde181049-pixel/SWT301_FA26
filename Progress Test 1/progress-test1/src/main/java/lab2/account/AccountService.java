package lab2.account;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class AccountService {

    public static final int MAX_FAILED_ATTEMPTS = 5;
    public static final int PASSWORD_HISTORY_SIZE = 3;
    public static final int MIN_AGE = 18;

    private final Map<String, Account> accountsByUsername = new HashMap<>();
    private final Map<String, String> usernameByEmail = new HashMap<>();
    private final Map<String, String> usernameByToken = new HashMap<>();
    private final Map<String, String> tokenByUsername = new HashMap<>();

    public AccountService() {
    }

    // =========================================================
    // REGISTER
    // =========================================================

    public ResultCode register(
            String username,
            String email,
            String password,
            String confirmPassword,
            LocalDate dateOfBirth,
            String phone) {

        LocalDate today = LocalDate.now();

        // 1. INVALID_INPUT
        if (isBlank(username)
                || isBlank(email)
                || isBlank(password)
                || isBlank(confirmPassword)
                || dateOfBirth == null
                || dateOfBirth.isAfter(today)) {

            return ResultCode.INVALID_INPUT;
        }

        // 2. INVALID_USERNAME
        if (!AccountValidator.isValidUsername(username)) {
            return ResultCode.INVALID_USERNAME;
        }

        // 3. INVALID_EMAIL
        if (!AccountValidator.isValidEmail(email)) {
            return ResultCode.INVALID_EMAIL;
        }

        // 4. WEAK_PASSWORD
        if (!AccountValidator.isValidPassword(password, username)) {
            return ResultCode.WEAK_PASSWORD;
        }

        // 5. PASSWORD_MISMATCH
        if (!password.equals(confirmPassword)) {
            return ResultCode.PASSWORD_MISMATCH;
        }

        // 6. UNDERAGE
        int age = AccountValidator.calculateAge(dateOfBirth, today);

        if (age < MIN_AGE) {
            return ResultCode.UNDERAGE;
        }

        // 7. INVALID_PHONE
        // null và "" được phép bỏ trống.
        // "   " không hợp lệ.
        if (phone != null
                && !phone.isEmpty()
                && !AccountValidator.isValidPhone(phone)) {

            return ResultCode.INVALID_PHONE;
        }

        // 8. DUPLICATE_USERNAME
        String usernameKey = key(username);

        if (accountsByUsername.containsKey(usernameKey)) {
            return ResultCode.DUPLICATE_USERNAME;
        }

        // 9. DUPLICATE_EMAIL
        String emailKey = key(email);

        if (usernameByEmail.containsKey(emailKey)) {
            return ResultCode.DUPLICATE_EMAIL;
        }

        // 10. CREATE ACCOUNT
        String salt = PasswordHasher.generateSalt();

        String passwordHash =
                PasswordHasher.hash(salt, password);

        Account account = new Account(
                username,
                emailKey,
                dateOfBirth,
                phone,
                salt,
                AccountStatus.ACTIVE,
                passwordHash
        );

        accountsByUsername.put(usernameKey, account);
        usernameByEmail.put(emailKey, usernameKey);

        return ResultCode.SUCCESS;
    }

    // =========================================================
    // LOGIN
    // =========================================================

    public ResultCode login(String username, String password) {

        // Invalid input
        if (isBlank(username) || isBlank(password)) {
            return ResultCode.INVALID_INPUT;
        }

        // Username không phân biệt hoa thường
        Account acc = accountsByUsername.get(key(username));

        // User không tồn tại
        if (acc == null) {
            return ResultCode.INVALID_CREDENTIALS;
        }

        // Account bị disable
        if (acc.getStatus() == AccountStatus.DISABLED) {
            return ResultCode.ACCOUNT_DISABLED;
        }

        // Account đã bị khóa
        if (acc.isLocked()) return ResultCode.ACCOUNT_LOCKED;


        // Kiểm tra password
        boolean passwordCorrect = PasswordHasher.matches(
                acc.getSalt(),
                password,
                acc.getCurrentPasswordHash()
        );

        // =====================================================
        // PASSWORD SAI
        // =====================================================

        if (!passwordCorrect) {

            // Tăng số lần nhập sai
            acc.incrementFailedAttempts();

            // Sai lần thứ 5 -> KHÓA NGAY
            if (acc.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
                acc.lock();

                return ResultCode.ACCOUNT_LOCKED;
            }

            // Sai lần 1 -> 4
            return ResultCode.INVALID_CREDENTIALS;
        }

        // =====================================================
        // PASSWORD ĐÚNG
        // =====================================================

        // Đăng nhập đúng thì reset counter
        acc.resetFailedAttempts();

        return ResultCode.SUCCESS;
    }

    // =========================================================
    // CHANGE PASSWORD
    // =========================================================

    public ResultCode changePassword(
            String username,
            String oldPassword,
            String newPassword) {

        throw new UnsupportedOperationException("TODO");
    }

    // =========================================================
    // REQUEST PASSWORD RESET
    // =========================================================

    public TokenResult requestPasswordReset(String username) {

        throw new UnsupportedOperationException("TODO");
    }

    // =========================================================
    // RESET PASSWORD
    // =========================================================

    public ResultCode resetPassword(
            String token,
            String newPassword) {

        throw new UnsupportedOperationException("TODO");
    }

    // =========================================================
    // DISABLE ACCOUNT
    // =========================================================

    public ResultCode disableAccount(String username) {

        throw new UnsupportedOperationException("TODO");
    }

    // =========================================================
    // FIND USER
    // =========================================================

    public Optional<Account> findByUsername(String username) {

        if (username == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(
                accountsByUsername.get(key(username))
        );
    }

    // =========================================================
    // CHECK LOCKED
    // =========================================================

    public boolean isLocked(String username) {

        Optional<Account> account = findByUsername(username);

        return account.isPresent()
                && account.get().isLocked();
    }

    // =========================================================
    // UNLOCK
    // =========================================================

    public ResultCode unlockAccount(String username) {

        Optional<Account> account = findByUsername(username);

        if (account.isEmpty()) {
            return ResultCode.USER_NOT_FOUND;
        }

        account.get().unlock();

        return ResultCode.SUCCESS;
    }

    // =========================================================
    // HELPER
    // =========================================================

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String key(String s) {
        return s.toLowerCase(Locale.ROOT);
    }
}