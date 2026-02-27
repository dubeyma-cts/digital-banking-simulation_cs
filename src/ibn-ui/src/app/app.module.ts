import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HeaderComponent } from './components/header/header.component';
import { FooterComponent } from './components/footer/footer.component';
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
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { ApiService } from './services/api.service';
import { LoginService } from './services/login.service';
import { AccountService } from './services/account.service';
import { FundTransferService } from './services/fund-transfer.service';
import { AuthTokenInterceptor } from './interceptors/auth-token.interceptor';

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    FooterComponent,
    LoginComponent,
    BankerLoginComponent,
    LandingComponent,
    ForgotPasswordComponent,
    ForgotCustomerIdComponent
    ,AccountsComponent,
    FundTransfersComponent,
    FundTransferSuccessComponent,
    PaymentsComponent,
    ChequeDepositsComponent,
    ProfileComponent,
    CustomerRegistrationComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    HttpClientModule
  ],
  providers: [
    ApiService,
    LoginService,
    AccountService,
    FundTransferService,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthTokenInterceptor,
      multi: true
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }