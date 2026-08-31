# BaseRuleEvaluator - Complete End-to-End Flow

## Overview
**Location:** `src/main/java/io/recruitcrm/microservice/timesheet/rule_engine/rules/BaseRuleEvaluator.java`

**Purpose:** Abstract base class that implements the core rule evaluation algorithm using the **Template Method Pattern**. Provides shared functionality for all rule evaluators while allowing subclasses to customize specific behaviors.

**Key Responsibility:** Orchestrates the entire rule evaluation process - from timesheet validation to final pay/bill calculations, with special handling for multi-interval days and weekly overtime processing.

---

## Architecture Pattern: Template Method

### Base Class (BaseRuleEvaluator)
- ✅ **Defines the algorithm skeleton** - Main evaluation flow
- ✅ **Implements common functionality** - Validation, weekly splitting, state management
- ✅ **Provides extension points** - Abstract methods for subclass customization

### Subclasses
- **RangeBasedRuleEvaluator** - For START_AND_END_TIME timesheets (with intervals)
- **DurationBasedRuleEvaluator** - For DURATION timesheets (total hours only)

---

## Real-World Example

**Scenario:** Processing a weekly timesheet for Sarah with multi-interval days:
- **Monday:** 9:00-12:00, 13:00-18:00 (8 hours total)
- **Tuesday:** 9:00-12:00, 13:00-19:00 (9 hours total - 8 Regular + 1 Daily OT)
- **Wednesday:** 9:00-17:00 (8 hours)
- **Thursday:** 8:00-12:00, 13:00-20:00 (11 hours total - 8 Regular + 3 Daily OT)
- **Friday:** 9:00-17:00 (8 hours)
- **Total:** 44 hours (40 Regular + 4 Daily OT + 0 Weekly OT)

**Rules Applied:**
1. **Regular Hours Rule** - Up to 8 hours/day at base rate
2. **Break Rule** - Unpaid break time deduction
3. **Daily Overtime Rule** - Hours beyond 8/day at 1.5x rate
4. **Weekly Overtime Rule** - Hours beyond 40/week at 2.0x rate (deferred)

---

## Complete Function Flow

### Visual Flow Diagram
```
┌─────────────────────────────────────────────────────────────┐
│  1. evaluateRules()                                         │
│     Main entry point - Template method                      │
└────────────┬────────────────────────────────────────────────┘
             │
┌────────────▼─────────────────────────────────────────────────┐
│  2. validateTimesheet()                                     │
│     Ensures timesheet has required data                     │
└────────────┬────────────────────────────────────────────────┘
             │
┌────────────▼─────────────────────────────────────────────────┐
│  3. prepareWeeklyTimeLogs()                                 │
│     Splits time logs into weekly groups                     │
│     Filters out zero-duration logs                          │
└────────────┬────────────────────────────────────────────────┘
             │
             ├─ For each week ──────────────────────────┐
             │                                          │
┌────────────▼──────────────────────────────────────────────────┐
│  4. evaluateWeeklyTimeLogs()                                 │
│     Manages daily state and processes all time logs         │
└────────────┬─────────────────────────────────────────────────┘
             │
             ├─ For each time log ──────────────────────┐
             │                                          │
┌────────────▼──────────────────────────────────────────────────┐
│  5. evaluateTimeLog()                                        │
│     Evaluates all rules for one time log/interval           │
│     Handles multi-interval daily state sharing              │
└────────────┬─────────────────────────────────────────────────┘
             │
             ├─ For each rule ──────────────────────────┐
             │                                          │
┌────────────▼──────────────────────────────────────────────────┐
│  6. resolveTimeRangesForRule()                              │
│     Calculates what time ranges a rule claims               │
│     Creates TimeRangeResolverContext with same-day logs     │
└────────────┬─────────────────────────────────────────────────┘
             │
┌────────────▼──────────────────────────────────────────────────┐
│  7. processRuleEvaluation()                                 │
│     Converts time ranges to pay/bill amounts                │
└────────────┬─────────────────────────────────────────────────┘
             │
┌────────────▼──────────────────────────────────────────────────┐
│  8. evaluateWeeklyOvertimeRules()                           │
│     Processes weekly OT after all daily rules               │
│     Uses collected weekly OT candidate ranges               │
└────────────┬─────────────────────────────────────────────────┘
             │
┌────────────▼──────────────────────────────────────────────────┐
│  8b. evaluateDefaultPayRules()                              │
│     Week-end sweep — pays any worked time still unallocated │
│     after WOT, at base rate × 1.0. Gated on                 │
│     TimesheetSetting.isUnplannedHoursPayEnabled.            │
│     See DefaultPayRule.md for the chronological-backfill    │
│     algorithm (latest days first → WOT, earliest → pay).    │
└────────────┬─────────────────────────────────────────────────┘
             │
┌────────────▼──────────────────────────────────────────────────┐
│  9. postProcessResult()                                     │
│     Finalizes calculations and applies rates                │
└────────────┬─────────────────────────────────────────────────┘
             │
┌────────────▼──────────────────────────────────────────────────┐
│  WeeklyRuleEvaluatorResult                                  │
│     Final output with complete pay/bill breakdown           │
└─────────────────────────────────────────────────────────────┘
```

---

## Function-by-Function Breakdown

### 1. `evaluateRules(Timesheet timesheet)` - Main Template Method
**Lines:** 52-92  
**Role:** Orchestrates the entire evaluation process using Template Method pattern

```java
Input: Timesheet with time logs and settings
↓
Step 1: validateTimesheet() → Ensure required data exists
Step 2: prepareWeeklyTimeLogs() → Split into weekly groups, filter invalid logs
Step 3: For each week:
        a. Create RuleEvaluatorResult
        b. evaluateWeeklyTimeLogs() → Process all time logs with daily state
        c. postProcessResult() → Apply rates and finalize
        d. Add to weekly results
↓
Output: WeeklyRuleEvaluatorResult with complete calculations
```

**Special Handling:**
- **RangeBasedRuleEvaluator Override:** When `workLogType == 2` (START_AND_END_TIME), this method is overridden to fetch time logs from `TimeLogInterval` table instead of using `timesheet.getTimeLogs()`

**Example:**
```java
Input: Timesheet with 5 days, including multi-interval days

After prepareWeeklyTimeLogs():
  Week 1 (Jan 8-14): [7 time log intervals] // Some days have 2 intervals

After evaluateWeeklyTimeLogs():
  Mon: 8h regular (2 intervals: 9-12, 13-18)
  Tue: 8h regular + 1h daily OT (2 intervals: 9-12, 13-19)
  Wed: 8h regular (1 interval: 9-17)
  Thu: 8h regular + 3h daily OT (2 intervals: 8-12, 13-20)
  Fri: 8h regular (1 interval: 9-17)

After postProcessResult():
  Regular: 40h × $25 = $1000 pay, 40h × $40 = $1600 bill
  Daily OT: 4h × $37.50 = $150 pay, 4h × $60 = $240 bill
  Total: $1150 pay, $1840 bill
```

---

### 2. `validateTimesheet(Timesheet timesheet)` - Input Validation
**Lines:** 97-107  
**Role:** Ensures timesheet has all required data before processing

```java
Input: Timesheet object
↓
Check 1: timesheet != null
Check 2: timesheet.getTimesheetSetting() != null
Check 3: timesheet.getTimeLogs() != null && !empty
↓
Output: void (throws IllegalArgumentException if invalid)
```

**Validation Rules:**
- ❌ `null` timesheet → `IllegalArgumentException`
- ❌ Missing timesheet settings → `IllegalArgumentException`
- ❌ No time logs → `IllegalArgumentException`
- ✅ Valid timesheet → Continue processing

---

### 3. `prepareWeeklyTimeLogs()` - Weekly Grouping with Filtering
**Lines:** 112-174 (3 overloaded versions)  
**Role:** Splits time logs into weekly groups and filters invalid entries

```java
Input: List<TimeLog> entities from database
↓
Step 1: Filter out invalid time logs
        - Calculate duration for each time log using TimeHelper.calculateTimeLogDuration()
        - Skip logs with zero duration
        - Log warnings for skipped logs with time log ID
↓
Step 2: Convert entities to DTOs
        - Use RuleEngineTimeLogMapper.INSTANCE.toTimeLog()
        - Maps entity fields to rule engine DTO fields
↓
Step 3: Split into weekly groups
        - Default: Sunday-Saturday weeks
        - Custom: Use timesheet start day setting
        - Call TimeHelper.splitTimeLogsOnWeeklyBasis()
↓
Output: List<List<TimeLog>> - Each inner list is one week
```

**Three Overloaded Versions:**
1. **Basic** (Lines 112-125) - Uses default Sunday-Saturday weeks
2. **Custom Week Start** (Lines 134-147) - Accepts WorkDay parameter (e.g., MONDAY)
3. **From Timesheet Settings** (Lines 158-174) - Uses `timesheet.timesheetStartDay`

**Example:**
```java
Input: 12 time logs (Jan 8-19, 2 weeks), including some zero-duration

After filtering:
  - Skip TimeLog(id=105) - zero duration (9:00-9:00)
  - Skip TimeLog(id=108) - zero duration (invalid times)
  - Keep 10 valid time logs

After weekly splitting:
[
  [ // Week 1: Jan 8-14
    TimeLog(Jan8, 9:00-12:00), TimeLog(Jan8, 13:00-18:00),  // Multi-interval day
    TimeLog(Jan9, 9:00-18:00), TimeLog(Jan10, 9:00-17:00),
    TimeLog(Jan11, 8:00-12:00), TimeLog(Jan11, 13:00-20:00), // Multi-interval day
    TimeLog(Jan12, 9:00-17:00)
  ],
  [ // Week 2: Jan 15-19
    TimeLog(Jan15, 9:00-17:00), TimeLog(Jan16, 9:00-18:00), TimeLog(Jan17, 9:00-17:00)
  ]
]
```

---

### 4. `evaluateWeeklyTimeLogs()` - Weekly Processing with Daily State Management
**Lines:** 181-202  
**Role:** Manages daily state and coordinates rule evaluation for one week

```java
Input: 
  - List<TimeLog> for one week (may include multiple intervals per day)
  - Timesheet settings
  - RuleEvaluatorResult accumulator
↓
Step 1: Create daily state map
        Map<LocalDate, EvaluationState> dailyStates = new LinkedHashMap<>();
        // Key insight: Same date = same EvaluationState instance
↓
Step 2: For each time log/interval:
        a. Get or create EvaluationState for the date
        b. Call evaluateTimeLog() with shared daily state
        c. State accumulates: occupied ranges, worked hours
↓
Step 3: Ensure all time logs in result map
        - Even if no rules applied (for response mapping)
        - Prevents missing intervals in final response
↓
Step 4: Evaluate weekly overtime rules
        - After all daily processing complete
        - Uses collected weekly OT candidate ranges
↓
Output: Updated RuleEvaluatorResult with all rule evaluations
```

**Key Innovation - Daily State Sharing:**
```java
// Monday has 2 intervals: morning (9-12) + afternoon (13-18)
dailyStates = {
  Jan15 → EvaluationState {
    occupiedRanges: [9:00-12:00, 13:00-18:00],  // Both intervals combined
    workedHours: 8 hours,                        // Total for the day
  }
}

// Both intervals on Jan15 share the SAME EvaluationState
// This enables accurate daily overtime calculation:
// - Morning interval: 3 hours worked, no daily OT yet
// - Afternoon interval: 8 hours total worked, can trigger daily OT
```

---

### 5. `evaluateTimeLog()` - Individual Time Log Processing
**Lines:** 260-400 (extensively commented)  
**Role:** Evaluates all rules for one time log/interval while maintaining shared daily state

```java
Input:
  - TimeLog (one interval, e.g., Monday morning 9-12)
  - Weekly context (all time logs for the week)
  - Timesheet settings and rules
  - Shared EvaluationState for the day
↓
Step 1: Initialize weekly OT candidate collection
        List<RangeSet<LocalTime>> weeklyOvertimeCandidateTimeRanges = new ArrayList<>();
↓
Step 2: Create unified rule list
        - Custom rules from timesheet settings
        - System rules (Regular Hours + Break)
        - Ordered by precedence using UnifiedRuleManager
↓
Step 3: For each rule in sequence:
        a. Check if rule applies on this day (workDays configuration)
        b. Handle weekly OT special case (defer to collectWeeklyOvertimeCandidates)
        c. Resolve time ranges for the rule
        d. Handle multi-interval constraint logic for Daily OT
        e. Constrain ranges to work boundaries
        f. Update shared daily state (occupied ranges, worked hours)
        g. Process rule evaluation (calculate pay/bill)
        h. Store result
↓
Step 4: Store weekly OT candidates for later evaluation
↓
Output: Updated result and state with rule evaluations
```

**Rule Processing Order:**
1. **Regular Hours** - Claims base hours (up to 8/day)
2. **Break** - Claims break time ranges
3. **Daily Overtime** - Claims hours beyond daily threshold
4. **Weekly Overtime** - Deferred to `collectWeeklyOvertimeCandidates()`

**Multi-Interval Daily OT Logic:**
```java
// For Daily OT rules with multi-interval days:
io.recruitcrm.microservice.timesheet.rule_engine.dto.TimeLog constrainTarget = 
    (isDailyOvertimeRuleType(currentRule) && isMultiInterval) 
        ? TimeHelper.createMergedTimeLog(timeLog, sameDayLogs)  // Use merged day (9:00-18:00)
        : timeLog;  // Use individual interval (9:00-12:00 or 13:00-18:00)

// This ensures Daily OT sees the full day's context for accurate calculation
```

**State Updates Example:**
```java
// Before processing Monday morning (9:00-12:00)
state.occupiedTimeRanges = []
state.workedHoursTillNow = 0 hours

// After Regular Hours rule
state.occupiedTimeRanges = [9:00-12:00]
state.workedHoursTillNow = 3 hours

// Before processing Monday afternoon (13:00-18:00) - SAME STATE INSTANCE
state.occupiedTimeRanges = [9:00-12:00]      // Previous interval remembered
state.workedHoursTillNow = 3 hours           // Previous hours remembered

// After Regular Hours rule for afternoon
state.occupiedTimeRanges = [9:00-12:00, 13:00-18:00]  // Accumulated
state.workedHoursTillNow = 8 hours                     // Total for day

// Daily OT rule can now see: 8 hours = threshold, no OT yet
```

---

### 6. `resolveTimeRangesForRule()` - Rule-Specific Time Range Calculation
**Lines:** 487-504 (2 overloaded versions)  
**Role:** Delegates to appropriate rule resolver to calculate time ranges

```java
Input:
  - TimeLog being evaluated
  - IEvaluatableRule (Regular Hours, Break, Daily OT, etc.)
  - Rule index in unified list
  - Timesheet context
  - Shared EvaluationState
  - Weekly time log context (for same-day aggregation)
↓
Step 1: Create TimeRangeResolverContext
        - Contains time log, occupied ranges, same-day logs
        - Includes custom rule index calculation for Daily OT resolvers
        - Provides context for rule-specific calculations
↓
Step 2: Get rule resolver from factory
        - ruleFactory.createTimeRangeResolver(ruleType)
        - Returns appropriate resolver (RegularHoursResolver, etc.)
↓
Step 3: Call resolver.resolveTimeRange()
        - Rule-specific logic determines time ranges
        - Uses context to avoid double-counting
↓
Output: RangeSet<LocalTime> - Time ranges this rule claims
```

**Key Enhancement - Same-Day Context:**
```java
// TimeRangeResolverContext now includes:
context.setSameDayTimeLogs(TimeHelper.getSameDayTimeLogs(timeLog, weeklyTimeLog));

// This enables Daily OT resolvers to:
// 1. Check if current interval is last of the day (isLastIntervalOfDay)
// 2. Calculate aggregated daily effective working time
// 3. Apply daily threshold only once per day
```

**Example - Daily Overtime Rule with Multi-Intervals:**
```java
Input: TimeLog(Jan15 afternoon, 13:00-18:00), DailyOvertimeRule(8h threshold)

TimeRangeResolverContext:
  - currentTimeLog: 13:00-18:00
  - sameDayTimeLogs: [9:00-12:00, 13:00-18:00]  // Both intervals for Jan15
  - occupiedRanges: [9:00-12:00] (from morning Regular Hours)

DailyOvertimeResolver.resolveTimeRange():
  - Check: isLastIntervalOfDay(13:00-18:00, sameDayTimeLogs) = true
  - Calculate: aggregated daily effective time = 8 hours
  - Apply threshold: 8 hours = 8h threshold, no OT
  - Result: []

Output: Empty RangeSet (no daily OT for 8-hour day)
```

---

### 7. `processRuleEvaluation()` - Pay/Bill Calculation
**Lines:** 582-616  
**Role:** Converts time ranges to monetary amounts using rates and multipliers

```java
Input:
  - TimeLog context
  - IEvaluatableRule with rates and multipliers
  - RangeSet<LocalTime> claimed by the rule (already constrained)
  - Rule context and settings
↓
Step 1: Validate input
        - Return null if evaluatedRangeSet is null or empty
        - Log debug message for empty ranges
↓
Step 2: Create RuleEvaluationContext
        - Contains time ranges, timesheet settings, rates
        - Provides all data needed for monetary calculations
↓
Step 3: Handle weekly overtime special case
        - Add to weeklyOvertimeCandidateTimeRanges
        - Set weekly context for later evaluation
↓
Step 4: Get rule evaluator from factory
        - ruleFactory.createRule(ruleType, logger)
        - Returns evaluator with calculation logic
↓
Step 5: Call evaluator.evaluate()
        - Calculates duration from time ranges
        - Applies pay/bill rates and multipliers
        - Creates RuleEvaluationResult with amounts
↓
Step 6: Set time range for non-weekly rules
        - Weekly OT rules set timeRange to null
        - Other rules preserve the evaluated range set
↓
Output: RuleEvaluationResult with pay/bill amounts and hours
```

**Example - Daily Overtime Rule:**
```java
Input: 
  - TimeLog(Jan16, 9:00-19:00) - 10 hour day
  - DailyOvertimeRule(threshold=8h, multiplier=1.5)
  - RangeSet: [17:00-19:00] (2 hours beyond 8h threshold)

RuleEvaluationContext:
  - timeRanges: [17:00-19:00]
  - payRate: $25/hour
  - billRate: $40/hour
  - multiplier: 1.5

DailyOvertimeEvaluator.evaluate():
  - Duration: 2 hours
  - Pay: 2h × $25 × 1.5 = $75
  - Bill: 2h × $40 × 1.5 = $120

Output: RuleEvaluationResult {
  payAmount: $75,
  billAmount: $120,
  hours: 2,
  ruleType: DAILY_OVERTIME,
  timeRange: [17:00-19:00]
}
```

---

### 8. `evaluateWeeklyOvertimeRules()` - Weekly Processing
**Lines:** 207-248  
**Role:** Evaluates weekly overtime after all daily processing is complete

```java
Input:
  - All time logs for the week
  - Timesheet settings
  - RuleEvaluatorResult with collected weekly OT candidates
↓
Step 1: Create unified rule list
        - Same as daily processing for consistency
        - Includes custom rules + system rules
↓
Step 2: Get weekly OT candidate ranges
        - Collected during daily processing via collectWeeklyOvertimeCandidates()
        - Represents potential weekly OT time ranges
↓
Step 3: For each weekly OT rule:
        a. Check if rule is weekly overtime type
        b. Create weekly OT evaluation context
        c. Include all weekly time logs and candidate ranges
        d. Call weekly OT evaluator
        e. Set result directly (no accumulation needed)
↓
Output: Updated result with weekly overtime calculations
```

**Why Separate Weekly Processing?**
- **Daily rules must run first** - Regular Hours and Daily OT claim time ranges
- **Weekly OT sees leftovers** - Only processes time not claimed by daily rules
- **Week-level context needed** - Must see total hours across all days

**Example:**
```java
Week totals after daily processing:
  - Regular Hours: 40 hours (8h × 5 days)
  - Daily OT: 4 hours (extra hours beyond 8h/day)
  - Total worked: 44 hours

Weekly OT Rule (threshold=40h, multiplier=2.0):
  - Eligible hours: 44 - 40 = 4 hours
  - But Daily OT already claimed 4 hours at 1.5x
  - Weekly OT rule doesn't override daily OT
  
Result: No weekly OT (daily OT already covered excess)
```

---

### 8b. `evaluateDefaultPayRules()` - Week-end Default Pay Sweep
**Role:** Pays any worked time still unallocated after Weekly OT, at base rate × 1.0. Runs **inside `evaluateWeeklyTimeLogs`**, immediately after `evaluateWeeklyOvertimeRules`.

**Gating:** No-op unless `timesheet.timesheetSetting.isUnplannedHoursPayEnabled == 1`. When the flag is 0/null, behavior is identical to today (no change to existing timesheets).

```java
Input:
  - All time logs for the week (DTOs, possibly multiple per date for multi-interval days)
  - Timesheet (read for the gating flag, base rates, and TimesheetSetting DTO)
  - RuleEvaluatorResult mutated in place by all earlier rules + WOT
↓
Step 1: Read flag → return early if 0/null
↓
Step 2: Group time logs by calendar date (LinkedHashMap preserving order)
↓
Step 3: For each date, compute free ranges per date:
        union(work intervals for the date) − union(timeRange across all
        rule evaluation results attached to that date's time logs)
        // Same shape as the mapper's per-interval unallocated, but
        // aggregated across all intervals belonging to a calendar date.
↓
Step 4: Read WOT consumed duration from result.weeklyOvertimeRuleEvaluationResult
        (treats null result OR null weeklyOvertimeHours as 0).
↓
Step 5: Walk dates LATEST → EARLIEST, subtracting WOT from each date's free
        duration. WOT consumes the *latest days first*, matching the
        intuition that overtime is earned at the end of the week.
        Default Pay duration per date = (date free) − (WOT consumed on date).
↓
Step 6: For each date with non-zero Default Pay duration:
        a. takeFromStart(freeRanges, dpDuration) → takes the chronological
           HEAD of the date's free ranges (so WOT lands on the tail and
           Default Pay lands on the head, within the same day too)
        b. Build a RuleEvaluationContext with timeRangesToEvaluate set to
           the carved ranges and currentTimeLogBeingEvaluated set to the
           FIRST time log of the date (the mapper aggregates per-date
           anyway via aggregateByRuleType, so attachment point is purely
           for dispatch — multi-interval days collapse into one entry)
        c. ruleFactory.createRule(getDefaultPayRuleType(), logger).evaluate(ctx)
        d. result.addRuleEvaluationResult(firstTimeLog, dpResult)
↓
Output: RuleEvaluatorResult mutated with Default Pay entries on the affected
        dates. evaluationDate, timeRange, payAmount/billAmount populated by
        the rule via createCompleteResult.
```

**Why week-end (not in the daily loop)?**
- Default Pay must subtract Weekly OT's claim, but `evaluateWeeklyOvertimeRules` itself runs at week end.
- Running Default Pay inline in the daily loop would claim time that WOT later wants.

**Side-effect on `unallocatedTimeRanges` in the response:**
- The mapper (`RuleEngineMapper.calculateUnallocatedTimeRangesForIntervals`) computes unallocated as `workInterval − occupied ranges across all per-timeLog rule results`. Default Pay's ranges are now occupied → they no longer show as unallocated.
- WOT remains intentionally OUT of per-timeLog results (its `timeRange` is set to null in `processRuleEvaluation`), so when Default Pay is on, the only ranges that still appear in `unallocatedTimeRanges` are the ones WOT claimed. To make `unallocatedTimeRanges` go fully to zero, WOT would need to populate per-day ranges too — that's a follow-up not implemented here.

**Helpers:**
- `computeFreeRangesForDate(dateTimeLogs, result)` — same formula as the mapper's per-interval unallocated, aggregated across the date.
- `takeFromStart(rangeSet, duration)` — chronological head; splits the trailing range partially when `duration` falls inside it.

**Subclass extension:**
- New abstract method `getDefaultPayRuleType()`:
  - `RangeBasedRuleEvaluator` → `RANGE_BASED_DEFAULT_PAY`
  - `DurationBasedRuleEvaluator` → `DURATION_BASED_DEFAULT_PAY`

**Edge cases handled (and tested in `BaseRuleEvaluatorDefaultPayTests`):**
- Flag null / 0 → early return, factory never called
- No unallocated time → no entries created
- No WOT result OR WOT result with `null` weeklyOvertimeHours → treated as 0
- WOT >= total unallocated → no entries created (clamps `wotRemaining` at 0)
- Partial-range split when WOT lands mid-range
- Multi-interval day → all intervals contribute to free ranges, attached to first interval
- Prior daily-rule results correctly reduce free ranges
- Empty weekly logs → no-op

---

### 9. `collectWeeklyOvertimeCandidates()` - Weekly OT Candidate Collection
**Lines:** 408-446  
**Role:** Collects weekly overtime candidate time ranges during daily processing

```java
Input:
  - Current time log being evaluated
  - Weekly OT rule
  - Daily state and context
  - Same-day time logs for multi-interval support
↓
Step 1: Check if multi-interval day
        boolean isMultiInterval = sameDayLogs.size() > 1;
↓
Step 2a: Multi-interval handling
        - Create merged time log spanning entire day
        - Calculate free ranges from merged boundary minus occupied ranges
        - Intersect with actual work ranges (exclude gaps between intervals)
        - Add to weekly OT candidates
↓
Step 2b: Single-interval handling
        - Use existing resolver logic
        - Constrain to individual time log boundaries
        - Add to weekly OT candidates
↓
Output: Updated weeklyOvertimeCandidateTimeRanges list
```

**Multi-Interval Logic:**
```java
// Monday: 9:00-12:00, 13:00-18:00 (gap: 12:00-13:00)
mergedTimeLog = TimeHelper.createMergedTimeLog(timeLog, sameDayLogs);  // 9:00-18:00
freeRanges = getFreeTimeRanges([9:00-18:00], occupiedRanges);
actualWorkRanges = buildActualWorkRanges(sameDayLogs);  // [9:00-12:00, 13:00-18:00]
workedFreeRanges = intersectRangeSets(freeRanges, actualWorkRanges);  // Exclude 12:00-13:00 gap

// This ensures weekly OT doesn't claim lunch break time
```

---

### 10. `postProcessResult(RuleEvaluatorResult result)` - Finalization
**Lines:** 789-791  
**Role:** Applies final calculations and populates monetary data

```java
Input: RuleEvaluatorResult with all rule evaluations
↓
Step 1: Call populateMoneyData()
        - Aggregates all rule results
        - Calculates totals per rule type
        - Applies any final adjustments
↓
Output: Finalized result ready for response mapping
```

---

## Core Data Structures

### EvaluationState - Daily State Management
**Lines:** 804-810  
**Purpose:** Tracks evaluation progress within a single day

```java
protected static class EvaluationState {
    // Time ranges already claimed by previous rules (prevents double-counting)
    RangeSet<LocalTime> occupiedTimeRanges = TreeRangeSet.create();
    
    // Total hours worked so far today (for daily overtime calculations)
    Duration workedHoursTillNow = Duration.ZERO;
}
```

**Multi-Interval Usage:**
```java
// Monday morning (9:00-12:00) - First interval
state.occupiedTimeRanges = [9:00-12:00]
state.workedHoursTillNow = 3 hours

// Monday afternoon (13:00-18:00) - SHARES SAME STATE
state.occupiedTimeRanges = [9:00-12:00, 13:00-18:00]  // Accumulated!
state.workedHoursTillNow = 8 hours                     // Total for day!

// Daily OT rule can now see total: 8 hours = threshold, no OT
// This prevents incorrect Daily OT calculation on partial intervals
```

---

## Key Helper Methods

### Custom Rule Index Calculation
**Lines:** 551-567  
**Purpose:** Calculates correct index for Daily OT rule resolvers

```java
private int calculateCustomRuleIndex(IEvaluatableRule currentRule, List<IEvaluatableRule> unifiedRules) {
    if (currentRule instanceof CustomRule customRule) {
        List<CustomRule> customRules = unifiedRules.stream()
            .filter(CustomRule.class::isInstance)
            .map(CustomRule.class::cast)
            .toList();
        
        for (int i = 0; i < customRules.size(); i++) {
            if (customRules.get(i).equals(customRule)) {
                return i;
            }
        }
    }
    
    return -1; // System rules don't have a custom rule index
}
```

**Why This Matters:**
- **Daily OT resolvers** need to find the "next" Daily OT rule for threshold calculation
- **Unified rules list** includes system rules, but resolvers only care about custom rules
- **Index mismatch** was causing incorrect threshold calculations (fixed in recent updates)

### Time Range Resolver Context Creation
**Lines:** 519-541  
**Purpose:** Creates context with same-day time logs for multi-interval support

```java
protected TimeRangeResolverContext createTimeRangeResolverContext(...) {
    TimeRangeResolverContext context = new TimeRangeResolverContext();
    // ... standard context setup ...
    
    // Key enhancement: Set same-day time logs for daily overtime aggregation
    context.setSameDayTimeLogs(TimeHelper.getSameDayTimeLogs(timeLog, weeklyTimeLog));
    
    return context;
}
```

### Multi-Interval Helper Methods
**Lines:** 452-475  
**Purpose:** Support multi-interval daily processing

```java
// Builds actual work ranges excluding gaps between intervals
private RangeSet<LocalTime> buildActualWorkRanges(List<TimeLog> sameDayLogs) {
    RangeSet<LocalTime> workRanges = TreeRangeSet.create();
    for (TimeLog tl : sameDayLogs) {
        LocalTime start = TimeHelper.getEffectiveStartTime(tl);
        LocalTime end = TimeHelper.getEffectiveEndTime(tl);
        if (start != null && end != null && start.isBefore(end)) {
            workRanges.add(TimeHelper.toRange(start, end));
        }
    }
    return workRanges;
}

// Returns intersection of two RangeSets
private RangeSet<LocalTime> intersectRangeSets(RangeSet<LocalTime> rangeSetA, RangeSet<LocalTime> rangeSetB) {
    RangeSet<LocalTime> result = TreeRangeSet.create();
    for (Range<LocalTime> rangeA : rangeSetA.asRanges()) {
        RangeSet<LocalTime> subView = rangeSetB.subRangeSet(rangeA);
        result.addAll(subView);
    }
    return result;
}
```

---

## Template Method Extension Points

### Abstract Methods (Must be implemented by subclasses)
```java
// Rule type configuration
protected abstract RuleType getRegularHoursRuleType();
protected abstract RuleType getBreakRuleType();

// Weekly overtime handling
protected abstract boolean isWeeklyOvertimeRule(IEvaluatableRule rule, Timesheet timesheet);
```

### Subclass Implementations

#### RangeBasedRuleEvaluator
```java
@Override
protected RuleType getRegularHoursRuleType() {
    return RuleType.RANGE_BASED_REGULAR_HOURS;
}

@Override
protected RuleType getBreakRuleType() {
    return RuleType.RANGE_BASED_BREAK;
}

@Override
protected boolean isWeeklyOvertimeRule(IEvaluatableRule rule, Timesheet timesheet) {
    return rule.getRuleType() == RuleType.RANGE_BASED_WEEKLY_OVERTIME
        && timesheet.getTimesheetSetting().getFrequency() != TimesheetFrequency.MONTHLY;
}

// Key override: Handles START_AND_END_TIME timesheets with intervals
@Override
public WeeklyRuleEvaluatorResult evaluateRules(Timesheet timesheet) {
    if (timesheet.getWorkLogType() == 2) {  // START_AND_END_TIME
        return evaluateRulesWithIntervals(timesheet);
    }
    return super.evaluateRules(timesheet);
}
```

#### DurationBasedRuleEvaluator
```java
@Override
protected RuleType getRegularHoursRuleType() {
    return RuleType.DURATION_BASED_REGULAR_HOURS;
}

@Override
protected RuleType getBreakRuleType() {
    return RuleType.DURATION_BASED_BREAK;
}

@Override
protected boolean isWeeklyOvertimeRule(IEvaluatableRule rule, Timesheet timesheet) {
    return rule.getRuleType() == RuleType.DURATION_BASED_WEEKLY_OVERTIME
        && timesheet.getTimesheetSetting().getFrequency() != TimesheetFrequency.MONTHLY;
}
```

---

## Complete Data Flow Example

### Input Timesheet with Multi-Intervals
```java
Timesheet {
  id: 500,
  candidate: "Sarah Johnson",
  workLogType: 2,  // START_AND_END_TIME
  timeLogs: [
    TimeLog(id=100, date=Jan15, start=9:00, end=12:00),    // Morning
    TimeLog(id=101, date=Jan15, start=13:00, end=18:00),   // Afternoon - 8h total
    TimeLog(id=102, date=Jan16, start=9:00, end=12:00),    // Morning
    TimeLog(id=103, date=Jan16, start=13:00, end=19:00),   // Afternoon - 9h total
    TimeLog(id=104, date=Jan17, start=9:00, end=17:00),    // Full day - 8h
    TimeLog(id=105, date=Jan18, start=8:00, end=12:00),    // Morning
    TimeLog(id=106, date=Jan18, start=13:00, end=20:00),   // Afternoon - 11h total
    TimeLog(id=107, date=Jan19, start=9:00, end=17:00)     // Full day - 8h
  ],
  settings: {
    payRate: $25/hour,
    billRate: $40/hour,
    rules: [
      RegularHoursRule(threshold=8h),
      DailyOvertimeRule(threshold=8h, multiplier=1.5),
      WeeklyOvertimeRule(threshold=40h, multiplier=2.0)
    ]
  }
}
```

### Processing Flow

#### Step 1: Weekly Preparation
```java
prepareWeeklyTimeLogs() →
[
  [ // Week Jan 15-21
    TimeLog(Jan15, 9:00-12:00),   // Monday morning
    TimeLog(Jan15, 13:00-18:00),  // Monday afternoon
    TimeLog(Jan16, 9:00-12:00),   // Tuesday morning
    TimeLog(Jan16, 13:00-19:00),  // Tuesday afternoon
    TimeLog(Jan17, 9:00-17:00),   // Wednesday full
    TimeLog(Jan18, 8:00-12:00),   // Thursday morning
    TimeLog(Jan18, 13:00-20:00),  // Thursday afternoon
    TimeLog(Jan19, 9:00-17:00)    // Friday full
  ]
]
```

#### Step 2: Daily Processing with State Sharing
```java
// Monday (Jan 15): 8 hours total across 2 intervals
dailyStates[Jan15] = new EvaluationState();

evaluateTimeLog(Jan15, 9:00-12:00):  // Morning interval
  RegularHours: [9:00-12:00] → 3h × $25 = $75 pay
  state.occupiedRanges = [9:00-12:00]
  state.workedHours = 3h

evaluateTimeLog(Jan15, 13:00-18:00):  // Afternoon interval - SAME STATE
  RegularHours: [13:00-18:00] → 5h × $25 = $125 pay
  DailyOT: [] → 0h (8h total = threshold)
  state.occupiedRanges = [9:00-12:00, 13:00-18:00]
  state.workedHours = 8h

// Tuesday (Jan 16): 9 hours total across 2 intervals  
dailyStates[Jan16] = new EvaluationState();

evaluateTimeLog(Jan16, 9:00-12:00):  // Morning interval
  RegularHours: [9:00-12:00] → 3h × $25 = $75 pay
  state.occupiedRanges = [9:00-12:00]
  state.workedHours = 3h

evaluateTimeLog(Jan16, 13:00-19:00):  // Afternoon interval - SAME STATE
  RegularHours: [13:00-18:00] → 5h × $25 = $125 pay
  DailyOT: [18:00-19:00] → 1h × $37.50 = $37.50 pay  // 9h total > 8h threshold
  state.occupiedRanges = [9:00-12:00, 13:00-19:00]
  state.workedHours = 9h

// ... similar processing for other days ...
```

#### Step 3: Weekly Overtime Processing
```java
evaluateWeeklyOvertimeRules():
  Total hours: 44 hours (40 regular + 4 daily OT)
  Weekly threshold: 40 hours
  Potential weekly OT: 4 hours
  
  But Daily OT already claimed 4 hours at 1.5x rate
  Weekly OT rule (2.0x) doesn't override daily OT
  
  Result: 0 additional weekly OT
```

#### Step 4: Final Totals
```java
postProcessResult():
  Regular Hours: 40h × $25 = $1000 pay, 40h × $40 = $1600 bill
  Daily OT: 4h × $37.50 = $150 pay, 4h × $60 = $240 bill
  Weekly OT: 0h
  
  Total: $1150 pay, $1840 bill
```

---

## Recent Enhancements

### Multi-Interval Support
- **Daily state sharing** across same-day intervals
- **Aggregated daily calculations** for accurate Daily OT
- **Gap exclusion** in weekly OT candidates
- **Last interval detection** for Daily OT application

### Custom Rule Index Fix
- **Separate index calculation** for custom rules vs unified rules
- **Prevents Daily OT resolver errors** when finding next rule
- **Maintains backward compatibility** with existing logic

### Zero-Duration Filtering
- **Automatic filtering** of invalid time logs during preparation
- **Detailed logging** with time log IDs for debugging
- **Prevents downstream errors** in rule evaluation

### Enhanced Context Creation
- **Same-day time logs** included in TimeRangeResolverContext
- **Weekly time log context** for multi-interval support
- **Proper custom rule index** calculation for resolvers

---

## Design Patterns Used

### 1. **Template Method Pattern**
- **BaseRuleEvaluator** defines algorithm skeleton
- **Subclasses** customize specific steps (rule types, weekly OT handling)
- **Benefits:** Code reuse, consistent flow, extensibility

### 2. **Strategy Pattern**
- Different **rule resolvers** for different rule types
- **RuleFactory** creates appropriate strategy
- **Benefits:** Pluggable rule implementations

### 3. **State Pattern**
- **EvaluationState** tracks daily progress
- **State sharing** across same-day intervals
- **Benefits:** Maintains context across rule evaluations

### 4. **Builder Pattern**
- **WeeklyRuleEvaluatorResult.builder()**
- **RuleEvaluationContext** creation
- **Benefits:** Flexible object construction

---

## Error Handling

### Validation Errors
```java
validateTimesheet():
  - null timesheet → IllegalArgumentException
  - null settings → IllegalArgumentException  
  - empty time logs → IllegalArgumentException
```

### Processing Errors
```java
prepareWeeklyTimeLogs():
  - Invalid time data → Log warning with ID, skip time log
  - Zero duration → Log warning with ID, skip time log
```

### Rule Evaluation Errors
```java
evaluateTimeLog():
  - Rule not applicable → Skip rule, continue
  - Empty time ranges → Skip rule, continue
  - Null rule result → Skip result, continue
```

---

## Performance Considerations

### Time Complexity
- **Weekly splitting:** O(n log n) - sorting by date
- **Rule evaluation:** O(n × r) - n time logs, r rules per log
- **State updates:** O(log m) - m occupied ranges per day
- **Multi-interval processing:** O(i × r) - i intervals per day, r rules
- **Overall:** O(n × r × log m) - efficient for typical timesheet sizes

### Memory Usage
- **EvaluationState per day:** Minimal memory footprint
- **Weekly grouping:** Processes one week at a time
- **Rule results:** Accumulated incrementally
- **Multi-interval sharing:** Single state instance per day

### Optimization Strategies
- **Early termination:** Skip non-applicable rules
- **Efficient range operations:** Use Guava RangeSet
- **State sharing:** Reuse EvaluationState across same-day intervals
- **Minimal object creation:** Reuse contexts where possible

---

## Testing Strategies

### Unit Tests
1. **validateTimesheet()** - All validation scenarios
2. **prepareWeeklyTimeLogs()** - Weekly splitting, zero-duration filtering
3. **evaluateTimeLog()** - Single time log with various rules
4. **EvaluationState sharing** - Multi-interval state accumulation
5. **calculateCustomRuleIndex()** - Custom rule index calculation
6. **collectWeeklyOvertimeCandidates()** - Multi-interval candidate collection

### Integration Tests
1. **End-to-end flow** - Complete timesheet processing
2. **Multi-interval scenarios** - Days with multiple work periods
3. **Mixed rule types** - Various combinations of rules
4. **Edge cases** - Boundary conditions (exactly 8h, 40h)
5. **Zero-duration handling** - Invalid time log filtering

### Performance Tests
1. **Large timesheets** - 100+ time logs with multiple intervals
2. **Complex rule sets** - 10+ rules per timesheet
3. **Memory usage** - Monitor EvaluationState growth
4. **Multi-interval processing** - Days with many intervals

---

## Related Files

### Core Dependencies
- **IRuleEvaluator.java** - Interface contract
- **IRuleFactory.java** - Rule creation factory
- **TimeHelper.java** - Time calculation utilities (enhanced for multi-intervals)
- **UnifiedRuleManager.java** - Rule list management

### Subclasses
- **RangeBasedRuleEvaluator.java** - START_AND_END_TIME implementation (with interval support)
- **DurationBasedRuleEvaluator.java** - DURATION implementation

### DTOs and Results
- **WeeklyRuleEvaluatorResult.java** - Final output structure
- **RuleEvaluatorResult.java** - Weekly result container
- **RuleEvaluationResult.java** - Individual rule result
- **TimeRangeResolverContext.java** - Rule resolver context (enhanced for same-day logs)

### Rule Resolvers
- **BaseDailyOvertimeRuleTimeRangeResolver.java** - Daily OT with multi-interval support
- **RangeBasedBreakRuleTimeRangeResolver.java** - Break handling with actual intervals
- **RangeBasedRegularHoursRuleTimeRangeResolver.java** - Regular hours calculation

---

## Summary

The **BaseRuleEvaluator** is the foundation of the rule engine, providing:

1. **Consistent Algorithm** - Template method ensures uniform processing
2. **Multi-Interval Support** - Accurate calculations across same-day work periods
3. **State Management** - Daily state sharing for precise Daily OT calculations
4. **Extensibility** - Subclasses customize specific behaviors
5. **Error Handling** - Graceful handling of invalid data with detailed logging
6. **Performance** - Efficient processing of large timesheets with multiple intervals

**Key Innovation:** The combination of Template Method pattern with daily state management and multi-interval support enables accurate overtime calculations across complex work schedules while maintaining code reusability and extensibility.

This class transforms complex timesheet rules into a systematic, predictable evaluation process that produces accurate pay and bill calculations for both simple single-interval days and complex multi-interval work schedules.