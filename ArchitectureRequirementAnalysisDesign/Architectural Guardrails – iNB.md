**Architectural Guardrails – iNB Online Banking**

_Baseline guardrails derived from clarified requirements, HLD security objectives, and STRIDE threat modeling._

**1\. Purpose & Applicability**

*   These guardrails define non-negotiable architectural constraints and minimum standards for designing, building, and operating iNB Online Banking.
*   They apply to all solution components: Web UI, Staff/Admin Portal, API Layer, domain services, data stores, and all external integrations (IdP/SMS/Email/PayPal/NEFT-Biller/SIEM).
*   Guardrails are enforceable via design reviews, automated controls (CI/CD quality gates), and operational evidence (monitoring, audits, DR drills).

**2\. Architecture Principles (Non‑Negotiable)**

*   Security-first: treat every boundary as hostile; least privilege everywhere; default deny.
*   Auditability by design: every security-sensitive and money-moving action must be traceable end-to-end.
*   Resilience by default: failure of external dependencies must not cascade; degrade gracefully.
*   Privacy & compliance by design: minimize, mask, and control PII/KYC data; enforce residency and retention.
*   Operational excellence: observability, runbooks, and automation are part of “done”.

**3\. Identity, Access & Session Guardrails**

**3.1 Authentication**

*   MFA is mandatory for all users (customers and staff). Support SMS OTP and/or authenticator-based MFA.
*   Integrate authentication with the bank’s SAML Identity Provider (IdP).
*   Account lockout after 3 failed login attempts; implement brute-force protections (rate limits, WAF rules, anomaly detection).

**3.2 Authorization**

*   Server-side authorization is mandatory at API and service layers; never rely on UI-only controls.
*   Enforce strict RBAC for Customer vs Staff vs Admin vs Auditor roles; separate admin APIs and privileges.
*   Privileged access: require strong MFA, consider conditional access, and periodic access reviews for staff/admin roles.

**3.3 Session Management**

*   Use secure session tokens/cookies (HttpOnly, Secure, SameSite) and regenerate session identifiers on login and after MFA.
*   Implement session timeout and re-authentication per policy; short session TTL for high-risk actions.
*   Step-up MFA is required for high-risk transfers, beneficiary changes, and high-impact admin changes.

**4\. Data Protection & Cryptography Guardrails**

**4.1 In-Transit & At-Rest Encryption**

*   TLS 1.3 is mandatory for all in-transit communication (client-to-edge, edge-to-API, service-to-service, service-to-external).
*   AES-256 encryption is mandatory for data at rest in relational databases and object storage.

**4.2 Key Management**

*   Centralize encryption key management using enterprise KMS/HSM. Use envelope encryption for data encryption keys (DEKs).
*   Implement key rotation with defined cadence; enforce access policies for key usage; retain key usage audit trails.
*   Never store secrets in code repositories, images, or logs; use a secrets manager and rotate credentials regularly.

**4.3 PII/KYC Handling**

*   Apply data masking/tokenization for PII in logs and non-production environments.
*   Restrict access to KYC documents (PAN/Aadhaar) with least privilege, time-limited signed access, and audit all access.
*   Do not expose sensitive details in error messages; avoid verbose authentication/transaction failures.

**5\. Audit Logging & Non‑Repudiation Guardrails**

*   Maintain an immutable audit trail for: login attempts, MFA challenges, session events, money transfers, bill payments, admin configuration changes, and access to audit logs.
*   Capture who/what/when/where: actor identity, role, action, object, outcome, timestamp, IP/device metadata, correlation ID, and reference IDs.
*   Audit logs must be append-only / write-once where possible; enforce RBAC and separation of duties for log access.
*   Application log retention is 90 days (minimum) as per clarified requirement; retain audit evidence as required for SOX/Legal Hold.

**6\. Transaction Integrity Guardrails (Transfers & Bill Payments)**

*   All money-moving APIs must be idempotent (idempotency keys) to prevent duplicates during retries/timeouts.
*   All validations are server-side: beneficiary, amount, per-customer limits, transaction type rules, and fraud/risk controls.
*   Real-time confirmation is mandatory for transfers and bill payments; ensure durable state before confirming to the user.
*   Notify customers with reference IDs via configured channels (SMS/email) for confirmations and critical events.

**7\. External Integrations & Resilience Guardrails**

*   Treat all external providers as trust boundaries: validate inputs/outputs; apply strict schemas and allow-lists.
*   Resilience patterns are mandatory at every third-party boundary: timeouts, retries with exponential backoff, circuit breakers, and bulkheads.
*   Implement rate limiting and backpressure on expensive endpoints (login, OTP issuance, transfers, payments) to reduce DoS risk.
*   Integration SLAs must be documented and tested; define fallback behavior and customer messaging for provider outages.

**8\. Availability, DR, Backup & Retention Guardrails**

*   Availability target: 99.95% for customer-facing services; design for HA at web/API/service tiers and data tier.
*   DR objectives (as clarified): RTO 30 minutes; RPO 10 minutes. DR runbooks must be tested via periodic drills with recorded evidence.
*   Backups: execute daily (nightly job); protect backups with encryption and access controls; test restore procedures regularly.
*   Retention: transactional data retained for 3 years; unstructured data retained for minimum 10 years with legal hold support.

**9\. Observability & Monitoring Guardrails**

*   Instrument logs, metrics, and traces across all components; propagate correlation IDs end-to-end.
*   Integrate telemetry with Splunk/AppDynamics; alert on auth anomalies, fraud flags, SLA breaches, error rates, and resource saturation.
*   Sensitive data controls: mask PII in telemetry; enforce RBAC for observability tools; audit access to logs and dashboards.

**10\. Secure SDLC & Quality Gates Guardrails**

*   Mandatory security testing before go-live: SAST, DAST, dependency scanning, secrets scanning, and penetration testing.
*   Apply secure coding controls: input validation, output encoding, parameterized queries/ORM, secure HTTP headers, CSP where applicable.
*   CI/CD must enforce quality gates: tests, coverage thresholds, security scan pass criteria, and approved deployment artifacts.
*   Threat model alignment: maintain traceability from STRIDE Threat IDs (TM-\*) to controls and test cases in the RTM.

**11\. Governance, Change Control & Exceptions**

*   High-impact changes (rates, limits, SLAs, penalties, access policies) require maker-checker approval workflow and audit trail.
*   Configuration must be versioned and rollbackable; changes must be attributable to an individual (non-repudiation).
*   Guardrail exceptions require documented risk acceptance, compensating controls, and time-bound remediation plan.

**12\. Minimum Evidence Checklist (Audit Readiness)**

1.  MFA and SAML integration evidence (config screenshots, test results).
2.  TLS 1.3 and AES-256 configuration evidence (scanner reports / platform configs).
3.  KMS/HSM policies and key rotation logs.
4.  Audit log samples showing correlation IDs and immutable storage settings.
5.  DR drill report meeting RTO/RPO; restore test evidence.
6.  Security test reports (SAST/DAST/dependency scans/secrets scan/pen-test).
7.  Monitoring dashboards and alert configuration evidence.
8.  Access review records for staff/admin/auditor roles.