# RangeBasedRegularHoursRuleTimeRangeResolver - Complete End-to-End Flow

## Overview
**Location:** `src/main/java/io/recruitcrm/microservice/timesheet/rule_engine/rules/timerange_resolver/range_based/RangeBasedRegularHoursRuleTimeRangeResolver.java`

**Purpose:** Calculates time ranges that qualify as "Regular Hours" for range-based timesheets. This is the **first rule** in the evaluation sequence and determines the foundation for all other rule calculations, with sophisticated support for multi-interval work days, break threshold adjustments, and daily overtime coordination.

**Key Responsibility:** Transforms template work schedules into actual claimable regular hours time ranges while respecting break thresholds, daily overtime limits, occupied time ranges, and complex multi-interval work patterns.

---

## Real-World Example

**Scenario:** Sarah works Monday with multiple intervals and break threshold requirements:
- **Template Schedule:** 9:00 AM - 5:00 PM (8 hours standard)
- **Morning Interval:** 9:00 AM - 12:00 PM (3 hours, 10-min break at 10:30-10:40)
- **Afternoon Interval:** 1:00 PM - 6:00 PM (5 hours, 15-min break at 3:00-3:15)
- **Break Threshold:** 30 minutes (minimum entitled break time)
- **Daily OT Threshold:** 8 hours
- **Total Work:** 8 hours, **Total Actual Breaks:** 25 minutes

**Processing Flow:**
1. **Template Range:** [9:00 AM - 5:00 PM] (8 hours baseline)
2. **Available Time:** [9:00-17:00] minus occupied ranges (none initially)
3. **Break Threshold Adjustment:** 30 min entitled - 25 min actual = 5 min adjustment
4. **Adjusted Available:** [9:00-16:55] (reduced by 5 min from end)
5. **Daily OT Limiting:** 7h 55m < 8h threshold ✓ (no limiting needed)
6. **Final Regular Hours:** [9:00-16:55] across both intervals

**Result:** Sarah gets 7h 55m regular hours + 5 min break compensation = 8h total pay

---

## Architecture Overview

### Rule Evaluation Sequence
```
1. Regular Hours Rule  ← THIS CLASS (establishes base work time)
    ↓ Claims: Template work time minus adjustments
    ↓ Sets: adjustedRegularHoursBreakThreshold in context
2. Break Rule         (claims actual break time from occupied ranges)
    ↓ Uses: Break intervals or dynamic allocation
3. Daily OT Rule      (claims time beyond daily threshold)
    ↓ Uses: adjustedRegularHoursBreakThreshold for accurate calculations
4. Weekly OT Rule     (claims time beyond weekly threshold)
```

### Key Architectural Concepts
- **Template Work Day** - Configured work schedule (e.g., Monday 9-5)
- **Break Threshold System** - Ensures minimum break time compensation
- **Multi-Interval Aggregation** - Handles same-day work periods correctly
- **Daily OT Coordination** - Sets up proper limiting for overtime calculations
- **Context Communication** - Passes break adjustments to downstream rules

---

## Complete Function Flow

### Visual Flow Diagram
```
┌─────────────────────────────────────────────────────────────┐
│  resolveTimeRange()                                         │
│  Main orchestrator - coordinates all regular hours logic    │
└────────────┬────────────────────────────────────────────────┘
             │
             ├─ DAY_OFF check ──────────────────────────┐
             │                                          │
┌────────────▼──────────────────────────────────────────────────┐
│  Template Work Day Lookup & Validation                      │
│  Gets configured schedule + validates start < end times     │
└────────────┬─────────────────────────────────────────────────┘
             │
┌────────────▼──────────────────────────────────────────────────┐
│  Create Full Regular Hours Range                             │
│  Template start → end (e.g., 9:00-17:00 = 8h baseline)      │
└────────────┬─────────────────────────────────────────────────┘
             │
┌────────────▼──────────────────────────────────────────────────┐
│  Subtract Occupied Time Ranges                              │
│  Remove time already claimed by previous rules               │
│  (Usually empty for first rule in sequence)                 │
└────────────┬─────────────────────────────────────────────────┘
             │
┌────────────▼──────────────────────────────────────────────────┐
│  Calculate Break Threshold Adjustment                       │
│  Compare actual breaks vs entitled breaks across same day   │
│  Apply adjustment if actual < entitled                      │
└────────────┬─────────────────────────────────────────────────┘
             │
┌────────────▼──────────────────────────────────────────────────┐
│  Cap Break Threshold Adjustment                             │
│  Ensure adjustment doesn't exceed reasonable limits         │
│  Set adjustedRegularHoursBreakThreshold in context         │
└────────────┬─────────────────────────────────────────────────┘
             │
┌────────────▼──────────────────────────────────────────────────┐
│  Apply Break Threshold Adjustment                           │
│  Trim time ranges from end by adjustment amount             │
│  Uses adjustTimeRangesFromEnd() for precise trimming       │
└────────────┬─────────────────────────────────────────────────┘
             │
┌────────────▼──────────────────────────────────────────────────┐
│  Apply Daily OT Threshold Limiting                          │
│  Cap total regular hours at daily overtime threshold        │
│  Handle multi-interval scenarios with last-interval logic  │
└────────────┬─────────────────────────────────────────────────┘
             │
┌────────────▼──────────────────────────────────────────────────┐
│  Return Final Regular Hours Time Ranges                     │
│  Processed ranges ready for rule engine to claim            │
└─────────────────────────────────────────────────────────────┘
```

---

## Function-by-Function Breakdown

### 1. `resolveTimeRange(TimeRangeResolverContext)` - Main Orchestrator
**Lines:** 53-129  
**Role:** Coordinates the entire regular hours calculation process with sophisticated multi-step logic

```java
Input: TimeRangeResolverContext containing:
  - Current time log (e.g., Monday morning 9:00-12:00)
  - Occupied time ranges (from previous rules - usually empty)
  - Same-day time logs (for multi-interval support)
  - Timesheet settings (template work days, break thresholds)
  - Custom rules (for daily OT threshold discovery)
↓
Step 1: DAY_OFF validation
        if (dayType == DAY_OFF) → return TreeRangeSet.create()
        Prevents regular hours calculation on non-working days
↓
Step 2: Template work day lookup
        Extract date → Convert to WorkDay enum → Lookup TemplateWorkDay
        Example: Monday Jan 15 → MONDAY → {9:00-17:00, 30min break threshold}
↓
Step 3: Template time validation
        Ensure workStartTime != null && workEndTime != null && start < end
        If invalid → return TreeRangeSet.create()
↓
Step 4: Create full regular hours range
        Range<LocalTime> fullRegularHoursRange = [workStartTime - workEndTime]
        Example: [9:00 - 17:00] (8 hours maximum regular hours)
↓
Step 5: Subtract occupied time ranges
        availableTimeRanges = TimeHelper.getAvailableTimeRanges(full, occupied)
        Usually no change for first rule, but handles edge cases
↓
Step 6: Calculate break threshold adjustment
        breakTimeThresholdAdjustment = calculateBreakTimeThresholdAdjustmentWithRangeConstraint()
        Considers ALL same-day intervals for accurate daily break calculation
↓
Step 7: Cap break threshold adjustment
        cappedAdjustment = capBreakThresholdAdjustment()
        Prevents excessive adjustments that would create negative regular hours
↓
Step 8: Set context for downstream rules
        context.setAdjustedRegularHoursBreakThreshold(cappedAdjustment)
        Daily OT rule uses this for accurate effective working time calculation
↓
Step 9: Apply break threshold adjustment
        if (!adjustment.isZero()) → adjustTimeRangesFromEnd(available, adjustment)
        Trims time from end of ranges to compensate for insufficient breaks
↓
Step 10: Apply daily OT threshold limiting
        limitedRanges = limitToRemainingRegularHours(available, context)
        Ensures regular hours don't exceed daily overtime threshold
↓
Output: RangeSet<LocalTime> of final regular hours time ranges
```

**Comprehensive Example:**
```java
Input Context:
  - TimeLog: Monday 9:00-12:00 (morning interval)
  - Same-day logs: [9:00-12:00, 13:00-18:00] (morning + afternoon)
  - Template: Monday 9:00-17:00 (8h), break threshold 30min
  - Daily OT threshold: 8 hours
  - Occupied ranges: [] (empty, first rule)

Step-by-Step Processing:

// Step 1: DAY_OFF check
dayType = WORKING_DAY ✓ (not DAY_OFF)

// Step 2: Template lookup
timeLogDate = Jan 15 (Monday)
WorkDay = MONDAY
TemplateWorkDay = {workStartTime: 9:00, workEndTime: 17:00, breakThreshold: 30min}

// Step 3: Validation
9:00 != null ✓, 17:00 != null ✓, 9:00 < 17:00 ✓

// Step 4: Full range creation
fullRegularHoursRange = [9:00 - 17:00] (8 hours baseline)

// Step 5: Subtract occupied ranges
occupiedRanges = [] (empty)
availableTimeRanges = [9:00 - 17:00] (no changes)

// Step 6: Calculate break threshold adjustment
actualBreaksAcrossDay = 10min (morning) + 15min (afternoon) = 25min
breakThreshold = 30min
rawAdjustment = max(0, 30 - 25) = 5min

// Step 7: Cap adjustment
actualWorkInRegular = 7h 35min (8h template - 25min actual breaks)
expectedMaxRegular = 7h 30min (8h template - 30min threshold)
excessWork = 7h 35min - 7h 30min = 5min
cappedAdjustment = min(5min, 5min) = 5min ✓

// Step 8: Set context
context.adjustedRegularHoursBreakThreshold = Duration.ofMinutes(5)

// Step 9: Apply break adjustment
availableTimeRanges = adjustTimeRangesFromEnd([9:00-17:00], 5min)
                   = [9:00-16:55] (reduced by 5min from end)

// Step 10: Daily OT limiting
totalRegularHours = 7h 55min (across both intervals after adjustment)
dailyOTThreshold = 8h
7h 55min < 8h ✓ (no limiting needed)

Output: RangeSet containing [9:00-16:55]
```

---

### 2. `limitToRemainingRegularHours()` - Daily OT Threshold Enforcement
**Lines:** 142-223  
**Role:** Ensures regular hours don't exceed daily overtime threshold across all same-day intervals

```java
Input:
  - Available time ranges after break threshold adjustments
  - TimeRangeResolverContext with same-day time logs and custom rules
↓
Step 1: Find lowest applicable daily OT threshold
        dailyOvertimeThreshold = findLowestApplicableDailyOvertimeThreshold(context)
        Scans custom rules for RANGE_BASED_DAILY_OVERTIME rules
        Returns lowest threshold found (e.g., 8 hours) or null if none
↓
Step 2: Calculate total regular hours duration
        if (multi-interval) → calculateCumulativeRegularHours(sameDayLogs, context)
        else → calculateTotalDurationFromRanges(availableTimeRanges)
        
        For multi-interval: Subtract break threshold adjustment to avoid double-counting
        (adjustment already applied to availableTimeRanges)
↓
Step 3: Calculate potential overtime
        if (totalRegularHours > threshold) → potentialOvertime = total - threshold
        else → potentialOvertime = Duration.ZERO
↓
Step 4: Determine if limiting should be applied
        shouldLimit = (single interval) || isLastIntervalOfDay(current, sameDayLogs)
        Multi-interval logic: Only limit on LAST interval to avoid double-limiting
↓
Step 5: Apply limitation if needed
        if (shouldLimit && potentialOvertime > 0) →
            limitedRanges = adjustTimeRangesFromEnd(available, potentialOvertime)
            Log limitation details for debugging
        else → return original availableTimeRanges
↓
Output: Limited time ranges or original ranges if no limiting needed
```

**Multi-Interval Limiting Example:**
```java
Scenario: Monday with morning + afternoon intervals
  Morning: 9:00-12:00 (3 hours work, 10min break)
  Afternoon: 13:00-19:00 (6 hours work, 15min break)
  Total work: 8h 35min, Total breaks: 25min
  Daily OT Threshold: 8 hours

Processing Morning Interval (9:00-12:00):
  availableTimeRanges = [9:00-12:00] (after break adjustments)
  
  // Calculate total across both intervals
  totalRegularHours = calculateCumulativeRegularHours(sameDayLogs, context)
    Morning: 3h - 10min breaks = 2h 50min
    Afternoon: 6h - 15min breaks = 5h 45min
    Total: 8h 35min
  
  // Subtract break threshold adjustment (already applied to availableTimeRanges)
  breakThresholdAdjustment = 5min (30min threshold - 25min actual)
  adjustedTotal = 8h 35min - 5min = 8h 30min
  
  potentialOvertime = 8h 30min - 8h = 30min
  isLastInterval = false (morning is not last)
  shouldLimit = false
  
  → Return [9:00-12:00] (no limiting on non-last interval)

Processing Afternoon Interval (13:00-19:00):
  availableTimeRanges = [13:00-19:00] (after break adjustments)
  
  // Same total calculation as morning
  adjustedTotal = 8h 30min
  potentialOvertime = 30min
  isLastInterval = true (afternoon is last)
  shouldLimit = true
  
  // Apply limiting
  limitedRanges = adjustTimeRangesFromEnd([13:00-19:00], 30min)
                = [13:00-18:30] (removed 30min from end)
  
  → Return [13:00-18:30]

Final Result:
  - Regular Hours: 8h total (3h morning + 5h afternoon)
  - Available for Daily OT: 30min (18:30-19:00)
  - Break compensation: 5min included in regular hours calculation
```

---

### 3. `calculateCumulativeRegularHours()` - Multi-Interval Aggregation
**Lines:** 233-298  
**Role:** Calculates total regular hours across all same-day intervals with template constraint

```java
Input:
  - List<TimeLog> sameDayTimeLogs (all intervals for the day)
  - TimeRangeResolverContext with template work day configuration
↓
Step 1: Get template Regular Hours range for current day
        Extract date → WorkDay → TemplateWorkDay → workStartTime/EndTime
        Create templateRange = [workStartTime - workEndTime]
        Example: [9:00-17:00] for Monday template
↓
Step 2: Initialize total regular hours accumulator
        Duration totalRegularHours = Duration.ZERO
↓
Step 3: For each same-day time log interval:
        a. Get effective start/end times using TimeHelper
        b. Create work range for the interval
        c. Intersect work range with template range
        d. Calculate intersection duration
        e. Subtract break time within intersection
        f. Add positive duration to total regular hours
↓
Output: Total duration of regular hours across all intervals
```

**Detailed Multi-Interval Example:**
```java
Input:
  sameDayTimeLogs = [
    TimeLog(9:00-12:00, breaks=[Break(10:30-10:40)]),    // Morning: 2h 50min work
    TimeLog(13:00-18:30, breaks=[Break(15:00-15:15)])    // Afternoon: 5h 15min work
  ]
  templateRange = [9:00-17:00] (8h template for Monday)

Processing Each Interval:

// Morning interval: 9:00-12:00
workStart = TimeHelper.getEffectiveStartTime(timeLog1) = 9:00
workEnd = TimeHelper.getEffectiveEndTime(timeLog1) = 12:00
workRange = [9:00-12:00]

// Intersect with template
intersection = [9:00-12:00] ∩ [9:00-17:00] = [9:00-12:00] ✓ (fully within template)
intervalDuration = Duration.between(9:00, 12:00) = 3h

// Subtract breaks within intersection
breakTimeInRange = calculateBreakTimeWithinRangeForTimeLog(timeLog1, [9:00-12:00])
  Break(10:30-10:40) ∩ [9:00-12:00] = [10:30-10:40] = 10min
intervalRegularHours = 3h - 10min = 2h 50min
totalRegularHours = 0 + 2h 50min = 2h 50min

// Afternoon interval: 13:00-18:30
workStart = 13:00, workEnd = 18:30
workRange = [13:00-18:30]

// Intersect with template (clips at 17:00)
intersection = [13:00-18:30] ∩ [9:00-17:00] = [13:00-17:00] (clipped to template end)
intervalDuration = Duration.between(13:00, 17:00) = 4h

// Subtract breaks within intersection
breakTimeInRange = calculateBreakTimeWithinRangeForTimeLog(timeLog2, [13:00-17:00])
  Break(15:00-15:15) ∩ [13:00-17:00] = [15:00-15:15] = 15min
intervalRegularHours = 4h - 15min = 3h 45min
totalRegularHours = 2h 50min + 3h 45min = 6h 35min

Output: Duration.ofMinutes(395) // 6h 35min total regular hours within template
```

**Key Insight:** Only work within template boundaries counts as regular hours. Work beyond template (18:00-18:30 in afternoon) is available for Daily OT.

---

### 4. `calculateBreakTimeWithinRangeForTimeLog()` - Break Intersection Calculator
**Lines:** 308-347  
**Role:** Calculates break time that falls within a specific time range for one time log

```java
Input:
  - TimeLog with break intervals (List<TimeLogBreakInterval>)
  - Range<LocalTime> to constrain breaks to
↓
Step 1: Validate inputs
        Guard clauses: return Duration.ZERO if timeLog/range null or no break intervals
↓
Step 2: Initialize break time accumulator
        Duration totalBreakTime = Duration.ZERO
↓
Step 3: For each break interval in time log:
        a. Extract breakStartTime and breakEndTime
        b. Validate break times (non-null, start < end)
        c. Create Range<LocalTime> for break interval
        d. Check if break range intersects with constraint range
        e. Calculate intersection duration
        f. Add intersection duration to total break time
↓
Output: Total break duration within the constraint range
```

**Comprehensive Example:**
```java
Input:
  timeLog.breakIntervals = [
    TimeLogBreakInterval(breakStartTime=10:30, breakEndTime=10:45),  // 15min morning coffee
    TimeLogBreakInterval(breakStartTime=12:00, breakEndTime=13:00),  // 1h lunch
    TimeLogBreakInterval(breakStartTime=15:00, breakEndTime=15:15),  // 15min afternoon coffee
    TimeLogBreakInterval(breakStartTime=null, breakEndTime=16:00),   // Invalid: null start
  ]
  constraintRange = [9:00-17:00]  // Regular hours template range

Processing Each Break Interval:

// Break 1: Morning coffee 10:30-10:45
breakStart = 10:30, breakEnd = 10:45
Validation: 10:30 != null ✓, 10:45 != null ✓, 10:30 < 10:45 ✓
breakRange = [10:30-10:45]
isConnected([10:30-10:45], [9:00-17:00]) = true ✓
intersection = [10:30-10:45] ∩ [9:00-17:00] = [10:30-10:45]
duration = Duration.between(10:30, 10:45) = 15min
totalBreakTime = 0 + 15min = 15min

// Break 2: Lunch 12:00-13:00
breakStart = 12:00, breakEnd = 13:00
Validation: 12:00 != null ✓, 13:00 != null ✓, 12:00 < 13:00 ✓
breakRange = [12:00-13:00]
isConnected([12:00-13:00], [9:00-17:00]) = true ✓
intersection = [12:00-13:00] ∩ [9:00-17:00] = [12:00-13:00]
duration = Duration.between(12:00, 13:00) = 60min
totalBreakTime = 15min + 60min = 75min

// Break 3: Afternoon coffee 15:00-15:15
breakStart = 15:00, breakEnd = 15:15
Validation: 15:00 != null ✓, 15:15 != null ✓, 15:00 < 15:15 ✓
breakRange = [15:00-15:15]
isConnected([15:00-15:15], [9:00-17:00]) = true ✓
intersection = [15:00-15:15] ∩ [9:00-17:00] = [15:00-15:15]
duration = Duration.between(15:00, 15:15) = 15min
totalBreakTime = 75min + 15min = 90min

// Break 4: Invalid break (null start)
breakStart = null, breakEnd = 16:00
Validation: null != null ✗ (fails validation)
→ Skip this break interval (continue to next)

Output: Duration.ofMinutes(90) // 1h 30min total break time within regular hours
```

---

### 5. `findLowestApplicableDailyOvertimeThreshold()` - Threshold Discovery
**Lines:** 375-413  
**Role:** Scans custom rules to find the most restrictive daily overtime threshold

```java
Input: TimeRangeResolverContext with custom rules and current time log date
↓
Step 1: Get custom rules list and current work day
        List<CustomRule> customRules = context.getInternalSortedCustomRules()
        WorkDay currentWorkDay = TimeHelper.getWorkDayFromLocalDate(timeLog.getDate())
↓
Step 2: Initialize threshold tracking
        Duration lowestThreshold = null
↓
Step 3: Scan through all custom rules
        For each CustomRule:
        a. Check if rule type is daily overtime (RANGE_BASED_DAILY_OVERTIME)
        b. Check if rule is applicable on current work day
        c. Extract daily threshold from rule
        d. Track lowest threshold found (most restrictive)
↓
Output: Lowest threshold Duration or null if no applicable daily OT rules
```

**Rule Scanning Example:**
```java
Input:
  customRules = [
    CustomRule(type=RANGE_BASED_DAILY_OVERTIME, threshold=8h, days=[MON,TUE,WED,THU,FRI]),
    CustomRule(type=RANGE_BASED_DAILY_OVERTIME, threshold=10h, days=[SAT,SUN]),
    CustomRule(type=RANGE_BASED_WEEKLY_OVERTIME, threshold=40h, days=[ALL]),
    CustomRule(type=RANGE_BASED_DAILY_OVERTIME, threshold=6h, days=[FRI])  // Special Friday rule
  ]
  currentWorkDay = FRIDAY

Processing Each Rule:

// Rule 1: Standard weekday daily OT (8h)
ruleType = RANGE_BASED_DAILY_OVERTIME ✓ (is daily OT rule)
isApplicableOnDay(FRIDAY) = true ✓ (FRI in [MON,TUE,WED,THU,FRI])
threshold = Duration.ofHours(8)
lowestThreshold = null → 8h (first threshold found)

// Rule 2: Weekend daily OT (10h)
ruleType = RANGE_BASED_DAILY_OVERTIME ✓ (is daily OT rule)
isApplicableOnDay(FRIDAY) = false ✗ (FRI not in [SAT,SUN])
→ Skip this rule (not applicable on Friday)

// Rule 3: Weekly OT (40h)
ruleType = RANGE_BASED_WEEKLY_OVERTIME ✗ (not daily OT rule)
→ Skip this rule (wrong rule type)

// Rule 4: Special Friday daily OT (6h)
ruleType = RANGE_BASED_DAILY_OVERTIME ✓ (is daily OT rule)
isApplicableOnDay(FRIDAY) = true ✓ (FRI in [FRI])
threshold = Duration.ofHours(6)
6h < 8h ✓ (more restrictive than current lowest)
lowestThreshold = 8h → 6h (update to more restrictive threshold)

Output: Duration.ofHours(6) // Most restrictive threshold for Friday
```

**Why Lowest Threshold Matters:** Using the most restrictive threshold ensures regular hours don't exceed any applicable daily overtime rule, preventing conflicts in rule evaluation.

---

### 6. `capBreakThresholdAdjustment()` - Adjustment Limiting
**Lines:** 436-487  
**Role:** Caps break threshold adjustment to prevent excessive deductions from regular hours

```java
Input:
  - Duration rawAdjustment (uncapped break threshold adjustment)
  - TimeRangeResolverContext with timesheet settings and same-day logs
  - Range<LocalTime> regularHoursRange (template range)
  - RangeSet<LocalTime> availableTimeRanges (template minus occupied)
↓
Step 1: Validate inputs
        if (rawAdjustment null/zero OR breakTimeThreshold null/zero) → return rawAdjustment
↓
Step 2: Calculate expected maximum regular hours
        templateDuration = regularHoursRange duration (e.g., 8h)
        expectedMaxRegularHours = templateDuration - breakTimeThreshold
        Example: 8h - 30min = 7h 30min (expected work after entitled breaks)
↓
Step 3: Calculate actual work in regular hours
        actualWorkInRegular = calculateCumulativeRegularHours(sameDayLogs, context)
        This is actual work time within template boundaries, excluding breaks
↓
Step 4: Calculate excess work above expected
        excessWork = actualWorkInRegular - expectedMaxRegularHours
        if (excessWork <= 0) → return Duration.ZERO (no adjustment needed)
↓
Step 5: Account for available time beyond current interval
        adjustedExcess = addAvailableTimeBeyondInterval(excessWork, context, ...)
        Compensates for trim that may be "wasted" on time beyond interval boundaries
↓
Step 6: Cap the adjustment
        cappedAdjustment = min(adjustedExcess, rawAdjustment)
        Log capping details for debugging
↓
Output: Capped adjustment duration that won't create negative regular hours
```

**Capping Logic Example:**
```java
Scenario: Employee works more than expected but adjustment should be limited

Input:
  rawAdjustment = Duration.ofMinutes(45) // 30min threshold - (-15min) actual = 45min
  breakTimeThreshold = Duration.ofMinutes(30) // Entitled to 30min breaks
  regularHoursRange = [9:00-17:00] // 8h template
  sameDayTimeLogs = [TimeLog(9:00-12:00), TimeLog(13:00-17:15)] // 7h 15min total work

Processing:

// Step 2: Expected maximum calculation
templateDuration = Duration.between(9:00, 17:00) = 8h
expectedMaxRegularHours = 8h - 30min = 7h 30min

// Step 3: Actual work calculation
actualWorkInRegular = calculateCumulativeRegularHours(sameDayLogs, context)
  Morning: 3h work - 0min breaks = 3h
  Afternoon: 4h 15min work - 0min breaks = 4h 15min
  Total: 7h 15min

// Step 4: Excess work calculation
excessWork = 7h 15min - 7h 30min = -15min (negative!)
excessWork.isNegative() = true ✓

// Employee worked LESS than expected, so no adjustment needed
logger.logDebug("Break threshold adjustment capped to ZERO: actualWork=7h15m <= expectedMax=7h30m")

Output: Duration.ZERO // No adjustment applied

Result: Employee gets full 7h 15min regular hours without break penalty
```

**Key Principle:** Never penalize employees who work at or below expected hours, even if they took fewer breaks than entitled.

---

### 7. `calculateBreakTimeThresholdAdjustmentWithRangeConstraint()` - Break Threshold Calculator
**Lines:** 541-572  
**Role:** Calculates break threshold adjustment using aggregated same-day break data

```java
Input:
  - TimeLog currentTimeLog (current interval being processed)
  - List<TimeLog> sameDayTimeLogs (all intervals for the day)
  - TimesheetSetting with breakTimeThreshold configuration
  - Range<LocalTime> regularHoursRange (template range constraint)
↓
Step 1: Validate inputs and get break threshold
        if (timeLog/settings/range null OR breakTimeThreshold null/zero) → return Duration.ZERO
↓
Step 2: Calculate aggregated break time within regular hours
        breakTimeWithinRegularHours = calculateAggregatedBreakTimeWithinRange(
            currentTimeLog, sameDayTimeLogs, regularHoursRange)
        Sums breaks across ALL same-day intervals within template boundaries
↓
Step 3: Calculate adjustment
        if (breakTimeWithinRegularHours null/zero) → adjustedThreshold = breakTimeThreshold
        else if (breakTimeThreshold <= breakTimeWithinRegularHours) → adjustedThreshold = Duration.ZERO
        else → adjustedThreshold = breakTimeThreshold - breakTimeWithinRegularHours
↓
Output: Duration of break threshold adjustment needed
```

**Aggregated Break Calculation Example:**
```java
Input:
  currentTimeLog = TimeLog(9:00-12:00, breaks=[Break(10:30-10:40)]) // Morning interval
  sameDayTimeLogs = [
    TimeLog(9:00-12:00, breaks=[Break(10:30-10:40)]),    // 10min morning break
    TimeLog(13:00-18:00, breaks=[Break(15:00-15:10)])    // 10min afternoon break
  ]
  breakTimeThreshold = Duration.ofMinutes(30) // Entitled to 30min breaks per day
  regularHoursRange = [9:00-17:00] // Template regular hours boundary

Processing:

// Step 2: Aggregate breaks across all same-day intervals
breakTimeWithinRegularHours = calculateAggregatedBreakTimeWithinRange(...)

// Process morning interval
morningBreaks = calculateBreakTimeWithinRange(timeLog1, [9:00-17:00])
  Break(10:30-10:40) ∩ [9:00-17:00] = [10:30-10:40] = 10min

// Process afternoon interval  
afternoonBreaks = calculateBreakTimeWithinRange(timeLog2, [9:00-17:00])
  Break(15:00-15:10) ∩ [9:00-17:00] = [15:00-15:10] = 10min

// Total aggregated breaks
breakTimeWithinRegularHours = 10min + 10min = 20min

// Step 3: Calculate adjustment
breakTimeThreshold = 30min
actualBreakTime = 20min
30min > 20min ✓ (threshold exceeds actual)
adjustedThreshold = 30min - 20min = 10min

Output: Duration.ofMinutes(10) // 10min adjustment needed

Interpretation: Employee took 20min breaks but is entitled to 30min,
so deduct 10min from regular hours end to compensate for missing break time.
```

---

### 8. `adjustTimeRangesFromEnd()` - Precise Range Trimming
**Lines:** 659-696  
**Role:** Removes specified duration from the end of time ranges with reverse-order processing

```java
Input:
  - RangeSet<LocalTime> availableTimeRanges to trim
  - Duration adjustment to remove from end
↓
Step 1: Validate inputs
        if (ranges null/empty OR adjustment.isZero()) → return original ranges
↓
Step 2: Convert to list and initialize processing
        List<Range<LocalTime>> rangeList = new ArrayList<>(ranges.asRanges())
        RangeSet<LocalTime> adjustedRanges = TreeRangeSet.create()
        Duration remainingAdjustment = adjustment
↓
Step 3: Process ranges in REVERSE order (latest first)
        for (i = rangeList.size() - 1; i >= 0; i--):
        a. If no remaining adjustment → Add range unchanged
        b. Calculate range duration
        c. If range duration <= remaining adjustment → Remove entire range
        d. If range duration > remaining adjustment → Trim range from end
        e. Update remaining adjustment
↓
Output: New RangeSet with specified duration removed from chronological end
```

**Reverse-Order Trimming Example:**
```java
Input:
  availableTimeRanges = RangeSet containing [9:00-12:00, 13:00-17:00]
  adjustment = Duration.ofMinutes(90) // Remove 1.5 hours from end

Processing:

// Convert to list (maintains chronological order)
rangeList = [
  Range[9:00-12:00],   // 3 hours (index 0)
  Range[13:00-17:00]   // 4 hours (index 1)
]

// Process in reverse order (i = 1, then i = 0)
adjustedRanges = TreeRangeSet.create()
remainingAdjustment = Duration.ofMinutes(90) // 1.5h

// Process Range[13:00-17:00] (index 1, latest range)
rangeDuration = Duration.between(13:00, 17:00) = 4h = 240min
remainingAdjustment = 90min
90min < 240min ✓ (trim range, don't remove entirely)

// Trim from end: 17:00 - 90min = 15:30
adjustedEnd = 17:00.minus(Duration.ofMinutes(90)) = 15:30
15:30.isAfter(13:00) ✓ (valid range)
adjustedRanges.add(Range[13:00-15:30])
remainingAdjustment = 90min - 90min = 0min

// Process Range[9:00-12:00] (index 0, earlier range)
remainingAdjustment = 0min ✓ (no adjustment left)
adjustedRanges.add(Range[9:00-12:00]) // Add unchanged

Final Result:
adjustedRanges = RangeSet containing [9:00-12:00, 13:00-15:30]

Output: RangeSet with 1.5h removed from chronological end
```

**Why Reverse Order?** Trimming from the chronological end (latest time) preserves earlier work periods and aligns with business logic that overtime/adjustments typically affect end-of-day work.

---

## Advanced Multi-Interval Scenarios

### Complex Same-Day Processing
```java
Scenario: Monday with 3 intervals + varying break patterns
  Interval 1: 8:00-11:00 (3h, 10min break)
  Interval 2: 12:00-15:00 (3h, 20min break)  
  Interval 3: 16:00-19:00 (3h, 5min break)
  Total work: 8h 25min, Total breaks: 35min
  Break threshold: 30min, Daily OT threshold: 8h

Template: Monday 9:00-17:00 (8h regular hours boundary)

Processing Flow:

// Step 1: Calculate cumulative regular hours within template
Interval 1: [8:00-11:00] ∩ [9:00-17:00] = [9:00-11:00] = 2h - 10min breaks = 1h 50min
Interval 2: [12:00-15:00] ∩ [9:00-17:00] = [12:00-15:00] = 3h - 20min breaks = 2h 40min
Interval 3: [16:00-19:00] ∩ [9:00-17:00] = [16:00-17:00] = 1h - 5min breaks = 55min
Total within template: 1h 50min + 2h 40min + 55min = 5h 25min

// Step 2: Break threshold adjustment
actualBreaks = 35min (across all intervals)
breakThreshold = 30min
35min > 30min ✓ (no adjustment needed, employee took sufficient breaks)
adjustment = Duration.ZERO

// Step 3: Daily OT limiting
totalRegularHours = 5h 25min (within template)
dailyOTThreshold = 8h
5h 25min < 8h ✓ (no limiting needed)

// Step 4: Available for other rules
Work beyond template: [8:00-9:00] + [17:00-19:00] = 1h + 2h = 3h
This 3h is available for Daily OT rule to evaluate

Result: 5h 25min regular hours + 3h available for Daily OT
```

### Break Threshold Edge Cases
```java
Edge Case 1: No breaks taken, high threshold
  actualBreaks = 0min
  breakThreshold = 60min
  rawAdjustment = 60min - 0min = 60min
  
  // Capping logic prevents excessive adjustment
  actualWork = 7h 30min, expectedMax = 7h (8h - 60min)
  excessWork = 7h 30min - 7h = 30min
  cappedAdjustment = min(60min, 30min) = 30min
  
  Result: Only 30min adjustment applied (reasonable compensation)

Edge Case 2: Breaks exceed threshold
  actualBreaks = 90min
  breakThreshold = 30min
  90min > 30min ✓ (employee took more breaks than entitled)
  adjustment = Duration.ZERO
  
  Result: No adjustment needed, employee already took sufficient breaks
```

---

## Integration with Rule Engine Context

### Context Communication Pattern
```java
// Before Regular Hours Rule execution
TimeRangeResolverContext context = {
  occupiedTimeRanges: [],
  adjustedRegularHoursBreakThreshold: null,
  sameDayTimeLogs: [morning, afternoon],
  currentTimesheetSetting: {breakTimeThreshold: 30min}
}

// After Regular Hours Rule execution
context = {
  occupiedTimeRanges: [], // Still empty (first rule)
  adjustedRegularHoursBreakThreshold: Duration.ofMinutes(15), // Set for Daily OT
  sameDayTimeLogs: [morning, afternoon], // Unchanged
  currentTimesheetSetting: {breakTimeThreshold: 30min} // Unchanged
}

// Daily OT Rule uses the break threshold adjustment
dailyOTResolver.resolveTimeRange(context) {
  // Gets accurate effective working time calculation
  adjustedDailyTime = context.getAdjustedDailyEffectiveWorkingTime()
  // This includes the 15min break threshold adjustment
}
```

### Rule Sequence Coordination
```java
1. Regular Hours Rule (THIS CLASS)
   ↓ Processes: Template work schedule
   ↓ Claims: Base work time with break/OT adjustments
   ↓ Sets: adjustedRegularHoursBreakThreshold in context
   ↓ Result: [9:00-16:45] (7h 45min after 15min break adjustment)

2. Break Rule
   ↓ Processes: Actual break intervals or dynamic allocation
   ↓ Claims: [12:00-12:30, 15:00-15:15] (45min actual breaks)
   ↓ Updates: occupiedTimeRanges with break periods
   ↓ Result: Break time ranges for payment calculation

3. Daily OT Rule
   ↓ Processes: Time beyond daily threshold
   ↓ Uses: adjustedRegularHoursBreakThreshold for accurate calculation
   ↓ Claims: [16:45-17:00] if daily threshold exceeded
   ↓ Result: Daily overtime time ranges

4. Weekly OT Rule
   ↓ Processes: Time beyond weekly threshold
   ↓ Claims: Remaining unclaimed time based on weekly totals
   ↓ Result: Weekly overtime time ranges
```

---

## Performance and Optimization

### Computational Complexity
- **Template lookup:** O(1) - Direct map access by WorkDay
- **Range operations:** O(log n) - Guava TreeRangeSet operations
- **Break calculations:** O(m × k) - m break intervals × k same-day logs
- **Multi-interval processing:** O(k) - k intervals per day
- **Overall complexity:** O(k × m × log n) - Scales well for typical scenarios

### Memory Efficiency
- **Range reuse:** Guava Range objects are immutable and efficiently shared
- **Context sharing:** Single context object passed through all operations
- **Minimal allocations:** Reuses Duration and LocalTime objects where possible
- **Garbage collection:** Short-lived temporary objects for calculations

### Optimization Strategies
1. **Early termination:** DAY_OFF and invalid template checks prevent unnecessary processing
2. **Efficient aggregation:** Single-pass calculation for multi-interval scenarios
3. **Range intersection caching:** Reuses intersection results within same calculation
4. **Lazy evaluation:** Only calculates adjustments when thresholds are configured

---

## Error Handling and Resilience

### Input Validation
```java
// DAY_OFF handling
if (dayType == WorkDayType.DAY_OFF) {
    return TreeRangeSet.create(); // Graceful empty result
}

// Template validation
if (workStartTime == null || workEndTime == null || !workStartTime.isBefore(workEndTime)) {
    return TreeRangeSet.create(); // Handle misconfigured templates
}

// Break interval validation
if (breakStart == null || breakEnd == null || !breakStart.isBefore(breakEnd)) {
    continue; // Skip invalid breaks, process remaining
}
```

### Defensive Programming
```java
// Null-safe operations
Duration totalBreak = (breakTime != null) ? breakTime : Duration.ZERO;

// Negative duration prevention
if (intervalRegularHours.isNegative()) {
    intervalRegularHours = Duration.ZERO; // Prevent negative regular hours
}

// Range boundary checks
if (adjustedEnd.isAfter(rangeStart)) {
    adjustedRanges.add(TimeHelper.toRange(rangeStart, adjustedEnd));
}
```

### Comprehensive Logging
```java
// Break threshold adjustment logging
logger.logDebug("Applied break time threshold adjustment: %s", adjustment);

// Daily OT limiting logging
logger.logDebug("Limited regular hours: total=%s, threshold=%s, overtime=%s", 
                totalRegularHours, dailyOTThreshold, potentialOvertime);

// Capping logic logging
logger.logDebug("Break threshold adjustment capped: raw=%s, capped=%s", 
                rawAdjustment, cappedAdjustment);
```

---

## Testing Strategies

### Unit Test Categories

#### 1. Core Flow Testing
```java
@Test
void testBasicRegularHoursCalculation() {
    // Single interval, no adjustments
    // Template: 9:00-17:00, Work: 9:00-17:00
    // Expected: [9:00-17:00] (8 hours)
}

@Test
void testDAYOFFHandling() {
    // DAY_OFF work day type
    // Expected: Empty RangeSet
}

@Test
void testInvalidTemplateHandling() {
    // Template with start > end or null times
    // Expected: Empty RangeSet
}
```

#### 2. Break Threshold Testing
```java
@Test
void testBreakThresholdAdjustment() {
    // Break threshold: 30min, Actual breaks: 15min
    // Expected: 15min deduction from regular hours end
}

@Test
void testBreakThresholdCapping() {
    // Excessive adjustment that would create negative regular hours
    // Expected: Capped adjustment to prevent negative hours
}

@Test
void testMultiIntervalBreakAggregation() {
    // Multiple intervals with breaks across the day
    // Expected: Aggregated break calculation for threshold
}
```

#### 3. Multi-Interval Testing
```java
@Test
void testMultiIntervalCumulativeCalculation() {
    // Morning + afternoon intervals
    // Expected: Correct aggregation across intervals
}

@Test
void testDailyOTLimitingOnLastInterval() {
    // Multi-interval day exceeding daily OT threshold
    // Expected: Limiting applied only on last interval
}

@Test
void testComplexMultiIntervalScenario() {
    // 3+ intervals with varying break patterns
    // Expected: Accurate processing across all intervals
}
```

#### 4. Edge Case Testing
```java
@Test
void testEmptyBreakIntervals() {
    // Time log with no break intervals
    // Expected: Graceful handling, no break calculations
}

@Test
void testBreaksOutsideTemplate() {
    // Breaks that fall outside regular hours template
    // Expected: Only breaks within template considered
}

@Test
void testZeroDurationRanges() {
    // Edge case with zero-duration work or break periods
    // Expected: Proper filtering and handling
}
```

### Integration Test Scenarios
```java
@Test
void testFullRuleSequenceIntegration() {
    // Regular Hours → Break → Daily OT → Weekly OT
    // Verify context passing and rule coordination
}

@Test
void testMultiDayWeeklyProcessing() {
    // Multiple days with varying patterns
    // Verify weekly aggregation and rule interactions
}
```

---

## Common Issues and Troubleshooting

### Issue 1: Incorrect Multi-Interval Regular Hours Totals
**Symptoms:**
- Regular hours exceed daily OT threshold across intervals
- Daily OT rule not triggering when expected
- Inconsistent totals between intervals

**Root Causes:**
1. Not using `calculateCumulativeRegularHours()` for multi-interval aggregation
2. Double-counting break threshold adjustment across intervals
3. Incorrect template range intersection calculations

**Diagnosis Steps:**
```java
// Check cumulative calculation
Duration totalRegular = calculateCumulativeRegularHours(sameDayLogs, context);
// Expected: Accurate total across all intervals

// Verify break threshold handling
Duration breakAdjustment = context.getAdjustedRegularHoursBreakThreshold();
// Expected: Applied once per day, not per interval

// Check template intersection
Range<LocalTime> intersection = workRange.intersection(templateRange);
// Expected: Only work within template counts as regular hours
```

**Solutions:**
1. **Use aggregation methods:** Always use `calculateCumulativeRegularHours()` for multi-interval scenarios
2. **Single adjustment application:** Apply break threshold adjustment once per day, not per interval
3. **Template constraint:** Ensure work ranges are properly intersected with template boundaries

### Issue 2: Break Threshold Adjustment Creating Negative Regular Hours
**Symptoms:**
- Regular hours duration becomes negative or zero unexpectedly
- Excessive break threshold adjustments applied
- Employee penalized despite working expected hours

**Root Cause:**
- Break threshold adjustment not properly capped by `capBreakThresholdAdjustment()`

**Solution:**
```java
// Implement proper capping logic
Duration cappedAdjustment = capBreakThresholdAdjustment(rawAdjustment, context, ...);
// Ensures adjustment never exceeds reasonable limits

// Verify expected vs actual work calculation
Duration expectedMax = templateDuration.minus(breakThreshold);
Duration actualWork = calculateCumulativeRegularHours(sameDayLogs, context);
// Only apply adjustment if actualWork > expectedMax
```

### Issue 3: Daily OT Limiting Applied to Wrong Interval
**Symptoms:**
- Regular hours limited on morning interval instead of afternoon
- Inconsistent regular hours across same-day intervals
- Daily OT rule receives incorrect available time

**Root Cause:**
- Not checking `isLastIntervalOfDay()` before applying daily OT limiting

**Solution:**
```java
// Check interval position before limiting
boolean shouldLimit = (sameDayTimeLogs.size() <= 1) 
    || isLastIntervalOfDay(currentTimeLog, sameDayTimeLogs);

if (shouldLimit) {
    // Apply limiting only on last interval
    return adjustTimeRangesFromEnd(availableTimeRanges, potentialOvertime);
}
return availableTimeRanges; // No limiting on non-last intervals
```

### Issue 4: Context Not Updated for Downstream Rules
**Symptoms:**
- Daily OT rule calculations incorrect
- Break threshold adjustment not reflected in Daily OT
- Inconsistent effective working time calculations

**Root Cause:**
- Not setting `adjustedRegularHoursBreakThreshold` in context

**Solution:**
```java
// Set context for downstream rules
context.setAdjustedRegularHoursBreakThreshold(breakThresholdAdjustment);

// Verify context usage in Daily OT rule
Duration adjustedDaily = context.getAdjustedDailyEffectiveWorkingTime();
// Should include break threshold adjustment
```

---

## Related Files and Dependencies

### Core Dependencies
- **TimeHelper.java** - Time calculation utilities, range operations, and multi-interval support
- **TimeRangeResolverContext.java** - Context object with evaluation state and configuration
- **ICustomRuleTimeRangeResolver.java** - Interface contract for rule resolvers

### Related Resolvers (Same Package)
- **RangeBasedBreakRuleTimeRangeResolver.java** - Processes actual break intervals
- **RangeBasedDailyOvertimeRuleRangeResolver.java** - Uses break threshold from this class
- **RangeBasedWeeklyOvertimeRuleTimeRangeResolver.java** - Weekly overtime calculations

### DTOs and Configuration
- **TimeLog.java** - Time log data with break intervals and effective times
- **TemplateWorkDay.java** - Template work schedule configuration
- **TimesheetSetting.java** - Break thresholds and timesheet configuration
- **CustomRule.java** - Daily overtime rule configuration and thresholds

### Rule Engine Components
- **BaseRuleEvaluator.java** - Main rule evaluation orchestrator
- **UnifiedRuleManager.java** - Rule precedence and ordering management
- **IRuleFactory.java** - Factory for creating rule resolvers and evaluators

---

## Summary

The **RangeBasedRegularHoursRuleTimeRangeResolver** serves as the sophisticated foundation of the range-based rule engine, providing:

1. **Template-Based Foundation** - Transforms configured work schedules into claimable time ranges
2. **Multi-Interval Intelligence** - Handles complex same-day work patterns with accurate aggregation
3. **Break Threshold Integration** - Ensures fair compensation for entitled break time
4. **Daily OT Coordination** - Sets up proper limiting and context for overtime calculations
5. **Robust Error Handling** - Gracefully manages edge cases and invalid configurations
6. **Performance Optimization** - Efficient processing with minimal memory overhead

**Key Innovations:**
- **Aggregated Multi-Interval Processing:** Accurately calculates regular hours across multiple same-day work periods
- **Intelligent Break Threshold System:** Compensates employees for entitled break time while preventing excessive adjustments
- **Last-Interval Daily OT Limiting:** Prevents double-limiting across intervals while ensuring proper overtime setup
- **Context Communication:** Passes critical adjustment information to downstream rules for accurate calculations

**Architectural Benefits:**
- **Foundation Role:** Establishes the baseline for all subsequent rule evaluations
- **Business Rule Compliance:** Ensures template work schedules and break policies are properly enforced
- **Multi-Interval Support:** Handles modern flexible work arrangements with multiple daily work periods
- **Accurate Calculations:** Provides precise regular hours calculation that serves as the foundation for overtime rules

This class transforms configured work policies into actual claimable time ranges while respecting complex business rules, multi-interval work patterns, and employee entitlements, creating a robust foundation for the entire rule evaluation system.