import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent implements OnInit {
  customerId: string = '';
  email: string = '';
  step: number = 1; // Step 1: Enter Customer ID, Step 2: Verify Email, Step 3: Reset Password
  isLoading: boolean = false;
  successMessage: string = '';
  errorMessage: string = '';
  showError: boolean = false;
  showSuccess: boolean = false;

  constructor(private router: Router) {}

  ngOnInit(): void {}

  /**
   * Submit customer ID for password recovery
   */
  submitCustomerId(): void {
    this.errorMessage = '';
    this.successMessage = '';
    this.showError = false;
    this.showSuccess = false;

    if (!this.customerId.trim()) {
      this.showError = true;
      this.errorMessage = 'Please enter your Customer ID.';
      return;
    }

    this.isLoading = true;
    // Simulate API call to verify customer ID
    setTimeout(() => {
      this.isLoading = false;
      this.step = 2;
      this.showSuccess = true;
      this.successMessage = `A verification code has been sent to your registered email.`;
    }, 2000);
  }

  /**
   * Go back to login page
   */
  backToLogin(): void {
    this.router.navigate(['/login']);
  }
}
