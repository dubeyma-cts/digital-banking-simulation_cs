import { Injectable } from '@angular/core';
import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { NgZone } from '@angular/core';

@Injectable()
export class AuthTokenInterceptor implements HttpInterceptor {

  constructor(private router: Router, private ngZone: NgZone) {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = localStorage.getItem('authToken');
    const shouldSkipToken = this.isPublicEndpoint(request.url);

    const authRequest = (!shouldSkipToken && token)
      ? request.clone({
          setHeaders: {
            Authorization: `Bearer ${token}`
          }
        })
      : request;

    return next.handle(authRequest).pipe(
      catchError((error: HttpErrorResponse) => {
        const isAuthFailure = error.status === 401 || error.status === 403 || error.status === 0;
        if (isAuthFailure && !shouldSkipToken && !!token) {
          this.forceLogoutAndRedirect();
        }
        return throwError(() => error);
      })
    );
  }

  private isPublicEndpoint(url: string): boolean {
    return url.includes('/api/auth/login')
      || url.includes('/api/auth/banker-login')
      || url.includes('/api/auth/validate-captcha')
      || url.includes('/api/auth/health')
      || url.includes('/api/customers/save');
  }

  private forceLogoutAndRedirect(): void {
    this.clearAuthData();

    this.ngZone.run(() => {
      this.router.navigateByUrl('/login', { replaceUrl: true }).then((navigated) => {
        if (!navigated) {
          window.location.href = '/login';
        }
      });
    });
  }

  private clearAuthData(): void {
    localStorage.removeItem('authToken');
    localStorage.removeItem('customerId');
    localStorage.removeItem('userId');
    localStorage.removeItem('email');
    localStorage.removeItem('userName');
    localStorage.removeItem('userType');
  }
}
