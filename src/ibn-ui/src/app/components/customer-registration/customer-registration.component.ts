import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import {
  CustomerRegistrationListItem,
  CustomerRegistrationPayload,
  CustomerRegistrationService
} from '../../services/customer-registration.service';

interface CustomerRegistrationRequest extends CustomerRegistrationPayload {
  firstName: string;
  lastName: string;
  aadhaar: string;
  pan: string;
  username: string;
  password: string;
  addressLine1: string;
  addressLine2: string;
  addressLine3: string;
  city: string;
  state: string;
  zip: string;
  phone: string;
  email: string;
  accountType: 'Savings' | 'Current' | '';
}

@Component({
  selector: 'app-customer-registration',
  templateUrl: './customer-registration.component.html',
  styleUrls: ['./customer-registration.component.css']
})
export class CustomerRegistrationComponent implements OnInit {
  showSuccessMessage = false;
  generatedCustomerId = '';
  errorMessage = '';
  userType: string = 'personal'; // 'personal' or 'banker'
  pendingCustomers: CustomerRegistrationListItem[] = [];

  registration: CustomerRegistrationRequest = {
    firstName: '',
    lastName: '',
    aadhaar: '',
    pan: '',
    username: '',
    password: '',
    addressLine1: '',
    addressLine2: '',
    addressLine3: '',
    city: '',
    state: '',
    zip: '',
    phone: '',
    email: '',
    accountType: ''
  };

  constructor(
    private router: Router,
    private customerRegistrationService: CustomerRegistrationService
  ) {}

  ngOnInit(): void {
    // Get user type from localStorage
    this.userType = localStorage.getItem('userType') || 'personal';
    
    // Load customer registrations for bankers
    if (this.userType === 'banker') {
      this.loadPendingCustomers();
    }
  }

  private loadPendingCustomers(): void {
    this.customerRegistrationService.getCustomerRegistrations().subscribe({
      next: (customers) => {
        this.pendingCustomers = customers || [];
      },
      error: () => {
        this.pendingCustomers = [];
        this.errorMessage = 'Unable to load customer registrations.';
      }
    });
  }

  onSubmit(): void {
    const payload: CustomerRegistrationPayload = {
      ...this.registration
    };

    const request$ = this.generatedCustomerId
      ? this.customerRegistrationService.updateCustomer(this.generatedCustomerId, payload)
      : this.customerRegistrationService.saveCustomer(payload);

    request$.subscribe({
      next: (response) => {
        if (response.success) {
          this.generatedCustomerId = response.customerId || this.generatedCustomerId;
          this.showSuccessMessage = true;
          this.errorMessage = '';

          if (this.userType === 'banker') {
            this.loadPendingCustomers();
          }

          return;
        }

        this.showSuccessMessage = false;
        this.errorMessage = response.message || 'Unable to submit registration.';
      },
      error: (error) => {
        this.showSuccessMessage = false;
        this.errorMessage = error?.error?.message || 'Unable to submit registration.';
      }
    });
  }

  onBackToLogin(): void {
    this.router.navigate(['/login']);
  }

  /**
   * Approve customer registration
   */
  approveCustomer(customerId: string): void {
    this.customerRegistrationService.approveCustomerRegistration(customerId).subscribe({
      next: (response) => {
        if (response.success) {
          this.errorMessage = '';
          this.loadPendingCustomers();
          return;
        }

        this.errorMessage = response.message || 'Unable to approve customer registration.';
      },
      error: (error) => {
        this.errorMessage = error?.error?.message || 'Unable to approve customer registration.';
      }
    });
  }

  /**
   * Reject customer registration
   */
  rejectCustomer(customerId: string): void {
    this.customerRegistrationService.rejectCustomerRegistration(customerId).subscribe({
      next: (response) => {
        if (response.success) {
          this.errorMessage = '';
          this.loadPendingCustomers();
          return;
        }

        this.errorMessage = response.message || 'Unable to reject customer registration.';
      },
      error: (error) => {
        this.errorMessage = error?.error?.message || 'Unable to reject customer registration.';
      }
    });
  }

  private keepDigits(value: string, maxLength: number): string {
    return (value || '').replace(/\D/g, '').slice(0, maxLength);
  }

  private keepAlphanumeric(value: string, maxLength: number): string {
    return (value || '').replace(/[^a-zA-Z0-9]/g, '').slice(0, maxLength);
  }

  private applyDashFormat(digits: string, firstPartLength: number): string {
    if (digits.length <= firstPartLength) {
      return digits;
    }
    return `${digits.slice(0, firstPartLength)}-${digits.slice(firstPartLength)}`;
  }

  formatPhone(value: string): string {
    const digits = this.keepDigits(value, 8);
    return this.applyDashFormat(digits, 4);
  }

  formatAadhaar(value: string): string {
    return this.keepDigits(value, 12);
  }

  formatPan(value: string): string {
    return this.keepAlphanumeric(value, 10).toUpperCase();
  }

}

