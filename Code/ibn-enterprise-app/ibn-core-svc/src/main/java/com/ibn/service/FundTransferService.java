package com.ibn.service;

import com.ibn.core.entity.*;
import com.ibn.dao.*;
import com.ibn.dto.FundTransferRequest;
import com.ibn.dto.FundTransferResponse;
import com.ibn.dto.PayeeValidationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class FundTransferService {

    private final AccountDao accountDao;
    private final AccountBalanceDao accountBalanceDao;
    private final CustomerDao customerDao;
    private final UserDao userDao;
    private final BeneficiaryDao beneficiaryDao;
    private final IdempotencyKeyDao idempotencyKeyDao;
    private final TransferInstructionDao transferInstructionDao;
    private final TransactionDao transactionDao;

    @Autowired
    public FundTransferService(
            AccountDao accountDao,
            AccountBalanceDao accountBalanceDao,
            CustomerDao customerDao,
            UserDao userDao,
            BeneficiaryDao beneficiaryDao,
            IdempotencyKeyDao idempotencyKeyDao,
            TransferInstructionDao transferInstructionDao,
            TransactionDao transactionDao) {
        this.accountDao = accountDao;
        this.accountBalanceDao = accountBalanceDao;
        this.customerDao = customerDao;
        this.userDao = userDao;
        this.beneficiaryDao = beneficiaryDao;
        this.idempotencyKeyDao = idempotencyKeyDao;
        this.transferInstructionDao = transferInstructionDao;
        this.transactionDao = transactionDao;
    }

    public PayeeValidationResponse validatePayee(String payeeAccountNumber) {
        String accountNumber = trim(payeeAccountNumber);
        if (accountNumber.isEmpty()) {
            return new PayeeValidationResponse(false, "", "", "Payee account number is required");
        }

        Optional<Account> payeeOpt = accountDao.findByAccountNumber(accountNumber);
        if (!payeeOpt.isPresent()) {
            return new PayeeValidationResponse(false, "", "", "Payee account not found");
        }

        Account payeeAccount = payeeOpt.get();
        String holderName = customerDao.findById(payeeAccount.getCustomerId())
                .map(Customer::getFullName)
                .orElse("");

        return new PayeeValidationResponse(
                true,
                holderName,
                payeeAccount.getAccountId() == null ? "" : payeeAccount.getAccountId().toString(),
                "Payee account verified");
    }

    @Transactional
    public FundTransferResponse transfer(FundTransferRequest request) {
        validateTransferRequest(request);

        UUID customerId = parseUuid(request.getCustomerId(), "Invalid customer ID");
        UUID sourceAccountId = parseUuid(request.getSourceAccountId(), "Invalid source account ID");
        BigDecimal amount = BigDecimal.valueOf(request.getAmount()).setScale(2, RoundingMode.HALF_UP);
        String payeeAccountNumber = trim(request.getPayeeAccountNumber());

        Account sourceAccount = accountDao.findById(sourceAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Source account not found"));

        if (!customerId.equals(sourceAccount.getCustomerId())) {
            throw new IllegalArgumentException("Source account does not belong to customer");
        }

        Account payeeAccount = accountDao.findByAccountNumber(payeeAccountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Payee account not found"));

        if (sourceAccount.getAccountId().equals(payeeAccount.getAccountId())) {
            throw new IllegalArgumentException("Source and payee account cannot be same");
        }

        AccountBalance sourceBalance = accountBalanceDao.findById(sourceAccount.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Source account balance not found"));
        AccountBalance payeeBalance = accountBalanceDao.findById(payeeAccount.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Payee account balance not found"));

        BigDecimal sourceAvailable = nonNullMoney(sourceBalance.getAvailableBalance());
        if (sourceAvailable.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        LocalDateTime now = LocalDateTime.now();
        sourceBalance.setAvailableBalance(sourceAvailable.subtract(amount));
        sourceBalance.setLedgerBalance(nonNullMoney(sourceBalance.getLedgerBalance()).subtract(amount));
        sourceBalance.setAsOf(now);

        payeeBalance.setAvailableBalance(nonNullMoney(payeeBalance.getAvailableBalance()).add(amount));
        payeeBalance.setLedgerBalance(nonNullMoney(payeeBalance.getLedgerBalance()).add(amount));
        payeeBalance.setAsOf(now);

        accountBalanceDao.save(sourceBalance);
        accountBalanceDao.save(payeeBalance);

        String payeeName = customerDao.findById(payeeAccount.getCustomerId()).map(Customer::getFullName).orElse("");
        Beneficiary beneficiary = beneficiaryDao.findByCustomerIdAndAccountNumber(customerId, payeeAccountNumber)
                .orElseGet(() -> createBeneficiary(customerId, payeeAccountNumber, payeeName, now));

        User ownerUser = userDao.findByCustomerId(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer user not found"));

        String reference = generateReference();
        IdempotencyKey idempotencyKey = createIdempotencyKey(ownerUser.getUserId(), reference, now);
        idempotencyKeyDao.save(idempotencyKey);

        UUID transferId = UUID.randomUUID();
        TransferInstruction transferInstruction = new TransferInstruction();
        transferInstruction.setTransferId(transferId);
        transferInstruction.setCustomerId(customerId);
        transferInstruction.setSourceAccountId(sourceAccount.getAccountId());
        transferInstruction.setBeneficiaryId(beneficiary.getBeneficiaryId());
        transferInstruction.setAmount(amount);
        transferInstruction.setCurrency(defaultCurrency(sourceAccount.getCurrency()));
        transferInstruction.setMode("INTERNAL");
        transferInstruction.setStatus("SUCCESS");
        transferInstruction.setIdempotencyKeyId(idempotencyKey.getKeyId());
        transferInstruction.setCreatedAt(now);
        transferInstruction.setCompletedAt(now);
        transferInstruction.setReference(reference);
        transferInstruction.setCorrelationId(reference);
        transferInstructionDao.save(transferInstruction);

        transactionDao.save(createTransaction(sourceAccount.getAccountId(), "TRANSFER", "D", amount, sourceAccount.getCurrency(), now, request.getRemarks(), reference));
        transactionDao.save(createTransaction(payeeAccount.getAccountId(), "TRANSFER", "C", amount, payeeAccount.getCurrency(), now, request.getRemarks(), reference));

        FundTransferResponse response = new FundTransferResponse();
        response.setSuccess(true);
        response.setMessage("Fund transfer successful");
        response.setTransactionId(transferId.toString());
        response.setReference(reference);
        response.setFromAccountNumber(sourceAccount.getAccountNumber());
        response.setPayeeAccountNumber(payeeAccount.getAccountNumber());
        response.setPayeeAccountHolderName(payeeName);
        response.setAmount(amount.doubleValue());
        response.setCurrency(defaultCurrency(sourceAccount.getCurrency()));
        response.setTransferredAt(now.toString());
        return response;
    }

    private void validateTransferRequest(FundTransferRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Transfer request is required");
        }

        if (trim(request.getCustomerId()).isEmpty() || trim(request.getSourceAccountId()).isEmpty()) {
            throw new IllegalArgumentException("Customer and source account are required");
        }

        String payee = trim(request.getPayeeAccountNumber());
        String confirm = trim(request.getConfirmPayeeAccountNumber());
        if (payee.isEmpty() || confirm.isEmpty()) {
            throw new IllegalArgumentException("Payee account number and confirmation are required");
        }

        if (!payee.equals(confirm)) {
            throw new IllegalArgumentException("Payee account number and confirmation must match");
        }

        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero");
        }
    }

    private Beneficiary createBeneficiary(UUID customerId, String payeeAccountNumber, String payeeName, LocalDateTime now) {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setBeneficiaryId(UUID.randomUUID());
        beneficiary.setCustomerId(customerId);
        beneficiary.setName(trim(payeeName).isEmpty() ? "Payee" : trim(payeeName));
        beneficiary.setBankName("iBN Bank");
        beneficiary.setIfsc("IBN0000001");
        beneficiary.setAccountNumber(payeeAccountNumber);
        beneficiary.setBeneficiaryType("INTERNAL");
        beneficiary.setStatus("ACTIVE");
        beneficiary.setCreatedAt(now);
        beneficiary.setUpdatedAt(now);
        return beneficiaryDao.save(beneficiary);
    }

    private IdempotencyKey createIdempotencyKey(UUID ownerUserId, String reference, LocalDateTime now) {
        IdempotencyKey key = new IdempotencyKey();
        key.setKeyId(UUID.randomUUID());
        key.setOwnerUserId(ownerUserId);
        key.setScope("TRANSFER");
        key.setStatus("COMPLETED");
        key.setResponseRef(reference);
        key.setCreatedAt(now);
        key.setExpiresAt(now.plusDays(1));
        byte[] hash = sha256(reference + now.toString());
        key.setKeyHash(hash);
        key.setRequestFingerprint(hash);
        return key;
    }

    private Transaction createTransaction(UUID accountId, String txnType, String direction, BigDecimal amount, String currency, LocalDateTime now, String remarks, String reference) {
        Transaction transaction = new Transaction();
        transaction.setTxnId(UUID.randomUUID());
        transaction.setAccountId(accountId);
        transaction.setTxnType(txnType);
        transaction.setDirection(direction);
        transaction.setAmount(amount);
        transaction.setCurrency(defaultCurrency(currency));
        transaction.setPostedAt(now);
        transaction.setValueDate(LocalDate.now());
        transaction.setStatus("POSTED");
        transaction.setReference(reference);
        transaction.setNarration(trim(remarks).isEmpty() ? "Fund Transfer" : trim(remarks));
        transaction.setCorrelationId(reference);
        return transaction;
    }

    private BigDecimal nonNullMoney(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : amount.setScale(2, RoundingMode.HALF_UP);
    }

    private UUID parseUuid(String value, String errorMessage) {
        try {
            return UUID.fromString(trim(value));
        } catch (Exception ex) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private String generateReference() {
        return "TRF" + System.currentTimeMillis() + (int) (Math.random() * 10000);
    }

    private String defaultCurrency(String currency) {
        String value = trim(currency);
        return value.isEmpty() ? "INR" : value;
    }

    private byte[] sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            return value.getBytes(StandardCharsets.UTF_8);
        }
    }
}
