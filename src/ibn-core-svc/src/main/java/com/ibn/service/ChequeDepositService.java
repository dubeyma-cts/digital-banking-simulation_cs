package com.ibn.service;

import com.ibn.core.entity.Account;
import com.ibn.core.entity.ChequeDeposit;
import com.ibn.core.entity.ChequeStatusHistory;
import com.ibn.core.entity.User;
import com.ibn.dao.AccountDao;
import com.ibn.dao.ChequeDepositDao;
import com.ibn.dao.ChequeStatusHistoryDao;
import com.ibn.dao.UserDao;
import com.ibn.dto.ChequeDepositListItemDto;
import com.ibn.dto.ChequeDepositRequest;
import com.ibn.dto.ChequeDepositResponse;
import com.ibn.dto.ChequeStatusUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ChequeDepositService {

    private static final String INITIAL_STATUS = "Not received";
    private static final Set<String> ALLOWED_STATUSES = new HashSet<String>();

    static {
        ALLOWED_STATUSES.add("Not received");
        ALLOWED_STATUSES.add("Received");
        ALLOWED_STATUSES.add("Sent for Clearance");
    }

    private final ChequeDepositDao chequeDepositDao;
    private final ChequeStatusHistoryDao chequeStatusHistoryDao;
    private final AccountDao accountDao;
    private final UserDao userDao;
    private final ChequeAttachmentBlobStorageService chequeAttachmentBlobStorageService;

    @Autowired
    public ChequeDepositService(ChequeDepositDao chequeDepositDao,
                                ChequeStatusHistoryDao chequeStatusHistoryDao,
                                AccountDao accountDao,
                                UserDao userDao,
                                ChequeAttachmentBlobStorageService chequeAttachmentBlobStorageService) {
        this.chequeDepositDao = chequeDepositDao;
        this.chequeStatusHistoryDao = chequeStatusHistoryDao;
        this.accountDao = accountDao;
        this.userDao = userDao;
        this.chequeAttachmentBlobStorageService = chequeAttachmentBlobStorageService;
    }

    @Transactional
    public ChequeDepositResponse submitChequeDeposit(ChequeDepositRequest request) {
        validate(request);

        UUID customerId = parseUuid(request.getCustomerId(), "Invalid customer id");
        UUID accountId = parseUuid(request.getAccountId(), "Invalid account id");
        Account account = accountDao.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (!customerId.equals(account.getCustomerId())) {
            throw new IllegalArgumentException("Selected account does not belong to customer");
        }

        LocalDateTime now = LocalDateTime.now();
        UUID depositId = UUID.randomUUID();

        ChequeDeposit chequeDeposit = new ChequeDeposit();
        chequeDeposit.setChequeDepositId(depositId);
        chequeDeposit.setCustomerId(customerId);
        chequeDeposit.setAccountId(accountId);
        chequeDeposit.setDepositorName(trim(request.getDepositorName()));
        chequeDeposit.setChequeNumber(trim(request.getChequeNumber()));
        chequeDeposit.setChequeDate(parseDate(request.getChequeDate()));
        chequeDeposit.setDrawerBank(trim(request.getBankName()));
        chequeDeposit.setBranchName(trim(request.getBranchName()));
        chequeDeposit.setAmount(BigDecimal.valueOf(request.getAmount()));
        chequeDeposit.setCurrency(defaultCurrency(account.getCurrency()));
        chequeDeposit.setSubmittedAt(now);
        chequeDeposit.setCurrentStatus(INITIAL_STATUS);
        chequeDeposit.setClearanceSLADays(2);
        chequeDeposit.setBouncePenaltyAmount(BigDecimal.ZERO);
        chequeDeposit.setRemarks(trim(request.getRemarks()));
        chequeDeposit.setAttachmentName(trim(request.getAttachmentName()));

        byte[] attachmentContent = decodeBase64(request.getAttachmentBase64());
        if (attachmentContent != null && attachmentContent.length > 0 && chequeAttachmentBlobStorageService.isEnabled()) {
            String blobUrl = chequeAttachmentBlobStorageService.upload(
                    depositId,
                    chequeDeposit.getAttachmentName(),
                    attachmentContent
            );
            chequeDeposit.setAttachmentBlobUrl(blobUrl);
            chequeDeposit.setAttmtDoc(null);
        } else {
            chequeDeposit.setAttachmentBlobUrl("");
            chequeDeposit.setAttmtDoc(attachmentContent);
        }

        chequeDeposit.setUpdatedAt(now);
        chequeDepositDao.save(chequeDeposit);

        ChequeStatusHistory history = new ChequeStatusHistory();
        history.setHistoryId(UUID.randomUUID());
        history.setChequeDepositId(depositId);
        history.setOldStatus("NEW");
        history.setNewStatus(INITIAL_STATUS);
        history.setChangedBy(resolveChangedBy(request.getChangedByUserId(), customerId));
        history.setChangedAt(now);
        history.setRemarks(trim(request.getRemarks()));
        chequeStatusHistoryDao.save(history);

        ChequeDepositResponse response = new ChequeDepositResponse();
        response.setSuccess(true);
        response.setMessage("Cheque deposit submitted successfully");
        response.setReferenceId(depositId.toString());
        response.setStatus(INITIAL_STATUS);
        return response;
    }

    public List<ChequeDepositListItemDto> getDepositsByCustomer(String customerId) {
        UUID customerUuid = parseUuid(customerId, "Invalid customer id");
        List<ChequeDeposit> deposits = chequeDepositDao.findAllByCustomerIdOrderBySubmittedAtDesc(customerUuid);
        return mapDeposits(deposits);
    }

    public List<ChequeDepositListItemDto> getAllDeposits() {
        return mapDeposits(chequeDepositDao.findAllByOrderBySubmittedAtDesc());
    }

    @Transactional
    public ChequeDepositResponse updateStatus(String referenceId, ChequeStatusUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request is required");
        }

        UUID depositId = parseUuid(referenceId, "Invalid cheque reference id");
        String nextStatus = trim(request.getStatus());
        if (!ALLOWED_STATUSES.contains(nextStatus)) {
            throw new IllegalArgumentException("Invalid cheque status");
        }

        ChequeDeposit chequeDeposit = chequeDepositDao.findById(depositId)
                .orElseThrow(() -> new IllegalArgumentException("Cheque deposit not found"));

        String oldStatus = defaultValue(chequeDeposit.getCurrentStatus());
        if (oldStatus.equals(nextStatus)) {
            ChequeDepositResponse noChangeResponse = new ChequeDepositResponse();
            noChangeResponse.setSuccess(true);
            noChangeResponse.setMessage("Status unchanged");
            noChangeResponse.setReferenceId(referenceId);
            noChangeResponse.setStatus(oldStatus);
            return noChangeResponse;
        }

        LocalDateTime now = LocalDateTime.now();
        chequeDeposit.setCurrentStatus(nextStatus);
        chequeDeposit.setUpdatedAt(now);
        chequeDepositDao.save(chequeDeposit);

        ChequeStatusHistory history = new ChequeStatusHistory();
        history.setHistoryId(UUID.randomUUID());
        history.setChequeDepositId(depositId);
        history.setOldStatus(oldStatus.isEmpty() ? "NEW" : oldStatus);
        history.setNewStatus(nextStatus);
        history.setChangedBy(resolveChangedBy(request.getChangedByUserId(), chequeDeposit.getCustomerId()));
        history.setChangedAt(now);
        history.setRemarks(trim(request.getRemarks()));
        chequeStatusHistoryDao.save(history);

        ChequeDepositResponse response = new ChequeDepositResponse();
        response.setSuccess(true);
        response.setMessage("Cheque status updated successfully");
        response.setReferenceId(referenceId);
        response.setStatus(nextStatus);
        return response;
    }

    private List<ChequeDepositListItemDto> mapDeposits(List<ChequeDeposit> deposits) {
        List<ChequeDepositListItemDto> items = new ArrayList<>();

        for (ChequeDeposit deposit : deposits) {
            ChequeDepositListItemDto item = new ChequeDepositListItemDto();
            item.setReferenceId(deposit.getChequeDepositId() == null ? "" : deposit.getChequeDepositId().toString());
            item.setDepositorName(defaultValue(deposit.getDepositorName()));
            item.setAccountId(deposit.getAccountId() == null ? "" : deposit.getAccountId().toString());
            item.setAccountNumber(resolveAccountNumber(deposit.getAccountId()));
            item.setChequeNumber(defaultValue(deposit.getChequeNumber()));
            item.setChequeDate(deposit.getChequeDate() == null ? "" : deposit.getChequeDate().toString());
            item.setBankName(defaultValue(deposit.getDrawerBank()));
            item.setBranchName(defaultValue(deposit.getBranchName()));
            item.setAmount(deposit.getAmount() == null ? 0 : deposit.getAmount().doubleValue());
            item.setRemarks(defaultValue(deposit.getRemarks()));
            item.setStatus(defaultValue(deposit.getCurrentStatus()));
            item.setAttachmentName(defaultValue(deposit.getAttachmentName()));
            items.add(item);
        }

        return items;
    }

    private String resolveAccountNumber(UUID accountId) {
        if (accountId == null) {
            return "";
        }

        return accountDao.findById(accountId)
                .map(Account::getAccountNumber)
                .orElse("");
    }

    private void validate(ChequeDepositRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request is required");
        }
        if (trim(request.getCustomerId()).isEmpty()) {
            throw new IllegalArgumentException("Customer id is required");
        }
        if (trim(request.getAccountId()).isEmpty()) {
            throw new IllegalArgumentException("Account is required");
        }
        if (trim(request.getDepositorName()).isEmpty()) {
            throw new IllegalArgumentException("Depositor name is required");
        }
        if (trim(request.getChequeNumber()).isEmpty()) {
            throw new IllegalArgumentException("Cheque number is required");
        }
        if (trim(request.getChequeDate()).isEmpty()) {
            throw new IllegalArgumentException("Cheque date is required");
        }
        if (trim(request.getBankName()).isEmpty()) {
            throw new IllegalArgumentException("Bank name is required");
        }
        if (trim(request.getBranchName()).isEmpty()) {
            throw new IllegalArgumentException("Branch name is required");
        }
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }

    private UUID resolveChangedBy(String changedByUserId, UUID customerId) {
        String changedBy = trim(changedByUserId);
        if (!changedBy.isEmpty()) {
            try {
                UUID changedByUuid = UUID.fromString(changedBy);
                if (userDao.existsById(changedByUuid)) {
                    return changedByUuid;
                }
            } catch (Exception ignored) {
            }
        }

        User user = userDao.findByCustomerId(customerId)
                .orElseThrow(() -> new IllegalArgumentException("User not found for customer"));
        return user.getUserId();
    }

    private LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(trim(value));
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid cheque date");
        }
    }

    private byte[] decodeBase64(String base64) {
        String value = trim(base64);
        if (value.isEmpty()) {
            return null;
        }

        String normalized = value;
        int separator = value.indexOf(',');
        if (separator >= 0) {
            normalized = value.substring(separator + 1);
        }

        try {
            return Base64.getDecoder().decode(normalized);
        } catch (IllegalArgumentException ex) {
            return normalized.getBytes(StandardCharsets.UTF_8);
        }
    }

    private UUID parseUuid(String value, String errorMessage) {
        try {
            return UUID.fromString(trim(value));
        } catch (Exception ex) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private String defaultCurrency(String currency) {
        String value = trim(currency);
        return value.isEmpty() ? "INR" : value;
    }

    private String defaultValue(String value) {
        return value == null ? "" : value;
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
