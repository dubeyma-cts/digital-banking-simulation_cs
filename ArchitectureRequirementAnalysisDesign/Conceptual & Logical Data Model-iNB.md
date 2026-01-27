iNB Online Banking – Conceptual & Logical Data Model

Derived from architectural artifacts (C4 context/container, requirement clarifications, and architectural guardrails).

# 1\. Scope and Modeling Assumptions

*   Primary persistence is a relational database for customers, accounts, transactions, configuration, reconciliation and audit data.
*   Unstructured artifacts (KYC documents, statement exports) are stored in object storage and referenced from the relational model by immutable URIs and content hashes.
*   MFA is mandatory for all users; step-up MFA is required for high-risk actions.
*   Auditability is a first-class requirement: security-sensitive and money-moving actions produce immutable audit events with correlation IDs.
*   Retention drivers considered: application logs >= 90 days, transactional data 3 years, unstructured artifacts >= 10 years, with legal hold support.

**Note:** Some artifact values conflict (e.g., RTO 30 vs 60 minutes; supported languages include English+French in one section and English+Hindi in another). These are captured as open items for reconciliation.

# 2\. Conceptual Data Model (Domain View)

The conceptual model identifies the core business concepts and their relationships across functional and non-functional use cases.

![](diagrams/CDM_DomainView.png)

Figure 1 – High-level conceptual entities and relationships.

## 2.1 Conceptual Entities (Definitions)

| Entity | Definition |
| --- | --- |
| Customer | Bank customer who uses iNB services; owns accounts and initiates transactions. |
| User | Digital identity used to authenticate (customer or staff/admin/auditor) and perform actions. |
| Role | Authorization role for RBAC (Customer, Staff, Admin, Auditor) assigned to users. |
| Session | Authenticated session lifecycle, including timeout and step-up triggers. |
| MFA Challenge | One-time challenge for login or high-risk actions; tracks expiry and retry count. |
| KYC Document | PAN/Aadhaar and other KYC artifacts stored in object storage with strict access and audit. |
| Account | Savings/Current account owned by a customer; subject to limits and overdraft rules. |
| Transaction | Posted ledger transaction; parent concept for transfers and bill payments. |
| Transfer | Money transfer (internal or NEFT/RTGS) initiated by customer, confirmed in real time. |
| Bill Payment | Immediate or recurring payment to billers using bank biller APIs and payment gateway. |
| Cheque Deposit | Cheque deposit request with lifecycle (received/clearance/cleared/bounced) and penalties. |
| Statement | Generated statement artifact (PDF/Excel) for posted transactions only. |
| Notification | Outbound SMS/email notifications for approvals, status changes, overdraft usage, and confirmations. |
| Audit Event | Immutable audit trail record for sensitive actions and administrative changes. |
| Configuration | Versioned business configuration such as limits, interest rates, SLAs and penalties with maker-checker workflow. |
| Fraud Alert | Flags suspicious transactions/transfers/payments for staff review and case management. |

# 3\. Logical Data Model

The logical model expresses entities as relational tables with keys, core attributes, and constraints. It is normalized for transactional integrity and auditability.

## 3.1 Logical ER – Core Banking & Payments

![] (diagrams/CDM_CoreBankPaymt.png)

Figure 2 – Core banking, payments, and reporting entities.

## 3.2 Logical ER – Security, Audit & Configuration

![](diagrams/CDM_SecAudConf.png)

Figure 3 – Identity, MFA/session, audit trail, and configuration governance entities.

## 3.3 Logical Table Definitions (Key Entities)

### CUSTOMER

Customer profile and onboarding state (registration/approval), including KYC identifiers and consent metadata.

| Column | Type | Constraints | Description |
| --- | --- | --- | --- |
| customer_id | UUID | PK | Internal identifier. |
| full_name | VARCHAR(200) | NOT NULL | Customer legal name. |
| dob | DATE | NULL | Date of birth (optional in initial scope). |
| pan | VARCHAR(16) | UNIQUE, NOT NULL | PAN identifier (store encrypted/tokenized). |
| aadhaar_hash | VARBINARY(64) | UNIQUE, NOT NULL | Hashed Aadhaar (avoid storing raw Aadhaar). |
| phone | VARCHAR(20) | NOT NULL | Registered phone (for MFA/alerts; store normalized). |
| email | VARCHAR(254) | NOT NULL | Registered email. |
| preferred_language | VARCHAR(10) | DEFAULT "en" | UI language preference (e.g., en, fr, hi). |
| onboarding_status | VARCHAR(30) | NOT NULL | PENDING_APPROVAL / APPROVED / REJECTED / SUSPENDED. |
| created_at | TIMESTAMP | NOT NULL | Creation timestamp. |
| updated_at | TIMESTAMP | NOT NULL | Last update timestamp. |

*   PII/Regulated: encrypt PAN, avoid raw Aadhaar; tokenize/mask in non-prod and logs.
*   Retention: transactional profile data aligns with banking/customer retention policy; audit access to KYC fields.

### USER

Digital identity mapped to the bank Identity Provider (SAML subject) for customers and staff.

| Column | Type | Constraints | Description |
| --- | --- | --- | --- |
| user_id | UUID | PK | Internal user identifier. |
| customer_id | UUID | FK -> CUSTOMER.customer_id, NULL for staff | Link to customer profile where applicable. |
| idp_subject | VARCHAR(200) | UNIQUE, NOT NULL | Subject/NameID from SAML IdP. |
| username | VARCHAR(100) | UNIQUE, NOT NULL | Login identifier used in iNB. |
| user_type | VARCHAR(20) | NOT NULL | CUSTOMER / STAFF / ADMIN / AUDITOR. |
| status | VARCHAR(20) | NOT NULL | ACTIVE / LOCKED / DISABLED. |
| last_login_at | TIMESTAMP | NULL | Last successful login time. |
| created_at | TIMESTAMP | NOT NULL | Creation timestamp. |

*   Lockout policy: track failed attempts via LOGIN\_ATTEMPT; set status=LOCKED when threshold reached.

### ROLE

RBAC roles used by the authorization layer.

| Column | Type | Constraints | Description |
| --- | --- | --- | --- |
| role_id | UUID | PK | Role identifier. |
| role_name | VARCHAR(50) | UNIQUE, NOT NULL | Role name (Customer/Staff/Admin/Auditor). |
| description | VARCHAR(250) | NULL | Role purpose. |

### USER\_ROLE

Many-to-many association between users and roles.

| Column | Type | Constraints | Description |
| --- | --- | --- | --- |
| user_id | UUID | PK, FK -> USER.user_id | User identifier. |
| role_id | UUID | PK, FK -> ROLE.role_id | Role identifier. |
| assigned_at | TIMESTAMP | NOT NULL | Assignment timestamp. |
| assigned_by | UUID | FK -> USER.user_id | Admin/staff who assigned role. |

*   Composite PK (user\_id, role\_id).

### SESSION

Session lifecycle and security attributes used for timeout and re-authentication.

| Column | Type | Constraints | Description |
| --- | --- | --- | --- |
| session_id | UUID | PK | Session identifier. |
| user_id | UUID | FK -> USER.user_id | Owner user. |
| created_at | TIMESTAMP | NOT NULL | Session start time. |
| expires_at | TIMESTAMP | NOT NULL | Hard expiry time. |
| last_seen_at | TIMESTAMP | NOT NULL | Activity timestamp. |
| ip_address | VARCHAR(45) | NOT NULL | IPv4/IPv6. |
| device_fingerprint | VARCHAR(200) | NULL | Optional device identifier. |
| status | VARCHAR(20) | NOT NULL | ACTIVE / REVOKED / EXPIRED. |

*   Do not store raw session tokens in DB; store opaque session\_id only; bind tokens via secure cookies.

### MFA\_METHOD

Registered MFA methods (SMS OTP or authenticator app) for each user.

| Column | Type | Constraints | Description |
| --- | --- | --- | --- |
| method_id | UUID | PK | MFA method identifier. |
| user_id | UUID | FK -> USER.user_id | Owner user. |
| method_type | VARCHAR(20) | NOT NULL | SMS / TOTP. |
| masked_destination | VARCHAR(80) | NULL | Masked phone/email for user display. |
| totp_secret_ref | VARCHAR(200) | NULL | Reference to secret stored in KMS/HSM (never store plaintext). |
| enabled | BOOLEAN | NOT NULL | Is method enabled. |
| verified_at | TIMESTAMP | NULL | Verification time. |

*   Secrets must be stored/managed via KMS/HSM and rotated per policy.

### MFA\_CHALLENGE

OTP/MFA challenge instance for login or step-up; includes retry/attempt counters.

| Column | Type | Constraints | Description |
| --- | --- | --- | --- |
| challenge_id | UUID | PK | Challenge identifier. |
| user_id | UUID | FK -> USER.user_id | User performing authentication. |
| method_id | UUID | FK -> MFA_METHOD.method_id | MFA method used. |
| purpose | VARCHAR(30) | NOT NULL | LOGIN / HIGH_RISK_ACTION. |
| related_entity_type | VARCHAR(40) | NULL | Optional: TRANSFER_INSTRUCTION, CONFIG_ITEM, etc. |
| related_entity_id | UUID | NULL | Optional related entity id. |
| otp_hash | VARBINARY(64) | NULL | Hash of OTP if stored server-side (SMS). |
| expires_at | TIMESTAMP | NOT NULL | Expiry time. |
| attempt_count | INT | NOT NULL DEFAULT 0 | Failed attempts for this challenge. |
| status | VARCHAR(20) | NOT NULL | PENDING / VERIFIED / LOCKED / EXPIRED. |
| created_at | TIMESTAMP | NOT NULL | Creation timestamp. |
| correlation_id | VARCHAR(64) | NULL | Trace id across logs/telemetry. |

*   Enforce retry limits and cooldown after threshold; invalidate challenge upon lock/expiry.

### ACCOUNT

Savings/Current account information and ownership.

| Column | Type | Constraints | Description |
| --- | --- | --- | --- |
| account_id | UUID | PK | Account identifier. |
| customer_id | UUID | FK -> CUSTOMER.customer_id | Account owner. |
| account_number | VARCHAR(30) | UNIQUE, NOT NULL | Customer-facing account number. |
| account_type | VARCHAR(20) | NOT NULL | SAVINGS / CURRENT. |
| branch_code | VARCHAR(20) | NOT NULL | Branch identifier. |
| currency | CHAR(3) | NOT NULL | ISO currency code. |
| status | VARCHAR(20) | NOT NULL | ACTIVE / CLOSED / FROZEN. |
| opened_at | DATE | NOT NULL | Account open date. |
| closed_at | DATE | NULL | Closure date (if any). |

*   Balances should be maintained in a separate ACCOUNT\_BALANCE or derived from ledger with proper isolation/locking.

### TRANSACTION

Posted ledger transaction (statements show posted transactions only).

| Column | Type | Constraints | Description |
| --- | --- | --- | --- |
| txn_id | UUID | PK | Transaction id. |
| account_id | UUID | FK -> ACCOUNT.account_id | Account affected. |
| txn_type | VARCHAR(30) | NOT NULL | TRANSFER / BILL_PAYMENT / CHEQUE / INTEREST / FEE etc. |
| amount | DECIMAL(18,2) | NOT NULL | Signed amount (use entries for debit/credit). |
| currency | CHAR(3) | NOT NULL | Currency. |
| posted_at | TIMESTAMP | NOT NULL | Posting timestamp. |
| value_date | DATE | NOT NULL | Value date. |
| status | VARCHAR(20) | NOT NULL | POSTED / REVERSED. |
| reference | VARCHAR(64) | UNIQUE | Customer-visible reference id. |
| channel | VARCHAR(20) | NOT NULL | WEB / ADMIN / SYSTEM. |
| correlation_id | VARCHAR(64) | NULL | Trace correlation id. |

*   Retention: transactional data retained 3 years (minimum as clarified).

### BENEFICIARY

Saved beneficiary/payee details for transfers.

| Column | Type | Constraints | Description |
| --- | --- | --- | --- |
| beneficiary_id | UUID | PK | Beneficiary id. |
| customer_id | UUID | FK -> CUSTOMER.customer_id | Owner customer. |
| name | VARCHAR(200) | NOT NULL | Beneficiary name. |
| bank_name | VARCHAR(200) | NULL | Beneficiary bank. |
| ifsc | VARCHAR(16) | NULL | IFSC for Indian transfers. |
| account_number | VARCHAR(30) | NOT NULL | Beneficiary account number. |
| beneficiary_type | VARCHAR(20) | NOT NULL | INTERNAL / EXTERNAL. |
| status | VARCHAR(20) | NOT NULL | ACTIVE / DISABLED. |
| created_at | TIMESTAMP | NOT NULL | Creation timestamp. |

*   High-risk changes (add/edit beneficiary) should require step-up MFA and be fully audited.

### TRANSFER\_INSTRUCTION

Customer-initiated transfer request; idempotent and confirmed in real time.

| Column | Type | Constraints | Description |
| --- | --- | --- | --- |
| transfer_id | UUID | PK | Transfer instruction id. |
| customer_id | UUID | FK -> CUSTOMER.customer_id | Initiating customer. |
| source_account_id | UUID | FK -> ACCOUNT.account_id | Debit account. |
| beneficiary_id | UUID | FK -> BENEFICIARY.beneficiary_id | Payee/beneficiary. |
| amount | DECIMAL(18,2) | NOT NULL | Transfer amount. |
| mode | VARCHAR(20) | NOT NULL | INTERNAL / NEFT / RTGS. |
| status | VARCHAR(20) | NOT NULL | INITIATED / PROCESSING / COMPLETED / FAILED. |
| idempotency_key_id | UUID | FK -> IDEMPOTENCY_KEY.key_id | Idempotency binding. |
| created_at | TIMESTAMP | NOT NULL | Request time. |
| completed_at | TIMESTAMP | NULL | Completion time. |
| reference | VARCHAR(64) | UNIQUE | Customer reference id. |

*   Requires step-up MFA for high-risk transfers; validate per-customer and per-transaction limits server-side.

### BILLER

Master list of billers integrated via bank biller APIs.

| Column | Type | Constraints | Description |
| --- | --- | --- | --- |
| biller_id | UUID | PK | Biller id. |
| name | VARCHAR(200) | NOT NULL | Biller name. |
| category | VARCHAR(50) | NULL | Utilities/Telecom/etc. |
| api_reference | VARCHAR(100) | NOT NULL | Identifier used in biller APIs. |
| active | BOOLEAN | NOT NULL | Is biller active. |

### BILL\_PAYMENT

Bill payment (immediate or recurring), processed via payment gateway and confirmed in real time.

| Column | Type | Constraints | Description |
| --- | --- | --- | --- |
| bill_payment_id | UUID | PK | Bill payment id. |
| customer_id | UUID | FK -> CUSTOMER.customer_id | Paying customer. |
| source_account_id | UUID | FK -> ACCOUNT.account_id | Debit account. |
| biller_id | UUID | FK -> BILLER.biller_id | Biller. |
| consumer_number | VARCHAR(60) | NOT NULL | Customer identifier at biller. |
| amount | DECIMAL(18,2) | NOT NULL | Amount. |
| is_recurring | BOOLEAN | NOT NULL | Recurring flag. |
| status | VARCHAR(20) | NOT NULL | INITIATED / COMPLETED / FAILED. |
| gateway_reference | VARCHAR(100) | NULL | Payment gateway ref (e.g., PayPal). |
| idempotency_key_id | UUID | FK -> IDEMPOTENCY_KEY.key_id | Idempotency binding. |
| created_at | TIMESTAMP | NOT NULL | Initiation time. |

*   Recurring schedules may be modeled separately (PAYMENT\_SCHEDULE) for monthly runs.

### CHEQUE\_DEPOSIT

Cheque deposit requests and lifecycle with SLA and bounce penalties.

| Column | Type | Constraints | Description |
| --- | --- | --- | --- |
| cheque_deposit_id | UUID | PK | Cheque deposit id. |
| customer_id | UUID | FK -> CUSTOMER.customer_id | Submitting customer. |
| account_id | UUID | FK -> ACCOUNT.account_id | Credit account. |
| cheque_number | VARCHAR(30) | NOT NULL | Cheque number. |
| drawer_bank | VARCHAR(200) | NULL | Drawer bank name. |
| Amount | DECIMAL(18,2) | NOT NULL | Cheque amount. |
| submitted_at | TIMESTAMP | NOT NULL | Submission time. |
| current_status | VARCHAR(30) | NOT NULL | RECEIVED / IN_CLEARANCE / CLEARED / BOUNCED. |
| clearance_sla_days | INT | NOT NULL | SLA days (configurable; default 3). |
| bounce_penalty_amount | DECIMAL(18,2) | NULL | Penalty (configurable by branch). |

*   Track lifecycle changes in CHEQUE\_STATUS\_HISTORY and notify customer via SMS on status updates.

### AUDIT\_EVENT

Immutable audit record for security-sensitive and business-critical actions.

| Column | Type | Constraints | Description |
| --- | --- | --- | --- |
| audit_id | UUID | PK | Audit event id. |
| actor_user_id | UUID | FK -> USER.user_id | Actor user. |
| actor_role | VARCHAR(50) | NOT NULL | Role at the time of action. |
| action | VARCHAR(80) | NOT NULL | Action code (LOGIN, TRANSFER_CREATE, CONFIG_CHANGE, etc.). |
| entity_type | VARCHAR(60) | NOT NULL | Target entity type. |
| entity_id | UUID | NOT NULL | Target entity id. |
| outcome | VARCHAR(20) | NOT NULL | SUCCESS / FAILURE. |
| timestamp | TIMESTAMP | NOT NULL | Event time. |
| ip_address | VARCHAR(45) | NOT NULL | IP address. |
| device_metadata | VARCHAR(400) | NULL | Device/user-agent metadata. |
| correlation_id | VARCHAR(64) | NULL | Correlation id (propagated to SIEM/APM). |

*   Audit logs should be append-only / write-once where feasible; access controlled and itself audited.

### CONFIG\_ITEM

Versioned business configuration (limits, rates, SLAs, penalties) with auditability and rollback.

| Column | Type | Constraints | Description |
| --- | --- | --- | --- |
| config_id | UUID | PK | Configuration item id. |
| config_key | VARCHAR(100) | NOT NULL | Key name (e.g., TRANSFER_LIMIT_DAILY). |
| scope_type | VARCHAR(20) | NOT NULL | GLOBAL / BRANCH / CUSTOMER. |
| scope_id | VARCHAR(60) | NULL | Branch code or customer id (if applicable). |
| value_json | JSON | NOT NULL | Configuration value as JSON. |
| version | INT | NOT NULL | Monotonic version number. |
| status | VARCHAR(20) | NOT NULL | DRAFT / ACTIVE / RETIRED. |
| effective_from | TIMESTAMP | NOT NULL | Start of validity. |
| effective_to | TIMESTAMP | NULL | End of validity. |
| created_by | UUID | FK -> USER.user_id | Maker (proposer). |
| created_at | TIMESTAMP | NOT NULL | Creation time. |

*   High-impact changes require maker-checker approval (CONFIG\_APPROVAL).

# 4\. Appendix – Use-Case Coverage (Mapping)

The following mapping indicates primary entities supporting each major use case.

| Use Case | Primary Entities |
| --- | --- |
| UC1 Customer Registration | CUSTOMER, USER, KYC_DOCUMENT, AUDIT_EVENT, NOTIFICATION, CONFIG_ITEM (approval rules) |
| UC2 Login & Home Page | USER, ROLE, USER_ROLE, SESSION, MFA_METHOD, MFA_CHALLENGE, LOGIN_ATTEMPT, AUDIT_EVENT |
| UC3 Account Management | ACCOUNT, ACCOUNT_LIMIT, CONFIG_ITEM (interest/overdraft), TRANSACTION, NOTIFICATION, AUDIT_EVENT |
| UC4 Statements | STATEMENT_REQUEST, STATEMENT_ARTIFACT, TRANSACTION, ACCOUNT |
| UC5 Cheque Deposits | CHEQUE_DEPOSIT, CHEQUE_STATUS_HISTORY, NOTIFICATION, AUDIT_EVENT |
| UC6 Reconciliation | RECONCILIATION_RUN, RECONCILIATION_ITEM, TRANSACTION, AUDIT_EVENT |
| UC7 Bill Payments | BILLER, BILL_ACCOUNT, PAYMENT_SCHEDULE, BILL_PAYMENT, IDEMPOTENCY_KEY, TRANSACTION, NOTIFICATION |
| UC8 Money Transfer | BENEFICIARY, TRANSFER_INSTRUCTION, TRANSFER_EXECUTION, IDEMPOTENCY_KEY, TRANSACTION, NOTIFICATION, FRAUD_ALERT |
| UC9 Security/Audit/Compliance | AUDIT_EVENT, CONFIG_ITEM/APPROVAL, LOGIN_ATTEMPT, MFA_CHALLENGE, RETENTION fields |
