import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { BankerLoginComponent } from './components/banker-login/banker-login.component';
import { LandingComponent } from './components/landing/landing.component';
import { ForgotPasswordComponent } from './components/forgot-password/forgot-password.component';
import { ForgotCustomerIdComponent } from './components/forgot-customer-id/forgot-customer-id.component';
import { AccountsComponent } from './components/accounts/accounts.component';
import { FundTransfersComponent } from './components/fund-transfers/fund-transfers.component';
import { FundTransferSuccessComponent } from './components/fund-transfer-success/fund-transfer-success.component';
import { PaymentsComponent } from './components/payments/payments.component';
import { ChequeDepositsComponent } from './components/cheque-deposits/cheque-deposits.component';
import { ProfileComponent } from './components/profile/profile.component';
import { CustomerRegistrationComponent } from './components/customer-registration/customer-registration.component';

const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'banker-login', component: BankerLoginComponent },
  { path: 'landing', component: LandingComponent },
  { path: 'accounts', component: AccountsComponent },
  { path: 'fund-transfers', component: FundTransfersComponent },
  { path: 'fund-transfer-success', component: FundTransferSuccessComponent },
  { path: 'payments', component: PaymentsComponent },
  { path: 'cheque-deposits', component: ChequeDepositsComponent },
  { path: 'profile', component: ProfileComponent },
  { path: 'customer-registration', component: CustomerRegistrationComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'forgot-customer-id', component: ForgotCustomerIdComponent },
  { path: '**', redirectTo: '/login' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
