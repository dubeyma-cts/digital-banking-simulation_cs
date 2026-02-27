package com.ibn.controller;

import com.ibn.dto.ChequeDepositListItemDto;
import com.ibn.dto.ChequeDepositRequest;
import com.ibn.dto.ChequeDepositResponse;
import com.ibn.dto.ChequeStatusUpdateRequest;
import com.ibn.service.ChequeDepositService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cheque-deposits")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ChequeDepositController {

    private final ChequeDepositService chequeDepositService;

    @Autowired
    public ChequeDepositController(ChequeDepositService chequeDepositService) {
        this.chequeDepositService = chequeDepositService;
    }

    @PostMapping
    public ResponseEntity<ChequeDepositResponse> submit(@RequestBody ChequeDepositRequest request) {
        try {
            return ResponseEntity.ok(chequeDepositService.submitChequeDeposit(request));
        } catch (IllegalArgumentException ex) {
            ChequeDepositResponse response = new ChequeDepositResponse();
            response.setSuccess(false);
            response.setMessage(ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception ex) {
            ChequeDepositResponse response = new ChequeDepositResponse();
            response.setSuccess(false);
            response.setMessage("Unable to submit cheque deposit. " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<ChequeDepositListItemDto>> getByCustomer(@PathVariable String customerId) {
        return ResponseEntity.ok(chequeDepositService.getDepositsByCustomer(customerId));
    }

    @GetMapping
    public ResponseEntity<List<ChequeDepositListItemDto>> getAll() {
        return ResponseEntity.ok(chequeDepositService.getAllDeposits());
    }

    @PutMapping("/{referenceId}/status")
    public ResponseEntity<ChequeDepositResponse> updateStatus(
            @PathVariable String referenceId,
            @RequestBody ChequeStatusUpdateRequest request) {
        try {
            return ResponseEntity.ok(chequeDepositService.updateStatus(referenceId, request));
        } catch (IllegalArgumentException ex) {
            ChequeDepositResponse response = new ChequeDepositResponse();
            response.setSuccess(false);
            response.setMessage(ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception ex) {
            ChequeDepositResponse response = new ChequeDepositResponse();
            response.setSuccess(false);
            response.setMessage("Unable to update cheque status. " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
