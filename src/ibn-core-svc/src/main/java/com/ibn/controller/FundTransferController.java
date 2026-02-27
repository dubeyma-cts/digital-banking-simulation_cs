package com.ibn.controller;

import com.ibn.dto.FundTransferRequest;
import com.ibn.dto.FundTransferResponse;
import com.ibn.dto.PayeeValidationResponse;
import com.ibn.service.FundTransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fund-transfers")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class FundTransferController {

    private final FundTransferService fundTransferService;

    @Autowired
    public FundTransferController(FundTransferService fundTransferService) {
        this.fundTransferService = fundTransferService;
    }

    @GetMapping("/payee/{payeeAccountNumber}")
    public ResponseEntity<PayeeValidationResponse> validatePayee(@PathVariable String payeeAccountNumber) {
        return ResponseEntity.ok(fundTransferService.validatePayee(payeeAccountNumber));
    }

    @PostMapping
    public ResponseEntity<FundTransferResponse> transfer(@RequestBody FundTransferRequest request) {
        try {
            FundTransferResponse response = fundTransferService.transfer(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            FundTransferResponse response = new FundTransferResponse();
            response.setSuccess(false);
            response.setMessage(ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
