# RangeBasedRuleEvaluator - Complete End-to-End Flow

## Overview
**Location:** `src/main/java/io/recruitcrm/microservice/timesheet/rule_engine/rules/RangeBasedRuleEvaluator.java`

**Purpose:** Specialized rule evaluator that handles timesheets where employees log **start and end times** for their work (not just total hours). Supports **multiple work intervals per day** through sophisticated database integration and time log expansion, enabling accurate rule evaluation for complex work schedules.

**Key Innovation:** Transforms a single database time log record (with multiple intervals stored separately) into individual evaluable time log DTOs, processes each interval through the complete rule engine, then intelligently aggregates results while maintaining daily and weekly context.

---

## Real-World Example

**Scenario:** Sarah works Monday, January 15th with a split schedule:
- **Morning shift:** 8:00 AM - 12:00 PM (4 hours, 15-min coffee break at 10:00-10:15)
- **Lunch break:** 12:00 PM - 1:00 PM (1 hour unpaid)
- **Afternoon shift:** 1:00 PM - 6:00 PM (5 hours, 15-min coffee break at 3:30-3:45)
- **Total work:** 8.5 hours, **Total breaks:** 30 minutes

**Database Structure:**

**Table: `cst_time_log_t`** (Main time log record)
```sql
id  | date       | candidate_id | job_id | timesheet_id | ...
150 | 2024-01-15 | 789         | 456    | 100          | ...
```

**Table: `cst_time_log_interval_t`** (Detailed work intervals)
```sql
id | time_log_id | work_start_time | work_end_time | break_interval
1  | 150         | 28800 (8:00)    | 43200 (12:00) | [{"id":1,"breakStartTime":36000,"breakEndTime":36900}]
2  | 150         | 46800 (13:00)   | 64800 (18:00) | [{"id":2,"breakStartTime":55800,"breakEndTime":56700}]
```

**Transformation Process:**
1. **Single database row** → **2 expanded time log DTOs**
2. **Each interval evaluated separately** by rule engine
3. **Results aggregated** per day with shared daily state
4. **Accurate overtime calculations** across intervals

**Final Result:** 8 hours regular + 0.5 hours daily OT = $212.50 pay, $340 bill

---

## Architecture Overview

### Rule Evaluation Strategy
```
┌─────────────────────────────────────────────────────────────┐
│ START_AND_END_TIME Timesheet (workLogType = 2)             │
└─────────────────┬───────────────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────────────┐
│ RangeBasedRuleEvaluator.evaluateRules()                    │
│ ├─ Detects workLogType = START_AND_END_TIME                │
│ └─ Routes to evaluateRulesWithIntervals()                  │
└─────────────────┬───────────────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────────────┐
│ Interval Expansion & Database Integration                   │
│ ├─ Query cst_time_log_interval_t for detailed intervals    │
│ ├─ Parse JSON break intervals                              │
│ ├─ Create individual TimeLog DTOs per interval             │
│ └─ Maintain parent time log context (candidate, job, etc.) │
└─────────────────┬───────────────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────────────┐
│ Standard Rule Engine Processing                             │
│ ├─ Weekly splitting with custom start days                 │
│ ├─ Daily state management across same-day intervals        │
│ ├─ Rule sequence: Regular Hours → Break → Daily OT → Weekly OT │
│ └─ Result aggregation and post-processing                  │
└─────────────────┬───────────────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────────────┐
│ WeeklyRuleEvaluatorResult                                   │
│ Complete pay/bill calculations with interval-aware logic   │
└─────────────────────────────────────────────────────────────┘
```

### Key Architectural Components
- **Interval Repository Integration** - Fetches detailed work intervals from database
- **JSON Break Parsing** - Converts stored break data to rule engine DTOs
- **Time Log Expansion** - Transforms 1 database record → N evaluable DTOs
- **Context Preservation** - Maintains parent time log metadata across intervals
- **Backwards Compatibility** - Falls back to legacy time logs when no intervals exist

---

## Complete Function Flow

### Visual Flow Diagram
```
┌─────────────────────────────────────────────────────────────┐
│  1. evaluateRules()                                         │
│     Entry Point - Strategy selection based on workLogType   │
└────────────┬────────────────────────────────────────────────┘
             │
             ├─ workLogType == START_AND_END_TIME? ────────────┐
             │                                                │
             ├─ Other types? → super.evaluateRules() ─────────┤
             │                                                │
┌────────────▼────────────────────────────────────────────────▼┐
│  2. evaluateRulesWithIntervals()                            │
│     Orchestrates interval expansion and rule evaluation     │
└────────────┬────────────────────────────────────────────────┘
             │
┌────────────▼─────────────────────────────────────────────────┐
│  3. fetchTimeLogsFromIntervals()                            │
│     Database integration - fetches and expands intervals    │
│     ├─ Extract time log IDs from timesheet                  │
│     ├─ Query timeLogIntervalRepository.findIntervalsByIds() │
│     ├─ Group intervals by time log ID                       │
│     └─ Process each time log + its intervals                │
└────────────┬────────────────────────────────────────────────┘
             │
             ├─ For each TimeLog with intervals ──────────────┐
             │                                                │
┌────────────▼────────────────────────────────────────────────▼┐
│  4. createTimeLogFromInterval() [Called per interval]       │
│     Converts single interval + parent data → complete DTO   │
│     ├─ Map parent time log entity to DTO                    │
│     ├─ Override start/end times from interval               │
│     ├─ Calculate work duration from interval times          │
│     ├─ Parse break intervals from JSON                      │
│     └─ Set break time and validate result                   │
└────────────┬────────────────────────────────────────────────┘
             │
             ├──────────────────────────────────┐
             │                                  │
┌────────────▼────────────────────┐  ┌─────────▼──────────────┐
│  5. parseBreakIntervalsFromJson()│  │ 6. calculateTotal      │
│     JSON → TimeLogBreakInterval  │  │    BreakDuration()     │
│     ├─ Parse JSON array          │  │ Sums break durations  │
│     ├─ Convert seconds to times  │  │ ├─ Validate intervals │
│     ├─ Validate break periods    │  │ ├─ Calculate durations│
│     └─ Create DTO objects        │  │ └─ Sum valid breaks   │
└──────────────────────────────────┘  └────────────────────────┘
             │
             │ (All intervals expanded into DTOs)
             │
┌────────────▼─────────────────────────────────────────────────┐
│  7. evaluateWithExpandedTimeLogs()                          │
│     Standard rule engine processing with expanded time logs │
│     ├─ Validate timesheet                                   │
│     ├─ Split expanded logs into weekly groups               │
│     ├─ For each week: evaluate all rules in sequence        │
│     ├─ Daily state management across same-day intervals     │
│     └─ Post-process and aggregate results                   │
└────────────┬────────────────────────────────────────────────┘
             │
┌────────────▼─────────────────────────────────────────────────┐
│  WeeklyRuleEvaluatorResult                                  │
│     Final output with complete pay/bill calculations        │
│     ├─ Regular hours across all intervals                   │
│     ├─ Break time from actual recorded intervals            │
│     ├─ Daily OT calculated with interval-aware logic        │
│     └─ Weekly OT with proper candidate aggregation          │
└─────────────────────────────────────────────────────────────┘
```

---

## Function-by-Function Breakdown

### 1. `evaluateRules(Timesheet timesheet)` - Strategy Selection Entry Point
**Lines:** 53-64  
**Role:** Determines evaluation strategy based on timesheet work log type configuration

```java
Input: Timesheet object with timesheetSetting configuration
↓
Step 1: Extract workLogType from timesheet settings
        Integer workLogType = timesheet.getTimesheetSetting().getWorkLogType()
↓
Step 2: Strategy decision based on work log type
        if (workLogType == WorkLogType.START_AND_END_TIME.getTypeId()) // Value: 2
            → Call evaluateRulesWithIntervals() [NEW interval-based path]
        else
            → Call super.evaluateRules() [LEGACY single-entry path]
↓
Output: WeeklyRuleEvaluatorResult with appropriate processing strategy
```

**Strategy Selection Logic:**
```java
// START_AND_END_TIME (Type 2): Multi-interval support
WorkLogType.START_AND_END_TIME.getTypeId() = 2
→ Employees log specific start/end times for work periods
→ Supports multiple intervals per day (morning + afternoon)
→ Requires interval expansion from cst_time_log_interval_t
→ Uses evaluateRulesWithIntervals()

// DURATION (Type 1): Legacy single-entry support  
WorkLogType.DURATION.getTypeId() = 1
→ Employees log total hours worked per day
→ Single work period per day
→ Uses existing time log data from cst_time_log_t
→ Uses super.evaluateRules() (BaseRuleEvaluator)
```

**Real Example:**
```java
// Scenario 1: Modern interval-based timesheet
Timesheet timesheet1 = {
  timesheetSetting: {
    workLogType: 2  // START_AND_END_TIME
  }
}
evaluateRules(timesheet1)
→ workLogType == 2 ✓
→ Calls evaluateRulesWithIntervals()
→ Fetches intervals from cst_time_log_interval_t
→ Expands to multiple time logs per day

// Scenario 2: Legacy duration-based timesheet
Timesheet timesheet2 = {
  timesheetSetting: {
    workLogType: 1  // DURATION
  }
}
evaluateRules(timesheet2)
→ workLogType != 2 ✓
→ Calls super.evaluateRules()
→ Uses existing time log data as-is
→ Single evaluation per time log
```

---

### 2. `evaluateRulesWithIntervals(Timesheet timesheet)` - Interval Processing Orchestrator
**Lines:** 77-90  
**Role:** Coordinates the complete interval expansion and rule evaluation process

```java
Input: Timesheet with time logs that need interval expansion
↓
Step 1: Extract time logs from timesheet
        List<TimeLog> timeLogs = timesheet.getTimeLogs()
        These are entity objects from cst_time_log_t table
↓
Step 2: Expand time logs using interval data
        expandedTimeLogs = fetchTimeLogsFromIntervals(timeLogs)
        Queries cst_time_log_interval_t and creates individual DTOs
↓
Step 3: Log expansion results for debugging
        "Expanded {original} time logs to {expanded} interval-based time logs"
        Helps track interval expansion in production
↓
Step 4: Evaluate using standard rule engine
        return evaluateWithExpandedTimeLogs(timesheet, expandedTimeLogs)
        Uses parent class logic with pre-expanded time logs
↓
Output: WeeklyRuleEvaluatorResult with interval-aware calculations
```

**Expansion Example:**
```java
Input Timesheet:
  - TimeLog(id=100, date=Jan15) // Monday
  - TimeLog(id=101, date=Jan16) // Tuesday  
  - TimeLog(id=102, date=Jan17) // Wednesday
  Total: 3 time logs from database

After fetchTimeLogsFromIntervals():
  - TimeLog(id=100, date=Jan15, 8:00-12:00)  // Monday morning
  - TimeLog(id=100, date=Jan15, 13:00-18:00) // Monday afternoon
  - TimeLog(id=101, date=Jan16, 9:00-17:00)  // Tuesday full day
  - TimeLog(id=102, date=Jan17, 8:30-12:30)  // Wednesday morning
  - TimeLog(id=102, date=Jan17, 13:30-17:30) // Wednesday afternoon
  Total: 5 expanded time logs for rule evaluation

Logging Output:
"Expanded 3 time logs to 5 interval-based time logs for timesheet 500"

After evaluateWithExpandedTimeLogs():
  - Complete rule evaluation for all 5 intervals
  - Daily state shared between same-day intervals
  - Results aggregated per day and week
```

---

### 3. `fetchTimeLogsFromIntervals(List<TimeLog> timeLogs)` - Database Integration
**Lines:** 99-139  
**Role:** Queries database for intervals and transforms them into evaluable time log DTOs

```java
Input: List<TimeLog> entities from cst_time_log_t (parent records)
↓
Step 1: Extract time log IDs for database query
        List<Integer> timeLogIds = timeLogs.stream().map(TimeLog::getId).toList()
        Example: [100, 101, 102, 103, 104]
↓
Step 2: Query interval repository for all intervals
        Map<Integer, List<TimeLogIntervalDto>> intervalsByTimeLogId = 
            timeLogIntervalRepository.findIntervalsByTimeLogIds(timeLogIds)
        Single efficient query to avoid N+1 problem
↓
Step 3: Process each parent time log
        For each TimeLog entity:
        a. Check if intervals exist for this time log ID
        b. If intervals found → Create DTO for EACH interval
        c. If no intervals → Use original time log (backwards compatibility)
        d. Validate duration and skip zero-duration logs
↓
Step 4: Build expanded time log list
        List<TimeLog> expandedTimeLogs = new ArrayList<>()
        Each valid interval becomes a separate evaluable time log DTO
↓
Output: Expanded list of time log DTOs ready for rule evaluation
```

**Detailed Database Integration Example:**
```java
// Input: Parent time logs from cst_time_log_t
List<TimeLog> timeLogs = [
  TimeLog(id=100, date=Jan15, candidate=789, job=456),
  TimeLog(id=101, date=Jan16, candidate=789, job=456),
  TimeLog(id=102, date=Jan17, candidate=789, job=456)
]

// Step 1: Extract IDs
timeLogIds = [100, 101, 102]

// Step 2: Database query result
Map<Integer, List<TimeLogIntervalDto>> intervalsByTimeLogId = {
  100 → [
    TimeLogIntervalDto(id=1, timeLogId=100, workStartTime=28800, workEndTime=43200, 
                       breakInterval='[{"id":1,"breakStartTime":36000,"breakEndTime":36900}]'),
    TimeLogIntervalDto(id=2, timeLogId=100, workStartTime=46800, workEndTime=64800,
                       breakInterval='[{"id":2,"breakStartTime":55800,"breakEndTime":56700}]')
  ],
  101 → [
    TimeLogIntervalDto(id=3, timeLogId=101, workStartTime=32400, workEndTime=61200,
                       breakInterval='[{"id":3,"breakStartTime":43200,"breakEndTime":45000}]')
  ],
  102 → [] // No intervals found - will use original time log
}

// Step 3: Process each parent time log

// Process TimeLog(id=100) - Has 2 intervals
intervals = intervalsByTimeLogId.get(100) // 2 intervals found
for (TimeLogIntervalDto interval : intervals) {
  // Call createTimeLogFromInterval(timeLog, interval1)
  mappedTimeLog1 = TimeLog(id=100, date=Jan15, start=8:00, end=12:00, breaks=[10:00-10:15])
  if (duration > 0) expandedTimeLogs.add(mappedTimeLog1)
  
  // Call createTimeLogFromInterval(timeLog, interval2)  
  mappedTimeLog2 = TimeLog(id=100, date=Jan15, start=13:00, end=18:00, breaks=[15:30-15:45])
  if (duration > 0) expandedTimeLogs.add(mappedTimeLog2)
}

// Process TimeLog(id=101) - Has 1 interval
intervals = intervalsByTimeLogId.get(101) // 1 interval found
mappedTimeLog3 = TimeLog(id=101, date=Jan16, start=9:00, end=17:00, breaks=[12:00-12:30])
if (duration > 0) expandedTimeLogs.add(mappedTimeLog3)

// Process TimeLog(id=102) - No intervals (backwards compatibility)
intervals = intervalsByTimeLogId.get(102) // null or empty
mappedTimeLog4 = RuleEngineTimeLogMapper.INSTANCE.toTimeLog(timeLog102)
duration = TimeHelper.calculateTimeLogDuration(mappedTimeLog4)
if (duration > 0) {
  expandedTimeLogs.add(mappedTimeLog4) // Use original time log
} else {
  logger.logWarn("Skipping timelog with ID 102 due to invalid time data")
}

// Final Result
List<TimeLog> expandedTimeLogs = [
  TimeLog(id=100, date=Jan15, start=8:00, end=12:00, breaks=[10:00-10:15]),    // Interval 1
  TimeLog(id=100, date=Jan15, start=13:00, end=18:00, breaks=[15:30-15:45]),   // Interval 2
  TimeLog(id=101, date=Jan16, start=9:00, end=17:00, breaks=[12:00-12:30]),    // Interval 3
  TimeLog(id=102, date=Jan17, start=9:00, end=17:00, breaks=[])                // Original (no intervals)
]
```

**Backwards Compatibility Handling:**
- **No intervals found** → Uses original time log from cst_time_log_t
- **Zero duration intervals** → Skipped with warning log
- **Mixed scenarios** → Some time logs expanded, others use original data
- **Seamless operation** → Works with both new and legacy data

---

### 4. `createTimeLogFromInterval(TimeLog timeLog, TimeLogIntervalDto interval)` - DTO Construction
**Lines:** 148-196  
**Role:** Converts a single interval + parent time log data into a complete evaluable time log DTO

```java
Input: 
  - TimeLog entity (parent record from cst_time_log_t)
  - TimeLogIntervalDto (single work interval from cst_time_log_interval_t)
↓
Step 1: Map parent entity to base DTO
        mappedTimeLog = RuleEngineTimeLogMapper.INSTANCE.toTimeLog(timeLog)
        Copies: id, date, candidate, job, rates, etc. from parent
↓
Step 2: Override start/end times from interval
        intervalStartTime = LocalTime.ofSecondOfDay(interval.getWorkStartTime())
        intervalEndTime = LocalTime.ofSecondOfDay(interval.getWorkEndTime())
        Set both workStartTime and normalizedWorkStartTime for consistency
↓
Step 3: Calculate work duration from interval times
        intervalDuration = TimeHelper.calculateDuration(start, end)
        mappedTimeLog.setWorkTime(intervalDuration)
↓
Step 4: Parse break intervals from JSON
        breakIntervals = parseBreakIntervalsFromJson(interval.getBreakInterval(), timeLogId)
        Converts JSON string to List<TimeLogBreakInterval> DTOs
↓
Step 5: Calculate and set total break time
        if (breakIntervals not empty) {
          totalBreakDuration = calculateTotalBreakDuration(breakIntervals)
          mappedTimeLog.setBreakIntervals(breakIntervals)
          mappedTimeLog.setBreakTime(totalBreakDuration)
        } else {
          mappedTimeLog.setBreakTime(Duration.ZERO)
        }
↓
Step 6: Validate and return result
        if (intervalDuration > 0) → return complete DTO
        else → log warning and return null (invalid interval)
↓
Output: Complete TimeLog DTO ready for rule evaluation, or null if invalid
```

**Comprehensive Construction Example:**
```java
// Input Data
TimeLog parentEntity = TimeLog(
  id=150,
  date=LocalDate.of(2024, 1, 15),  // Monday
  candidateId=789,
  jobId=456,
  timesheetId=100,
  payRate=BigDecimal.valueOf(25.00),
  billRate=BigDecimal.valueOf(40.00)
  // ... other parent fields
)

TimeLogIntervalDto interval = TimeLogIntervalDto(
  id=1,
  timeLogId=150,
  workStartTime=28800,    // 8:00 AM in seconds since midnight
  workEndTime=43200,      // 12:00 PM in seconds since midnight
  breakInterval='[{"id":1,"breakStartTime":36000,"breakEndTime":36900}]'
  // 36000 = 10:00 AM, 36900 = 10:15 AM (15-minute break)
)

// Step-by-Step Processing

// Step 1: Map parent to base DTO
TimeLog mappedTimeLog = RuleEngineTimeLogMapper.INSTANCE.toTimeLog(parentEntity)
// Result: All parent fields copied (id=150, date=Jan15, candidate=789, etc.)

// Step 2: Override times from interval
intervalStartTime = LocalTime.ofSecondOfDay(28800)  // 08:00:00
intervalEndTime = LocalTime.ofSecondOfDay(43200)    // 12:00:00

mappedTimeLog.setWorkStartTime(LocalTime.of(8, 0))           // 08:00
mappedTimeLog.setNormalizedWorkStartTime(LocalTime.of(8, 0)) // For getEffectiveStartTime()
mappedTimeLog.setWorkEndTime(LocalTime.of(12, 0))            // 12:00
mappedTimeLog.setNormalizedWorkEndTime(LocalTime.of(12, 0))  // For getEffectiveEndTime()

// Step 3: Calculate work duration
intervalDuration = TimeHelper.calculateDuration(
  LocalTime.of(8, 0),   // start
  LocalTime.of(12, 0)   // end
) // Result: Duration.ofHours(4)
mappedTimeLog.setWorkTime(Duration.ofHours(4))

// Step 4: Parse break intervals (calls Function #5)
breakIntervals = parseBreakIntervalsFromJson(
  '[{"id":1,"breakStartTime":36000,"breakEndTime":36900}]',
  150  // timeLogId for logging
)
// Result: [TimeLogBreakInterval(id=1, timeLogId=150, start=10:00, end=10:15)]

// Step 5: Calculate break time (calls Function #6)
if (breakIntervals != null && !breakIntervals.isEmpty()) {
  mappedTimeLog.setBreakIntervals(breakIntervals)
  
  totalBreakDuration = calculateTotalBreakDuration(breakIntervals)
  // Result: Duration.ofMinutes(15)
  mappedTimeLog.setBreakTime(Duration.ofMinutes(15))
} else {
  mappedTimeLog.setBreakTime(Duration.ZERO)
}

// Step 6: Validate
if (!intervalDuration.isZero()) {  // 4 hours > 0 ✓
  return mappedTimeLog  // Valid interval
} else {
  logger.logWarn("Skipping interval for timeLog ID 150 due to invalid time data")
  return null  // Invalid interval
}

// Final Output: Complete TimeLog DTO
TimeLog(
  id=150,                                    // From parent
  date=LocalDate.of(2024, 1, 15),          // From parent
  candidateId=789,                          // From parent
  jobId=456,                                // From parent
  payRate=BigDecimal.valueOf(25.00),        // From parent
  billRate=BigDecimal.valueOf(40.00),       // From parent
  workStartTime=LocalTime.of(8, 0),         // From interval
  workEndTime=LocalTime.of(12, 0),          // From interval
  normalizedWorkStartTime=LocalTime.of(8, 0), // From interval
  normalizedWorkEndTime=LocalTime.of(12, 0),  // From interval
  workTime=Duration.ofHours(4),             // Calculated
  breakIntervals=[                          // Parsed from JSON
    TimeLogBreakInterval(id=1, timeLogId=150, start=10:00, end=10:15)
  ],
  breakTime=Duration.ofMinutes(15)          // Calculated
)
```

**Key Features:**
- **Parent Context Preservation** - Maintains candidate, job, rates from parent time log
- **Interval Time Override** - Uses specific interval start/end times for accurate calculations
- **Break Integration** - Parses and includes actual break periods from JSON
- **Validation** - Ensures only valid intervals (duration > 0) are processed
- **Consistency** - Sets both regular and normalized times for rule engine compatibility

---

### 5. `parseBreakIntervalsFromJson(String breakIntervalJson, Integer timeLogId)` - JSON Break Parser
**Lines:** 206-255  
**Role:** Converts JSON string from database into structured TimeLogBreakInterval DTOs

```java
Input: 
  - String breakIntervalJson (from cst_time_log_interval_t.break_interval column)
  - Integer timeLogId (for logging and DTO population)
↓
Step 1: Guard clause validation
        if (breakIntervalJson == null || breakIntervalJson.isBlank())
            return new ArrayList<>() // Empty list for no breaks
↓
Step 2: Parse JSON using Jackson ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper()
        JsonNode jsonArray = objectMapper.readTree(breakIntervalJson)
        Validate that root element is an array
↓
Step 3: Process each break interval in JSON array
        For each JsonNode in jsonArray:
        a. Extract fields with null safety:
           - id (optional, nullable)
           - breakStartTime (required, seconds since midnight)
           - breakEndTime (required, seconds since midnight)
        b. Convert seconds to LocalTime:
           LocalTime.ofSecondOfDay(breakStartTimeSeconds)
        c. Validate break interval:
           - Both times non-null
           - Start time before end time
        d. Create TimeLogBreakInterval DTO if valid
        e. Log warning and skip if invalid
↓
Step 4: Exception handling
        Catch any JSON parsing errors
        Log warning with time log ID and error message
        Return partial list (graceful degradation)
↓
Output: List<TimeLogBreakInterval> DTOs ready for rule evaluation
```

**Comprehensive JSON Parsing Example:**
```java
// Input JSON from database (cst_time_log_interval_t.break_interval)
String breakIntervalJson = '[
  {"id":1, "breakStartTime":36000, "breakEndTime":36900},
  {"id":2, "breakStartTime":54000, "breakEndTime":54900},
  {"id":3, "breakStartTime":null, "breakEndTime":57600},
  {"id":4, "breakStartTime":61200, "breakEndTime":59400}
]'
Integer timeLogId = 150

// Step 2: Parse JSON
ObjectMapper objectMapper = new ObjectMapper()
JsonNode jsonArray = objectMapper.readTree(breakIntervalJson)
if (jsonArray.isArray()) { // ✓ Valid array

// Step 3: Process each break interval

  // Process break interval #1
  JsonNode node1 = {"id":1, "breakStartTime":36000, "breakEndTime":36900}
  
  // Extract fields with null safety
  Integer id = node1.has("id") && !node1.get("id").isNull() 
               ? node1.get("id").asInt() : null  // Result: 1
  Integer breakStartTimeSeconds = node1.has("breakStartTime") && !node1.get("breakStartTime").isNull()
                                  ? node1.get("breakStartTime").asInt() : null  // Result: 36000
  Integer breakEndTimeSeconds = node1.has("breakEndTime") && !node1.get("breakEndTime").isNull()
                                ? node1.get("breakEndTime").asInt() : null  // Result: 36900
  
  // Convert seconds to LocalTime
  LocalTime breakStartTime = LocalTime.ofSecondOfDay(36000)  // 10:00:00
  LocalTime breakEndTime = LocalTime.ofSecondOfDay(36900)    // 10:15:00
  
  // Validate break interval
  if (breakStartTime != null && breakEndTime != null && breakStartTime.isBefore(breakEndTime)) {
    // 10:00 < 10:15 ✓ Valid break
    TimeLogBreakInterval breakInterval1 = new TimeLogBreakInterval()
    breakInterval1.setId(1)
    breakInterval1.setTimeLogId(150)
    breakInterval1.setBreakStartTime(LocalTime.of(10, 0))
    breakInterval1.setBreakEndTime(LocalTime.of(10, 15))
    breakIntervals.add(breakInterval1)
  }

  // Process break interval #2
  JsonNode node2 = {"id":2, "breakStartTime":54000, "breakEndTime":54900}
  // Similar processing...
  // 54000 = 15:00:00, 54900 = 15:15:00
  // Valid: 15:00 < 15:15 ✓
  TimeLogBreakInterval breakInterval2 = new TimeLogBreakInterval()
  breakInterval2.setId(2)
  breakInterval2.setTimeLogId(150)
  breakInterval2.setBreakStartTime(LocalTime.of(15, 0))
  breakInterval2.setBreakEndTime(LocalTime.of(15, 15))
  breakIntervals.add(breakInterval2)

  // Process break interval #3 (Invalid: null start time)
  JsonNode node3 = {"id":3, "breakStartTime":null, "breakEndTime":57600}
  breakStartTimeSeconds = null  // null value
  breakStartTime = null
  breakEndTime = LocalTime.ofSecondOfDay(57600)  // 16:00:00
  
  // Validation fails
  if (null != null && breakEndTime != null && null.isBefore(breakEndTime)) {
    // False - validation fails
  } else {
    logger.logWarn("Invalid break interval for timeLog ID 150: startTime=null, endTime=16:00")
    // Skip this break interval
  }

  // Process break interval #4 (Invalid: end before start)
  JsonNode node4 = {"id":4, "breakStartTime":61200, "breakEndTime":59400}
  breakStartTime = LocalTime.ofSecondOfDay(61200)  // 17:00:00
  breakEndTime = LocalTime.ofSecondOfDay(59400)    // 16:30:00
  
  // Validation fails
  if (breakStartTime != null && breakEndTime != null && breakStartTime.isBefore(breakEndTime)) {
    // 17:00 < 16:30 ✗ False - end time before start time
  } else {
    logger.logWarn("Invalid break interval for timeLog ID 150: startTime=17:00, endTime=16:30")
    // Skip this break interval
  }
}

// Final Result (only valid breaks included)
List<TimeLogBreakInterval> breakIntervals = [
  TimeLogBreakInterval(id=1, timeLogId=150, breakStartTime=10:00, breakEndTime=10:15),
  TimeLogBreakInterval(id=2, timeLogId=150, breakStartTime=15:00, breakEndTime=15:15)
]
```

**Expected JSON Format:**
```json
[
  {
    "id": 1,                    // Optional: Break interval ID (can be null)
    "breakStartTime": 36000,    // Required: Seconds since midnight (10:00 AM)
    "breakEndTime": 36900       // Required: Seconds since midnight (10:15 AM)
  },
  {
    "id": 2,
    "breakStartTime": 54000,    // 15:00 PM
    "breakEndTime": 54900       // 15:15 PM
  }
]
```

**Error Handling:**
- **Malformed JSON** → Catch exception, log warning, return partial results
- **Invalid break times** → Skip individual breaks, continue processing others
- **Null values** → Handle gracefully with null-safe extraction
- **Negative durations** → Validate start < end, skip invalid breaks

---

### 6. `calculateTotalBreakDuration(List<TimeLogBreakInterval> breakIntervals)` - Break Duration Calculator
**Lines:** 262-278  
**Role:** Calculates total break duration from validated break intervals

```java
Input: List<TimeLogBreakInterval> DTOs with validated break periods
↓
Step 1: Guard clause validation
        if (breakIntervals == null || breakIntervals.isEmpty())
            return Duration.ZERO
↓
Step 2: Initialize duration accumulator
        Duration totalDuration = Duration.ZERO
↓
Step 3: Process each break interval
        For each TimeLogBreakInterval:
        a. Validate break start and end times are non-null
        b. Calculate interval duration: Duration.between(start, end)
        c. Validate duration is not negative
        d. Add valid duration to total
        e. Skip invalid intervals (defensive programming)
↓
Output: Total break duration as Duration object
```

**Detailed Calculation Example:**
```java
// Input: Validated break intervals from parseBreakIntervalsFromJson()
List<TimeLogBreakInterval> breakIntervals = [
  TimeLogBreakInterval(id=1, timeLogId=150, start=10:00, end=10:15),  // 15 minutes
  TimeLogBreakInterval(id=2, timeLogId=150, start=15:00, end=15:15),  // 15 minutes
  TimeLogBreakInterval(id=3, timeLogId=150, start=null, end=16:00),   // Invalid: null start
  TimeLogBreakInterval(id=4, timeLogId=150, start=17:00, end=16:30)   // Invalid: negative duration
]

// Processing Each Break

// Initialize accumulator
Duration totalDuration = Duration.ZERO  // 0 minutes

// Process break #1
TimeLogBreakInterval interval1 = breakIntervals.get(0)
if (interval1.getBreakStartTime() != null && interval1.getBreakEndTime() != null) {
  // 10:00 != null ✓, 10:15 != null ✓
  Duration intervalDuration = Duration.between(
    LocalTime.of(10, 0),   // breakStartTime
    LocalTime.of(10, 15)   // breakEndTime
  ) // Result: Duration.ofMinutes(15)
  
  if (!intervalDuration.isNegative()) {
    // 15 minutes >= 0 ✓ (not negative)
    totalDuration = totalDuration.plus(intervalDuration)
    // totalDuration = 0 + 15 = 15 minutes
  }
}

// Process break #2
TimeLogBreakInterval interval2 = breakIntervals.get(1)
if (interval2.getBreakStartTime() != null && interval2.getBreakEndTime() != null) {
  // 15:00 != null ✓, 15:15 != null ✓
  Duration intervalDuration = Duration.between(
    LocalTime.of(15, 0),   // breakStartTime
    LocalTime.of(15, 15)   // breakEndTime
  ) // Result: Duration.ofMinutes(15)
  
  if (!intervalDuration.isNegative()) {
    // 15 minutes >= 0 ✓ (not negative)
    totalDuration = totalDuration.plus(intervalDuration)
    // totalDuration = 15 + 15 = 30 minutes
  }
}

// Process break #3 (Invalid: null start time)
TimeLogBreakInterval interval3 = breakIntervals.get(2)
if (interval3.getBreakStartTime() != null && interval3.getBreakEndTime() != null) {
  // null != null ✗ (fails null check)
  // Skip this interval - no processing
}

// Process break #4 (Invalid: negative duration)
TimeLogBreakInterval interval4 = breakIntervals.get(3)
if (interval4.getBreakStartTime() != null && interval4.getBreakEndTime() != null) {
  // 17:00 != null ✓, 16:30 != null ✓
  Duration intervalDuration = Duration.between(
    LocalTime.of(17, 0),   // breakStartTime
    LocalTime.of(16, 30)   // breakEndTime
  ) // Result: Duration.ofMinutes(-30) (negative!)
  
  if (!intervalDuration.isNegative()) {
    // -30 minutes >= 0 ✗ (is negative)
    // Skip this interval - no addition to total
  }
}

// Final Result
return totalDuration  // Duration.ofMinutes(30) - only valid breaks counted
```

**Defensive Programming Features:**
- **Null safety** - Validates both start and end times before calculation
- **Negative duration handling** - Skips breaks where end time is before start time
- **Partial failure resilience** - Continues processing even if some breaks are invalid
- **Zero-safe accumulation** - Handles empty lists and all-invalid scenarios gracefully

---

### 7. `evaluateWithExpandedTimeLogs(Timesheet timesheet, List<TimeLog> expandedTimeLogs)` - Rule Engine Integration
**Lines:** 287-331  
**Role:** Processes expanded time logs through the standard rule engine with weekly splitting and aggregation

```java
Input: 
  - Timesheet (configuration, rules, rates)
  - List<TimeLog> expandedTimeLogs (already converted from intervals)
↓
Step 1: Validate timesheet using parent class validation
        validateTimesheet(timesheet) // Ensures required fields present
↓
Step 2: Extract week start day configuration
        Integer timesheetStartDay = timesheet.getTimesheetSetting().getTimesheetStartDay()
        Determines weekly grouping (e.g., MONDAY=1, SUNDAY=7)
↓
Step 3: Split expanded time logs into weekly groups
        if (timesheetStartDay == null)
            weeklyTimeLogs = TimeHelper.splitTimeLogsOnWeeklyBasis(expandedTimeLogs)  // Default: Sunday-Saturday
        else
            WorkDay weekStartDay = WorkDay.getWorkDayType(timesheetStartDay)
            weeklyTimeLogs = TimeHelper.splitTimeLogsOnWeeklyBasis(expandedTimeLogs, weekStartDay)
↓
Step 4: Initialize weekly result accumulator
        WeeklyRuleEvaluatorResult weeklyResult = WeeklyRuleEvaluatorResult.builder()
            .timesheet(timesheet).build()
↓
Step 5: Process each week
        For each List<TimeLog> weeklyTimeLog in weeklyTimeLogs:
        a. Skip empty weeks
        b. Extract week start/end dates from time logs
        c. Create RuleEvaluatorResult for this week
        d. Call evaluateWeeklyTimeLogs() [Inherited from BaseRuleEvaluator]
           → Evaluates all rules in sequence with daily state management
           → Regular Hours → Break → Daily OT → Weekly OT
        e. Call postProcessResult() [Inherited from BaseRuleEvaluator]
           → Applies rates, calculates totals, finalizes results
        f. Add week result to weeklyResult
↓
Output: WeeklyRuleEvaluatorResult with complete pay/bill calculations
```

**Weekly Processing Example:**
```java
// Input: Expanded time logs spanning 2 weeks
Timesheet timesheet = Timesheet(
  id=500,
  timesheetSetting=TimesheetSetting(
    timesheetStartDay=1,  // MONDAY (weeks start on Monday)
    payRate=BigDecimal.valueOf(25.00),
    billRate=BigDecimal.valueOf(40.00)
  )
)

List<TimeLog> expandedTimeLogs = [
  // Week 1: Jan 8-14 (Monday-Sunday)
  TimeLog(id=100, date=Jan8, start=8:00, end=12:00),    // Mon morning
  TimeLog(id=100, date=Jan8, start=13:00, end=18:00),   // Mon afternoon
  TimeLog(id=101, date=Jan9, start=9:00, end=17:00),    // Tue full day
  TimeLog(id=102, date=Jan10, start=8:30, end=12:30),   // Wed morning
  TimeLog(id=102, date=Jan10, start=13:30, end=17:30),  // Wed afternoon
  
  // Week 2: Jan 15-21
  TimeLog(id=103, date=Jan15, start=9:00, end=12:00),   // Mon morning
  TimeLog(id=103, date=Jan15, start=13:00, end=17:00),  // Mon afternoon
  TimeLog(id=104, date=Jan16, start=8:00, end=16:00),   // Tue full day
]

// Step 3: Split into weekly groups
WorkDay weekStartDay = WorkDay.getWorkDayType(1)  // MONDAY
List<List<TimeLog>> weeklyTimeLogs = TimeHelper.splitTimeLogsOnWeeklyBasis(expandedTimeLogs, MONDAY)

// Result:
weeklyTimeLogs = [
  [ // Week 1: Jan 8-14
    TimeLog(Jan8, 8:00-12:00),   // Mon morning
    TimeLog(Jan8, 13:00-18:00),  // Mon afternoon  
    TimeLog(Jan9, 9:00-17:00),   // Tue full
    TimeLog(Jan10, 8:30-12:30),  // Wed morning
    TimeLog(Jan10, 13:30-17:30)  // Wed afternoon
  ],
  [ // Week 2: Jan 15-21
    TimeLog(Jan15, 9:00-12:00),  // Mon morning
    TimeLog(Jan15, 13:00-17:00), // Mon afternoon
    TimeLog(Jan16, 8:00-16:00)   // Tue full
  ]
]

// Step 5: Process each week

// Process Week 1
List<TimeLog> week1TimeLogs = weeklyTimeLogs.get(0)  // 5 time logs
LocalDate weekStartDate = Jan8   // First time log date
LocalDate weekEndDate = Jan10    // Last time log date

RuleEvaluatorResult week1Result = new RuleEvaluatorResult()
week1Result.setTimesheet(timesheet)

// Call inherited method from BaseRuleEvaluator
evaluateWeeklyTimeLogs(week1TimeLogs, timesheet, week1Result)

// Inside evaluateWeeklyTimeLogs (from parent):
// - Creates daily state map: Map<LocalDate, EvaluationState>
// - Jan8 state shared between morning + afternoon intervals
// - Jan9 state for single full-day interval  
// - Jan10 state shared between morning + afternoon intervals
// - Evaluates rules in sequence for each interval
// - Updates occupied ranges and worked hours per day
// - Handles Daily OT only on last interval of each day

// After rule evaluation:
// Jan8: 9 hours total (4h morning + 5h afternoon) → 8h regular + 1h daily OT
// Jan9: 8 hours total → 8h regular + 0h daily OT
// Jan10: 8 hours total (4h morning + 4h afternoon) → 8h regular + 0h daily OT
// Week total: 25 hours → 24h regular + 1h daily OT + 0h weekly OT

postProcessResult(week1Result)
// Applies pay/bill rates:
// Regular: 24h × $25 = $600 pay, 24h × $40 = $960 bill
// Daily OT: 1h × $37.50 = $37.50 pay, 1h × $60 = $60 bill
// Total: $637.50 pay, $1020 bill

weeklyResult.addWeeklyResult(Jan8, Jan10, week1Result)

// Process Week 2 (similar logic)
// ...

// Final WeeklyRuleEvaluatorResult
WeeklyRuleEvaluatorResult {
  timesheet: timesheet,
  weeklyResults: [
    {
      weekStartDate: Jan8,
      weekEndDate: Jan10,
      result: {
        totalPay: $637.50,
        totalBill: $1020.00,
        regularHours: 24,
        dailyOvertimeHours: 1,
        weeklyOvertimeHours: 0
      }
    },
    {
      weekStartDate: Jan15,
      weekEndDate: Jan16,
      result: { ... }
    }
  ]
}
```

**Key Integration Features:**
- **Weekly Splitting** - Respects custom week start days from timesheet settings
- **Daily State Management** - Shared EvaluationState across same-day intervals
- **Rule Sequence** - Standard rule evaluation order maintained
- **Result Aggregation** - Weekly results properly accumulated and post-processed
- **Rate Application** - Pay/bill rates applied consistently across all intervals

---

### 8. `isWeeklyOvertimeRule(IEvaluatableRule rule, Timesheet timesheet)` - Weekly OT Filter
**Lines:** 333-349  
**Role:** Determines if a rule should be evaluated as weekly overtime based on rule type and timesheet frequency

```java
Input: 
  - IEvaluatableRule (rule to evaluate)
  - Timesheet (for frequency configuration)
↓
Step 1: Check rule type
        boolean isWeeklyOvertimeRuleType = (rule.getRuleType() == RuleType.RANGE_BASED_WEEKLY_OVERTIME)
        If not weekly OT rule type → return false
↓
Step 2: Extract timesheet frequency
        TimesheetFrequency frequency = RuleEngineTimesheetSettingMapper.INSTANCE
            .toTimesheetSetting(timesheet.getTimesheetSetting())
            .getTimesheetFrequency()
↓
Step 3: Apply frequency filter
        if (frequency == TimesheetFrequency.MONTHLY) → return false (skip weekly OT for monthly)
        else → return true (allow weekly OT for weekly/bi-weekly/null frequency)
↓
Output: boolean (true = evaluate as weekly OT, false = skip)
```

**Frequency-Based Logic Examples:**
```java
// Scenario 1: Weekly timesheet with weekly OT rule
CustomRule weeklyOTRule = CustomRule(ruleType=RANGE_BASED_WEEKLY_OVERTIME, threshold=40h)
Timesheet weeklyTimesheet = Timesheet(
  timesheetSetting=TimesheetSetting(frequency=WEEKLY)
)

isWeeklyOvertimeRule(weeklyOTRule, weeklyTimesheet)
→ Step 1: ruleType == RANGE_BASED_WEEKLY_OVERTIME ✓
→ Step 2: frequency = WEEKLY
→ Step 3: WEEKLY != MONTHLY ✓
→ Return true (evaluate weekly OT)

// Scenario 2: Monthly timesheet with weekly OT rule
CustomRule weeklyOTRule = CustomRule(ruleType=RANGE_BASED_WEEKLY_OVERTIME, threshold=40h)
Timesheet monthlyTimesheet = Timesheet(
  timesheetSetting=TimesheetSetting(frequency=MONTHLY)
)

isWeeklyOvertimeRule(weeklyOTRule, monthlyTimesheet)
→ Step 1: ruleType == RANGE_BASED_WEEKLY_OVERTIME ✓
→ Step 2: frequency = MONTHLY
→ Step 3: MONTHLY == MONTHLY ✓
→ Return false (skip weekly OT for monthly timesheets)

// Scenario 3: Daily overtime rule (not weekly)
CustomRule dailyOTRule = CustomRule(ruleType=RANGE_BASED_DAILY_OVERTIME, threshold=8h)
Timesheet weeklyTimesheet = Timesheet(
  timesheetSetting=TimesheetSetting(frequency=WEEKLY)
)

isWeeklyOvertimeRule(dailyOTRule, weeklyTimesheet)
→ Step 1: ruleType == RANGE_BASED_DAILY_OVERTIME ≠ RANGE_BASED_WEEKLY_OVERTIME ✗
→ Return false (not a weekly OT rule)
```

**Business Logic Rationale:**
- **Monthly timesheets** should calculate monthly OT, not weekly OT
- **Prevents incorrect calculations** when employees work 50 hours in week 1 and 10 hours in week 2 of a monthly timesheet
- **Flexible frequency support** - Works with weekly, bi-weekly, and null frequencies
- **Rule type validation** - Ensures only actual weekly OT rules are processed

---

### 9. `getRegularHoursRuleType()` & `getBreakRuleType()` - Rule Type Configuration
**Lines:** 351-359  
**Role:** Provides rule type configuration for range-based evaluation strategy

```java
// Regular Hours Rule Type
@Override
protected RuleType getRegularHoursRuleType() {
    return RuleType.RANGE_BASED_REGULAR_HOURS;
}

// Break Rule Type  
@Override
protected RuleType getBreakRuleType() {
    return RuleType.RANGE_BASED_BREAK;
}
```

**Template Method Integration:**
- **BaseRuleEvaluator** uses these methods to identify which rules handle specific functions
- **Range-based strategy** uses range-based rule types for time range calculations
- **Consistent rule factory integration** - Ensures correct rule resolver creation
- **Polymorphic behavior** - Different evaluator types can use different rule strategies

---

## Advanced Multi-Interval Processing

### Daily State Management Across Intervals
```java
// Example: Monday with 3 work intervals
TimeLog morning = TimeLog(id=100, date=Jan15, start=8:00, end=12:00)
TimeLog lunch = TimeLog(id=100, date=Jan15, start=13:00, end=14:00)  
TimeLog afternoon = TimeLog(id=100, date=Jan15, start=15:00, end=19:00)

// BaseRuleEvaluator creates shared daily state
Map<LocalDate, EvaluationState> dailyStates = {
  Jan15 → EvaluationState {
    occupiedTimeRanges: TreeRangeSet.create(),
    workedHoursTillNow: Duration.ZERO
  }
}

// All 3 intervals share the SAME EvaluationState instance
// This enables accurate daily overtime calculation:

// Morning interval processing
evaluateTimeLog(morning, ..., dailyStates.get(Jan15))
→ Regular Hours claims [8:00-12:00]
→ state.occupiedTimeRanges = [8:00-12:00]
→ state.workedHoursTillNow = 4 hours

// Lunch interval processing  
evaluateTimeLog(lunch, ..., dailyStates.get(Jan15))  // SAME STATE
→ Regular Hours claims [13:00-14:00]
→ state.occupiedTimeRanges = [8:00-12:00, 13:00-14:00]
→ state.workedHoursTillNow = 5 hours

// Afternoon interval processing
evaluateTimeLog(afternoon, ..., dailyStates.get(Jan15))  // SAME STATE  
→ Regular Hours claims [15:00-19:00]
→ state.occupiedTimeRanges = [8:00-12:00, 13:00-14:00, 15:00-19:00]
→ state.workedHoursTillNow = 9 hours

// Daily OT rule (only on last interval)
→ Total worked: 9 hours > 8 hour threshold
→ Daily OT claims 1 hour from afternoon interval
→ Final: 8h regular + 1h daily OT
```

### Break Time Integration with Intervals
```java
// Morning interval with break
TimeLog morning = TimeLog(
  start=9:00, end=12:00,
  breakIntervals=[TimeLogBreakInterval(10:30-10:45)]  // 15 min break
)

// Afternoon interval with break  
TimeLog afternoon = TimeLog(
  start=13:00, end=18:00,
  breakIntervals=[TimeLogBreakInterval(15:00-15:15)]  // 15 min break
)

// Break rule processing
RangeBasedBreakRuleTimeRangeResolver.resolveTimeRange():
  
  // Morning interval
  → Uses actual break intervals: [10:30-10:45]
  → Claims 15 minutes break time
  → Updates occupied ranges
  
  // Afternoon interval  
  → Uses actual break intervals: [15:00-15:15]
  → Claims 15 minutes break time
  → Updates occupied ranges
  
  // Total break time: 30 minutes across both intervals
  // Break threshold logic applied once per day
```

---

## Database Schema Integration

### Primary Tables

#### cst_time_log_t (Parent Records)
```sql
CREATE TABLE cst_time_log_t (
    id                SERIAL PRIMARY KEY,
    date              DATE NOT NULL,
    candidate_id      INTEGER NOT NULL,
    job_id            INTEGER NOT NULL,
    timesheet_id      INTEGER NOT NULL,
    work_start_time   INTEGER,          -- Legacy: seconds since midnight
    work_end_time     INTEGER,          -- Legacy: seconds since midnight  
    work_time         INTEGER,          -- Legacy: total duration in seconds
    break_time        INTEGER,          -- Legacy: total break in seconds
    pay_rate          DECIMAL(10,2),
    bill_rate         DECIMAL(10,2),
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### cst_time_log_interval_t (Interval Details)
```sql
CREATE TABLE cst_time_log_interval_t (
    id                  SERIAL PRIMARY KEY,
    time_log_id         INTEGER NOT NULL REFERENCES cst_time_log_t(id),
    work_start_time     INTEGER NOT NULL,    -- Seconds since midnight (e.g., 28800 = 8:00 AM)
    work_end_time       INTEGER NOT NULL,    -- Seconds since midnight (e.g., 43200 = 12:00 PM)
    break_interval      JSONB,               -- Array of break intervals
    range_based_remark  TEXT,                -- Optional notes for this interval
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for efficient querying
CREATE INDEX idx_time_log_interval_time_log_id ON cst_time_log_interval_t(time_log_id);
```

### JSON Break Interval Format
```json
[
  {
    "id": 1,                      // Optional: Break interval ID
    "breakStartTime": 36000,      // Required: 10:00 AM in seconds since midnight
    "breakEndTime": 36900         // Required: 10:15 AM in seconds since midnight
  },
  {
    "id": 2,
    "breakStartTime": 54000,      // 15:00 PM in seconds
    "breakEndTime": 54900         // 15:15 PM in seconds
  }
]
```

### Repository Integration
```java
@Repository
public interface ITimeLogIntervalRepository extends JpaRepository<TimeLogInterval, Integer> {
    
    /**
     * Finds all intervals for given time log IDs.
     * Returns Map<timeLogId, List<intervals>> for efficient lookup.
     * Prevents N+1 query problem.
     */
    @Query("SELECT tli FROM TimeLogInterval tli WHERE tli.timeLogId IN :timeLogIds ORDER BY tli.timeLogId, tli.workStartTime")
    Map<Integer, List<TimeLogIntervalDto>> findIntervalsByTimeLogIds(@Param("timeLogIds") List<Integer> timeLogIds);
}
```

---

## Error Handling and Resilience

### Validation Layers

#### 1. Timesheet Validation
```java
// From BaseRuleEvaluator.validateTimesheet()
if (timesheet == null) {
    throw new IllegalArgumentException("Timesheet cannot be null");
}
if (timesheet.getTimesheetSetting() == null) {
    throw new IllegalArgumentException("Timesheet setting cannot be null");
}
if (timesheet.getTimeLogs() == null || timesheet.getTimeLogs().isEmpty()) {
    throw new IllegalArgumentException("Timesheet must contain time logs");
}
```

#### 2. Interval Validation
```java
// In createTimeLogFromInterval()
Duration intervalDuration = TimeHelper.calculateDuration(intervalStartTime, intervalEndTime);
if (!intervalDuration.isZero()) {
    return mappedTimeLog;  // Valid interval
} else {
    logger.logWarn("Skipping interval for timeLog ID {0} due to invalid time data", timeLogId);
    return null;  // Invalid interval - skip gracefully
}
```

#### 3. Break Interval Validation
```java
// In parseBreakIntervalsFromJson()
if (breakStartTime != null && breakEndTime != null && breakStartTime.isBefore(breakEndTime)) {
    // Valid break - create DTO
    TimeLogBreakInterval breakInterval = new TimeLogBreakInterval();
    // ... populate DTO
    breakIntervals.add(breakInterval);
} else {
    logger.logWarn("Invalid break interval for timeLog ID {0}: startTime={1}, endTime={2}", 
                   timeLogId, breakStartTime, breakEndTime);
    // Skip invalid break, continue with others
}
```

### Backwards Compatibility
```java
// In fetchTimeLogsFromIntervals()
List<TimeLogIntervalDto> intervals = intervalsByTimeLogId.get(timeLog.getId());

if (intervals != null && !intervals.isEmpty()) {
    // New path: Use intervals from cst_time_log_interval_t
    for (TimeLogIntervalDto interval : intervals) {
        TimeLog mappedTimeLog = createTimeLogFromInterval(timeLog, interval);
        if (mappedTimeLog != null) {
            expandedTimeLogs.add(mappedTimeLog);
        }
    }
} else {
    // Backwards compatibility: Use original time log from cst_time_log_t
    TimeLog mappedTimeLog = RuleEngineTimeLogMapper.INSTANCE.toTimeLog(timeLog);
    Duration duration = TimeHelper.calculateTimeLogDuration(mappedTimeLog);
    if (!duration.isZero()) {
        expandedTimeLogs.add(mappedTimeLog);
    } else {
        logger.logWarn("Skipping timelog with ID {0} due to invalid time data", timeLog.getId());
    }
}
```

### Exception Handling
```java
// JSON parsing with graceful degradation
try {
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode jsonArray = objectMapper.readTree(breakIntervalJson);
    // ... process JSON
} catch (Exception ex) {
    logger.logWarn("Failed to parse break intervals JSON for timeLog ID {0}: {1}", 
                   timeLogId, ex.getMessage());
    // Return partial results - don't fail entire evaluation
}
```

---

## Performance Optimization

### Database Query Efficiency
```java
// Single query for all intervals (avoids N+1 problem)
Map<Integer, List<TimeLogIntervalDto>> intervalsByTimeLogId = 
    timeLogIntervalRepository.findIntervalsByTimeLogIds(timeLogIds);

// Efficient lookup during processing
for (TimeLog timeLog : timeLogs) {
    List<TimeLogIntervalDto> intervals = intervalsByTimeLogId.get(timeLog.getId());
    // O(1) lookup instead of O(n) query per time log
}
```

### Memory Management
```java
// Process intervals incrementally
for (TimeLogIntervalDto interval : intervals) {
    TimeLog mappedTimeLog = createTimeLogFromInterval(timeLog, interval);
    if (mappedTimeLog != null) {
        expandedTimeLogs.add(mappedTimeLog);
    }
    // Each interval processed and added immediately
    // No large intermediate collections
}
```

### Time Complexity Analysis
- **Interval expansion:** O(n × m) where n = time logs, m = average intervals per log
- **Rule evaluation:** O(k × r) where k = expanded time logs, r = rules per log
- **Weekly splitting:** O(k log k) for sorting by date
- **Overall complexity:** O(n × m × r) - linear with total work intervals

### Typical Performance Metrics
```java
// Typical timesheet: 5 days × 2 intervals per day = 10 expanded time logs
// Processing time: < 100ms for complete rule evaluation
// Memory usage: < 1MB for expanded time log DTOs

// Large timesheet: 20 days × 3 intervals per day = 60 expanded time logs  
// Processing time: < 500ms for complete rule evaluation
// Memory usage: < 5MB for expanded time log DTOs
```

---

## Testing Strategies

### Unit Test Coverage

#### Core Expansion Logic
```java
@Test
void testFetchTimeLogsFromIntervals_MultipleIntervals() {
    // Setup: Time log with 3 intervals
    // Expected: 3 expanded time log DTOs
}

@Test
void testFetchTimeLogsFromIntervals_NoIntervals() {
    // Setup: Time log with no intervals in database
    // Expected: Uses original time log (backwards compatibility)
}

@Test
void testFetchTimeLogsFromIntervals_MixedScenario() {
    // Setup: Some time logs with intervals, some without
    // Expected: Proper handling of both scenarios
}
```

#### DTO Creation
```java
@Test
void testCreateTimeLogFromInterval_ValidInterval() {
    // Setup: Valid interval with breaks
    // Expected: Complete time log DTO with correct times and breaks
}

@Test
void testCreateTimeLogFromInterval_ZeroDuration() {
    // Setup: Interval with start == end time
    // Expected: null return with warning log
}

@Test
void testCreateTimeLogFromInterval_NullTimes() {
    // Setup: Interval with null start or end time
    // Expected: null return with warning log
}
```

#### JSON Break Parsing
```java
@Test
void testParseBreakIntervalsFromJson_ValidBreaks() {
    // Setup: Valid JSON with multiple breaks
    // Expected: List of TimeLogBreakInterval DTOs
}

@Test
void testParseBreakIntervalsFromJson_InvalidBreaks() {
    // Setup: JSON with invalid break times
    // Expected: Skip invalid breaks, return valid ones
}

@Test
void testParseBreakIntervalsFromJson_MalformedJson() {
    // Setup: Invalid JSON string
    // Expected: Empty list with warning log
}

@Test
void testParseBreakIntervalsFromJson_NullJson() {
    // Setup: null or blank JSON
    // Expected: Empty list (no error)
}
```

#### Break Duration Calculation
```java
@Test
void testCalculateTotalBreakDuration_MultipleBreaks() {
    // Setup: List with valid breaks
    // Expected: Sum of all break durations
}

@Test
void testCalculateTotalBreakDuration_InvalidBreaks() {
    // Setup: List with null times and negative durations
    // Expected: Only valid breaks counted
}
```

### Integration Test Scenarios
```java
@Test
void testEndToEndIntervalExpansion() {
    // Setup: Complete timesheet with intervals in database
    // Execute: Full evaluateRules() flow
    // Verify: Correct expansion, rule evaluation, and result aggregation
}

@Test
void testMultiIntervalDailyOvertimeCalculation() {
    // Setup: Day with 3 intervals totaling 10 hours
    // Execute: Rule evaluation
    // Verify: 8h regular + 2h daily OT, applied only on last interval
}

@Test
void testWeeklyOvertimeWithIntervals() {
    // Setup: Week with multiple multi-interval days
    // Execute: Rule evaluation
    // Verify: Correct weekly OT calculation across all intervals
}

@Test
void testBackwardsCompatibilityMixed() {
    // Setup: Timesheet with some time logs having intervals, others not
    // Execute: Rule evaluation
    // Verify: Proper handling of both interval and non-interval time logs
}
```

---

## Common Issues and Troubleshooting

### Issue 1: Intervals Not Expanding
**Symptoms:**
- Time logs not expanding despite intervals in database
- Evaluation results same as single-interval processing

**Root Causes:**
1. `workLogType` not set to `START_AND_END_TIME` (value 2)
2. No records in `cst_time_log_interval_t` for time log IDs
3. Repository query returning empty results

**Diagnosis:**
```java
// Check work log type
Integer workLogType = timesheet.getTimesheetSetting().getWorkLogType();
// Expected: 2 (START_AND_END_TIME)

// Check database records
SELECT * FROM cst_time_log_interval_t WHERE time_log_id IN (100, 101, 102);
// Expected: Records with valid work_start_time and work_end_time

// Check repository method
Map<Integer, List<TimeLogIntervalDto>> intervals = 
    timeLogIntervalRepository.findIntervalsByTimeLogIds(Arrays.asList(100, 101, 102));
// Expected: Non-empty map with interval lists
```

**Solutions:**
1. Verify timesheet configuration: `workLogType = 2`
2. Ensure interval records exist in database
3. Check repository query and mapping

### Issue 2: Break Time Calculations Incorrect
**Symptoms:**
- Break time doesn't match expected values
- Missing breaks in rule evaluation results

**Root Causes:**
1. Invalid JSON format in `break_interval` column
2. Break times outside work period boundaries
3. JSON parsing failures due to malformed data

**Diagnosis:**
```java
// Check JSON format
String breakJson = interval.getBreakInterval();
// Expected: '[{"id":1,"breakStartTime":36000,"breakEndTime":36900}]'

// Validate break times
LocalTime breakStart = LocalTime.ofSecondOfDay(36000);  // 10:00
LocalTime breakEnd = LocalTime.ofSecondOfDay(36900);    // 10:15
// Expected: start < end, both within work period

// Check parsing results
List<TimeLogBreakInterval> breaks = parseBreakIntervalsFromJson(breakJson, timeLogId);
// Expected: Non-empty list with valid break intervals
```

**Solutions:**
1. Validate JSON format in database
2. Ensure break times are within work period boundaries
3. Add data validation at entry point

### Issue 3: Daily Overtime Calculated Multiple Times
**Symptoms:**
- Daily OT calculated on each interval instead of once per day
- Total daily OT exceeds expected amount

**Root Cause:**
- Daily state not properly shared across same-day intervals

**Diagnosis:**
```java
// Check daily state management in BaseRuleEvaluator
Map<LocalDate, EvaluationState> dailyStates = new LinkedHashMap<>();
for (TimeLog timeLog : weeklyTimeLog) {
    LocalDate date = timeLog.getDate();
    EvaluationState dailyState = dailyStates.computeIfAbsent(date, d -> new EvaluationState());
    // Expected: Same state instance for same date
}
```

**Solution:**
- Verify `BaseRuleEvaluator.evaluateWeeklyTimeLogs()` properly shares daily state
- Ensure Daily OT rule uses `isLastIntervalOfDay()` check

### Issue 4: Weekly Overtime Applied to Monthly Timesheets
**Symptoms:**
- Weekly OT calculated for monthly frequency timesheets
- Incorrect overtime calculations for long time periods

**Root Cause:**
- `isWeeklyOvertimeRule()` not properly filtering by frequency

**Diagnosis:**
```java
// Check timesheet frequency
TimesheetFrequency frequency = timesheet.getTimesheetSetting().getTimesheetFrequency();
// Expected: MONTHLY for monthly timesheets

// Check rule filtering
boolean shouldEvaluate = isWeeklyOvertimeRule(weeklyOTRule, timesheet);
// Expected: false for monthly timesheets
```

**Solution:**
- Verify frequency configuration in timesheet settings
- Ensure `isWeeklyOvertimeRule()` returns false for monthly frequency

---

## Related Files and Dependencies

### Core Dependencies
- **BaseRuleEvaluator.java** - Parent class providing rule evaluation framework
- **ITimeLogIntervalRepository.java** - Database interface for interval queries
- **TimeLogIntervalRepository.java** - JPA repository implementation
- **RuleEngineTimeLogMapper.java** - MapStruct mapper for entity-to-DTO conversion
- **RuleEngineTimesheetSettingMapper.java** - MapStruct mapper for settings
- **TimeHelper.java** - Time calculation utilities and range operations

### Rule Resolvers (Range-Based Strategy)
- **RangeBasedRegularHoursRuleTimeRangeResolver.java** - Regular hours with template work days
- **RangeBasedBreakRuleTimeRangeResolver.java** - Break time with actual intervals
- **RangeBasedDailyOvertimeRuleRangeResolver.java** - Daily OT with multi-interval support
- **RangeBasedWeeklyOvertimeRuleTimeRangeResolver.java** - Weekly OT calculations

### DTOs and Data Structures
- **TimeLog.java** (DTO) - Rule engine time log data transfer object
- **TimeLogIntervalDto.java** - Database interval data structure
- **TimeLogBreakInterval.java** - Break interval DTO for rule evaluation
- **RuleEvaluationResult.java** - Individual rule evaluation result
- **WeeklyRuleEvaluatorResult.java** - Final aggregated output

### Database Entities
- **TimeLog.java** (Entity) - JPA entity for cst_time_log_t table
- **TimeLogInterval.java** (Entity) - JPA entity for cst_time_log_interval_t table
- **Timesheet.java** (Entity) - JPA entity for timesheet configuration

### Configuration and Constants
- **WorkLogType.java** - Enum defining work log types (DURATION=1, START_AND_END_TIME=2)
- **RuleType.java** - Enum defining rule types (RANGE_BASED_REGULAR_HOURS, etc.)
- **TimesheetFrequency.java** - Enum defining timesheet frequencies (WEEKLY, MONTHLY, etc.)

---

## Summary

The **RangeBasedRuleEvaluator** serves as the sophisticated bridge between modern interval-based time tracking and the rule engine's evaluation framework, providing:

1. **Intelligent Strategy Selection** - Routes to interval expansion based on work log type configuration
2. **Database Integration** - Efficiently queries and processes interval data with single-query optimization
3. **JSON Break Parsing** - Converts stored break data into structured rule engine DTOs
4. **Context Preservation** - Maintains parent time log metadata across expanded intervals
5. **Backwards Compatibility** - Seamlessly handles both interval-based and legacy time logs
6. **Rule Engine Integration** - Processes expanded time logs through standard rule evaluation framework

**Key Innovations:**
- **Interval Expansion Algorithm:** Transforms 1 database record → N evaluable time log DTOs
- **Daily State Management:** Shares evaluation state across same-day intervals for accurate overtime calculations
- **JSON Break Integration:** Converts stored break intervals into rule-evaluable break periods
- **Frequency-Aware Processing:** Properly handles weekly vs monthly timesheet scenarios
- **Performance Optimization:** Single database query with efficient in-memory processing

**Architectural Benefits:**
- **Scalable Design:** Handles complex work schedules with multiple daily intervals
- **Data Integrity:** Preserves all parent time log context while expanding intervals
- **Rule Compatibility:** Works seamlessly with existing rule resolvers and evaluation logic
- **Error Resilience:** Graceful handling of invalid data with comprehensive logging
- **Future-Proof:** Extensible design supports additional interval-based features

This class enables accurate pay and bill calculations for modern flexible work arrangements while maintaining full compatibility with existing timesheet data and rule configurations, creating a robust foundation for complex time tracking scenarios.