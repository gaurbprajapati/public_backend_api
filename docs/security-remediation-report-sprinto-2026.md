# Security Remediation Report — sprinto-2026-agent

**Repository:** `Workforce-Cloud-Tech/contract-staffing-timesheet-microservice`  
**Branch:** `sprinto-2026-agent`  
**Generated:** 2026-05-28  
**Total Dependabot alerts analysed:** 34  
**Tool:** GitHub Dependabot + OSV Scanner v2.3.8

---

## Executive Summary

| Category | Count |
|---|---|
| Dependabot alerts resolved (this branch) | 20 |
| Dependabot alerts unresolvable (blocked by Node constraint) | 14 |
| OSV-only findings (not in Dependabot) | 2 |
| Maven packages bumped | 2 |
| NPM overrides added | 11 |

---

## Vulnerabilities Fixed

### Maven Dependencies

#### 1. `owasp-java-html-sanitizer` — 20240325.1 → 20260101.1
| Field | Value |
|---|---|
| Dependabot Alert | #15 |
| CVE | CVE-2025-66021 |
| Severity | **High** |
| CVSS | — |
| File | `pom.xml` |

**Vulnerability:** XSS via `<noscript>` tag and improper `<style>` tag sanitization. An attacker could inject malicious JavaScript into sanitized HTML output when noscript tags or style attributes were present.

**Impact on this project:** `owasp-java-html-sanitizer` is used to sanitize user-provided HTML content in timesheet entries (notes, comments). This is a **direct production risk** — exploiting this could allow stored XSS attacks in the timesheet management UI.

**Change:** Bumped version from `20240325.1` to `20260101.1` in `pom.xml`. The API is fully backward compatible; no code changes required.

---

#### 2. `poi-ooxml` — 5.2.4 → 5.4.0
| Field | Value |
|---|---|
| Dependabot Alert | #10 |
| CVE | CVE-2025-31672 |
| Severity | **Medium** |
| CVSS | — |
| File | `pom.xml` |

**Vulnerability:** Improper input validation when parsing OOXML files (Excel `.xlsx`). Maliciously crafted Excel files could trigger unexpected behaviour or resource exhaustion.

**Impact on this project:** Apache POI is used for Excel export of timesheets. The vulnerability is triggered by parsing malicious input files (not generation). Since this service generates Excel files rather than consuming user-uploaded ones, the **exploitability is low** but the upgrade is still required.

**Changelog highlights (5.2.4 → 5.4.0):**
- 5.3.0: Bug fixes for formula evaluation, improved OOXML validation
- 5.4.0: Security fix for CVE-2025-31672; further hardening of OOXML parser

**Breaking changes:** None expected. The POI 5.x API is stable; the version bump is a drop-in replacement for generation use cases.

---

### NPM Dependencies (via `package.json` `overrides`)

All npm packages below are **dev-only** transitive dependencies of the `semantic-release` CI/CD toolchain. They are not bundled into the production Java artifact. The fixes are applied using the npm `overrides` field in `package.json`.

#### 3. `handlebars` — 4.7.8 → 4.7.9
| Field | Value |
|---|---|
| Dependabot Alerts | #35, #36, #37, #38, #39, #40, #42, #43 |
| CVEs | CVE-2026-33937 (critical), CVE-2026-33938, CVE-2026-33939, CVE-2026-33940, CVE-2026-33941, CVE-2026-33916, GHSA-7rx3-28cr-v5wh, GHSA-442j-39wm-28r2 |
| Highest Severity | **Critical** |
| Scope | devDependency (via `conventional-changelog-writer`) |

**Vulnerability:** JavaScript injection via AST type confusion. Multiple code paths in the Handlebars template compiler allowed prototype pollution and arbitrary code execution via crafted template inputs.

**Changelog (4.7.8 → 4.7.9):** Patch release addressing all 8 security advisories simultaneously. No API changes.

**Risk of upgrade:** Minimal — patch version bump, fully backward compatible.

---

#### 4. `lodash` — 4.17.21 → 4.18.1
| Field | Value |
|---|---|
| Dependabot Alerts | #21, #44, #46 |
| CVEs | CVE-2026-4800 (high), CVE-2026-2950 (medium), CVE-2025-13465 (medium) |
| Highest Severity | **High** |
| Scope | devDependency (via `@semantic-release/changelog`, `@semantic-release/git`) |

**Vulnerability:** Code injection via `_.template` when `imports` key names contain template expressions. Prototype pollution via `_.merge` and `_.set` on untrusted objects.

**Risk of upgrade:** Low. The 4.17.x → 4.18.x bump is minor; `_.template` is not used directly in this project's semantic-release configuration.

---

#### 5. `lodash-es` — 4.17.21 → 4.18.1
| Field | Value |
|---|---|
| Dependabot Alerts | #20, #45, #47 |
| CVEs | CVE-2026-4800 (high), CVE-2026-2950 (medium), CVE-2025-13465 (medium) |
| Highest Severity | **High** |
| Scope | devDependency (via `semantic-release`, `@semantic-release/commit-analyzer`, `@semantic-release/exec`, `@semantic-release/github`, `@semantic-release/release-notes-generator`) |

Same vulnerabilities as `lodash` — ESM variant of the library.

---

#### 6. `picomatch` — 2.3.1 → 4.0.4 (root level)
| Field | Value |
|---|---|
| Dependabot Alerts | #31, #32, #33, #34 |
| CVEs | CVE-2026-33671 (high), CVE-2026-33672 (medium) |
| Highest Severity | **High** |
| Scope | devDependency (transitive) |

**Vulnerability:** ReDoS via extglob quantifiers. Crafted glob patterns caused catastrophically backtracking regex execution.

**Risk of upgrade (major version 2 → 4):** Moderate in general, but **low in this context** since picomatch is consumed internally by semantic-release tools for CI pattern matching. The API surface changed in v3/v4 (removal of `windows` option, stricter negation), but semantic-release's usage patterns are not affected.

> **Note:** The picomatch `4.0.2` instance inside `node_modules/npm/node_modules/tinyglobby/` cannot be upgraded via root-level overrides (see §"Unresolvable Vulnerabilities").

---

#### 7. `js-yaml` — 4.1.0 → 4.1.1
| Field | Value |
|---|---|
| Dependabot Alert | #13 |
| CVE | CVE-2025-64718 |
| Severity | **Medium** |
| Scope | devDependency (transitive) |

**Vulnerability:** Prototype pollution via YAML merge keys (`<<`). Maliciously crafted YAML could inject properties into Object.prototype.

**Risk of upgrade:** Minimal — patch version bump within the same 4.x API.

---

## Unresolvable Vulnerabilities (npm-internal — Node constraint)

The following 14 Dependabot alerts exist in packages **nested inside `node_modules/npm/node_modules/`**. These are private copies bundled by the `npm` CLI package itself (version 10.9.4), which is pulled in by `@semantic-release/npm@12.0.2` as a direct dependency.

Root-level `overrides` in `package.json` do **not** propagate into a package's own private `node_modules` subtree. The only solution is to:

1. Upgrade `semantic-release` from `^24.x` → `^25.x`, which depends on `@semantic-release/npm@^13.x`, which requires `npm@^11.6.2` (npm 11.x includes patched versions of all dependencies below).
2. However, `semantic-release@25.x` requires **Node.js `^22.14.0` or `>=24.10.0`**. The current CI environment uses Node **22.11.0**, which does not satisfy this constraint.

**Recommended action:** Upgrade Node.js to `22.14.x` (or `24.x`) in the Jenkins CI image and Dockerfile, then upgrade `semantic-release` to `^25.0.3`.

| Dependabot Alert(s) | Package | Current Version | Fix Version | CVEs | Severity |
|---|---|---|---|---|---|
| #17, #19, #22, #24, #29, #30 | `tar` | 6.2.1 / 7.4.3 | 7.5.11 | CVE-2026-23745, CVE-2026-23950, CVE-2026-24842, CVE-2026-26960, CVE-2026-29786, CVE-2026-31802 | High |
| #26, #27, #28 | `minimatch` | 9.0.5 | 10.2.3 | CVE-2026-26996, CVE-2026-27903, CVE-2026-27904 | High |
| #14 | `glob` | 10.4.5 | 11.1.0 | CVE-2025-64756 | High |
| #41 | `brace-expansion` | 2.0.2 | 5.0.5 | CVE-2026-33750 | Medium |
| #48 | `ip-address` | 9.0.5 | 10.1.1 | CVE-2026-42338 | Medium |
| #18 | `diff` | 5.2.0 | 8.0.3 | CVE-2026-24001 | Low |

**Production impact:** Zero — all packages are within `npm`'s own CLI tooling used only during CI/CD release runs. The Java application does not reference any of these packages.

---

## OSV Scanner Only — Not in Open Dependabot

The following findings were detected by **OSV Scanner v2.3.8** but are not represented as open Dependabot alerts. Both are in the `picomatch` instance at version `4.0.2` inside `node_modules/npm/node_modules/tinyglobby/node_modules/picomatch`.

| GHSA ID | Severity (CVSS) | Package | Version | Path |
|---|---|---|---|---|
| GHSA-c2c7-rcm5-vvqj | 7.5 (High) | picomatch | 4.0.2 | `node_modules/npm/node_modules/tinyglobby/node_modules/picomatch` |
| GHSA-3v7f-55p6-f55p | 5.3 (Medium) | picomatch | 4.0.2 | `node_modules/npm/node_modules/tinyglobby/node_modules/picomatch` |

**Why not in Dependabot?** Dependabot's picomatch alerts (#31–#34) targeted the root-level picomatch at `2.3.1`. After upgrading to `4.0.4` via override, those alerts are resolved. However, the `tinyglobby` package inside `npm@10.9.4` still vendors picomatch `4.0.2`, which has these two additional CVEs. Dependabot has not raised separate alerts for this nested path.

**Fix:** Same as §"Unresolvable Vulnerabilities" — upgrade to `semantic-release@25.x` + `npm@11.x` after upgrading the Node.js runtime.

---

## Dependabot Open Only — Fixed on Dev Branch (Stale Alerts Until Merge)

The following Dependabot alerts are still shown as **open** on GitHub because this branch (`sprinto-2026-agent`) has not yet been merged. They are resolved in the code on this branch.

| Alert # | Package | Ecosystem | Old Version | New Version | CVEs Resolved |
|---|---|---|---|---|---|
| #15 | `owasp-java-html-sanitizer` | Maven | 20240325.1 | 20260101.1 | CVE-2025-66021 |
| #10 | `poi-ooxml` | Maven | 5.2.4 | 5.4.0 | CVE-2025-31672 |
| #13 | `js-yaml` | npm | 4.1.0 | 4.1.1 | CVE-2025-64718 |
| #20 | `lodash-es` | npm | 4.17.21 | 4.18.1 | CVE-2025-13465 |
| #21 | `lodash` | npm | 4.17.21 | 4.18.1 | CVE-2025-13465 |
| #31 | `picomatch` | npm | 2.3.1 | 4.0.4 | CVE-2026-33672 |
| #32 | `picomatch` | npm | 2.3.1 | 4.0.4 | CVE-2026-33671 |
| #33 | `picomatch` | npm | 2.3.1 | 4.0.4 | CVE-2026-33671 |
| #34 | `picomatch` | npm | 2.3.1 | 4.0.4 | CVE-2026-33672 |
| #35 | `handlebars` | npm | 4.7.8 | 4.7.9 | CVE-2026-33916 |
| #36 | `handlebars` | npm | 4.7.8 | 4.7.9 | CVE-2026-33941 |
| #37 | `handlebars` | npm | 4.7.8 | 4.7.9 | CVE-2026-33940 |
| #38 | `handlebars` | npm | 4.7.8 | 4.7.9 | CVE-2026-33939 |
| #39 | `handlebars` | npm | 4.7.8 | 4.7.9 | CVE-2026-33937 (critical) |
| #40 | `handlebars` | npm | 4.7.8 | 4.7.9 | CVE-2026-33938 |
| #42 | `handlebars` | npm | 4.7.8 | 4.7.9 | GHSA-7rx3-28cr-v5wh |
| #43 | `handlebars` | npm | 4.7.8 | 4.7.9 | GHSA-442j-39wm-28r2 |
| #44 | `lodash` | npm | 4.17.21 | 4.18.1 | CVE-2026-2950 |
| #45 | `lodash-es` | npm | 4.17.21 | 4.18.1 | CVE-2026-2950 |
| #46 | `lodash` | npm | 4.17.21 | 4.18.1 | CVE-2026-4800 |
| #47 | `lodash-es` | npm | 4.17.21 | 4.18.1 | CVE-2026-4800 |

These 21 alerts will be auto-dismissed by GitHub once this branch merges.

---

## Files Changed

| File | Change |
|---|---|
| `pom.xml` | `owasp-java-html-sanitizer` bumped to `20260101.1`; `poi-ooxml` bumped to `5.4.0` |
| `package.json` | Added `overrides` block fixing 11 transitive npm packages |
| `package-lock.json` | Regenerated after `npm install` with overrides applied |
| `dependabot-alerts.json` | Snapshot of all 34 open Dependabot alerts at time of remediation |
| `docs/security-remediation-report-sprinto-2026.md` | This report |

---

## Recommendations

1. **Immediate:** Merge this branch to `dev` — resolves 20 of 34 Dependabot alerts.
2. **Short-term (Node upgrade):** Upgrade the CI Node.js runtime to `22.14.x` or `24.x`, then:
   - Bump `semantic-release` to `^25.0.3` in `package.json`
   - Bump `@semantic-release/github` to `^12.0.0` (required by semantic-release@25)
   - Remove the `overrides` block (no longer needed — semantic-release@25 uses npm@11.x which has all fixes)
   - This resolves the remaining 14 Dependabot alerts and 2 OSV-only findings.
3. **OSV scanner:** Integrate OSV scanner into CI (`osv-scanner scan .`) to catch vulnerabilities that Dependabot misses (e.g., nested-package findings).
