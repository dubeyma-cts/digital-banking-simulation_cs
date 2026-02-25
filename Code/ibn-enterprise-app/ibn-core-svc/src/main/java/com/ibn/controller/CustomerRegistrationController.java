package com.ibn.controller;

import com.ibn.dto.CustomerRegistrationListItem;
import com.ibn.dto.CustomerRegistrationRequest;
import com.ibn.dto.CustomerRegistrationResponse;
import com.ibn.service.CustomerRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CustomerRegistrationController {

    private final CustomerRegistrationService customerRegistrationService;

    @Autowired
    public CustomerRegistrationController(CustomerRegistrationService customerRegistrationService) {
        this.customerRegistrationService = customerRegistrationService;
    }

    @PostMapping("/save")
    public ResponseEntity<CustomerRegistrationResponse> saveCustomer(
            @RequestBody CustomerRegistrationRequest request) {
        CustomerRegistrationResponse response = customerRegistrationService.saveCustomer(request);
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @PutMapping("/update/{customerId}")
    public ResponseEntity<CustomerRegistrationResponse> updateCustomer(
            @PathVariable String customerId,
            @RequestBody CustomerRegistrationRequest request) {
        CustomerRegistrationResponse response = customerRegistrationService.updateCustomer(customerId, request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        if ("Customer not found".equals(response.getMessage())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @GetMapping("/registrations")
    public ResponseEntity<List<CustomerRegistrationListItem>> getCustomerRegistrations() {
        return ResponseEntity.ok(customerRegistrationService.getCustomerRegistrations());
    }

    @PutMapping("/{customerId}/approve")
    public ResponseEntity<CustomerRegistrationResponse> approveCustomer(@PathVariable String customerId) {
        CustomerRegistrationResponse response = customerRegistrationService.approveCustomer(customerId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        if ("Customer not found".equals(response.getMessage())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @PutMapping("/{customerId}/reject")
    public ResponseEntity<CustomerRegistrationResponse> rejectCustomer(@PathVariable String customerId) {
        CustomerRegistrationResponse response = customerRegistrationService.rejectCustomer(customerId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        if ("Customer not found".equals(response.getMessage())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
