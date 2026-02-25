import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface LoginRequest {
  customerId: string;
  password: string;
  captcha?: string;
}

export interface LoginResponse {
  success: boolean;
  message: string;
  token?: string;
  userId?: string;
  email?: string;
}

@Injectable({
  providedIn: 'root'
})
export class LoginService {
  private apiUrl = environment.coreApiBaseUrl;

  constructor(private http: HttpClient) {}

  /**
   * Authenticate user with customer ID and password
   * @param loginRequest - Login credentials
   * @returns Observable of login response
   */
  authenticate(loginRequest: LoginRequest): Observable<LoginResponse> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    return this.http.post<LoginResponse>(
      `${this.apiUrl}/auth/login`,
      loginRequest,
      { headers }
    );
  }

  /**
   * Authenticate banker with login ID and password
   * @param loginRequest - Banker login credentials
   * @returns Observable of login response
   */
  authenticateBanker(loginRequest: any): Observable<LoginResponse> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    return this.http.post<LoginResponse>(
      `${this.apiUrl}/auth/banker-login`,
      loginRequest,
      { headers }
    );
  }

  /**
   * Validate CAPTCHA
   * @param captchaValue - CAPTCHA value to validate
   * @returns Observable of validation response
   */
  validateCaptcha(captchaValue: string): Observable<any> {
    return this.http.post(
      `${this.apiUrl}/auth/validate-captcha`,
      { captcha: captchaValue }
    );
  }

  /**
   * Check if account is locked
   * @param customerId - Customer ID to check
   * @returns Observable of status response
   */
  checkAccountStatus(customerId: string): Observable<any> {
    return this.http.get(
      `${this.apiUrl}/auth/account-status/${customerId}`
    );
  }

  /**
   * Get remaining login attempts
   * @param customerId - Customer ID
   * @returns Observable with remaining attempts
   */
  getRemainingAttempts(customerId: string): Observable<any> {
    return this.http.get(
      `${this.apiUrl}/auth/remaining-attempts/${customerId}`
    );
  }

  /**
   * Logout user
   * @returns Observable of logout response
   */
  logout(): Observable<any> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${localStorage.getItem('authToken')}`
    });

    return this.http.post(
      `${this.apiUrl}/auth/logout`,
      {},
      { headers }
    );
  }

  /**
   * Clear stored authentication data
   */
  clearAuthData(): void {
    localStorage.removeItem('authToken');
    localStorage.removeItem('customerId');
    localStorage.removeItem('userId');
    localStorage.removeItem('email');
    localStorage.removeItem('userName');
    localStorage.removeItem('userType');
  }

  /**
   * Get stored authentication token
   * @returns Auth token or null
   */
  getAuthToken(): string | null {
    return localStorage.getItem('authToken');
  }

  /**
   * Check if user is authenticated
   * @returns Boolean indicating if user is logged in
   */
  isAuthenticated(): boolean {
    return !!this.getAuthToken();
  }
}
