import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-forgot-customer-id',
  templateUrl: './forgot-customer-id.component.html',
  styleUrls: ['./forgot-customer-id.component.css']
})
export class ForgotCustomerIdComponent implements OnInit {
  email: string = '';
  phoneNumber: string = '';
  verificationMethod: string = 'email'; // email or phone
  step: number = 1; // Step 1: Enter Details, Step 2: Verification Sent
  isLoading: boolean = false;
  successMessage: string = '';
  errorMessage: string = '';
  showError: boolean = false;
  showSuccess: boolean = false;

  constructor(private router: Router) {}

  ngOnInit(): void {}

  /**
   * Submit request to retrieve customer ID
   */
  submitRequest(): void {
    this.errorMessage = '';
    this.successMessage = '';
    this.showError = false;
    this.showSuccess = false;

    if (this.verificationMethod === 'email' && !this.email.trim()) {
      this.showError = true;
      this.errorMessage = 'Please enter your email address.';
      return;
    }

    if (this.verificationMethod === 'phone' && !this.phoneNumber.trim()) {
      this.showError = true;
      this.errorMessage = 'Please enter your phone number.';
      return;
    }

    this.isLoading = true;
    // Simulate API call to retrieve customer ID
    setTimeout(() => {
      this.isLoading = false;
      this.step = 2;
      this.showSuccess = true;
      const method = this.verificationMethod === 'email' ? 'your registered email' : 'your registered phone number';
      this.successMessage = `Your Customer ID has been sent to ${method}.`;
    }, 2000);
  }

  /**
   * Go back to login page
   */
  backToLogin(): void {
    this.router.navigate(['/login']);
  }
}
