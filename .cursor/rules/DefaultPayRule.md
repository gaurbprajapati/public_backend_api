# Default Pay Rule (Range-Based + Duration-Based) - Complete End-to-End Flow

## Overview

**Locations:**
- `src/main/java/io/recruitcrm/microservice/timesheet/rule_engine/rules/shifts/DefaultPayRule.java` (range-based)
- `src/main/java/io/recruitcrm/microservice/timesheet/rule_engine/rules/hourly/DefaultPayRule.java` (duration-based)
- Wiring: `BaseRuleEvaluator.evaluateDefaultPayRules` (week-end sweep, called inside `evaluateWeeklyTimeLogs` after `evaluateWeeklyOvertimeRules`)
- Resolvers (registered for completeness, **never invoked**): `RangeBasedDefaultPayRuleTimeRangeResolver`, `DurationBasedDefaultPayRuleTimeRangeResolver`

**Purpose:** Pay any worked time that no other rule (Regular Hours, shift rules, specific time/hour, Daily OT, Weekly OT) claimed, at the timesheet's base pay/bill rate × 1.0. Closes the historic gap where unallocated minutes silently fell through and the worker wasn't paid for them.

**Gating:** Only runs when `TimesheetSetting.isUnplannedHoursPayEnabled == 1`. With the flag null/0, behavior is identical to pre-feature (no entries added, no totals affected).

**Virtual/system rule:** Like Regular Hours and Break, Default Pay is system-injected — not a user-configured `CustomRule`. `BaseRule.isVirtualRule()` returns true for both DEFAULT_PAY rule types.

---

## Why This Exists

Before this feature, when:
- A user worked time that no daily rule claimed (e.g., 30 min between Regular Hours ending and the next interval starting)
- AND that time wasn't picked up by Weekly Overtime (either no WOT rule configured, or weekly threshold not crossed, or WOT only consumed part of it)

…the unallocated portion appeared in the response under `unallocatedTimeRanges` and earned **zero pay/bill**. Default Pay closes that gap.

---

## Execution Order Within a Week

```
Daily loop (per time log):
  Break → After/Before Shift → Specific Time/Hour → Regular Hours → Daily OT
  (Weekly OT collected as candidates only)
↓
Week-end:
  evaluateWeeklyOvertimeRules()  ← claims hours above weekly threshold
↓
Week-end:
  evaluateDefaultPayRules()      ← claims everything still unallocated
```

`RulePrecedenceConfig` lists DEFAULT_PAY last in both range-based and duration-based precedence lists for symmetry, but the rule is dispatched directly by `BaseRuleEvaluator.evaluateDefaultPayRules` — it never goes through `UnifiedRuleManager` or the daily rule loop. The registered resolvers exist only because `RuleFactory.createTimeRangeResolver` requires an entry per `RuleType`; they return an empty `TreeRangeSet` and are never called in practice.

---

## Algorithm: Chronological Backfill

The business intent: "Weekly OT is earned by the hours that pushed you over the threshold, which chronologically are the **latest** ones. Default Pay covers everything that came before — at base rate."

### Per-day, per-week math

```
For each WeeklyResult (each week of the timesheet, independently):

  1. Group time logs by calendar date.
  2. For each date, compute free ranges =
       union(work intervals on the date) − union(timeRange on all rule
       results attached to that date's time logs)
     This is the same shape as the mapper's per-interval unallocated, but
     aggregated across all intervals belonging to the same calendar date.
  3. Read WOT consumed duration from
       result.weeklyOvertimeRuleEvaluationResult.weeklyOvertimeHours
     (treated as 0 if either is null — covers MONTHLY frequency where
     WOT is suppressed by isWeeklyOvertimeRule, and weeks where the
     threshold was never crossed).
  4. Walk dates LATEST → EARLIEST:
       consumed = min(date free duration, WOT remaining)
       date Default Pay duration = date free − consumed
       WOT remaining -= consumed
  5. For each date with Default Pay duration > 0:
       Carve the EARLIEST portion of that date's free ranges totaling the
       Default Pay duration (takeFromStart). The remaining (latest) ranges
       belong to WOT — within a day, WOT takes the tail and Default Pay
       takes the head.
       Build a RuleEvaluationContext with:
         currentTimeLogBeingEvaluated = first time log of the date
         timeRangesToEvaluate = carved ranges
         currentRuleBeingEvaluated = null (system rule)
         currentRuleIndex = -1
       Invoke ruleFactory.createRule(getDefaultPayRuleType(), logger).evaluate(ctx).
       Attach the RuleEvaluationResult to that first time log.
```

The mapper's `aggregateByRuleType` collapses per-interval results back to one per-date entry in the response, so attaching to the first interval is purely for engine dispatch.

### Worked example (uneven days, partial WOT)

| Day | Worked | Daily rules claim | Free (unallocated) |
|-----|--------|-------------------|---------------------|
| Mon | 12 h | 8 h Regular | **4 h** |
| Tue | 8 h | 8 h Regular | 0 |
| Wed | 8 h | 8 h Regular | 0 |
| Thu | 10 h | 8 h Regular | **2 h** |
| Fri | 8 h | 8 h Regular | 0 |

Total worked = 46 h; Weekly threshold = 45 h → WOT claims **1 h**.
Total free = 6 h; WOT takes 1 h → Default Pay covers **5 h**.

Walking latest → earliest:
- Thu: free 2 h, WOT takes `min(2, 1) = 1`. Thu Default Pay = **1 h** (head 15:00-16:00 if Thu is 09-17 and last hour is 16-17; remaining `wotRemaining = 0`).
- Wed: free 0, skip.
- Tue: free 0, skip.
- Mon: free 4 h, WOT takes `min(4, 0) = 0`. Mon Default Pay = **4 h** (the entire 4-hour gap on Monday).

**Response entries:** Mon gets a `RANGE_BASED_DEFAULT_PAY` entry (4 h, $80 pay, $120 bill at $20/$30 base rates). Thu gets one (1 h, $20/$30). Tue/Wed/Fri get none — they had nothing to pay for.

---

## Rate Calculation

Both `shifts.DefaultPayRule.evaluate` and `hourly.DefaultPayRule.evaluate` do:

```java
RangeSet<LocalTime> timeRange = ctx.getTimeRangesToEvaluate();
Duration duration = TimeHelper.convertRangeSetToDuration(timeRange);

float basePayRate = ctx.getTimesheetSetting().getPayRate();
float baseBillRate = ctx.getTimesheetSetting().getBillRate();

BigDecimal payableAmount = TimeHelper.calculatePayAmount(
    duration, ChargeMethodType.MULTIPLIER, basePayRate, 1.0f, basePayRate);
BigDecimal billableAmount = TimeHelper.calculateBillAmount(
    duration, ChargeMethodType.MULTIPLIER, baseBillRate, 1.0f, baseBillRate);

return createCompleteResult(ctx, timeRange, payableAmount, billableAmount);
```

`createCompleteResult` from `BaseRule` populates `evaluationDate` (from `currentTimeLogBeingEvaluated.date`), `evaluatedDuration`, `metadata`, `ruleIndex` (-1 for system rules), `virtualRule = true` (because `isVirtualRule` includes both DEFAULT_PAY types), and `successful = true`.

---

## Response Shape

A Default Pay entry appears in `weeklyResults[].timeLogRuleEvaluations[].ruleEvaluationResults[]`:

```json
{
  "timeRanges": [["09:00:00", "13:00:00"]],
  "ruleType": "RANGE_BASED_DEFAULT_PAY",
  "ruleName": "Range-Based Default Pay Rule",
  "billAmount": 120.00,
  "payAmount": 80.00,
  "evaluatedDurationInSeconds": 14400,
  "evaluatedDurationApproximateHours": 4.0,
  "successful": true,
  "errorMessage": null,
  "evaluationDate": "2026-04-13",
  "ruleIndex": -1,
  "virtualRule": true,
  "metadata": "Rule: Range-Based Default Pay Rule, Date: 2026-04-13, Duration: 4h 0m, TimeRanges: [[09:00..13:00)]"
}
```

`RuleEngineMapper.getRuleTypeOrder` places DEFAULT_PAY at order **8** (range-based) and **18** (duration-based), so it sorts after Weekly OT in the response.

---

## What Default Pay Does NOT Touch

- **`daily_regular_hour` / `total_regular_hour` columns** — `RuleEngineService.extractRegularHoursFromResult` only counts `RANGE_BASED_REGULAR_HOURS` and `DURATION_BASED_REGULAR_HOURS`. Default Pay is intentionally excluded from the regular-hour count to keep the regular-hours metric semantically distinct from "1x sweep pay."
- **`totalBillData` / `totalPayData` on the Timesheet entity** — these ARE updated correctly because they sum `RuleEvaluatorResult.getTotalBillAmount/getTotalPayAmount`, which iterate **all** per-timeLog rule results. Default Pay is just another entry in that list, so no separate persistence wiring was needed.
- **Daily rules and WOT logic** — completely untouched. Default Pay is a pure post-step on whatever the engine produced.

---

## Side-Effect on `unallocatedTimeRanges`

`RuleEngineMapper.calculateUnallocatedTimeRangesForIntervals` computes unallocated as `workInterval − occupied ranges across all per-timeLog rule results`. With Default Pay added to `ruleEvaluationResults`, those ranges become "occupied" → they no longer appear in `unallocatedTimeRanges`.

WOT remains intentionally OUT of per-timeLog results (its `timeRange` is set to `null` in `BaseRuleEvaluator.processRuleEvaluation`), so when Default Pay is on, the only ranges that still appear in `unallocatedTimeRanges` are the ones WOT claimed. Reconciliation: those minutes show up in `weeklyResults[].weeklyOvertimeResult` instead.

To make `unallocatedTimeRanges` go fully to zero whenever Default Pay is on, WOT would need to populate per-day ranges too. That's a follow-up not implemented here.

---

## Edge Cases (and Tests)

All covered in `BaseRuleEvaluatorDefaultPayTests`:

- Flag null / 0 → early return; `ruleFactory.createRule` never called.
- Flag 1 but no unallocated time → no entries created, factory never called.
- Flag 1 with unallocated and no WOT result → entire unallocated becomes Default Pay on each day.
- WOT result with `null` weeklyOvertimeHours → treated as 0.
- WOT consumes part of the latest day → that day's Default Pay = head; tail belongs to WOT.
- WOT consumes the entire latest day → that day has no Default Pay entry; the prior day(s) still get their full amount.
- WOT >= total weekly unallocated → no Default Pay entries; `wotRemaining` clamped at 0.
- Partial-range split (`takeFromStart` carves a sub-range when duration falls inside a single range).
- Multi-interval day → all intervals contribute to the date's free ranges; Default Pay attached to the first interval; mapper aggregates correctly.
- Prior daily-rule results correctly reduce free ranges (e.g., Regular Hours + Break leave only the gaps for Default Pay).
- Empty weekly time logs → no-op.

---

## MONTHLY Frequency

For monthly timesheets, `RangeBasedRuleEvaluator.isWeeklyOvertimeRule` and the duration-based equivalent return false, so WOT is suppressed entirely. Default Pay still runs per `WeeklyResult` (the engine splits monthly timesheets into weekly chunks regardless of frequency) — and because `wotRemaining = 0` for every week, **every** unallocated minute on a monthly timesheet becomes Default Pay when the flag is on. This is intentional but worth flagging to product: enabling Default Pay on a monthly timesheet with non-working-day work will pay for that work at 1x, whereas today those hours silently disappear.

---

## Non-Working-Day Behavior

On a non-working day, `RegularHoursRule` is not applicable (template work-day check fails) and therefore claims nothing. The full day's worked time falls into `weeklyOvertimeCandidateTimeRanges` (and equivalently into the per-date free ranges Default Pay computes). With WOT claiming the latest portion (if applicable) and Default Pay backfilling the rest, non-working-day work always gets paid at 1x when the flag is on — no special handling required.

If product later wants non-working-day work to **only** flow through WOT (never Default Pay), the fix would be to filter non-working-day ranges out of the eligible pool in `evaluateDefaultPayRules`. Not implemented here.

---

## Related Files

- [BaseRuleEvaluator.java](mdc:src/main/java/io/recruitcrm/microservice/timesheet/rule_engine/rules/BaseRuleEvaluator.java) — owns `evaluateDefaultPayRules`, `computeFreeRangesForDate`, `takeFromStart`
- [BaseRuleEvaluator.md](mdc:.cursor/rules/BaseRuleEvaluator.md) — week-end flow diagram
- [RuleType.java](mdc:src/main/java/io/recruitcrm/microservice/timesheet/rule_engine/constants/RuleType.java) — IDs 13, 14
- [RulePrecedenceConfig.java](mdc:src/main/java/io/recruitcrm/microservice/timesheet/rule_engine/constants/RulePrecedenceConfig.java) — last entry in both lists
- [RuleFactory.java](mdc:src/main/java/io/recruitcrm/microservice/timesheet/rule_engine/RuleFactory.java) — switch + resolver map registration
- [BaseRule.java](mdc:src/main/java/io/recruitcrm/microservice/timesheet/rule_engine/rules/BaseRule.java) — `isVirtualRule` includes DEFAULT_PAY
- [RuleEngineMapper.java](mdc:src/main/java/io/recruitcrm/microservice/timesheet/mapper/RuleEngineMapper.java) — `getRuleTypeOrder` slots 8 and 18
- [BaseRuleEvaluatorDefaultPayTests.java](mdc:src/test/java/io/recruitcrm/microservice/timesheet/rule_engine/rules/BaseRuleEvaluatorDefaultPayTests.java) — sweep behavior tests
