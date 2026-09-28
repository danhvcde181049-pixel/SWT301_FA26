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

    public ResultCode register(String username, String email, String password,
                               String confirmPassword, LocalDate dateOfBirth, String phone) {

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
        // null hoặc "" được chấp nhận
        // "   " sẽ không hợp lệ
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

        // 10. Tạo tài khoản
        String salt = PasswordHasher.generateSalt();

        String passwordHash = PasswordHasher.hash(salt, password);

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

    public ResultCode login(String username, String password) {
        throw new UnsupportedOperationException("TODO");
    }

    public ResultCode changePassword(String username, String oldPassword,
                                     String newPassword, String confirmPassword) {
        throw new UnsupportedOperationException("TODO");
    }

    public TokenResult requestPasswordReset(String email) {
        throw new UnsupportedOperationException("TODO");
    }

    public ResultCode resetPassword(String token, String newPassword,
                                    String confirmPassword) {
        throw new UnsupportedOperationException("TODO");
    }

    public ResultCode disableAccount(String username) {
        throw new UnsupportedOperationException("TODO");
    }

    public Optional<Account> findByUsername(String username) {
        Optional<Account> account = Optional.ofNullable(
                accountsByUsername.get(key(username))
        );

        return account;
    }

    public boolean isLocked(String username) {
        Optional<Account> account = findByUsername(username);

        return account.isPresent() && account.get().isLocked();
    }

    public ResultCode unlockAccount(String username) {
        Optional<Account> account = findByUsername(username);

        if (account.isEmpty()) {
            return ResultCode.USER_NOT_FOUND;
        }

        account.get().unlock();

        return ResultCode.SUCCESS;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String key(String s) {
        return s.toLowerCase(Locale.ROOT);
    }
}