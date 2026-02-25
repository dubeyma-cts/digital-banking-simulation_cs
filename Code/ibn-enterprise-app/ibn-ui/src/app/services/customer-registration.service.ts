import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface CustomerRegistrationPayload {
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
  accountType: string;
}

export interface CustomerRegistrationApiResponse {
  success: boolean;
  message: string;
  customerId?: string;
}

export interface CustomerRegistrationListItem {
  customerId: string;
  firstName: string;
  lastName: string;
  email: string;
  accountType: string;
  registrationDate: string;
  phone: string;
  status: string;
}

@Injectable({
  providedIn: 'root'
})
export class CustomerRegistrationService {
  private apiUrl = `${environment.coreApiBaseUrl}/customers`;

  constructor(private http: HttpClient) {}

  saveCustomer(payload: CustomerRegistrationPayload): Observable<CustomerRegistrationApiResponse> {
    return this.http.post<CustomerRegistrationApiResponse>(`${this.apiUrl}/save`, payload);
  }

  updateCustomer(customerId: string, payload: CustomerRegistrationPayload): Observable<CustomerRegistrationApiResponse> {
    return this.http.put<CustomerRegistrationApiResponse>(`${this.apiUrl}/update/${customerId}`, payload);
  }

  getCustomerRegistrations(): Observable<CustomerRegistrationListItem[]> {
    return this.http.get<CustomerRegistrationListItem[]>(`${this.apiUrl}/registrations`);
  }

  approveCustomerRegistration(customerId: string): Observable<CustomerRegistrationApiResponse> {
    return this.http.put<CustomerRegistrationApiResponse>(`${this.apiUrl}/${customerId}/approve`, {});
  }

  rejectCustomerRegistration(customerId: string): Observable<CustomerRegistrationApiResponse> {
    return this.http.put<CustomerRegistrationApiResponse>(`${this.apiUrl}/${customerId}/reject`, {});
  }
}
