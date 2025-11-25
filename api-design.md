# API Design - Fraud Engine Service

## 1. Overview
The Fraud Engine REST API provides endpoints to submit transactions for fraud evaluation, retrieve fraud decisions and underlying rule results, manage merchant blacklist state, and perform health checks. All endpoints are currently versioned under `/api/v1/*`. Responses are JSON; request validation relies on Bean Validation.

## 2. Design Principles
- Clarity: Resource-oriented URIs and consistent HTTP verbs.
- Determinism: Transaction evaluation is synchronous; response contains final decision.
- Explainability: Decisions expose underlying rule evaluations when requested.
- Validation First: Reject malformed requests early with clear error payloads.
- Extensibility: Versioned base path (`/api/v1`) to enable non-breaking evolution.
- Minimal Coupling: DTOs decouple external contracts from internal JPA entities.

## 3. Versioning Strategy
- Current: URI-based versioning (`/api/v1`).
- Future: Support parallel `/api/v2` when introducing breaking changes; maintain old version for deprecation window.
- Consider media-type versioning (e.g., `application/vnd.fraudengine.v2+json`) for selective evolution if payload divergence increases.
- Backward compatibility: Add new optional fields; avoid renaming or removing existing fields in minor updates.

## 4. Resource Model
Primary resources:
- Transaction: Submitted for evaluation (not retrievable directly by dedicated GET yet; evaluation immediate on POST).
- FraudDecision: Aggregated fraud result per transaction (includes severity & isFraud flag).
- RuleResult: Per-rule evaluation details (exposed embedded in FraudDecision response).
- Merchant: Merchant entries with blacklist state.
- Health: Simple service status indicator.

## 5. Endpoint Catalog
Transactions:
- POST `/api/v1/transactions` → Evaluate a transaction and return `TransactionResponse` (decision summary).
Fraud Decisions:
- GET `/api/v1/fraud/{transactionId}` → Retrieve full decision + rule results.
- GET `/api/v1/fraud/flagged?severity=&fromDate=&toDate=` → List flagged decisions (filters optional).
Merchants:
- GET `/api/v1/merchants` → List all merchants.
- POST `/api/v1/merchants?merchantName=` → Create merchant.
- POST `/api/v1/merchants/{merchantName}/blacklist` → Blacklist merchant.
- DELETE `/api/v1/merchants/{merchantName}/blacklist` → Remove blacklist.
- GET `/api/v1/merchants/{merchantName}/blacklist` → Check blacklist status (true/false).
Health:
- GET `/api/v1/health` → Returns "OK" string body.

## 6. Request/Response Schemas
TransactionRequest:
- transactionId (string, required, non-blank)
- customerId (string, required, non-blank)
- amount (decimal >= 0.01, required)
- timestamp (ISO-8601 date-time, required)
- merchant (string, required, non-blank)
- location (string, required, non-blank)
- channel (enum Channel, required) Allowed: CARD, EFT, ATM, POS, ONLINE, MOBILE, USSD, BRANCH, DEBIT_ORDER, QR_CODE, TAP_TO_PAY, WALLET, UNKNOWN.
TransactionResponse:
- transactionId (string)
- isFraud (boolean)
- severity (integer >= 0)
- evaluatedAt (ISO-8601 date-time)
FraudDecisionResponse:
- transactionId (string)
- isFraud (boolean)
- severity (integer)
- evaluatedAt (ISO-8601 date-time)
- rules (array of RuleResultResponse)
RuleResultResponse:
- ruleName (string)
- passed (boolean)
- reason (string)
- score (integer | null)
Merchant (in responses):
- id (long)
- merchantName (string)
- blacklisted (boolean) (exposed as isBlacklisted internally)
- createdAt, updatedAt (timestamps) — may appear depending on serialization settings.
Error Response (standardized):
- timestamp
- status
- error
- message
- details (optional map: fields, violation, etc.)

## 7. Validation Rules
- @NotBlank fields reject empty or whitespace-only strings (transactionId, customerId, merchant, location).
- amount must be >= 0.01.
- channel must be provided; unrecognized values mapped to UNKNOWN (still passes validation due to enum conversion strategy; consider stricter rejection in future).
- timestamp required; rely on Jackson for ISO parsing; invalid format yields 400.
- Duplicate transactionId triggers 409 (IllegalStateException in service layer).

## 8. Error Handling & Status Codes
Status codes:
- 200: Successful operation.
- 400: Validation errors (field-level), constraint violations.
- 404: Not found (transaction decision request for missing transaction, unknown merchant check, etc.).
- 409: Conflict (duplicate transaction submission, merchant already exists).
- 500: Unexpected internal error.
Sample validation error payload:
{
  "timestamp": "2025-11-25T10:15:30.123",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "details": {
    "fields": {
      "amount": "must be greater than or equal to 0.01",
      "transactionId": "must not be blank"
    }
  }
}

## 9. Filtering, Sorting, Pagination
Current:
- Fraud flagged listing filters by severity threshold, date range (fromDate, toDate) in-memory.
No sorting/pagination implemented; risk of large payloads for big datasets.
Recommendations:
- Add pagination params: page, size, sort (default sort by evaluatedAt desc).
- Server-side filtering at repository level to improve performance & reduce memory footprint.
- Consider cursor-based pagination for near-real-time streams.

## 10. Idempotency & Consistency
- Transaction POST is idempotent only if client refrains from re-sending identical transactionId after success; current behavior treats re-submit as conflict (409).
- Consider explicit Idempotency-Key header support for future multi-step ingestion.
- FraudDecision unique per transaction ensures consistent retrieval state.

## 11. Performance & Limits
- Synchronous evaluation may increase latency as rules scale; typical rule count small presently.
- Recommend soft limit on request payload size (<10KB) — currently no explicit enforcement.
- Add metrics for average evaluation duration; define SLO (e.g., p95 < 250ms).
- Introduce rate limiting or asynchronous ingestion when throughput > defined threshold.

## 12. Authentication & Authorization
Current: None (open API). Risks include unauthorized blacklist modifications.
Future Plan:
- Add JWT / OAuth2 (client credentials or user-based) via Spring Security.
- Role separation: ADMIN (merchant + config), ANALYST (decision read), INTEGRATION (transaction submit).
- Use scopes or RBAC claims for endpoint grouping.

## 13. Rate Limiting Strategy (Future)
- Apply per API key or client principal: fixed window or token bucket (e.g., 100 TPS for transaction submission).
- Provide 429 Too Many Requests with Retry-After header.
- Use gateway (NGINX / API Gateway) or Spring filter-level enforcement.

## 14. Observability (Logging & Tracing)
- All transaction-related logs prefixed with [tx=<transactionId>] for correlation.
- Rule evaluation events structured for ruleName, performance metrics, outcomes.
- Recommend adding trace IDs (OpenTelemetry) and exposing them in responses via header (e.g., X-Trace-Id).
- Add access logs with method, path, latency, status code at INFO level for audit.

## 15. OpenAPI Integration & Documentation Guidelines
- Springdoc auto-generates OpenAPI spec; accessible via /swagger-ui.
- Guidelines:
  - Annotate controllers and DTOs with description & example values.
  - Use @Schema for enum descriptions; list Channel values.
  - Add tags: Transactions, Decisions, Merchants, Health.
  - Provide error response schema components.
- Version spec file (openapi-v1.yaml) for contract tracking.

## 16. Change Management & Backward Compatibility
- Semantic changes require bumping base URI version (v1 → v2).
- Additive changes (new optional fields/endpoints) safe within v1.
- Deprecation Process:
  - Mark endpoints deprecated in OpenAPI with @Deprecated annotations & description.
  - Provide sunset timeline in documentation.
- Maintain migration notes for consumers (mapping old fields to new semantics).

## 17. Examples (curl)
Submit transaction:
```powershell
curl -X POST http://localhost:8080/api/v1/transactions ^
  -H "Content-Type: application/json" ^
  -d '{
    "transactionId": "TX123456", "customerId": "C987", "amount": 125.50,
    "timestamp": "2025-11-25T10:30:00", "merchant": "ACME-STORE",
    "location": "NYC", "channel": "ONLINE"
  }'
```
Get decision:
```powershell
curl http://localhost:8080/api/v1/fraud/TX123456
```
List flagged decisions with filters:
```powershell
curl "http://localhost:8080/api/v1/fraud/flagged?severity=3&fromDate=2025-11-01T00:00:00&toDate=2025-11-30T23:59:59"
```
Create merchant:
```powershell
curl -X POST "http://localhost:8080/api/v1/merchants?merchantName=ACME-STORE"
```
Blacklist merchant:
```powershell
curl -X POST http://localhost:8080/api/v1/merchants/ACME-STORE/blacklist
```
Check blacklist:
```powershell
curl http://localhost:8080/api/v1/merchants/ACME-STORE/blacklist
```
Health:
```powershell
curl http://localhost:8080/api/v1/health
```

## 18. Future Improvements
- Implement pagination & sorting for flagged decision listing.
- Add endpoint to retrieve a transaction by ID and rule history summary.
- Provide bulk transaction ingestion endpoint (batch) or async queue submission.
- Introduce decision export endpoint (CSV/NDJSON) for analytics.
- Add administrative endpoints for rule configuration inspection & modification.
- Harden validation: reject UNKNOWN channel unless explicitly allowed.
- Implement correlation & trace ID propagation in responses.
- Introduce error codes (machine-readable) alongside human messages.

---
This document should be updated as new versions, authentication, pagination, and configuration management endpoints are introduced.

