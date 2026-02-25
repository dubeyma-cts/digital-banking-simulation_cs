import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { LoginService } from '../../services/login.service';

@Component({
  selector: 'app-banker-login',
  templateUrl: './banker-login.component.html',
  styleUrls: ['./banker-login.component.css']
})
export class BankerLoginComponent implements OnInit {
  loginId: string = '';
  password: string = '';
  attemptCount: number = 0;
  maxAttempts: number = 3;
  isAccountLocked: boolean = false;
  showCaptcha: boolean = false;
  showErrorMessage: boolean = false;
  errorMessage: string = '';
  isLoading: boolean = false;
  captchaValue: string = '';
  captchaError: string = '';
  isBankerUser: boolean = true; // Flag to indicate banker login

  // Sample test credentials for development/testing
  // Leave empty in production or connect to real backend API
  private readonly SAMPLE_BANKERS = [
    { loginId: 'banker001', password: 'Banker@123' },
    { loginId: 'banker002', password: 'Secure@Pass123' },
    { loginId: 'admin', password: 'Admin@123' }
  ];

  constructor(
    private loginService: LoginService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Initialize component
    // Sample credentials for testing:
    // Login ID: banker001, Password: Banker@123
    // Login ID: banker002, Password: Secure@Pass123
    // Login ID: admin, Password: Admin@123
  }

  /**
   * Handle banker login form submission
   */
  onLogin(): void {
    // Reset error message
    this.errorMessage = '';
    this.captchaError = '';
    this.showErrorMessage = false;

    // Validate form inputs
    if (!this.loginId.trim() || !this.password.trim()) {
      this.showErrorMessage = true;
      this.errorMessage = 'Please enter both Login ID and Password.';
      return;
    }

    // Check if account is locked
    if (this.isAccountLocked) {
      this.showErrorMessage = true;
      this.errorMessage = 'Your account has been locked. Please contact the administrator.';
      return;
    }

    // Validate captcha if enabled
    if (this.showCaptcha && !this.captchaValue.trim()) {
      this.captchaError = 'Please complete the CAPTCHA verification.';
      return;
    }

    // Send banker login request
    this.isLoading = true;
    const loginRequest = {
      loginId: this.loginId,
      password: this.password,
      captcha: this.showCaptcha ? this.captchaValue : undefined,
      userType: 'banker'
    };

    this.loginService.authenticateBanker(loginRequest).subscribe(
      (response) => {
        this.isLoading = false;
        if (response && response.success) {
          // Clear form and reset attempts
          this.loginId = '';
          this.password = '';
          this.attemptCount = 0;
          this.showCaptcha = false;
          this.captchaValue = '';

          // Store auth token and user type
          if (response.token) {
            localStorage.setItem('authToken', response.token);
            localStorage.setItem('userType', 'banker');
          }

          // Redirect to landing page
          this.router.navigate(['/landing']);
        } else {
          this.handleLoginFailure();
        }
      },
      (error) => {
        this.isLoading = false;
        // Try local test credentials as fallback
        this.tryLocalTestCredentials(loginRequest);
      }
    );
  }

  /**
   * Try to authenticate using sample test credentials (for development/testing)
   */
  private tryLocalTestCredentials(loginRequest: any): void {
    const testCredential = this.SAMPLE_BANKERS.find(
      b => b.loginId === loginRequest.loginId && b.password === loginRequest.password
    );

    if (testCredential) {
      // Simulate successful login with test credentials
      this.loginId = '';
      this.password = '';
      this.attemptCount = 0;
      this.showCaptcha = false;
      this.captchaValue = '';

      // Store auth token and user type
      localStorage.setItem('authToken', 'test-token-' + Date.now());
      localStorage.setItem('userType', 'banker');
      localStorage.setItem('userName', 'Banker (' + loginRequest.loginId + ')');

      // Redirect to landing page
      this.router.navigate(['/landing']);
    } else {
      this.handleLoginFailure();
    }
  }

  /**
   * Handle login failure scenarios
   */
  private handleLoginFailure(): void {
    this.attemptCount++;
    this.showErrorMessage = true;

    if (this.attemptCount >= this.maxAttempts) {
      this.isAccountLocked = true;
      this.errorMessage = 'Your account has been locked due to multiple failed login attempts. Please contact the administrator.';
    } else if (this.attemptCount === 1) {
      this.showCaptcha = true;
      this.generateCaptcha();
      this.errorMessage = `Invalid credentials. ${this.maxAttempts - this.attemptCount} attempt(s) remaining. CAPTCHA has been enabled.`;
    } else {
      this.errorMessage = `Invalid credentials. ${this.maxAttempts - this.attemptCount} attempt(s) remaining.`;
    }

    // Clear password field on failed attempt
    this.password = '';
    this.captchaValue = '';
  }

  /**
   * Generate a simple CAPTCHA
   */
  generateCaptcha(): void {
    const num1 = Math.floor(Math.random() * 10) + 1;
    const num2 = Math.floor(Math.random() * 10) + 1;
    this.captchaValue = `${num1 + num2}`;
  }

  /**
   * Navigate to forgot password page
   */
  onForgotPassword(): void {
    this.router.navigate(['/forgot-password']);
  }

  /**
   * Navigate back to login page (Personal)
   */
  onBackToPersonalLogin(): void {
    this.router.navigate(['/login']);
  }

  /**
   * Clear form on reset
   */
  resetForm(): void {
    this.loginId = '';
    this.password = '';
    this.captchaValue = '';
    this.errorMessage = '';
    this.captchaError = '';
    this.showErrorMessage = false;
  }
}
