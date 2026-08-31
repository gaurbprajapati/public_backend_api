# Sonar Fix & Coverage Report

**Repo:** contract-staffing-timesheet-microservice
**PR:** [#1051](https://github.com/Workforce-Cloud-Tech/contract-staffing-timesheet-microservice/pull/1051) — `enhancement-bnp-11917-rule-engine-changes` → `dev`
**Project key:** `Workforce-Cloud-Tech_contract-staffing-timesheet-microservice`
**Date:** 2026-07-20

---

## Summary

| Metric | Baseline | Post-fix | Result |
|---|---|---|---|
| Open issues | 1 | 0 | ✅ Fixed |
| New-code coverage | 97.6% | 97.6% | ✅ PASS (≥95% target) |
| Quality gate | ERROR | **OK** | ✅ |

**Threshold result: TARGET (≥95%)** — coverage was already at target before this run; only the one open issue blocked the quality gate.

---

## Issues Fixed

| # | Rule | Severity | File:Line | Message | Disposition |
|---|------|----------|-----------|---------|--------------|
| 1 | `java:S125` | MAJOR | `src/test/java/io/recruitcrm/microservice/timesheet/services/rule_engine/RuleEngineServiceTests.java:2046` | "This block of commented-out lines of code should be removed." | **fixed** — false positive triggered by a prose comment containing `hours * rate`, which Sonar's heuristic read as a commented-out multiplication expression. Reworded to prose (`hours multiplied by rate, computed in floats`) with no behavioral change. |

**Fix commit:** `426ebe1c3` — `fix: reword comment to avoid S125 commented-out-code false positive`

---

## Coverage

New-code coverage was already 97.6% at baseline (min 90%, target 95%) — no file was below either threshold, so no additional tests were added:

| File | New coverage | New uncovered lines |
|---|---|---|
| `RuleEngineService.java` | 97.4% | 3 |
| `RuleEvaluationResult.java` | 100% | 0 |
| `BaseRule.java` | 100% | 0 |

The 3 uncovered lines in `RuleEngineService.java` are informational only — both the file (97.4%) and the overall PR (97.6%) already clear the 95% target, so per the workflow's own threshold rule (act only when `new_coverage < 90%`), no test additions were made.

---

## Verification

- `mvn compile` — pass
- `mvn checkstyle:check` — pass
- `mvn spring-javaformat:apply` — clean (no reformatting needed beyond the fix)
- Full test suite: 6788 tests, 6 failures / 3 errors — **all 9 confirmed pre-existing and unrelated** (same classes/counts as the pre-existing baseline established during implementation: `AccessLevelHandlerTests`, `JobContractorControllerTests`, `ControllerLoggingAspectTests`, `ReimbursementExportServiceTests`, `MultiFactorAuthenticationJwtInterceptorTests`, `JooqORMConfigurationTests`). None touch rule-engine code.
- Rule-engine test classes specifically (`RuleEngineServiceTests`, `RuleEvaluationResultTests`, and the affected rule test classes): all pass.

## CI Notes

- Jenkins job `continuous-integration/jenkins/pr-head` reported `error` ("Something is wrong with the build of this commit") on the pushed commit. Cross-checked directly against SonarCloud: the PR analysis stage completed successfully — quality gate **OK**, 0 issues, analyzed at `2026-07-20T09:39:38Z`. The Jenkinsfile pipeline runs several stages *after* the Sonar scan (AWS environment provisioning via `katia-fractional`, a `Test_the_REST` API test job, de-provisioning) that are unrelated to source code or Sonar; the generic error is most likely from one of those later infra stages. No Jenkins log access was available to confirm further — flag to the team if the PR check remains red.

## Sonar re-scan only — not in open baseline

None.

## Sonar open only — fixed on branch (stale until merge)

None — the `dev` target project has no separate baseline scan for this issue; it was PR-scoped and resolved.

## Files still below 90% new coverage

None.

---

## Not Started From PR (N/A)

Started from PR link — existing PR #1051 was updated directly; no new PR opened.
