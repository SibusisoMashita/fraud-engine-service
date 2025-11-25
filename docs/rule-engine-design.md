# Rule Engine Design

## 1. Purpose & Scope
Defines how transactions are evaluated for fraud using a modular, configurable rule pipeline. Focuses on design of rule execution, configuration, scoring, extensibility, and integration with decision computation. Excludes external systems (e.g., ML models, streaming ingestion) and broader platform orchestration.

## 2. Goals & Non-Goals
Goals:
- Deterministic, transparent evaluation of rules per transaction.
- Simple mechanism to enable/disable rules via configuration.
- Extensible rule interface supporting gradual evolution (context-aware evaluation).
- Persisted audit trail (RuleResult rows) for explainability and analytics.
- Low operational overhead for adding new rules & adjusting thresholds.
Non-Goals (current state):
- Real-time streaming ingestion optimization.
- Machine learning–based anomaly detection.
- Dynamic hot-reload of rules without restart (future enhancement).
- Complex rule dependency graph resolution / rule chaining.

## 3. High-Level Architecture
Components:
- FraudRule (interface): Contract for evaluating a transaction; default context and legacy single-argument method for backward compatibility.
- RulePipeline: Orchestrates sequential evaluation of enabled rule beans; normalizes configured logical names → bean identifiers.
- RuleContext: Shared evaluation context (start timestamp, expandable metadata container).
- FraudRuleProperties: YAML-bound list of enabled rules.
- RuleConfigService & RuleConfig (DB): Key/value configuration per rule (thresholds, windows, baselines).
- RuleResultRepository: Persists evaluation outcomes.
- FraudDecisionService: Aggregates failed rule scores → severity & isFraud flag.
- TransactionService / RuleEvaluationService: Entry points invoking the pipeline.

## 4. Data Model
- RuleResult: (resultId UUID, transactionId, ruleName, passed boolean, reason TEXT, score Integer). Score contributes to severity if rule fails.
- RuleConfig: Composite key (ruleName + configKey) storing parameter values (string-typed).
- RuleName enum: Canonical internal bean names used for mapping logical identifiers.
- Scoring: Each rule assigns an integer score when failing; aggregated severity = sum(score of failed rules). Severity > 0 ⇒ isFraud=true.
- Decision: FraudDecision stores final aggregated state (transactionId uniqueness ensures idempotence per transaction).

## 5. Configuration
Sources:
- YAML (`fraud.enabled-rules`): Activates subset of implemented rules (logical names). Requires restart to take effect.
- DB (`rule_config` table): Runtime adjustable parameters (threshold, windowMinutes, startHour/endHour, etc.). Retrieved per evaluation.
Normalization:
- RulePipeline maps logical enum-style identifiers (HIGH_VALUE) to bean names (HighValueTransactionRule).
Validation:
- Missing config keys should produce clear logs; current design assumes keys exist. Future improvement: default fallback & integrity check.
Precedence:
- Current execution order: insertion order of bean list filtered by enabled set (effectively order of injection). No explicit priority metadata yet.

## 6. Execution Flow (evaluate())
1. `RuleEvaluationService.evaluate(tx)` logs start, timestamps `startTimeMs`.
2. Builds `RuleContext` (start time + optional metadata).
3. Fetches enabled rule names from `FraudRuleProperties` and normalizes names.
4. Filters instantiated rule beans against normalized active set.
5. Sequentially invokes `FraudRule.evaluate(tx, ctx)` (default fallback to single-arg version).
6. Collects `RuleResult` objects; identifies triggered (failed) rules.
7. Persists all rule results in batch (`ruleResultRepository.saveAll`).
8. Logs completion duration, triggered rule list.
9. Delegates to `FraudDecisionService.computeDecision(tx, results)` for severity aggregation and decision persistence.
10. Transaction-level logs finalize status.

## 7. Rule Lifecycle
Stages:
- Authoring: Implement new class implementing `FraudRule`; add to `RuleName` enum (optional but recommended for mapping).
- Registration: Annotate with `@Component` so Spring auto-detects bean.
- Configuration: Insert required parameters into `rule_config` table.
- Activation: Add logical identifier to YAML `fraud.enabled-rules` list.
- Evaluation: Included automatically in pipeline if enabled & bean present.
- Modification: Update DB config values (new threshold) — takes effect next evaluation.
- Deactivation: Remove from YAML list; restart service.
Versioning (future):
- Introduce `rule_version` column and soft-historical records to audit configuration changes over time.
- Immutable rule result records already provide historical evaluation evidence.

## 8. Extensibility Points
- RuleContext Expansion: Add cached aggregates (e.g., customer historical velocity, region risk score) to avoid repeated repository hits.
- Composite Rules: Create meta-rule that depends on multiple underlying RuleResults (future design; requires passing partial results into dependent evaluation or second phase).
- Dynamic Loading: Potential integration with scripting (e.g., DSL / expression engine) loaded from DB; requires sandboxing.
- Prioritization: Add numeric priority to `FraudRule` and sort before evaluation.
- Partial Short-Circuit: Optional optimization to stop evaluating after severity crosses threshold (trade-off with full audit completeness).

## 9. Logging & Observability
Current:
- Structured DEBUG logs per rule with prefixed `[tx=<id>]` and `rule_evaluation` events (fields: amount, threshold, count, locations, hours, etc.).
- INFO-level summary for start, pass-all, triggered list, final decision severity.
Recommended Metrics (Micrometer):
- Counter: `fraud.rule.invocations{rule="name"}`
- Counter: `fraud.rule.failures{rule="name"}`
- Timer: `fraud.rule.duration{rule="name"}`
- Distribution: `fraud.decision.severity` histogram.
- Gauge: `fraud.rules.enabled` current count.
Tracing:
- Introduce correlation / trace IDs for multi-service propagation (OpenTelemetry).

## 10. Performance & Concurrency
- Sequential evaluation ensures deterministic ordering and simple resource usage.
- Current rules mostly I/O bound (DB reads for velocity & previous transaction); potential contention at high throughput.
Scaling Strategies:
- Parallel evaluation of independent rules (executor) with concurrency controls.
- Caching recent transactions / merchant blacklist status.
- Asynchronous ingestion (queue) decoupling request latency from rule evaluation.
- Bulk evaluation: batch API endpoint or streaming consumer.
Complexity Considerations:
- Avoid premature parallelism before measuring latency & CPU profile.

## 11. Error Handling
- Rule-specific recoverable issues (e.g., unknown merchant) handled internally (logged, treated as clean) to reduce pipeline failure risk.
- Unexpected exceptions bubble; evaluation aborts and transaction processing fails (rollback). Could wrap each rule execution to isolate failures and continue with degraded result set.
- Configuration errors (missing keys) currently would generate runtime exceptions (e.g., NumberFormatException); need validation pass at startup.

## 12. Testing Strategy
- Unit Tests: Each rule with pass/fail boundary cases (e.g., threshold exactly equal, time within off-hours boundary, velocity max count).
- Pipeline Tests: Enabled subset evaluation order, verifying all results persisted.
- Decision Tests: Aggregation of scores yielding expected severity & isFraud.
- Integration / End-to-End: Full flow from API request to persisted decision using Testcontainers (PostgreSQL).
- Configuration Tests: RuleConfigService resolves overlapping keys; update operations persist correctly.
- Negative Tests: Duplicate transaction, missing merchant, invalid config detection (future startup validator).

## 13. Security & Integrity
- No authentication currently; rule enabling and config updates should be restricted (future: admin role endpoints).
- Protect rule_config modifications using authenticated API or direct DB access controls.
- Integrity checks: At startup, verify each enabled rule has non-empty required config set & valid numeric ranges.
- Potential for tampering: Add audit table for rule_config changes (actor, timestamp, old/new value).

## 14. Migration & Evolution Path
Near-term Enhancements:
- Startup config validation + metrics exposure.
- Custom Micrometer metrics for rule performance.
Mid-term:
- Priority + short-circuit evaluation.
- Dynamic enable/disable via DB flag or config server (no restart).
- Composite rules referencing prior RuleResults.
Long-term:
- Rule DSL (JSON/YAML/Expression) interpreted at runtime.
- ML score integration appended to severity calculation (weighted blending).
- Multi-tenancy: rule sets & configs scoped by tenantId.

## 15. Example: Adding a New Rule
Goal: Add GeoVelocityRule detecting rapid location changes across large distances.
Steps:
1. Implement `GeoVelocityRule implements FraudRule` using repository distance logic (needs location history + geo lookup service).
2. Add `GEO_VELOCITY("GeoVelocityRule")` to `RuleName`.
3. Insert config rows: (GeoVelocityRule, maxDistanceKm), (GeoVelocityRule, windowMinutes).
4. Add `- GEO_VELOCITY` to `fraud.enabled-rules` YAML.
5. Write tests: normal travel vs excessive distance, edge case exactly threshold.
6. (Optional) Extend `RuleContext` with pre-fetched previous transaction locations.
7. Deploy; verify metrics counters increment and rule appears in logs.

## 16. Risks & Mitigations
- Performance Degradation: Too many DB queries per transaction → Mitigation: caching, query optimization, indexes.
- Config Drift / Invalid Thresholds: Manual DB edits cause mis-evaluation → Mitigation: startup validation, change audit trail.
- Lack of Observability: Hard to tune severity → Mitigation: add metrics & dashboards.
- Rule Explosion Complexity: Too many bespoke rules → Mitigation: introduce DSL and consolidation guidelines.
- Sequential Bottleneck: CPU underutilized at scale → Mitigation: parallel evaluation strategy when latency SLO threatened.
- Security Exposure: Unsecured config updates → Mitigation: restrict admin endpoints behind auth + RBAC.

## 17. Roadmap (Condensed)
Q1:
- Metrics instrumentation, startup config validator.
- Raise Jacoco coverage threshold (>80%).
Q2:
- Dynamic rule toggling (DB flag), priority ordering, partial short-circuit.
Q3:
- Composite rules, risk score enrichment (external service), audit log enhancements.
Q4:
- DSL-based rule definitions, multi-tenancy foundational changes.
Future:
- ML model integration, streaming ingestion (Kafka), advanced anomaly detection.

---
This design document should evolve alongside functional and operational maturity; update when adding major rule categories, changing severity model, or introducing new extensibility mechanisms.

