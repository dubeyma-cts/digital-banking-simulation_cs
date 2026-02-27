import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface AccountInfo {
  accountNumber: string;
  balance: number;
  customerId: string;
}

export interface AccountSummary {
  accountId: string;
  accountNumber: string;
  accountType: string;
  status: string;
  currency: string;
  balance: number;
}

export interface StatementTransaction {
  date: string;
  description: string;
  type: 'Debit' | 'Credit' | string;
  amount: number;
}

@Injectable({
  providedIn: 'root'
})
export class AccountService {
  private apiUrl = environment.coreApiBaseUrl;

  constructor(private http: HttpClient) {}

  getAccount(customerId: string): Observable<AccountInfo> {
    return this.http.get<AccountInfo>(`${this.apiUrl}/accounts/${customerId}`);
  }

  getAccounts(customerId: string): Observable<AccountSummary[]> {
    return this.http.get<AccountSummary[]>(`${this.apiUrl}/accounts/customer/${customerId}`);
  }

  getMiniStatement(accountId: string, limit: number = 5): Observable<StatementTransaction[]> {
    return this.http.get<StatementTransaction[]>(`${this.apiUrl}/accounts/${accountId}/mini-statement?limit=${limit}`);
  }

  getDetailedStatement(accountId: string, fromDate?: string, toDate?: string): Observable<StatementTransaction[]> {
    const params: string[] = [];
    if (fromDate) {
      params.push(`fromDate=${encodeURIComponent(fromDate)}`);
    }
    if (toDate) {
      params.push(`toDate=${encodeURIComponent(toDate)}`);
    }
    const query = params.length ? `?${params.join('&')}` : '';
    return this.http.get<StatementTransaction[]>(`${this.apiUrl}/accounts/${accountId}/detailed-statement${query}`);
  }
}
