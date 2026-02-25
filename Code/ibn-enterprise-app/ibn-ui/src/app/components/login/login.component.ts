import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { LoginService } from '../../services/login.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  customerId: string = '';
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
  showUserTypeDropdown: boolean = false;
  selectedUserType: string = 'Personal';

  constructor(
    private loginService: LoginService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Initialize component
    // Clear any banker login flag when returning to personal login
    localStorage.removeItem('userType');
  }

  /**
   * Handle login form submission
   */
  onLogin(): void {
    // Reset error message
    this.errorMessage = '';
    this.captchaError = '';
    this.showErrorMessage = false;

    // Validate form inputs
    if (!this.customerId.trim() || !this.password.trim()) {
      this.showErrorMessage = true;
      this.errorMessage = 'Please enter both Customer ID and Password.';
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

    // Send login request
    this.isLoading = true;
    const loginRequest = {
      customerId: this.customerId,
      password: this.password,
      captcha: this.showCaptcha ? this.captchaValue : undefined
    };

    this.loginService.authenticate(loginRequest).subscribe(
      (response) => {
        this.isLoading = false;
        if (response && response.success) {
          const loggedInCustomerId = this.customerId;
          // Clear form and reset attempts
          this.customerId = '';
          this.password = '';
          this.attemptCount = 0;
          this.showCaptcha = false;
          this.captchaValue = '';

          // Store auth token if provided
          if (response.token) {
            localStorage.setItem('authToken', response.token);
          }

          localStorage.setItem('customerId', loggedInCustomerId);
          localStorage.setItem('userId', response.userId || loggedInCustomerId);
          localStorage.setItem('email', response.email || '');
          localStorage.setItem('userType', 'personal');

          // Redirect to landing page
          this.router.navigate(['/landing']);
        } else {
          this.handleLoginFailure();
        }
      },
      (error) => {
        this.isLoading = false;
        this.handleLoginFailure();
      }
    );
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
    // Generate a simple numeric CAPTCHA (you can enhance this with image-based CAPTCHA)
    const num1 = Math.floor(Math.random() * 10) + 1;
    const num2 = Math.floor(Math.random() * 10) + 1;
    this.captchaValue = `${num1 + num2}`;
    // In a real application, you would display this in an image or obfuscated format
  }

  /**
   * Navigate to forgot password page
   */
  onForgotPassword(): void {
    this.router.navigate(['/forgot-password']);
  }

  /**
   * Navigate to forgot customer ID page
   */
  onForgotCustomerId(): void {
    this.router.navigate(['/forgot-customer-id']);
  }

  /**
   * Navigate to customer registration page
   */
  onCustomerRegistration(): void {
    this.router.navigate(['/customer-registration']);
  }

  /**
   * Clear form on reset
   */
  resetForm(): void {
    this.customerId = '';
    this.password = '';
    this.captchaValue = '';
    this.errorMessage = '';
    this.captchaError = '';
    this.showErrorMessage = false;
  }

  /**
   * Toggle user type dropdown
   */
  toggleUserTypeDropdown(): void {
    this.showUserTypeDropdown = !this.showUserTypeDropdown;
  }

  /**
   * Select user type and navigate accordingly
   */
  selectUserType(userType: string): void {
    this.selectedUserType = userType;
    this.showUserTypeDropdown = false;
    
    if (userType === 'Banker') {
      this.router.navigate(['/banker-login']);
    }
    // Personal is the default, stays on the current page
  }
}
