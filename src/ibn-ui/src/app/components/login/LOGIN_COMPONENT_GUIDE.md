# Digital Banking Login Component

A comprehensive, secure, and user-friendly login component for the Digital Banking application built with Angular.

## Features

### 1. **User Authentication**
- Login with Customer ID and Password
- Real-time form validation
- Secure credential transmission to backend

### 2. **Account Security**
- **Failed Login Attempt Tracking**: Monitors login failures
- **Progressive CAPTCHA**: Enables after the first failed attempt
- **Account Locking**: Locks account after 3 failed attempts
- **Attempt Counter**: Displays remaining login attempts to user

### 3. **Error Handling**
- User-friendly error messages
- Clear indication of account lock status
- Informative error alerts with icons

### 4. **User Recovery Options**
- **Forgot Password**: Users can request a password reset
- **Forgot Customer ID**: Users can retrieve their Customer ID via email or phone

### 5. **User Experience**
- Clean, modern UI with light color scheme
- Responsive design (desktop, tablet, mobile)
- Smooth animations and transitions
- Accessibility features (ARIA labels, keyboard navigation)
- Dark mode support
- Loading states with spinner animation

## Component Structure

```
src/app/
├── components/
│   ├── login/
│   │   ├── login.component.ts
│   │   ├── login.component.html
│   │   └── login.component.css
│   ├── landing/
│   │   ├── landing.component.ts
│   │   ├── landing.component.html
│   │   └── landing.component.css
│   ├── forgot-password/
│   │   ├── forgot-password.component.ts
│   │   ├── forgot-password.component.html
│   │   └── forgot-password.component.css
│   └── forgot-customer-id/
│       ├── forgot-customer-id.component.ts
│       ├── forgot-customer-id.component.html
│       └── forgot-customer-id.component.css
├── services/
│   ├── login.service.ts
│   └── api.service.ts
└── app-routing.module.ts
```

## Usage

### 1. **Installation**

The components are already integrated into the Angular module. No additional installation is required.

### 2. **Navigation Routes**

```typescript
// Routes configured in app-routing.module.ts
/login                  - Login page
/landing                - User landing/dashboard page
/forgot-password        - Password recovery page
/forgot-customer-id     - Customer ID recovery page
```

### 3. **Backend API Integration**

The `LoginService` communicates with the following backend endpoints:

#### Authentication Endpoint
```
POST /api/auth/login
Request Body:
{
  "customerId": "string",
  "password": "string",
  "captcha": "string" (optional)
}

Response:
{
  "success": boolean,
  "message": "string",
  "token": "string",
  "userId": "string",
  "email": "string"
}
```

#### Account Status Endpoint
```
GET /api/auth/account-status/{customerId}
Response: Account lock status
```

#### Remaining Attempts Endpoint
```
GET /api/auth/remaining-attempts/{customerId}
Response: Number of remaining login attempts
```

#### CAPTCHA Validation Endpoint
```
POST /api/auth/validate-captcha
Request Body: { "captcha": "string" }
Response: Validation result
```

#### Logout Endpoint
```
POST /api/auth/logout
Headers: { "Authorization": "Bearer {token}" }
Response: Logout confirmation
```

### 4. **Backend Configuration**

Update the API URL in `login.service.ts`:

```typescript
private apiUrl = 'http://localhost:8080/api'; // Change to your backend URL
```

## Login Flow

1. User enters Customer ID and Password
2. Form validates inputs
3. If CAPTCHA is enabled (after 1st failed attempt), user must complete verification
4. On submit, credentials are sent to backend
5. Backend authenticates user
6. If successful:
   - Auth token is stored in localStorage
   - User is redirected to dashboard (/landing)
7. If failed:
   - Attempt counter increments
   - Error message is displayed
   - If attempt < 1: CAPTCHA is enabled
   - If attempt >= 3: Account is locked

## Key Features Explained

### Account Lockout Mechanism

| Attempt | Status | Action |
|---------|--------|--------|
| 1st fail | Active | Show error, enable CAPTCHA |
| 2nd fail | Active | Show error with attempts left |
| 3rd fail | Locked | Show account locked message |

### CAPTCHA Implementation

- Simple numeric CAPTCHA (can be enhanced with image-based)
- Displayed after first failed login attempt
- User must answer correctly to proceed
- Backend can validate CAPTCHA using `validateCaptcha()` method

### Session Management

- Auth token is stored in `localStorage`
- Token is used for authenticated API calls
- Logout clears stored credentials
- Session is cleared on logout or browser close

## Styling Details

### Color Scheme (Light Theme)
- Primary: #4f46e5 (Indigo)
- Secondary: #1a3a52 (Dark Blue)
- Background: Linear gradient (Light blue to light gray)
- Success: #f0fdf4 (Light Green)
- Error: #fef2f2 (Light Red)

### Responsive Breakpoints
- Desktop: > 1024px
- Tablet: 768px - 1024px
- Mobile: < 768px

### Accessibility Features
- ARIA labels for screen readers
- Keyboard navigation support
- Focus indicators (2px outline)
- High contrast text colors
- Proper semantic HTML
- Alt text for icons and images

## Customization Guide

### 1. **Customize Colors**

Edit the CSS files to change the color scheme:

```css
/* login.component.css */
.btn-login {
  background: linear-gradient(135deg, #YOUR_COLOR 0%, #YOUR_COLOR_DARK 100%);
}
```

### 2. **Customize CAPTCHA**

Replace the simple numeric CAPTCHA with an image-based CAPTCHA:

```typescript
// In login.component.ts
generateCaptcha(): void {
  // Integrate your CAPTCHA provider (e.g., reCAPTCHA)
  // Example: Use Google reCAPTCHA
}
```

### 3. **Add Additional Validation**

```typescript
// In login.component.ts
onLogin(): void {
  // Add custom validation rules
  if (!this.isValidEmail(this.email)) {
    this.errorMessage = 'Invalid email format';
    return;
  }
}
```

### 4. **Customize Error Messages**

Edit the error messages in `login.component.ts`:

```typescript
this.errorMessage = 'Your custom error message';
```

## Security Considerations

### HTTPS
- Always use HTTPS in production
- Never expose sensitive data in URLs

### Token Storage
- Currently stored in localStorage (consider sessionStorage for higher security)
- Implement token refresh mechanism for long sessions
- Add token expiration validation

### Password Transmission
- Use HTTPS to encrypt password in transit
- Never log passwords
- Implement rate limiting on backend

### CAPTCHA
- Current implementation uses simple numeric CAPTCHA
- Recommend using Google reCAPTCHA v3 for production
- Validate CAPTCHA on backend

### Account Lockout
- Implement configurable lockout duration (e.g., 30 minutes)
- Send notification email to user account
- Provide unlock mechanism via email verification

## Testing

### Unit Tests
```typescript
// Test login success
it('should redirect to landing on successful login', () => {
  // Test implementation
});

// Test failed login
it('should show error message on failed login', () => {
  // Test implementation
});

// Test CAPTCHA enable after first fail
it('should enable CAPTCHA after first failed attempt', () => {
  // Test implementation
});
```

## Browser Compatibility

- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+
- Mobile browsers (iOS Safari, Chrome Android)

## Performance Optimization

- Component lazy loading (consider implementing route-based code splitting)
- CSS is minified in production build
- Images are optimized
- HTTP interceptors for request/response handling (can be added)

## Troubleshooting

### Issue: CAPTCHA not showing after first failed attempt
**Solution**: Check that `showCaptcha` is set to `true` in component logic

### Issue: Login button disabled after error
**Solution**: Ensure `isAccountLocked` is not set to `true` prematurely

### Issue: Styles not applied
**Solution**: Verify CSS files are linked correctly and no CSS conflicts exist

### Issue: API calls failing
**Solution**: 
- Check backend API URL in `login.service.ts`
- Verify CORS is enabled on backend
- Check network tab in browser dev tools

## Future Enhancements

1. **Social Login**: Add Google/Microsoft login options
2. **Biometric Authentication**: Fingerprint/Face ID on mobile
3. **Two-Factor Authentication (2FA)**: SMS or email OTP
4. **Session Management**: Multiple device login tracking
5. **Login History**: Track login attempts and locations
6. **Advanced CAPTCHA**: Image-based or puzzle-based CAPTCHA
7. **Email Verification**: Verify email on account creation
8. **Security Questions**: Additional verification layer

## Support

For issues or questions, please contact:
- Support Email: support@digitalbanking.com
- Support Phone: 1-800-BANKING

---

**Last Updated**: February 2026
**Version**: 1.0.0
