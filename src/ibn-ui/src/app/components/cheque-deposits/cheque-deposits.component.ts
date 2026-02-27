import { Component, OnInit } from '@angular/core';
import { AccountService, AccountSummary } from '../../services/account.service';
import {
  ChequeDepositListItem,
  ChequeDepositRequestPayload,
  ChequeStatusUpdateRequestPayload,
  ChequeDepositService
} from '../../services/cheque-deposit.service';

interface ChequeDepositRequest {
  depositorName: string;
  toAccount: string;
  chequeNumber: string;
  chequeDate: string;
  bankName: string;
  branchName: string;
  amount: number | null;
  remarks: string;
}

interface SavedChequeDeposit {
  referenceId: string;
  depositorName: string;
  toAccount: string;
  chequeNumber: string;
  chequeDate: string;
  bankName: string;
  branchName: string;
  amount: number;
  remarks: string;
  attachmentName: string;
  status: string;
}

@Component({
  selector: 'app-cheque-deposits',
  templateUrl: './cheque-deposits.component.html',
  styleUrls: ['./cheque-deposits.component.css']
})
export class ChequeDepositsComponent implements OnInit {
  showSuccessMessage = false;
  canPrint = false;
  referenceId = '';
  selectedFileName = '';
  selectedFileBase64 = '';
  customerId = '';
  userId = '';
  isSubmitting = false;
  isUpdatingStatus = false;
  errorMessage = '';
  selectedStatusFilter = 'ALL';
  savedDeposits: SavedChequeDeposit[] = [];
  userType: string = 'personal'; // 'personal' or 'banker'

  toAccounts: AccountSummary[] = [];

  depositRequest: ChequeDepositRequest = {
    depositorName: '',
    toAccount: '',
    chequeNumber: '',
    chequeDate: '',
    bankName: '',
    branchName: '',
    amount: null,
    remarks: ''
  };

  constructor(
    private accountService: AccountService,
    private chequeDepositService: ChequeDepositService
  ) {}

  ngOnInit(): void {
    // Get user type from localStorage
    this.userType = localStorage.getItem('userType') || 'personal';
    this.userId = localStorage.getItem('userId') || '';
    
    if (this.userType === 'banker') {
      this.loadAllDeposits();
      return;
    }

    this.customerId = localStorage.getItem('customerId') || '';

    if (!this.customerId) {
      this.errorMessage = 'Customer session not found. Please login again.';
      return;
    }

    this.loadCustomerAccounts();
    this.loadSavedDeposits();
  }

  /**
   * Load sample cheque deposits for banker dashboard
   */
  private loadSampleChequeDeposits(): void {
    this.savedDeposits = [
      {
        referenceId: 'CHK20260217001',
        depositorName: 'Aarav Sharma',
        toAccount: 'SAVINGS_1234',
        chequeNumber: 'CHK-1001',
        chequeDate: '2026-02-15',
        bankName: 'State Bank of India',
        branchName: 'Mumbai Downtown',
        amount: 50000,
        remarks: 'Cheque from client payment',
        attachmentName: 'cheque_scan.pdf',
        status: 'Not received'
      },
      {
        referenceId: 'CHK20260216002',
        depositorName: 'Priya Patel',
        toAccount: 'CURRENT_5678',
        chequeNumber: 'CHK-2045',
        chequeDate: '2026-02-10',
        bankName: 'HDFC Bank',
        branchName: 'New Delhi Branch',
        amount: 75000,
        remarks: 'Monthly salary deposit',
        attachmentName: 'cheque_photo.jpg',
        status: 'Not received'
      },
      {
        referenceId: 'CHK20260215003',
        depositorName: 'Rajesh Kumar',
        toAccount: 'SAVINGS_1234',
        chequeNumber: 'CHK-3087',
        chequeDate: '2026-02-12',
        bankName: 'ICICI Bank',
        branchName: 'Bangalore Tech Park',
        amount: 100000,
        remarks: 'Vendor payment',
        attachmentName: 'cheque_scan.pdf',
        status: 'Sent for Clearance'
      },
      {
        referenceId: 'CHK20260214004',
        depositorName: 'Amit Singh',
        toAccount: 'CURRENT_5678',
        chequeNumber: 'CHK-4156',
        chequeDate: '2026-02-08',
        bankName: 'Axis Bank',
        branchName: 'Pune Branch',
        amount: 125000,
        remarks: 'Investment amount',
        attachmentName: 'cheque_photo.jpg',
        status: 'Received'
      }
    ];
  }

  onFileSelected(event: Event): void {
    this.errorMessage = '';
    const input = event.target as HTMLInputElement;
    const file = input.files && input.files[0];
    if (!file) {
      this.selectedFileName = '';
      this.selectedFileBase64 = '';
      return;
    }

    const allowedTypes = ['image/jpeg', 'image/png'];
    if (!allowedTypes.includes(file.type)) {
      this.selectedFileName = '';
      this.selectedFileBase64 = '';
      this.errorMessage = 'Only JPEG/PNG image attachments are allowed.';
      input.value = '';
      return;
    }

    this.selectedFileName = file.name;
    const reader = new FileReader();
    reader.onload = () => {
      this.selectedFileBase64 = typeof reader.result === 'string' ? reader.result : '';
    };
    reader.readAsDataURL(file);
  }

  onSubmit(): void {
    this.errorMessage = '';
    this.showSuccessMessage = false;

    if (!this.customerId) {
      this.errorMessage = 'Customer session not found. Please login again.';
      return;
    }

    if (!this.depositRequest.amount || this.depositRequest.amount <= 0) {
      this.errorMessage = 'Amount must be greater than zero.';
      return;
    }

    const payload: ChequeDepositRequestPayload = {
      customerId: this.customerId,
      accountId: this.depositRequest.toAccount,
      depositorName: this.depositRequest.depositorName,
      chequeNumber: this.depositRequest.chequeNumber,
      chequeDate: this.depositRequest.chequeDate,
      bankName: this.depositRequest.bankName,
      branchName: this.depositRequest.branchName,
      amount: this.depositRequest.amount,
      remarks: this.depositRequest.remarks,
      attachmentBase64: this.selectedFileBase64,
      attachmentName: this.selectedFileName,
      changedByUserId: this.userId
    };

    this.isSubmitting = true;
    this.chequeDepositService.submit(payload).subscribe({
      next: (response) => {
        this.isSubmitting = false;
        if (!response || !response.success) {
          this.errorMessage = response?.message || 'Unable to submit cheque deposit.';
          return;
        }

        this.referenceId = response.referenceId || this.generateReferenceId();
        this.showSuccessMessage = true;
        this.canPrint = true;
        this.loadSavedDeposits();
      },
      error: (error) => {
        this.isSubmitting = false;
        this.errorMessage = this.getApiErrorMessage(error, 'Unable to submit cheque deposit.');
      }
    });
  }

  printSlip(): void {
    window.print();
  }

  /**
   * Update cheque status to "Received"
   */
  markAsReceived(referenceId: string): void {
    this.updateChequeStatus(referenceId, 'Received');
  }

  /**
   * Update cheque status to "Sent for Clearance"
   */
  sendForClearance(referenceId: string): void {
    this.updateChequeStatus(referenceId, 'Sent for Clearance');
  }

  /**
   * Get status class name for badge styling
   */
  getStatusClass(status: string): string {
    return 'status-' + status.toLowerCase().replace(/ /g, '-');
  }

  private generateReferenceId(): string {
    const timestamp = Date.now().toString().slice(-8);
    const random = Math.floor(1000 + Math.random() * 9000);
    return `CHK${timestamp}${random}`;
  }

  private loadCustomerAccounts(): void {
    this.accountService.getAccounts(this.customerId).subscribe({
      next: (accounts) => {
        this.toAccounts = accounts || [];
        if (!this.depositRequest.toAccount && this.toAccounts.length > 0) {
          this.depositRequest.toAccount = this.toAccounts[0].accountId;
        }
      },
      error: () => {
        this.toAccounts = [];
      }
    });
  }

  private loadSavedDeposits(): void {
    this.chequeDepositService.getByCustomer(this.customerId).subscribe({
      next: (items) => {
        this.savedDeposits = (items || []).map((item: ChequeDepositListItem) => ({
          referenceId: item.referenceId,
          depositorName: item.depositorName,
          toAccount: item.accountNumber || item.accountId,
          chequeNumber: item.chequeNumber,
          chequeDate: item.chequeDate,
          bankName: item.bankName,
          branchName: item.branchName,
          amount: item.amount || 0,
          remarks: item.remarks,
          attachmentName: item.attachmentName,
          status: item.status || 'Not received'
        }));
      },
      error: () => {
        this.savedDeposits = [];
        this.errorMessage = 'Unable to load cheque deposits.';
      }
    });
  }

  private loadAllDeposits(): void {
    this.chequeDepositService.getAll().subscribe({
      next: (items) => {
        this.savedDeposits = (items || []).map((item: ChequeDepositListItem) => ({
          referenceId: item.referenceId,
          depositorName: item.depositorName,
          toAccount: item.accountNumber || item.accountId,
          chequeNumber: item.chequeNumber,
          chequeDate: item.chequeDate,
          bankName: item.bankName,
          branchName: item.branchName,
          amount: item.amount || 0,
          remarks: item.remarks,
          attachmentName: item.attachmentName,
          status: item.status || 'Not received'
        }));
      },
      error: () => {
        this.savedDeposits = [];
        this.errorMessage = 'Unable to load cheque deposits.';
      }
    });
  }

  private updateChequeStatus(referenceId: string, status: string): void {
    this.errorMessage = '';
    this.isUpdatingStatus = true;

    const payload: ChequeStatusUpdateRequestPayload = {
      status,
      changedByUserId: this.userId,
      remarks: status
    };

    this.chequeDepositService.updateStatus(referenceId, payload).subscribe({
      next: (response) => {
        this.isUpdatingStatus = false;
        if (!response || !response.success) {
          this.errorMessage = response?.message || 'Unable to update cheque status.';
          return;
        }
        this.loadAllDeposits();
      },
      error: (error) => {
        this.isUpdatingStatus = false;
        this.errorMessage = this.getApiErrorMessage(error, 'Unable to update cheque status.');
      }
    });
  }

  private getApiErrorMessage(error: any, fallbackMessage: string): string {
    const messageFromBody = error?.error?.message;
    if (messageFromBody && typeof messageFromBody === 'string') {
      return messageFromBody;
    }

    if (typeof error?.error === 'string' && error.error.trim().length > 0) {
      return error.error;
    }

    if (error?.status === 0) {
      return 'Core service is not reachable. Please start ibn-core-svc on port 8080.';
    }

    const messageFromException = error?.message;
    if (messageFromException && typeof messageFromException === 'string') {
      return messageFromException;
    }

    return fallbackMessage;
  }

  getFilteredDeposits(): SavedChequeDeposit[] {
    if (this.selectedStatusFilter === 'ALL') {
      return this.savedDeposits;
    }

    return this.savedDeposits.filter(item => item.status === this.selectedStatusFilter);
  }
}

