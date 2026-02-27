package com.ibn.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ibn.dto.*;
import com.ibn.service.AuthenticationService;

import javax.servlet.http.HttpServletRequest;

/**
 * REST Controller for handling authentication endpoints
 * Maps to /api/auth base path
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Authenticate user with customer ID and password
     * POST /api/auth/login
     * 
     * @param loginRequest - Request body containing customerId and password
     * @return ResponseEntity with LoginResponse containing authentication details
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        LoginResponse response = authenticationService.authenticate(loginRequest, request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/banker-login")
    public ResponseEntity<LoginResponse> bankerLogin(@RequestBody BankerLoginRequest loginRequest, HttpServletRequest request) {
        LoginResponse response = authenticationService.authenticateBanker(loginRequest, request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    /**
     * Validate CAPTCHA value
     * POST /api/auth/validate-captcha
     * 
     * @param captchaRequest - Request body containing captcha value
     * @return ResponseEntity with CaptchaResponse indicating validation result
     */
    @PostMapping("/validate-captcha")
    public ResponseEntity<CaptchaResponse> validateCaptcha(@RequestBody CaptchaRequest captchaRequest) {
        CaptchaResponse response = authenticationService.validateCaptcha(captchaRequest);
        
        if (response.isValid()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Check account status for a customer
     * GET /api/auth/account-status/{customerId}
     * 
     * @param customerId - Customer ID to check status for
     * @return ResponseEntity with AccountStatusResponse containing account details
     */
    @GetMapping("/account-status/{customerId}")
    public ResponseEntity<AccountStatusResponse> checkAccountStatus(
            @PathVariable String customerId) {
        AccountStatusResponse response = authenticationService.checkAccountStatus(customerId);
        
        if (!response.isLocked()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
    }

    /**
     * Get remaining login attempts for a customer
     * GET /api/auth/remaining-attempts/{customerId}
     * 
     * @param customerId - Customer ID to check remaining attempts for
     * @return ResponseEntity with RemainingAttemptsResponse containing attempt count
     */
    @GetMapping("/remaining-attempts/{customerId}")
    public ResponseEntity<RemainingAttemptsResponse> getRemainingAttempts(
            @PathVariable String customerId) {
        RemainingAttemptsResponse response = authenticationService.getRemainingAttempts(customerId);
        
        if (response.getRemainingAttempts() > 0) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
    }

    @GetMapping("/profile/{customerId}")
    public ResponseEntity<CustomerProfileDto> getCustomerProfile(@PathVariable String customerId) {
        try {
            return ResponseEntity.ok(authenticationService.getCustomerProfile(customerId));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Logout user and invalidate session
     * POST /api/auth/logout
     * 
     * @return ResponseEntity with LogoutResponse indicating logout status
     */
    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        LogoutResponse response = authenticationService.logout(authorizationHeader);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/admin/users/{userId}/unlock")
    public ResponseEntity<UnlockUserResponse> unlockUser(
            @PathVariable String userId,
            HttpServletRequest request) {
        UnlockUserResponse response = authenticationService.unlockUserByAdmin(userId, request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }

        if ("User not found".equals(response.getMessage())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        if ("Only admin can unlock account".equals(response.getMessage())
                || "Admin session is required".equals(response.getMessage())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Health check endpoint for authentication service
     * GET /api/auth/health
     * 
     * @return ResponseEntity with success message
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Authentication service is running");
    }
}
