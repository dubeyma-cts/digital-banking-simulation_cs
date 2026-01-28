# Architecture Decision Record (ADR) – iNB Online Banking Application

## 1\. Decision Summary

This ADR documents the decision to adopt a Domain‑Driven Modular Monolith built on a Layered (N‑Tier) structure with SOA interaction patterns, ACID guarantees, and selective event‑driven extensions.

## 2\. Status

Approved

## 3\. Context

iNB requires strong consistency, auditability, on‑prem deployment, and regulatory compliance.

## 4\. Decision Drivers

Regulatory compliance

ACID correctness

Auditability

Operational stability

Domain clarity

Avoid distributed complexity

## 5\. Considered Options

Microservices

Classic SOA

N‑Tier Monolith

Domain‑Driven Modular Monolith (Chosen)

Event‑Driven / Serverless

## 6\. Decision Outcome

Chosen architecture: Domain‑Driven Modular Monolith + Layered Architecture + SOA boundaries + ACID + selective events.

## 7\. Rationale

Ensures correctness

Simplifies audits

Avoids microservice overhead

Future extensible modules

## 8\. Pros & Cons

Pros: predictable, compliant, modular

Cons: single deployable, requires discipline

## 9\. Consequences

Positive: simpler ops, safer

Negative: less granular scaling

## 10\. Alternatives

Microservices rejected

ESB rejected

N‑Tier only rejected

Events partial

## 11\. Compliance Considerations

Supports RBI, SOX, Legal Hold, security controls

## 12\. Future Considerations

Modules can later be extracted

Event use can expand

## 13\. Final Recommendation

Architecture aligns with all NFRs and regulatory constraints.