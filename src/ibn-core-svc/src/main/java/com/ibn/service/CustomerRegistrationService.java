package com.ibn.service;

import com.ibn.core.entity.Customer;
import com.ibn.core.entity.User;
import com.ibn.core.entity.Account;
import com.ibn.core.entity.AccountBalance;
import com.ibn.dao.AccountBalanceDao;
import com.ibn.dao.AccountDao;
import com.ibn.dao.CustomerDao;
import com.ibn.dao.UserDao;
import com.ibn.dto.CustomerRegistrationListItem;
import com.ibn.dto.CustomerRegistrationRequest;
import com.ibn.dto.CustomerRegistrationResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CustomerRegistrationService {

    private static final DateTimeFormatter REGISTRATION_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final CustomerDao customerDao;
    private final UserDao userDao;
    private final AccountDao accountDao;
    private final AccountBalanceDao accountBalanceDao;

    @Autowired
    public CustomerRegistrationService(
            CustomerDao customerDao,
            UserDao userDao,
            AccountDao accountDao,
            AccountBalanceDao accountBalanceDao) {
        this.customerDao = customerDao;
        this.userDao = userDao;
        this.accountDao = accountDao;
        this.accountBalanceDao = accountBalanceDao;
    }

    public CustomerRegistrationResponse saveCustomer(CustomerRegistrationRequest request) {
        if (request == null) {
            return new CustomerRegistrationResponse(false, "Request body is required");
        }

        Customer customer = new Customer();
        customer.setCustomerId(UUID.randomUUID());
        mapRequestToCustomer(request, customer, true);

        try {
            Customer savedCustomer = customerDao.save(customer);
            return new CustomerRegistrationResponse(true, "Customer registration saved successfully", savedCustomer.getCustomerId().toString());
        } catch (DataIntegrityViolationException ex) {
            return new CustomerRegistrationResponse(false, mapCustomerPersistenceError(ex));
        } catch (Exception ex) {
            return new CustomerRegistrationResponse(false, "Unable to save customer registration");
        }
    }

    public CustomerRegistrationResponse updateCustomer(String customerId, CustomerRegistrationRequest request) {
        if (request == null) {
            return new CustomerRegistrationResponse(false, "Request body is required");
        }

        UUID customerUuid;
        try {
            customerUuid = UUID.fromString(customerId);
        } catch (IllegalArgumentException ex) {
            return new CustomerRegistrationResponse(false, "Invalid customer ID format");
        }

        Optional<Customer> existingCustomer = customerDao.findById(customerUuid);
        if (!existingCustomer.isPresent()) {
            return new CustomerRegistrationResponse(false, "Customer not found");
        }

        Customer customer = existingCustomer.get();
        mapRequestToCustomer(request, customer, false);

        try {
            Customer updatedCustomer = customerDao.save(customer);
            return new CustomerRegistrationResponse(true, "Customer registration updated successfully", updatedCustomer.getCustomerId().toString());
        } catch (DataIntegrityViolationException ex) {
            return new CustomerRegistrationResponse(false, mapCustomerPersistenceError(ex));
        } catch (Exception ex) {
            return new CustomerRegistrationResponse(false, "Unable to update customer registration");
        }
    }

    public List<CustomerRegistrationListItem> getCustomerRegistrations() {
        return customerDao.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toCustomerRegistrationListItem)
                .collect(Collectors.toList());
    }

    public CustomerRegistrationResponse approveCustomer(String customerId) {
        return updateCustomerStatus(customerId, "APPROVED", "Customer registration approved successfully");
    }

    public CustomerRegistrationResponse rejectCustomer(String customerId) {
        return updateCustomerStatus(customerId, "REJECTED", "Customer registration rejected successfully");
    }

    private void mapRequestToCustomer(CustomerRegistrationRequest request, Customer customer, boolean isNewCustomer) {
        customer.setUsername(trimToEmpty(request.getUsername()));
        customer.setDefaultPwd(trimToEmpty(request.getPassword()));
        customer.setFullName(buildFullName(request.getFirstName(), request.getLastName()));
        customer.setAddressLine1(request.getAddressLine1());
        customer.setAddressLine2(request.getAddressLine2());
        customer.setAddressLine3(request.getAddressLine3());
        customer.setCity(trimToEmpty(request.getCity()));
        customer.setState(trimToEmpty(request.getState()));
        customer.setZip(parseIntegerOrDefault(request.getZip(), 0));
        customer.setAccountType(trimToEmpty(request.getAccountType()));
        customer.setPanNumber(trimToEmpty(request.getPan()));
        customer.setAddhaarNum(parseAadhaarToInt(request.getAadhaar()));
        customer.setPanEncrypted(trimToEmpty(request.getPan()).getBytes(StandardCharsets.UTF_8));
        customer.setAadhaarHash(createAadhaarHash(request.getAadhaar()));
        customer.setPhone(trimToEmpty(request.getPhone()));
        customer.setEmail(trimToEmpty(request.getEmail()));
        customer.setPreferredLanguage("en");
        customer.setOnboardingStatus("PENDING");

        LocalDateTime now = LocalDateTime.now();
        if (isNewCustomer || customer.getCreatedAt() == null) {
            customer.setCreatedAt(now);
        }
        customer.setUpdatedAt(now);
    }

    private String buildFullName(String firstName, String lastName) {
        String first = trimToEmpty(firstName);
        String last = trimToEmpty(lastName);
        return (first + " " + last).trim();
    }

    private int parseAadhaarToInt(String aadhaar) {
        String digits = trimToEmpty(aadhaar).replaceAll("\\D", "");
        if (digits.isEmpty()) {
            return 0;
        }

        try {
            long parsed = Long.parseLong(digits);
            return (int) (parsed % Integer.MAX_VALUE);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private Integer parseIntegerOrDefault(String value, int defaultValue) {
        String sanitized = trimToEmpty(value).replaceAll("\\D", "");
        if (sanitized.isEmpty()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(sanitized);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private byte[] createAadhaarHash(String aadhaar) {
        String value = trimToEmpty(aadhaar);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            return value.getBytes(StandardCharsets.UTF_8);
        }
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private CustomerRegistrationListItem toCustomerRegistrationListItem(Customer customer) {
        CustomerRegistrationListItem item = new CustomerRegistrationListItem();
        item.setCustomerId(customer.getCustomerId() == null ? "" : customer.getCustomerId().toString());

        String[] names = splitName(customer.getFullName());
        item.setFirstName(names[0]);
        item.setLastName(names[1]);

        item.setEmail(trimToEmpty(customer.getEmail()));
        item.setAccountType(trimToEmpty(customer.getAccountType()));
        item.setPhone(trimToEmpty(customer.getPhone()));
        item.setStatus(formatStatusForUi(customer.getOnboardingStatus()));

        if (customer.getCreatedAt() != null) {
            item.setRegistrationDate(customer.getCreatedAt().toLocalDate().format(REGISTRATION_DATE_FORMATTER));
        } else {
            item.setRegistrationDate("");
        }

        return item;
    }

    private String[] splitName(String fullName) {
        String value = trimToEmpty(fullName);
        if (value.isEmpty()) {
            return new String[]{"", ""};
        }

        String[] parts = value.split("\\s+", 2);
        String firstName = parts[0];
        String lastName = parts.length > 1 ? parts[1] : "";
        return new String[]{firstName, lastName};
    }

    @Transactional
    private CustomerRegistrationResponse updateCustomerStatus(String customerId, String onboardingStatus, String successMessage) {
        UUID customerUuid;
        try {
            customerUuid = UUID.fromString(customerId);
        } catch (IllegalArgumentException ex) {
            return new CustomerRegistrationResponse(false, "Invalid customer ID format");
        }

        Optional<Customer> existingCustomer = customerDao.findById(customerUuid);
        if (!existingCustomer.isPresent()) {
            return new CustomerRegistrationResponse(false, "Customer not found");
        }

        Customer customer = existingCustomer.get();
        customer.setOnboardingStatus(onboardingStatus);
        customer.setUpdatedAt(LocalDateTime.now());

        Customer savedCustomer = customerDao.save(customer);
        if ("APPROVED".equalsIgnoreCase(onboardingStatus)) {
            persistApprovalEntities(savedCustomer);
        }
        return new CustomerRegistrationResponse(true, successMessage, savedCustomer.getCustomerId().toString());
    }

    private void persistApprovalEntities(Customer customer) {
        LocalDateTime now = LocalDateTime.now();
        User user = userDao.findByCustomerId(customer.getCustomerId()).orElseGet(User::new);
        if (user.getUserId() == null) {
            user.setUserId(UUID.randomUUID());
            user.setCreatedAt(now);
        }
        user.setCustomerId(customer.getCustomerId());
        user.setIdpSubject(customer.getCustomerId() == null ? UUID.randomUUID().toString() : customer.getCustomerId().toString());
        user.setUsername(trimToEmpty(customer.getUsername()));
        user.setPwd(hashPassword(trimToEmpty(customer.getDefaultPwd())));
        user.setEmail(trimToEmpty(customer.getEmail()));
        user.setPhone(trimToEmpty(customer.getPhone()));
        user.setUserType("CUSTOMER");
        user.setStatus("ACTIVE");
        user.setUpdatedAt(now);
        User savedUser = userDao.save(user);

        Account account = accountDao.findByCustomerId(customer.getCustomerId()).orElseGet(Account::new);
        if (account.getAccountId() == null) {
            account.setAccountId(UUID.randomUUID());
            account.setCreatedAt(now);
            account.setOpenedAt(LocalDate.now());
            account.setAccountNumber(generateAccountNumber(customer));
        }
        account.setCustomerId(customer.getCustomerId());
        account.setAccountType(defaultIfBlank(customer.getAccountType(), "SAVINGS"));
        account.setBranchCode("BR001");
        account.setCurrency("INR");
        account.setStatus("ACTIVE");
        account.setUpdatedAt(now);
        Account savedAccount = accountDao.save(account);

        AccountBalance accountBalance = accountBalanceDao.findById(savedAccount.getAccountId()).orElseGet(AccountBalance::new);
        accountBalance.setAccountId(savedAccount.getAccountId());
        if (accountBalance.getAvailableBalance() == null) {
            accountBalance.setAvailableBalance(new BigDecimal("1000.00"));
        }
        if (accountBalance.getLedgerBalance() == null) {
            accountBalance.setLedgerBalance(new BigDecimal("1000.00"));
        }
        if (accountBalance.getOverdraftUsed() == null) {
            accountBalance.setOverdraftUsed(BigDecimal.ZERO);
        }
        accountBalance.setAsOf(now);
        accountBalanceDao.save(accountBalance);

    }

    private String generateAccountNumber(Customer customer) {
        String seed = customer.getCustomerId() == null ? UUID.randomUUID().toString() : customer.getCustomerId().toString().replace("-", "");
        String suffix = seed.length() > 12 ? seed.substring(0, 12) : seed;
        return "ACC" + suffix.toUpperCase();
    }

    private String defaultIfBlank(String value, String defaultValue) {
        String trimmed = trimToEmpty(value);
        return trimmed.isEmpty() ? defaultValue : trimmed;
    }

    private String hashPassword(String rawPassword) {
        String value = trimToEmpty(rawPassword);
        if (value.isEmpty()) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            return value;
        }
    }

    private String formatStatusForUi(String status) {
        String value = trimToEmpty(status).toUpperCase();
        if (value.isEmpty()) {
            return "";
        }
        return value.charAt(0) + value.substring(1).toLowerCase();
    }

    private String mapCustomerPersistenceError(DataIntegrityViolationException ex) {
        String message = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase(Locale.ROOT);
        if (message.contains("uq_customer_pan") || message.contains("pan_number")) {
            return "PAN already exists";
        }
        if (message.contains("uq_customer_aadhaar") || message.contains("aadhaar_hash")) {
            return "Aadhaar already exists";
        }
        if (message.contains("duplicate key")
                || message.contains("unique index")
                || message.contains("unique constraint")
                || message.contains("error code 2627")
                || message.contains("error code 2601")) {
            return "Customer with same PAN or Aadhaar already exists";
        }
        return "Unable to save customer registration";
    }
}
