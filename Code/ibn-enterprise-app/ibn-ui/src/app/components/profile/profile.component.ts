import { Component, OnInit } from '@angular/core';
import { CustomerProfile, ProfileService } from '../../services/profile.service';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  profile: CustomerProfile = {
    fullName: '',
    customerId: '',
    dateOfBirth: '',
    mobile: '',
    email: '',
    address: '',
    accountNumber: '',
    accountType: '',
    branchName: '',
    ifsc: '',
    nominee: '',
    accountStatus: '',
    pan: '',
    aadhaarMasked: '',
    addressProof: '',
    communication: '',
    kycStatus: ''
  };

  isLoading = false;
  errorMessage = '';

  constructor(private profileService: ProfileService) {}

  ngOnInit(): void {
    const customerId = localStorage.getItem('customerId') || '';
    if (!customerId) {
      this.errorMessage = 'Customer session not found. Please login again.';
      return;
    }

    this.isLoading = true;
    this.profileService.getCustomerProfile(customerId).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.profile = response;
      },
      error: () => {
        this.isLoading = false;
        this.errorMessage = 'Unable to load profile details.';
      }
    });
  }
}
