import { Component, OnInit } from '@angular/core';
import { AccountService, AccountSummary } from '../../services/account.service';
import { FundTransferResponse, FundTransferService } from '../../services/fund-transfer.service';

interface TransferRequest {
  fromAccountId: string;
  payeeAccountNumber: string;
  confirmPayeeAccountNumber: string;
  accountHolderName: string;
  amount: number | null;
  remarks: string;
}

@Component({
  selector: 'app-fund-transfers',
  templateUrl: './fund-transfers.component.html',
  styleUrls: ['./fund-transfers.component.css']
})
export class FundTransfersComponent implements OnInit {
  customerId = '';
  fromAccounts: AccountSummary[] = [];
  payeeValidationMessage = '';
  transferErrorMessage = '';
  isPayeeValidated = false;
  isSubmitting = false;
  balanceValidationMessage = '';
  transferSuccessDetails: FundTransferResponse | null = null;
  lastTransferSourceAccountId = '';

  transferRequest: TransferRequest = {
    fromAccountId: '',
    payeeAccountNumber: '',
    confirmPayeeAccountNumber: '',
    accountHolderName: '',
    amount: null,
    remarks: ''
  };

  constructor(
    private accountService: AccountService,
    private fundTransferService: FundTransferService
  ) {}

  ngOnInit(): void {
    this.customerId = localStorage.getItem('customerId') || '';
    if (!this.customerId) {
      return;
    }

    this.loadSourceAccounts();
  }

  onPayeeFieldChange(): void {
    this.isPayeeValidated = false;
    this.payeeValidationMessage = '';
    this.transferRequest.accountHolderName = '';
  }

  onAmountOrSourceChange(): void {
    this.balanceValidationMessage = '';

    if (!this.transferRequest.amount || this.transferRequest.amount <= 0) {
      return;
    }

    if (!this.hasSufficientBalance()) {
      this.balanceValidationMessage = 'Insufficient available balance in selected source account.';
    }
  }

  validatePayeeDetails(): void {
    const payee = this.transferRequest.payeeAccountNumber?.trim();
    const confirm = this.transferRequest.confirmPayeeAccountNumber?.trim();

    this.isPayeeValidated = false;
    this.transferRequest.accountHolderName = '';
    this.payeeValidationMessage = '';

    if (!payee || !confirm) {
      return;
    }

    if (payee !== confirm) {
      this.payeeValidationMessage = 'Payee account number does not match confirmation.';
      return;
    }

    this.fundTransferService.validatePayee(payee).subscribe({
      next: (response) => {
        if (response && response.valid) {
          this.isPayeeValidated = true;
          this.transferRequest.accountHolderName = response.accountHolderName || '';
          this.payeeValidationMessage = '';
        } else {
          this.isPayeeValidated = false;
          this.transferRequest.accountHolderName = '';
          this.payeeValidationMessage = response?.message || 'Payee account not found.';
        }
      },
      error: () => {
        this.isPayeeValidated = false;
        this.transferRequest.accountHolderName = '';
        this.payeeValidationMessage = 'Unable to validate payee account.';
      }
    });
  }

  onTransfer(): void {
    this.transferErrorMessage = '';
    this.balanceValidationMessage = '';

    if (!this.isPayeeValidated) {
      this.transferErrorMessage = 'Please validate payee account details before transfer.';
      return;
    }

    if (!this.customerId || !this.transferRequest.fromAccountId || !this.transferRequest.amount) {
      this.transferErrorMessage = 'Please complete all required fields.';
      return;
    }

    if (!this.hasSufficientBalance()) {
      this.balanceValidationMessage = 'Insufficient available balance in selected source account.';
      return;
    }

    this.isSubmitting = true;
    this.fundTransferService.transfer({
      customerId: this.customerId,
      sourceAccountId: this.transferRequest.fromAccountId,
      payeeAccountNumber: this.transferRequest.payeeAccountNumber,
      confirmPayeeAccountNumber: this.transferRequest.confirmPayeeAccountNumber,
      amount: this.transferRequest.amount,
      remarks: this.transferRequest.remarks
    }).subscribe({
      next: (response) => {
        this.isSubmitting = false;
        if (response && response.success) {
          this.lastTransferSourceAccountId = this.transferRequest.fromAccountId;
          this.transferSuccessDetails = response;
          return;
        }
        this.transferErrorMessage = response?.message || 'Transfer failed.';
      },
      error: (error) => {
        this.isSubmitting = false;
        this.transferErrorMessage = error?.error?.message || 'Transfer failed.';
      }
    });
  }

  get selectedSourceAccount(): AccountSummary | undefined {
    return this.fromAccounts.find(account => account.accountId === this.transferRequest.fromAccountId);
  }

  private hasSufficientBalance(): boolean {
    const selectedAccount = this.selectedSourceAccount;
    const amount = this.transferRequest.amount || 0;
    if (!selectedAccount) {
      return false;
    }
    return amount <= (selectedAccount.balance || 0);
  }

  backToTransferForm(): void {
    this.transferSuccessDetails = null;
    this.transferErrorMessage = '';
    this.payeeValidationMessage = '';
    this.balanceValidationMessage = '';
    this.isPayeeValidated = false;
    this.transferRequest = {
      fromAccountId: '',
      payeeAccountNumber: '',
      confirmPayeeAccountNumber: '',
      accountHolderName: '',
      amount: null,
      remarks: ''
    };

    this.loadSourceAccounts(this.lastTransferSourceAccountId);
  }

  private loadSourceAccounts(preferredAccountId?: string): void {
    if (!this.customerId) {
      this.fromAccounts = [];
      this.transferRequest.fromAccountId = '';
      return;
    }

    this.accountService.getAccounts(this.customerId).subscribe({
      next: (accounts) => {
        this.fromAccounts = accounts || [];
        const preferred = (preferredAccountId || '').trim();
        const preferredExists = preferred !== '' && this.fromAccounts.some(account => account.accountId === preferred);
        this.transferRequest.fromAccountId = preferredExists
          ? preferred
          : (this.fromAccounts.length > 0 ? this.fromAccounts[0].accountId : '');
        this.onAmountOrSourceChange();
      },
      error: () => {
        this.fromAccounts = [];
        this.transferRequest.fromAccountId = '';
      }
    });
  }
}
