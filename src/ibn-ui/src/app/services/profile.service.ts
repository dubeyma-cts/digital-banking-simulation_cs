import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface CustomerProfile {
  fullName: string;
  customerId: string;
  dateOfBirth: string;
  mobile: string;
  email: string;
  address: string;
  accountNumber: string;
  accountType: string;
  branchName: string;
  ifsc: string;
  nominee: string;
  accountStatus: string;
  pan: string;
  aadhaarMasked: string;
  addressProof: string;
  communication: string;
  kycStatus: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProfileService {
  private apiUrl = `${environment.coreApiBaseUrl}/auth`;

  constructor(private http: HttpClient) {}

  getCustomerProfile(customerId: string): Observable<CustomerProfile> {
    return this.http.get<CustomerProfile>(`${this.apiUrl}/profile/${customerId}`);
  }
}
