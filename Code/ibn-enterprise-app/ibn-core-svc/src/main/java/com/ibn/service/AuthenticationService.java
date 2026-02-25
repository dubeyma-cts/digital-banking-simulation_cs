package com.ibn.service;

import com.ibn.core.entity.Account;
import com.ibn.core.entity.Customer;
import com.ibn.core.entity.LoginAttempt;
import com.ibn.core.entity.Session;
import com.ibn.core.entity.Transaction;
import com.ibn.core.entity.User;
import com.ibn.dao.AccountBalanceDao;
import com.ibn.dao.AccountDao;
import com.ibn.dao.CustomerDao;
import com.ibn.dao.LoginAttemptDao;
import com.ibn.dao.SessionDao;
import com.ibn.dao.TransactionDao;
import com.ibn.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ibn.dto.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

/**
 * Service class for handling authentication-related business logic
 */
@Service
public class AuthenticationService {

    private final AccountDao accountDao;
    private final AccountBalanceDao accountBalanceDao;
    private final TransactionDao transactionDao;
    private final UserDao userDao;
    private final CustomerDao customerDao;
    private final LoginAttemptDao loginAttemptDao;
    private final SessionDao sessionDao;
    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private static final long LOGIN_ATTEMPT_WINDOW_MINUTES = 15L;
    private static final long ACCOUNT_LOCK_MINUTES = 15L;
    private static final String ACCOUNT_ACTIVE = "ACTIVE";
    private static final String ACCOUNT_LOCKED = "LOCKED";
    private static final String USER_ACTIVE = "ACTIVE";
    private static final String USER_LOCKED = "LOCKED";
    private static final String USER_TYPE_BANKER = "BANKER";
    private static final String USER_TYPE_ADMIN = "ADMIN";
    private static final String LOGIN_OUTCOME_SUCCESS = "SUCCESS";
    private static final String LOGIN_OUTCOME_FAILED = "FAILED";
    private static final String FAILURE_REASON_INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    private static final String SESSION_ACTIVE = "ACTIVE";
    private static final String SESSION_REVOKED = "REVOKED";
    private static final long SESSION_INACTIVITY_MINUTES = 3L;

    @Autowired
    public AuthenticationService(AccountDao accountDao, AccountBalanceDao accountBalanceDao, TransactionDao transactionDao,
                                 UserDao userDao, CustomerDao customerDao, LoginAttemptDao loginAttemptDao, SessionDao sessionDao) {
        this.accountDao = accountDao;
        this.accountBalanceDao = accountBalanceDao;
        this.transactionDao = transactionDao;
        this.userDao = userDao;
        this.customerDao = customerDao;
        this.loginAttemptDao = loginAttemptDao;
        this.sessionDao = sessionDao;
    }

    /**
     * Authenticate user with customer ID and password
     * @param loginRequest - Login credentials containing customerId and password
     * @return LoginResponse with success status and authentication details
     */
    public LoginResponse authenticate(LoginRequest loginRequest, HttpServletRequest httpRequest) {
        // Validate input
        if (loginRequest == null || loginRequest.getCustomerId() == null || loginRequest.getPassword() == null) {
            return new LoginResponse(false, "Invalid login credentials");
        }

        String customerId = loginRequest.getCustomerId();
        String password = loginRequest.getPassword();
        Optional<User> userOptional = getUserByCustomerId(customerId);

        // Check if account is locked
        if (isUserCurrentlyLocked(userOptional.orElse(null))) {
            return new LoginResponse(false, "Account is locked. Please contact support.");
        }

        if (verifyCredentials(customerId, password)) {
            if (!userOptional.isPresent()) {
                return new LoginResponse(false, "User profile not found for customer");
            }

            User user = userOptional.get();
            Session session = createSession(user, httpRequest);
            String token = session.getSessionId().toString();
            String email = user.getEmail();
            
            // Reset login attempts on successful login
            recordLoginAttempt(customerId, user, httpRequest, LOGIN_OUTCOME_SUCCESS, null);
            resetLoginAttempts(customerId);
            
            return new LoginResponse(true, "Login successful", token, user.getUserId() == null ? customerId : user.getUserId().toString(), email);
        } else {
            // Increment failed login attempts
            incrementFailedAttempts(customerId, httpRequest);

            if (isUserCurrentlyLocked(userOptional.orElse(null))) {
                return new LoginResponse(false, "Account is locked. Please contact support.");
            }

            int remainingAttempts = getRemainingLoginAttempts(customerId);
            String message = String.format("Invalid credentials. Remaining attempts: %d", remainingAttempts);

            return new LoginResponse(false, message);
        }
    }

    public LoginResponse authenticateBanker(BankerLoginRequest loginRequest, HttpServletRequest httpRequest) {
        if (loginRequest == null || loginRequest.getLoginId() == null || loginRequest.getPassword() == null) {
            return new LoginResponse(false, "Invalid banker login credentials");
        }

        String loginId = loginRequest.getLoginId().trim();
        String password = loginRequest.getPassword();

        if (loginId.isEmpty() || password.trim().isEmpty()) {
            return new LoginResponse(false, "Invalid banker login credentials");
        }

        Optional<User> userOptional = userDao.findByUsernameIgnoreCase(loginId);
        if (!userOptional.isPresent()) {
            recordLoginAttempt(loginId, null, httpRequest, LOGIN_OUTCOME_FAILED, FAILURE_REASON_INVALID_CREDENTIALS);
            return new LoginResponse(false, "Invalid banker credentials");
        }

        User user = userOptional.get();
        if (isUserCurrentlyLocked(user)) {
            return new LoginResponse(false, "Banker account is locked. Please contact support.");
        }

        if (!isBankerUser(user)) {
            recordLoginAttempt(loginId, user, httpRequest, LOGIN_OUTCOME_FAILED, FAILURE_REASON_INVALID_CREDENTIALS);
            return new LoginResponse(false, "User is not authorized for banker login");
        }

        if (!USER_ACTIVE.equalsIgnoreCase(user.getStatus())) {
            recordLoginAttempt(loginId, user, httpRequest, LOGIN_OUTCOME_FAILED, "USER_NOT_ACTIVE");
            return new LoginResponse(false, "Banker account is not active");
        }

        if (!isPasswordValid(user.getPwd(), password)) {
            recordLoginAttempt(loginId, user, httpRequest, LOGIN_OUTCOME_FAILED, FAILURE_REASON_INVALID_CREDENTIALS);
            applyLockIfThresholdReached(loginId, user);
            if (isUserCurrentlyLocked(user)) {
                return new LoginResponse(false, "Banker account is locked. Please contact support.");
            }
            return new LoginResponse(false, "Invalid banker credentials");
        }

        LocalDateTime now = LocalDateTime.now();
        user.setLastLoginAt(now);
        user.setUpdatedAt(now);
        userDao.save(user);
        recordLoginAttempt(loginId, user, httpRequest, LOGIN_OUTCOME_SUCCESS, null);

        Session session = createSession(user, httpRequest);
        String token = session.getSessionId().toString();
        String email = user.getEmail();

        return new LoginResponse(true, "Banker login successful", token,
                user.getUserId() == null ? "" : user.getUserId().toString(),
                email == null ? "" : email);
    }

    public UnlockUserResponse unlockUserByAdmin(String targetUserId, HttpServletRequest request) {
        UUID actorUserId = extractActorUserId(request);
        if (actorUserId == null) {
            return new UnlockUserResponse(false, "Admin session is required", targetUserId);
        }

        Optional<User> actorOptional = userDao.findById(actorUserId);
        if (!actorOptional.isPresent() || !isAdminUser(actorOptional.get())) {
            return new UnlockUserResponse(false, "Only admin can unlock account", targetUserId);
        }

        UUID targetUuid;
        try {
            targetUuid = UUID.fromString(targetUserId);
        } catch (Exception ex) {
            return new UnlockUserResponse(false, "Invalid user ID format", targetUserId);
        }

        Optional<User> targetOptional = userDao.findById(targetUuid);
        if (!targetOptional.isPresent()) {
            return new UnlockUserResponse(false, "User not found", targetUserId);
        }

        User target = targetOptional.get();
        target.setStatus(USER_ACTIVE);
        target.setLockedUntil(null);
        target.setUpdatedAt(LocalDateTime.now());
        userDao.save(target);

        return new UnlockUserResponse(true, "User account unlocked successfully", targetUserId);
    }

    /**
     * Validate CAPTCHA value
     * @param captchaRequest - Request containing captcha value
     * @return CaptchaResponse with validation result
     */
    public CaptchaResponse validateCaptcha(CaptchaRequest captchaRequest) {
        if (captchaRequest == null || captchaRequest.getCaptcha() == null || captchaRequest.getCaptcha().isEmpty()) {
            return new CaptchaResponse(false, "CAPTCHA value is required");
        }

        // Placeholder validation logic - implement actual CAPTCHA service integration
        boolean isValid = captchaRequest.getCaptcha().length() > 0; // Simple validation
        
        if (isValid) {
            return new CaptchaResponse(true, "CAPTCHA validated successfully");
        } else {
            return new CaptchaResponse(false, "Invalid CAPTCHA value");
        }
    }

    /**
     * Check account status for a customer
     * @param customerId - Customer ID
     * @return AccountStatusResponse with account details
     */
    public AccountStatusResponse checkAccountStatus(String customerId) {
        if (customerId == null || customerId.isEmpty()) {
            return new AccountStatusResponse(customerId, ACCOUNT_ACTIVE, true, "Invalid customer ID", 0);
        }

        boolean isLocked = isAccountLocked(customerId);
        String status = isLocked ? ACCOUNT_LOCKED : ACCOUNT_ACTIVE;
        String message = isLocked ? "Account is locked" : "Account is active";
        int remainingAttempts = getRemainingLoginAttempts(customerId);

        return new AccountStatusResponse(customerId, status, isLocked, message, remainingAttempts);
    }

    /**
     * Get remaining login attempts for a customer
     * @param customerId - Customer ID
     * @return RemainingAttemptsResponse with remaining attempt count
     */
    public RemainingAttemptsResponse getRemainingAttempts(String customerId) {
        if (customerId == null || customerId.isEmpty()) {
            return new RemainingAttemptsResponse(customerId, 0, "Invalid customer ID");
        }

        int remainingAttempts = getRemainingLoginAttempts(customerId);
        String message = remainingAttempts > 0 ? "Attempts available" : "No attempts remaining";
        
        return new RemainingAttemptsResponse(customerId, remainingAttempts, message);
    }

    /**
     * Retrieve basic account info for a customer
     * @param customerId - Customer ID
     * @return AccountInfo with account details
     */
    public com.ibn.dto.AccountInfo getAccountInfo(String customerId) {
        if (customerId == null || customerId.isEmpty()) {
            return new com.ibn.dto.AccountInfo("N/A", 0.0, customerId);
        }

        UUID customerUuid;
        try {
            customerUuid = UUID.fromString(customerId);
        } catch (IllegalArgumentException ex) {
            return new com.ibn.dto.AccountInfo("N/A", 0.0, customerId);
        }

        Optional<Account> accountOptional = accountDao.findByCustomerId(customerUuid);
        if (!accountOptional.isPresent()) {
            return new com.ibn.dto.AccountInfo("N/A", 0.0, customerId);
        }

        Account account = accountOptional.get();
        double balance = accountBalanceDao.findById(account.getAccountId())
                .map(accountBalance -> accountBalance.getAvailableBalance() == null ? 0.0 : accountBalance.getAvailableBalance().doubleValue())
                .orElse(0.0);

        return new com.ibn.dto.AccountInfo(account.getAccountNumber(), balance, customerId);
    }

    public List<AccountSummaryDto> getAccountsByCustomer(String customerId) {
        UUID customerUuid;
        try {
            customerUuid = UUID.fromString(customerId);
        } catch (IllegalArgumentException ex) {
            return Collections.emptyList();
        }

        return accountDao.findAllByCustomerId(customerUuid)
                .stream()
                .map(account -> {
                    double balance = accountBalanceDao.findById(account.getAccountId())
                            .map(accountBalance -> accountBalance.getAvailableBalance() == null ? 0.0 : accountBalance.getAvailableBalance().doubleValue())
                            .orElse(0.0);
                    return new AccountSummaryDto(
                            account.getAccountId() == null ? "" : account.getAccountId().toString(),
                            account.getAccountNumber(),
                            account.getAccountType(),
                            account.getStatus(),
                            account.getCurrency(),
                            balance);
                })
                .collect(Collectors.toList());
    }

    public List<AccountTransactionDto> getMiniStatement(String accountId, int limit) {
        UUID accountUuid = parseUuid(accountId);
        if (accountUuid == null) {
            return Collections.emptyList();
        }

        int rowLimit = limit <= 0 ? 5 : limit;
        return transactionDao.findByAccountIdOrderByPostedAtDesc(accountUuid)
                .stream()
                .limit(rowLimit)
                .map(this::toTransactionDto)
                .collect(Collectors.toList());
    }

    public List<AccountTransactionDto> getDetailedStatement(String accountId, String fromDate, String toDate) {
        UUID accountUuid = parseUuid(accountId);
        if (accountUuid == null) {
            return Collections.emptyList();
        }

        LocalDate from = parseDate(fromDate);
        LocalDate to = parseDate(toDate);

        List<Transaction> transactions;
        if (from != null && to != null) {
            transactions = transactionDao.findByAccountIdAndValueDateBetweenOrderByPostedAtDesc(accountUuid, from, to);
        } else {
            transactions = transactionDao.findByAccountIdOrderByPostedAtDesc(accountUuid);
            if (from != null || to != null) {
                transactions = transactions.stream()
                        .filter(txn -> {
                            LocalDate valueDate = txn.getValueDate();
                            if (valueDate == null) {
                                return false;
                            }
                            boolean fromOk = from == null || !valueDate.isBefore(from);
                            boolean toOk = to == null || !valueDate.isAfter(to);
                            return fromOk && toOk;
                        })
                        .sorted(Comparator.comparing(Transaction::getPostedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                        .collect(Collectors.toList());
            }
        }

        return transactions.stream().map(this::toTransactionDto).collect(Collectors.toList());
    }

    /**
     * Logout user and invalidate session
     * @return LogoutResponse with logout status
     */
    public LogoutResponse logout(String authorizationHeader) {
        String token = extractBearerToken(authorizationHeader);
        if (token.isEmpty()) {
            return new LogoutResponse(false, "Missing session token");
        }

        UUID sessionId;
        try {
            sessionId = UUID.fromString(token);
        } catch (Exception ex) {
            return new LogoutResponse(false, "Invalid session token");
        }

        Optional<Session> sessionOptional = sessionDao.findById(sessionId);
        if (!sessionOptional.isPresent()) {
            return new LogoutResponse(false, "Session not found");
        }

        Session session = sessionOptional.get();
        session.setStatus(SESSION_REVOKED);
        session.setRevokedReason("User logout");
        session.setExpiresAt(LocalDateTime.now());
        sessionDao.save(session);

        return new LogoutResponse(true, "Logout successful");
    }

    public CustomerProfileDto getCustomerProfile(String customerId) {
        UUID customerUuid = parseUuid(customerId);
        if (customerUuid == null) {
            throw new IllegalArgumentException("Invalid customer ID");
        }

        Customer customer = customerDao.findById(customerUuid)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        Optional<Account> accountOptional = accountDao.findByCustomerId(customerUuid);

        CustomerProfileDto profile = new CustomerProfileDto();
        profile.setFullName(defaultValue(customer.getFullName()));
        profile.setCustomerId(defaultValue(customerId));
        profile.setDateOfBirth(customer.getDateOfBirth() == null ? "" : customer.getDateOfBirth().toString());
        profile.setMobile(defaultValue(customer.getPhone()));
        profile.setEmail(defaultValue(customer.getEmail()));
        profile.setAddress(buildAddress(customer));
        profile.setAccountNumber(accountOptional.map(Account::getAccountNumber).orElse(""));
        profile.setAccountType(accountOptional.map(Account::getAccountType).orElse(defaultValue(customer.getAccountType())));
        profile.setBranchName(accountOptional.map(Account::getBranchCode).orElse(""));
        profile.setIfsc(accountOptional.map(account -> buildIfsc(account.getBranchCode())).orElse(""));
        profile.setNominee("-");
        profile.setAccountStatus(accountOptional.map(Account::getStatus).orElse(defaultValue(customer.getOnboardingStatus())));
        profile.setPan(defaultValue(customer.getPanNumber()));
        profile.setAadhaarMasked(maskAadhaar(customer.getAddhaarNum()));
        profile.setAddressProof("N/A");
        profile.setCommunication("Email + SMS");
        profile.setKycStatus(toKycStatus(customer.getOnboardingStatus()));

        return profile;
    }

    // ========== Private Helper Methods ==========

    /**
     * Verify if provided credentials are valid
     * @param customerId - Customer ID
     * @param password - Password
     * @return True if credentials are valid
     */
    private boolean verifyCredentials(String customerId, String password) {
        if (password == null || password.isEmpty() || customerId == null || customerId.isEmpty()) {
            return false;
        }

        UUID customerUuid;
        try {
            customerUuid = UUID.fromString(customerId);
        } catch (IllegalArgumentException ex) {
            return false;
        }

        return userDao.findByCustomerId(customerUuid)
                .map(user -> {
                    String storedPassword = user.getPwd();
                    if (storedPassword == null || storedPassword.isEmpty()) {
                        return false;
                    }
                    String hashedInput = hashPassword(password);
                    return storedPassword.equals(hashedInput) || storedPassword.equals(password);
                })
                .orElse(false);
    }

    private boolean isPasswordValid(String storedPassword, String inputPassword) {
        if (storedPassword == null || storedPassword.isEmpty()) {
            return false;
        }

        String hashedInput = hashPassword(inputPassword);
        return storedPassword.equals(hashedInput) || storedPassword.equals(inputPassword);
    }

    private boolean isBankerUser(User user) {
        if (user == null || user.getUserType() == null) {
            return false;
        }

        String userType = user.getUserType().trim().toUpperCase();
        return USER_TYPE_BANKER.equals(userType) || USER_TYPE_ADMIN.equals(userType);
    }

    private boolean isAdminUser(User user) {
        if (user == null || user.getUserType() == null) {
            return false;
        }

        return USER_TYPE_ADMIN.equalsIgnoreCase(user.getUserType().trim());
    }

    private String getUserEmail(String customerId) {
        try {
            UUID customerUuid = UUID.fromString(customerId);
            return userDao.findByCustomerId(customerUuid)
                    .map(User::getEmail)
                    .filter(email -> email != null && !email.trim().isEmpty())
                    .orElse(customerId + "@bank.com");
        } catch (IllegalArgumentException ex) {
            return customerId + "@bank.com";
        }
    }

    private Optional<User> getUserByCustomerId(String customerId) {
        try {
            UUID customerUuid = UUID.fromString(customerId);
            return userDao.findByCustomerId(customerUuid);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private Session createSession(User user, HttpServletRequest httpRequest) {
        LocalDateTime now = LocalDateTime.now();

        if (user != null && user.getUserId() != null) {
            List<Session> activeSessions = sessionDao.findAllByUserIdAndStatus(user.getUserId(), SESSION_ACTIVE);
            for (Session activeSession : activeSessions) {
                activeSession.setStatus(SESSION_REVOKED);
                activeSession.setRevokedReason("New login");
                activeSession.setExpiresAt(now);
                sessionDao.save(activeSession);
            }
        }

        Session session = new Session();
        session.setSessionId(UUID.randomUUID());
        session.setUserId(user.getUserId());
        session.setCreatedAt(now);
        session.setLastSeenAt(now);
        session.setExpiresAt(now.plusMinutes(SESSION_INACTIVITY_MINUTES));
        session.setStatus(SESSION_ACTIVE);
        session.setIpAddress(resolveIpAddress(httpRequest));
        session.setDeviceFingerprint(resolveDeviceFingerprint(httpRequest));
        session.setCorrelationId(session.getSessionId().toString());
        return sessionDao.save(session);
    }

    private String resolveIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.trim().isEmpty()) {
            return forwarded.split(",")[0].trim();
        }

        String ip = request.getRemoteAddr();
        return ip == null ? "unknown" : ip;
    }

    private String resolveDeviceFingerprint(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null || userAgent.trim().isEmpty()) {
            return "unknown";
        }

        return userAgent.length() > 200 ? userAgent.substring(0, 200) : userAgent;
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null) {
            return "";
        }

        String header = authorizationHeader.trim();
        if (!header.toLowerCase().startsWith("bearer ")) {
            return "";
        }

        return header.substring(7).trim();
    }

    private String hashPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            return rawPassword;
        }
    }

    private UUID parseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (Exception ex) {
            return null;
        }
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private AccountTransactionDto toTransactionDto(Transaction transaction) {
        String date = transaction.getPostedAt() == null ? "" : transaction.getPostedAt().toLocalDate().toString();
        String description = transaction.getNarration() == null || transaction.getNarration().trim().isEmpty()
                ? transaction.getTxnType()
                : transaction.getNarration();
        String type = "D".equalsIgnoreCase(transaction.getDirection()) ? "Debit" : "Credit";
        double amount = transaction.getAmount() == null ? 0.0 : transaction.getAmount().doubleValue();
        return new AccountTransactionDto(date, description == null ? "" : description, type, amount);
    }

    private String defaultValue(String value) {
        return value == null ? "" : value;
    }

    private String buildAddress(Customer customer) {
        StringBuilder builder = new StringBuilder();
        appendPart(builder, customer.getAddressLine1());
        appendPart(builder, customer.getAddressLine2());
        appendPart(builder, customer.getAddressLine3());
        appendPart(builder, customer.getCity());
        appendPart(builder, customer.getState());
        if (customer.getZip() != null) {
            appendPart(builder, String.valueOf(customer.getZip()));
        }
        return builder.toString();
    }

    private void appendPart(StringBuilder builder, String value) {
        String part = defaultValue(value).trim();
        if (part.isEmpty()) {
            return;
        }
        if (builder.length() > 0) {
            builder.append(", ");
        }
        builder.append(part);
    }

    private String buildIfsc(String branchCode) {
        String value = defaultValue(branchCode).trim();
        if (value.isEmpty()) {
            return "";
        }
        return "IBN" + value;
    }

    private String maskAadhaar(Integer aadhaar) {
        if (aadhaar == null) {
            return "";
        }
        String digits = String.valueOf(aadhaar);
        if (digits.length() <= 4) {
            return digits;
        }
        String last4 = digits.substring(digits.length() - 4);
        return "XXXX-XXXX-" + last4;
    }

    private String toKycStatus(String onboardingStatus) {
        String status = defaultValue(onboardingStatus).trim().toUpperCase();
        if ("APPROVED".equals(status)) {
            return "Verified";
        }
        if ("REJECTED".equals(status)) {
            return "Rejected";
        }
        return "Pending";
    }

    /**
     * Generate authentication token for customer
     * @param customerId - Customer ID
     * @return Generated token
     */
    private String generateAuthToken(String customerId) {
        // Placeholder implementation - replace with actual JWT or OAuth token generation
        long timestamp = System.currentTimeMillis();
        return "AUTH_" + customerId + "_" + timestamp;
    }

    /**
     * Check if account is locked due to failed login attempts
     * @param customerId - Customer ID
     * @return True if account is locked
     */
    private boolean isAccountLocked(String customerId) {
        Optional<User> userOptional = getUserByCustomerId(customerId);
        return isUserCurrentlyLocked(userOptional.orElse(null));
    }

    /**
     * Increment failed login attempts for a customer
     * @param customerId - Customer ID
     */
    private void incrementFailedAttempts(String customerId) {
        incrementFailedAttempts(customerId, null);
    }

    private void incrementFailedAttempts(String customerId, HttpServletRequest request) {
        Optional<User> userOptional = getUserByCustomerId(customerId);
        recordLoginAttempt(customerId, userOptional.orElse(null), request, LOGIN_OUTCOME_FAILED, FAILURE_REASON_INVALID_CREDENTIALS);
        if (userOptional.isPresent()) {
            applyLockIfThresholdReached(customerId, userOptional.get());
        }
    }

    /**
     * Reset login attempts to 0 after successful login
     * @param customerId - Customer ID
     */
    private void resetLoginAttempts(String customerId) {
        // No-op: failed attempts are evaluated in a rolling window and account unlock is admin-driven.
    }

    /**
     * Get remaining login attempts for a customer
     * @param customerId - Customer ID
     * @return Number of remaining attempts
     */
    private int getRemainingLoginAttempts(String customerId) {
        long failures = countRecentFailedAttempts(customerId, getUserByCustomerId(customerId).orElse(null));
        int remaining = (int) (MAX_LOGIN_ATTEMPTS - failures);
        return Math.max(0, remaining);
    }

    private boolean isUserCurrentlyLocked(User user) {
        if (user == null) {
            return false;
        }

        if (ACCOUNT_LOCKED.equalsIgnoreCase(defaultValue(user.getStatus()))) {
            return true;
        }

        return user.getLockedUntil() != null;
    }

    private void resetUserLockIfExpired(User user) {
        // Intentionally no-op. Unlock is admin-controlled.
    }

    private void applyLockIfThresholdReached(String loginKey, User user) {
        if (user == null) {
            return;
        }

        long failures = countRecentFailedAttempts(loginKey, user);
        if (failures >= MAX_LOGIN_ATTEMPTS) {
            user.setStatus(USER_LOCKED);
            user.setLockedUntil(LocalDateTime.now().plusMinutes(ACCOUNT_LOCK_MINUTES));
            user.setUpdatedAt(LocalDateTime.now());
            userDao.save(user);
        }
    }

    private UUID extractActorUserId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) {
            return null;
        }

        try {
            String value = String.valueOf(userIdAttr).trim();
            if (value.isEmpty()) {
                return null;
            }
            return UUID.fromString(value);
        } catch (Exception ex) {
            return null;
        }
    }

    private long countRecentFailedAttempts(String loginKey, User user) {
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(LOGIN_ATTEMPT_WINDOW_MINUTES);

        if (user != null && user.getUserId() != null) {
            return loginAttemptDao.countByUserIdAndOutcomeAndAttemptAtAfter(user.getUserId(), LOGIN_OUTCOME_FAILED, windowStart);
        }

        if (loginKey == null || loginKey.trim().isEmpty()) {
            return 0;
        }

        return loginAttemptDao.countByUsernameIgnoreCaseAndOutcomeAndAttemptAtAfter(loginKey.trim(), LOGIN_OUTCOME_FAILED, windowStart);
    }

    private void recordLoginAttempt(String loginKey, User user, HttpServletRequest request, String outcome, String failureReason) {
        LoginAttempt attempt = new LoginAttempt();
        attempt.setAttemptId(UUID.randomUUID());
        attempt.setUserId(user == null ? null : user.getUserId());
        attempt.setUsername(loginKey == null ? "" : loginKey.trim());
        attempt.setAttemptAt(LocalDateTime.now());
        attempt.setIpAddress(resolveIpAddress(request));
        attempt.setDeviceFingerprint(resolveDeviceFingerprint(request));
        attempt.setOutcome(outcome);
        attempt.setFailureReason(failureReason);
        attempt.setCorrelationId(UUID.randomUUID().toString());
        loginAttemptDao.save(attempt);
    }
}
