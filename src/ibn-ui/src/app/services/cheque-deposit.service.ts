import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface ChequeDepositRequestPayload {
  customerId: string;
  accountId: string;
  depositorName: string;
  chequeNumber: string;
  chequeDate: string;
  bankName: string;
  branchName: string;
  amount: number;
  remarks: string;
  attachmentBase64?: string;
  attachmentName?: string;
  changedByUserId?: string;
}

export interface ChequeDepositResponsePayload {
  success: boolean;
  message: string;
  referenceId?: string;
  status?: string;
}

export interface ChequeStatusUpdateRequestPayload {
  status: string;
  remarks?: string;
  changedByUserId?: string;
}

export interface ChequeDepositListItem {
  referenceId: string;
  depositorName: string;
  accountId: string;
  accountNumber: string;
  chequeNumber: string;
  chequeDate: string;
  bankName: string;
  branchName: string;
  amount: number;
  remarks: string;
  status: string;
  attachmentName: string;
}

@Injectable({
  providedIn: 'root'
})
export class ChequeDepositService {
  private apiUrl = `${environment.coreApiBaseUrl}/cheque-deposits`;

  constructor(private http: HttpClient) {}

  submit(request: ChequeDepositRequestPayload): Observable<ChequeDepositResponsePayload> {
    return this.http.post<ChequeDepositResponsePayload>(this.apiUrl, request);
  }

  getAll(): Observable<ChequeDepositListItem[]> {
    return this.http.get<ChequeDepositListItem[]>(this.apiUrl);
  }

  getByCustomer(customerId: string): Observable<ChequeDepositListItem[]> {
    return this.http.get<ChequeDepositListItem[]>(`${this.apiUrl}/customer/${customerId}`);
  }

  updateStatus(referenceId: string, request: ChequeStatusUpdateRequestPayload): Observable<ChequeDepositResponsePayload> {
    return this.http.put<ChequeDepositResponsePayload>(`${this.apiUrl}/${referenceId}/status`, request);
  }
}
