**Architecture Decision Record (ADR)**

Indian Net Bank (iNB) – Online Banking Application

_Version 1.0 | Date: 27-Jan-2026_

Inputs: Requirements Clarification, Architectural Guardrails, C4 Context/Containers, Deployment & Network Segmentation Model

# 1\. Purpose

This ADR artefact records key architectural decisions for the iNB Online Banking platform. Each decision captures the context, the decision taken, consequences/trade-offs, governance expectations, and implementation notes to support review and sign-off.

# 2\. Input Artefacts

*   Architectural\_Guardrails\_iNB.docx
*   iBank\_RequirementAnalysisClarafication\_v2.docx
*   C4\_Level1\_System\_Context\_with\_L2\_Diagram\_iNB.docx
*   iNB\_Deployment\_Model\_UML\_OnPrem\_VM\_Oracle\_ActivePassive\_v3\_NetworkSegmentation\_FWZones.docx

# 3\. Architecture Characteristics (Extracted)

*   Security-first (SAML IdP, MFA, RBAC, session security, encryption).
*   Auditability and non-repudiation (immutable audit trail, correlation IDs).
*   Resilience and availability (99.95% target, graceful degradation).
*   Disaster recovery & backup (RTO 30 min, RPO 10 min, nightly backups and restore testing).
*   Privacy, compliance & data governance (India residency, retention, legal hold, PII masking).
*   Operational excellence (Splunk/AppDynamics observability, runbooks, automation).
*   Transaction integrity (idempotency, durable state, real-time confirmations).
*   Performance & scalability (VM pools, caching where appropriate).

# 4\. ADR Index

| ADR ID | Title | Status | Decision Drivers |
| --- | --- | --- | --- |
| INB-ADR-001 | Identity: SAML IdP with Mandatory MFA and Step-up for High-Risk Actions | Proposed | Security, Compliance, User Experience |
| INB-ADR-002 | Cryptography: TLS 1.3 In-Transit, AES-256 At-Rest, Centralized KMS/HSM | Proposed | Security, Privacy, Regulatory Compliance |
| INB-ADR-003 | Auditability: Immutable Audit Logging with Correlation IDs and Retention | Proposed | Auditability, Non-repudiation, Compliance |
| INB-ADR-004 | Resilience: Standard Resilience Patterns for External Integrations | Proposed | Resilience, Availability |
| INB-ADR-005 | Transaction Integrity: Idempotent Money-Moving APIs and Durable State | Proposed | Integrity, Customer Trust, Operational Risk |
| INB-ADR-006 | Availability & DR: On-Prem Active–Passive DR with Manual Failover Meeting RTO/RPO | Proposed | Availability, Disaster Recovery, Business Continuity |
| INB-ADR-007 | Data: Relational System of Record + Object Storage for Unstructured KYC/Exports with Retention | Proposed | Data Integrity, Consistency, Compliance Retention |
| INB-ADR-008 | Operations: Observability with Splunk + AppDynamics and Correlation IDs | Proposed | Operational Excellence, Performance, Security Monitoring |
| INB-ADR-009 | Compliance: India Data Residency + Privacy Controls (Masking, Least Privilege, Legal Hold) | Proposed | Regulatory Compliance, Privacy, Risk Management |
| INB-ADR-010 | Performance: Caching Strategy for High-Latency Hotspots | Proposed | Performance, Scalability |
| INB-ADR-011 | Deployment: On-Prem VM Pools with Network Segmentation (DMZ/App/Data/Ops) and Firewalls | Proposed | Compliance (Residency), Security, Operability |
| INB-ADR-012 | API Entry: DMZ WAF + Reverse Proxy/Load Balancer as the API Gateway Pattern | Proposed | Security, Rate Limiting, Observability |

# 5\. ADR Details

## INB-ADR-001: Identity: SAML IdP with Mandatory MFA and Step-up for High-Risk Actions

| Status | Proposed |
| --- | --- |
| Decision Drivers | Security, Compliance, User Experience |
| Source Artefacts | Architectural_Guardrails_iNB.docx; iBank_RequirementAnalysisClarafication_v2.docx; C4_Level1_System_Context_with_L2_Diagram_iNB.docx |
| Governance | Security/IAM design review; Pen-test validation for auth flows; Audit sign-off for privileged access controls. |

### Context

iNB must authenticate customers and staff using the bank’s existing identity platform, enforce strong authentication, and protect high-risk actions such as transfers and privileged admin changes.

### Decision

Use the bank SAML-based IdP for primary authentication. Enforce MFA for every login (SMS OTP and/or authenticator). Require step-up MFA for high-risk operations (transfers, beneficiary maintenance, high-impact admin changes). Apply account lockout after 3 failed attempts and session timeout/re-authentication policies.

### Consequences

Positive:

*   Leverages enterprise identity policies and centralized control.
*   Reduces account takeover risk via MFA and step-up.
*   Clear RBAC separation for Customer/Staff/Admin/Auditor roles.

Trade-offs / Negative:

*   Dependency on IdP/OTP availability; requires graceful degradation messaging.
*   Potential UX friction; step-up triggers must be tuned.

### Notes

*   Use secure session cookies/tokens (HttpOnly, Secure, SameSite) and rotate session identifiers after login/MFA.
*   Apply rate limiting and WAF rules for login and OTP issuance endpoints.
*   Authorization must be enforced server-side at API and service layers.

### Open Questions

*   Confirm MFA method mix for phase-1 (SMS OTP only vs also authenticator).
*   Define step-up thresholds (amount limits, new device/geo anomalies).

## INB-ADR-002: Cryptography: TLS 1.3 In-Transit, AES-256 At-Rest, Centralized KMS/HSM

| Status | Proposed |
| --- | --- |
| Decision Drivers | Security, Privacy, Regulatory Compliance |
| Source Artefacts | Architectural_Guardrails_iNB.docx; iBank_RequirementAnalysisClarafication_v2.docx; iNB_Deployment_Model_UML_OnPrem_VM_Oracle_ActivePassive_v3_NetworkSegmentation_FWZones.docx |
| Governance | Security architecture review; periodic crypto configuration audits; compliance evidence collection. |

### Context

iNB handles PII/KYC documents and financial transactions. Requirements mandate encryption for data in transit and at rest with enterprise-grade key management.

### Decision

Mandate TLS 1.3 for all hops (client-edge, edge-API, service-service, service-external). Encrypt all persistent stores (Oracle DB, object storage, backups) using AES-256. Use enterprise KMS/HSM with envelope encryption, key rotation, access policies, and key usage auditing. Use a secrets manager; prohibit secrets in code/images/logs.

### Consequences

Positive:

*   Meets required encryption standards for banking workloads.
*   Centralized key governance improves auditability and reduces operational risk.

Trade-offs / Negative:

*   Operational overhead for certificate and key rotation.
*   May require remediation for any legacy endpoints that cannot support TLS 1.3.

### Notes

*   Mask/tokenize PII in logs and non-production.
*   Use signed, time-limited access for KYC documents in object storage.

### Open Questions

*   Confirm enterprise KMS/HSM product and integration approach.
*   Define certificate lifecycle ownership (DevOps vs Security).

## INB-ADR-003: Auditability: Immutable Audit Logging with Correlation IDs and Retention

| Status | Proposed |
| --- | --- |
| Decision Drivers | Auditability, Non-repudiation, Compliance |
| Source Artefacts | Architectural_Guardrails_iNB.docx; iBank_RequirementAnalysisClarafication_v2.docx; C4_Level1_System_Context_with_L2_Diagram_iNB.docx |
| Governance | Compliance and Internal Audit review; separation-of-duties for log access; periodic access reviews. |

### Context

Banking systems require forensic readiness. iNB must provide end-to-end traceability across UI/API/services and external providers, with controlled access and retention.

### Decision

Implement an audit logging capability that records append-only audit events for authentication/session, money movement, admin configuration changes, and access to audit records. Include actor identity, role, action, object, outcome, timestamps, IP/device metadata, correlation ID, and external reference IDs. Forward logs to Splunk for centralized storage/SIEM correlation. Retain application logs at least 90 days; retain audit evidence per SOX and legal-hold requirements.

### Consequences

Positive:

*   Improves compliance posture and investigation capability.
*   Enables security monitoring and anomaly detection via SIEM.

Trade-offs / Negative:

*   Increased log volume and storage costs.
*   Requires strict PII masking and access controls to prevent sensitive data leakage.

### Notes

*   Standardize event schemas and correlation ID propagation.
*   Audit access to logs/dashboards; enforce RBAC in Splunk/AppDynamics.

### Open Questions

*   Decide immutable storage mechanism (WORM vs controlled append-only).
*   Confirm audit log retention duration beyond 90-day application log retention.

## INB-ADR-004: Resilience: Standard Resilience Patterns for External Integrations

| Status | Proposed |
| --- | --- |
| Decision Drivers | Resilience, Availability |
| Source Artefacts | Architectural_Guardrails_iNB.docx; C4_Level1_System_Context_with_L2_Diagram_iNB.docx |
| Governance | Architecture review board approval; operational readiness review (runbooks, SLOs). |

### Context

iNB integrates with IdP, SMS/Email providers, PayPal, NEFT/RTGS, and biller APIs. External outages/latency must not cascade into platform-wide failure.

### Decision

At each external boundary, enforce strict timeouts, retries with exponential backoff, circuit breakers, and bulkheads. Apply schema validation and allow-lists on all external payloads. Add rate limiting and backpressure on expensive endpoints (login, OTP issuance, transfers, payments). Define SLA expectations and fallback customer messaging for provider outages.

### Consequences

Positive:

*   Reduces cascading failures and improves uptime.
*   Predictable behavior under partial outages and peak load.

Trade-offs / Negative:

*   Requires tuning thresholds and coordinated operational runbooks.
*   Some fallbacks may require additional components (queues, caches).

### Notes

*   Track provider latency/error metrics; include in dashboards.
*   Test resilience using fault injection in non-prod where feasible.

### Open Questions

*   Do we need async queueing for non-critical notifications during downtime?
*   Define provider-specific SLAs and contract alignment.

## INB-ADR-005: Transaction Integrity: Idempotent Money-Moving APIs and Durable State

| Status | Proposed |
| --- | --- |
| Decision Drivers | Integrity, Customer Trust, Operational Risk |
| Source Artefacts | Architectural_Guardrails_iNB.docx; iBank_RequirementAnalysisClarafication_v2.docx |
| Governance | Risk and Operations review; reconciliation/audit sign-off for transaction lifecycle. |

### Context

Transfers and bill payments are critical workflows. Retries and partial failures can cause duplicates or inconsistent state if not explicitly handled.

### Decision

Design money-moving APIs to be idempotent using idempotency keys for client and internal retries. Persist durable transaction state and external reference IDs before confirming success to the user. Enforce server-side validation for beneficiary, amount, limits, and fraud/risk controls prior to execution.

### Consequences

Positive:

*   Prevents duplicate debits/credits during retries.
*   Improves reconciliation and customer support with consistent reference IDs.

Trade-offs / Negative:

*   Requires data model/state machine for idempotency and transaction lifecycle.
*   May add some latency due to durability guarantees.

### Notes

*   Return reference IDs in confirmations via SMS/email where configured.
*   Align reconciliation reports with transaction state transitions.

### Open Questions

*   Define idempotency key scope and retention window.
*   Confirm fraud detection MVP approach (rules vs third-party).

## INB-ADR-006: Availability & DR: On-Prem Active–Passive DR with Manual Failover Meeting RTO/RPO

| Status | Proposed |
| --- | --- |
| Decision Drivers | Availability, Disaster Recovery, Business Continuity |
| Source Artefacts | iNB_Deployment_Model_UML_OnPrem_VM_Oracle_ActivePassive_v3_NetworkSegmentation_FWZones.docx; Architectural_Guardrails_iNB.docx; iBank_RequirementAnalysisClarafication_v2.docx |
| Governance | DR committee sign-off; scheduled DR drills with documented evidence; change control for DR runbooks. |

### Context

Requirements specify 99.95% availability with DR targets RTO 30 minutes and RPO 10 minutes. Deployment model confirms on-prem hosting with active–passive warm standby and manual failover.

### Decision

Deploy web/API/service tiers on VM pools with redundancy in production. Use Oracle HA in production and standby replication (e.g., Data Guard) to a DR site. Implement active–passive warm standby DR with manual failover procedures. Execute nightly encrypted backups and periodic restore tests. Conduct periodic DR drills and capture evidence.

### Consequences

Positive:

*   Meets availability and recovery objectives with clear operational model.
*   Improves readiness through drills and restore testing.

Trade-offs / Negative:

*   Higher infrastructure cost for redundancy and DR.
*   Manual failover increases operational dependency and requires well-practiced runbooks.

### Notes

*   Monitor replication lag to ensure RPO is maintained.
*   Define cutover criteria and customer communication templates for DR events.

### Open Questions

*   Confirm tier-wise failover details and responsibilities.
*   Confirm hosting future roadmap impact (hybrid/cloud) on DR design.

## INB-ADR-007: Data: Relational System of Record + Object Storage for Unstructured KYC/Exports with Retention

| Status | Proposed |
| --- | --- |
| Decision Drivers | Data Integrity, Consistency, Compliance Retention |
| Source Artefacts | C4_Level1_System_Context_with_L2_Diagram_iNB.docx; iBank_RequirementAnalysisClarafication_v2.docx; Architectural_Guardrails_iNB.docx |
| Governance | Data governance approval; retention policy review; periodic access reviews for sensitive artifacts. |

### Context

iNB requires strong consistency for accounts/transactions and long-term compliant storage for KYC documents and exports.

### Decision

Use a relational database (Oracle) as the system of record for customers, accounts, transactions, configurations, and reconciliation data. Use compliant object storage for KYC documents and exported statements. Enforce retention: transactional data ~3 years; unstructured/KYC minimum 10 years; support legal hold. Restrict and audit access to KYC documents using least privilege and signed time-limited access.

### Consequences

Positive:

*   Strong integrity for financial data using relational constraints.
*   Optimized unstructured storage with clear retention governance.

Trade-offs / Negative:

*   Requires careful metadata linkage between DB records and object storage.
*   Long retention increases storage governance and cost.

### Notes

*   Encrypt both DB and object storage at rest and in transit.
*   Use masked/synthetic data in non-prod environments.

### Open Questions

*   Confirm object storage platform and whether WORM/immutability is required for some artifacts.

## INB-ADR-008: Operations: Observability with Splunk + AppDynamics and Correlation IDs

| Status | Proposed |
| --- | --- |
| Decision Drivers | Operational Excellence, Performance, Security Monitoring |
| Source Artefacts | Architectural_Guardrails_iNB.docx; C4_Level1_System_Context_with_L2_Diagram_iNB.docx; iNB_Deployment_Model_UML_OnPrem_VM_Oracle_ActivePassive_v3_NetworkSegmentation_FWZones.docx |
| Governance | SRE/Operations review; Security monitoring controls; periodic dashboard/access reviews. |

### Context

Operations require end-to-end visibility of logs, metrics, traces, and security anomalies. Artefacts specify Splunk and AppDynamics integrations.

### Decision

Instrument all components to emit structured logs, metrics, and traces. Deploy agents/forwarders to send telemetry to Splunk (logs/SIEM) and AppDynamics (APM). Standardize correlation IDs across inbound requests and propagate through services and external calls. Mask PII in telemetry and enforce RBAC for observability tools.

### Consequences

Positive:

*   Improves MTTR with tracing and dashboards.
*   Supports security monitoring via SIEM correlation.

Trade-offs / Negative:

*   Telemetry volume can increase costs; requires sampling and governance.
*   Requires disciplined PII masking and access control.

### Notes

*   Alert on auth anomalies, fraud flags, SLA breaches, error rates, and resource saturation.
*   Define standard log levels and retention policies.

### Open Questions

*   Define SLOs/SLIs and alert thresholds per service.
*   Choose trace sampling strategy for high-volume endpoints.

## INB-ADR-009: Compliance: India Data Residency + Privacy Controls (Masking, Least Privilege, Legal Hold)

| Status | Proposed |
| --- | --- |
| Decision Drivers | Regulatory Compliance, Privacy, Risk Management |
| Source Artefacts | C4_Level1_System_Context_with_L2_Diagram_iNB.docx; iBank_RequirementAnalysisClarafication_v2.docx; Architectural_Guardrails_iNB.docx; iNB_Deployment_Model_UML_OnPrem_VM_Oracle_ActivePassive_v3_NetworkSegmentation_FWZones.docx |
| Governance | Compliance & Legal sign-off; periodic access reviews; audit readiness checks. |

### Context

Data residency is restricted to India and the platform must implement privacy-by-design controls for PII/KYC data and legal hold readiness.

### Decision

Host and store all regulated customer data in India-based on-prem data centers. Apply privacy controls: data minimization, masking/tokenization for logs and non-prod, least privilege access to KYC documents, and full auditing of access. Enable legal hold workflows and retention controls for transactional and unstructured data.

### Consequences

Positive:

*   Aligns with residency requirements and reduces regulatory risk.
*   Reduces sensitive data exposure through masking and least privilege.

Trade-offs / Negative:

*   May constrain service choices and multi-region patterns.
*   Requires governance processes and periodic access reviews.

### Notes

*   Separate duties for access administrators and data viewers.
*   Document retention schedules and automation.

### Open Questions

*   Confirm any additional regulators/standards in scope and their retention rules.

## INB-ADR-010: Performance: Caching Strategy for High-Latency Hotspots

| Status | Proposed |
| --- | --- |
| Decision Drivers | Performance, Scalability |
| Source Artefacts | iBank_RequirementAnalysisClarafication_v2.docx; Architectural_Guardrails_iNB.docx |
| Governance | Performance review; security review for cache data classification; change control for cache policies. |

### Context

Latency concerns are noted with recommendation to introduce caching and call optimizations. iNB has read-heavy views (dashboard, configuration and reference data).

### Decision

Introduce a caching layer for read-heavy, non-sensitive data (e.g., limits/configuration, biller/reference metadata, pre-computed dashboard summaries). Use TTL-based invalidation and explicit cache busting for configuration changes. Prohibit caching of sensitive/PII payloads and enforce encryption-in-transit and access controls for cache endpoints.

### Consequences

Positive:

*   Reduces response time for frequent reads and improves user experience.
*   Decreases load on backend services and database.

Trade-offs / Negative:

*   Risk of stale data if TTL/invalidation is poorly configured.
*   Adds operational component requiring monitoring and capacity planning.

### Notes

*   Cache keys should avoid PII; use surrogate identifiers.
*   Capture cache hit ratio and eviction metrics in AppDynamics.

### Open Questions

*   Select cache technology (in-memory distributed cache vs local cache) aligned to on-prem constraints.
*   Define which endpoints are cacheable and maximum staleness tolerated.

## INB-ADR-011: Deployment: On-Prem VM Pools with Network Segmentation (DMZ/App/Data/Ops) and Firewalls

| Status | Proposed |
| --- | --- |
| Decision Drivers | Compliance (Residency), Security, Operability |
| Source Artefacts | iNB_Deployment_Model_UML_OnPrem_VM_Oracle_ActivePassive_v3_NetworkSegmentation_FWZones.docx; Architectural_Guardrails_iNB.docx |
| Governance | Infra/Security review; firewall policy change control; privileged access reviews for Ops zone. |

### Context

Deployment architecture confirms on-prem hosting in India with VM pools, Oracle, active–passive DR, and security zones with firewalls.

### Decision

Deploy iNB on-prem in India DCs using VM pools for horizontal scaling at web/API/service tiers. Enforce layered network segmentation: Internet/Public, DMZ/Edge (WAF, reverse proxy, external LB), Application Zone, Data Zone (Oracle + object storage + backups), and Operations/Management Zone. Implement perimeter, internal segmentation, and egress firewalls with strict allow-lists. Prohibit DMZ-to-Data direct access.

### Consequences

Positive:

*   Aligns with residency and enables strong network security controls.
*   Clear trust boundaries support zero-trust and auditability.

Trade-offs / Negative:

*   Less elastic than cloud; scaling requires capacity planning.
*   Network policy management adds operational complexity.

### Notes

*   Use bastion/jump hosts with strong MFA and audited sessions for admin access.
*   Maintain egress allow-lists for all external systems.

### Open Questions

*   Confirm firewall products and whether TLS inspection is permitted.
*   Confirm staff access method (VPN/ZTNA/intranet) for Ops/Admin portals.

## INB-ADR-012: API Entry: DMZ WAF + Reverse Proxy/Load Balancer as the API Gateway Pattern

| Status | Proposed |
| --- | --- |
| Decision Drivers | Security, Rate Limiting, Observability |
| Source Artefacts | iNB_Deployment_Model_UML_OnPrem_VM_Oracle_ActivePassive_v3_NetworkSegmentation_FWZones.docx; Architectural_Guardrails_iNB.docx; C4_Level1_System_Context_with_L2_Diagram_iNB.docx |
| Governance | Security architecture review; WAF rule governance; periodic penetration tests for edge exposure. |

### Context

iNB requires an edge layer in the DMZ to enforce security controls, routing, and traffic management while keeping application services isolated in the Application Zone.

### Decision

Adopt the DMZ edge pattern: WAF + reverse proxy + external load balancer as the API entry point for both Web UI and API calls. Centralize TLS policy (termination/re-encryption as permitted), request validation, routing, rate limiting, correlation ID injection, and security headers. Keep business services in the Application Zone and restrict inbound flows to HTTPS 443 from DMZ to Application Zone only.

### Consequences

Positive:

*   Consistent policy enforcement and reduced attack surface.
*   Simplifies throttling and protection of expensive endpoints.

Trade-offs / Negative:

*   Edge components become critical dependencies; require HA.
*   Requires coordination with network/security for rule tuning.

### Notes

*   Maintain allow-listing for egress to external providers via egress firewall.
*   Ensure correlation IDs and client IP forwarding are handled securely.

### Open Questions

*   Confirm TLS termination location and certificate management model at edge.
*   Confirm whether a dedicated API management product is required beyond WAF/proxy.