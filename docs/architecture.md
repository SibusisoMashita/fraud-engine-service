# 🚀 Fraud Engine Service Architecture

## 1. 🌐 Overview
The **Fraud Engine Service** is a high‑performance **Spring Boot 3.3 / Java 21** microservice designed to evaluate financial transactions in real time.  
It runs a configurable rule engine, computes fraud severity scores, persists full decision trails, and exposes clean REST APIs with OpenAPI docs.  
Schema management is fully automated using **Flyway**, and the service ships containerized with Docker.

## 2. 🧩 Core Domain Concepts
- **Transaction** — immutable record representing a financial event.
- **FraudDecision** — aggregated result of all rule outcomes.
- **RuleResult** — pass/fail details for each evaluated rule.
- **Merchant** — merchant registry + blacklist flags.
- **RuleConfig** — dynamic runtime configuration per rule.
- **RuleContext** — evaluation metadata (start time, shared caches, extensions).

## 3. 🏛️ Layered Architecture & Packages
- **API Layer** → REST controllers & request validation.
- **DTO Layer** → Boundary models decoupled from persistence.
- **Mapper Layer** → Clean transformations between DTOs and entities.
- **Service Layer** → Business workflows (transaction processing, rule evaluation, decision computation).
- **Rules Engine Layer** → Pluggable rule strategies orchestrated by `RulePipeline`.
- **Persistence Layer** → Entities + repositories powered by Spring Data JPA.
- **Configuration Layer** → YAML‑driven rule toggles, OpenAPI setup, external settings.
- **Exception Layer** → Centralized error handling with consistent responses.

Cross‑cutting: structured logging, validation, metrics, observability.

## 4. 🧠 Rule Evaluation Pipeline
1. Transaction ingested via API.
2. Converted to entity & persisted.
3. RuleEvaluationService invokes `RulePipeline`.
4. Pipeline executes all enabled rules sequentially.
5. Each rule returns a `RuleResult`.
6. Results persisted for auditing.
7. FraudDecision aggregated from failed rules (severity scoring).
8. Decision persisted and returned.

✨ Extensible by simply adding new classes implementing `FraudRule`.

## 5. 🔄 Data Flow (End‑to‑End)
1. Client sends transaction → `/transactions`.
2. Validation + mapping.
3. Service persists transaction.
4. Rule pipeline executes.
5. RuleResult entries saved.
6. FraudDecision computed + stored.
7. Response returned with severity + isFraud flag.
8. Operational logs & metrics captured throughout.

## 6. 🧰 Technology Stack
- Java 21
- Spring Boot 3.3.4
- PostgreSQL + Flyway
- Spring Data JPA
- Springdoc OpenAPI
- Testcontainers + JUnit 5
- Jacoco coverage
- Docker + multi‑stage builds

## 7. ⚙️ Config Profiles
- `application.yml` → selects active profile.
- `application-local.yml` → local Postgres + Flyway.
- `application-docker.yml` → container runtime settings.
- Rule enablement via YAML (`fraud.enabled-rules`).

## 8. 🛡️ Error Handling
Centralized controller advice catches and formats:
- Validation failures
- Missing entities
- Illegal states (duplicates, conflicts)
- Generic internal errors

Consistent JSON shape across endpoints.

## 9. 📘 API Documentation (Swagger / OpenAPI)
- Automatic via springdoc.
- Clean UI available at `/swagger-ui`.
- Supports request/response models, validation errors, examples.

## 10. 🔐 Security & Validation
- DTO validation on all incoming requests.
- No auth yet — future support for OAuth2/JWT.
- Designed for safe DB operations (parameter binding).

## 11. 🗂️ Persistence Model Summary
Tables:
- `transaction`
- `fraud_decision`
- `rule_result`
- `merchant`
- `rule_config`

Indexes suggested for performance and rule‑heavy queries.

## 12. 🧪 Testing Strategy
- **Unit tests** → rules, services, mappers.
- **Integration tests** → full pipeline using Testcontainers PostgreSQL.
- **Controller tests** → validation, JSON contracts.
- **Coverage** via Jacoco with planned quality gate increases.

## 13. 📦 Deployment Model
- Multi‑stage Dockerfile produces slim runtime image.
- docker‑compose orchestrates service + Postgres.
- Ready for Kubernetes (health probes, metrics).

## 14. ➕ Extending the System
Adding a rule:
1. Create rule class.
2. Add identifier enum.
3. Add YAML enable flag.
4. Add tests.
5. Optionally add RuleConfig entries.

## 15. 📊 Observability & Metrics
- Actuator: health, metrics, info.
- Recommended additions:
    - rule_evaluations_total
    - rule_failures_total
    - pipeline_latency_ms
    - severity_distribution

## 16. ⚡ Performance & Scalability
- Sequential rule pipeline (possible future parallelism).
- VelocityRule can become DB-heavy → indexing matters.
- Designed for future Kafka ingestion.

## 17. 💥 Reliability
- Duplicate transaction protection.
- Transactional boundaries guarantee atomic persistence.
- Merchant lookup safety.

## 18. 🔐 Security Hardening (Future)
- Authentication + RBAC.
- API rate-limiting.
- Config admin endpoints locked behind roles.

## 19. 🚧 Roadmap
Short-term:
- Coverage thresholds.
- Metrics expansion.
- Config caching.

Mid-term:
- Dynamic rule toggling.
- Async ingestion (Kafka).
- Rule chaining & compound rules.

Long-term:
- Multi-tenancy.
- External rule DSL.
- Real-time dashboards.

## 20. 📚 Glossary
- **Severity** → numeric risk score.
- **Pass/Fail** → rule’s boolean outcome.
- **Enabled rules** → YAML-defined active rule list.
- **Rule pipeline** → orchestrator of rule execution.

---

Enjoy building, extending, and scaling the Fraud Engine! 🚀  
