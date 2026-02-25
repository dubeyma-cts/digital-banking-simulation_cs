import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { LoginService } from '../../services/login.service';
import { AccountService, AccountInfo } from '../../services/account.service';

@Component({
  selector: 'app-landing',
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.css']
})
export class LandingComponent implements OnInit {
  userName: string = '';
  userEmail: string = '';
  activeSection: string = 'accounts';
  userType: string = 'personal'; // 'personal' or 'banker'
  account: { accountNumber: string; balance: number; customerId: string } = {
    accountNumber: '',
    balance: 0,
    customerId: ''
  };

  constructor(
    private loginService: LoginService,
    private router: Router,
    private accountService: AccountService
  ) {}

  ngOnInit(): void {
    // Check if user is authenticated
    if (!this.loginService.isAuthenticated()) {
      this.router.navigate(['/login']);
      return;
    }

    // Load user information and determine user type
    this.loadUserInfo();
  }

  /**
   * Load user information from localStorage or API
   */
  loadUserInfo(): void {
    this.userName = localStorage.getItem('userName') || 'User';
    this.userEmail = localStorage.getItem('email') || '';
    
    // Determine user type
    this.userType = localStorage.getItem('userType') || 'personal';
    
    // Set default active section based on user type
    if (this.userType === 'banker') {
      this.activeSection = 'cheque-deposits';
    } else {
      this.activeSection = 'accounts';
    }
    
    // Determine customerId
    const custId = localStorage.getItem('customerId') || localStorage.getItem('userId') || 'CUST-0001';
    this.account.customerId = custId;

    // Fetch real account info from backend
    this.accountService.getAccount(custId).subscribe(
      (info: AccountInfo) => {
        this.account.accountNumber = info.accountNumber || 'XXXX-XXXX-1234';
        this.account.balance = info.balance || 0;
        this.account.customerId = info.customerId || custId;
      },
      (err) => {
        // Fallback to placeholders on error
        this.account.accountNumber = localStorage.getItem('accountNumber') || 'XXXX-XXXX-1234';
        const bal = localStorage.getItem('accountBalance');
        this.account.balance = bal ? parseFloat(bal) : 12500.75;
        this.account.customerId = custId;
      }
    );
  }

  /**
   * Logout user
   */
  onLogout(): void {
    this.loginService.logout().subscribe(
      (response) => {
        this.loginService.clearAuthData();
        this.router.navigate(['/login']);
      },
      (error) => {
        // Even if logout API fails, clear local data and redirect
        this.loginService.clearAuthData();
        this.router.navigate(['/login']);
      }
    );
  }

  navigateTo(route: string): void {
    this.activeSection = route;

    if (route === 'accounts' || route === 'fund-transfers' || route === 'cheque-deposits' || route === 'profile' || route === 'customer-registration') {
      return;
    }

    // Map friendly route names to application routes
    switch (route) {
      case 'accounts':
        this.router.navigate(['/accounts']);
        break;
      case 'fund-transfers':
        this.router.navigate(['/fund-transfers']);
        break;
      case 'payments':
        this.router.navigate(['/payments']);
        break;
      case 'cheque-deposits':
        this.router.navigate(['/cheque-deposits']);
        break;
      case 'profile':
        this.router.navigate(['/profile']);
        break;
      case 'customer-registration':
        this.router.navigate(['/customer-registration']);
        break;
      default:
        this.router.navigate(['/']);
    }
  }
}
