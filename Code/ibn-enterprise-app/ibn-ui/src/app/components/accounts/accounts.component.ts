import { Component, OnInit } from '@angular/core';
import { AccountService, AccountSummary, StatementTransaction } from '../../services/account.service';

type Transaction = StatementTransaction;

@Component({
  selector: 'app-accounts',
  templateUrl: './accounts.component.html',
  styleUrls: ['./accounts.component.css']
})
export class AccountsComponent implements OnInit {
  customerId: string = '';
  accounts: AccountSummary[] = [];
  selectedAccountId: string = '';
  selectedStatementType: 'mini' | 'detailed' = 'mini';

  fromDate: string = '';
  toDate: string = '';

  pageSize = 5;
  currentPage = 1;

  miniStatementTransactions: Transaction[] = [];
  filteredDetailedTransactions: Transaction[] = [];
  paginatedTransactions: Transaction[] = [];

  constructor(private accountService: AccountService) {}

  ngOnInit(): void {
    this.customerId = localStorage.getItem('customerId') || localStorage.getItem('userId') || '';
    this.loadAccounts();
  }

  get selectedAccount(): AccountSummary | undefined {
    return this.accounts.find(account => account.accountId === this.selectedAccountId);
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.filteredDetailedTransactions.length / this.pageSize));
  }

  onAccountSelectionChange(): void {
    this.selectedStatementType = 'mini';
    this.fromDate = '';
    this.toDate = '';
    this.currentPage = 1;
    this.loadMiniStatement();
    this.loadDetailedStatement();
  }

  setStatementType(type: 'mini' | 'detailed'): void {
    this.selectedStatementType = type;
    this.currentPage = 1;

    if (type === 'mini') {
      this.loadMiniStatement();
    } else {
      this.loadDetailedStatement();
    }
  }

  applyDateFilter(): void {
    this.loadDetailedStatement();
  }

  prevPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.setPaginatedData();
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.setPaginatedData();
    }
  }

  private loadAccounts(): void {
    if (!this.customerId) {
      this.accounts = [];
      this.selectedAccountId = '';
      this.clearStatements();
      return;
    }

    this.accountService.getAccounts(this.customerId).subscribe({
      next: (accounts) => {
        this.accounts = accounts || [];
        this.selectedAccountId = this.accounts.length ? this.accounts[0].accountId : '';
        if (this.selectedAccountId) {
          this.loadMiniStatement();
          this.loadDetailedStatement();
        } else {
          this.clearStatements();
        }
      },
      error: () => {
        this.accounts = [];
        this.selectedAccountId = '';
        this.clearStatements();
      }
    });
  }

  private loadMiniStatement(): void {
    if (!this.selectedAccountId) {
      this.miniStatementTransactions = [];
      return;
    }

    this.accountService.getMiniStatement(this.selectedAccountId, 5).subscribe({
      next: (transactions) => {
        this.miniStatementTransactions = transactions || [];
      },
      error: () => {
        this.miniStatementTransactions = [];
      }
    });
  }

  private loadDetailedStatement(): void {
    if (!this.selectedAccountId) {
      this.filteredDetailedTransactions = [];
      this.setPaginatedData();
      return;
    }

    this.accountService.getDetailedStatement(this.selectedAccountId, this.fromDate, this.toDate).subscribe({
      next: (transactions) => {
        this.filteredDetailedTransactions = transactions || [];
        this.currentPage = 1;
        this.setPaginatedData();
      },
      error: () => {
        this.filteredDetailedTransactions = [];
        this.currentPage = 1;
        this.setPaginatedData();
      }
    });
  }

  private clearStatements(): void {
    this.miniStatementTransactions = [];
    this.filteredDetailedTransactions = [];
    this.currentPage = 1;
    this.setPaginatedData();
  }

  private setPaginatedData(): void {
    const start = (this.currentPage - 1) * this.pageSize;
    this.paginatedTransactions = this.filteredDetailedTransactions.slice(start, start + this.pageSize);
  }

}
