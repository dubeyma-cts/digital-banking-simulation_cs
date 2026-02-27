import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface PayeeValidationResponse {
  valid: boolean;
  accountHolderName: string;
  accountId: string;
  message: string;
}

export interface FundTransferRequest {
  customerId: string;
  sourceAccountId: string;
  payeeAccountNumber: string;
  confirmPayeeAccountNumber: string;
  amount: number;
  remarks: string;
}

export interface FundTransferResponse {
  success: boolean;
  message: string;
  transactionId: string;
  reference: string;
  fromAccountNumber: string;
  payeeAccountNumber: string;
  payeeAccountHolderName: string;
  amount: number;
  currency: string;
  transferredAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class FundTransferService {
  private apiUrl = `${environment.coreApiBaseUrl}/fund-transfers`;

  constructor(private http: HttpClient) {}

  validatePayee(payeeAccountNumber: string): Observable<PayeeValidationResponse> {
    return this.http.get<PayeeValidationResponse>(`${this.apiUrl}/payee/${encodeURIComponent(payeeAccountNumber)}`);
  }

  transfer(request: FundTransferRequest): Observable<FundTransferResponse> {
    return this.http.post<FundTransferResponse>(this.apiUrl, request);
  }
}
