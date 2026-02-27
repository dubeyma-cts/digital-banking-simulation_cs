package com.ibn.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ibn.dto.AccountInfo;
import com.ibn.dto.AccountSummaryDto;
import com.ibn.dto.AccountTransactionDto;
import com.ibn.service.AuthenticationService;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AccountController {

    private final AuthenticationService authenticationService;

    @Autowired
    public AccountController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<AccountInfo> getAccount(@PathVariable String customerId) {
        AccountInfo info = authenticationService.getAccountInfo(customerId);
        return ResponseEntity.ok(info);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<AccountSummaryDto>> getAccounts(@PathVariable String customerId) {
        return ResponseEntity.ok(authenticationService.getAccountsByCustomer(customerId));
    }

    @GetMapping("/{accountId}/mini-statement")
    public ResponseEntity<List<AccountTransactionDto>> getMiniStatement(
            @PathVariable String accountId,
            @RequestParam(name = "limit", defaultValue = "5") int limit) {
        return ResponseEntity.ok(authenticationService.getMiniStatement(accountId, limit));
    }

    @GetMapping("/{accountId}/detailed-statement")
    public ResponseEntity<List<AccountTransactionDto>> getDetailedStatement(
            @PathVariable String accountId,
            @RequestParam(name = "fromDate", required = false) String fromDate,
            @RequestParam(name = "toDate", required = false) String toDate) {
        return ResponseEntity.ok(authenticationService.getDetailedStatement(accountId, fromDate, toDate));
    }
}
