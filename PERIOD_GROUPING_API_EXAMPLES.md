# Period Grouping Export API Examples

## Overview
The export API now supports grouping timesheets by periods when `timesheetPeriods: true` is set. This creates period-based organization with the following features:

- **Grouping**: Data grouped by `period_start` and `period_end` converted from epoch to UTC
- **File Naming**: Filename uses period range format (e.g., "1 January - 7 January.xlsx")
- **Excel Sheets**: Each period gets its own sheet named after the period range
- **CSV Structure**: Periods are separated with headers in a single file
- **Performance Optimized**: Single database call for both data and filename generation

## API Endpoints

### 1. Export with Period Grouping

**Endpoint**: `POST /api/export/candidates-timesheets`

**Request Body (Period Grouping Enabled)**:
```json
{
  "selectedFields": [
    "timesheet_id",
    "timesheet_period", 
    "candidate_name",
    "candidate_email",
    "total_hours"
  ],
  "fileFormat": "EXCEL",
  "maxRecords": 1000,
  "timesheetPeriods": true,
  "filters": {
    "periodStartAfter": 1704067200,
    "periodEndBefore": 1706745600
  }
}
```

**Expected Result**:
- **Filename**: `1 January - 7 January.xlsx` (based on first period found)
- **Excel Structure**: Multiple sheets, each named after a period range
- **CSV Structure**: Period headers separating grouped data

### 2. Regular Export (No Grouping)

**Request Body (Regular Export)**:
```json
{
  "selectedFields": [
    "timesheet_id",
    "candidate_name", 
    "candidate_email"
  ],
  "fileFormat": "CSV",
  "maxRecords": 500,
  "timesheetPeriods": false
}
```

**Expected Result**:
- **Filename**: `export_data_20241201_143022.csv` (with timestamp)
- **Structure**: Single flat file with all data

### 3. Get Available Fields

**Endpoint**: `GET /api/export/available-fields`

**Response**:
```json
[
  {
    "frontendName": "timesheet_id",
    "displayName": "Timesheet ID",
    "javaType": "Integer",
    "nullable": false,
    "enabled": true,
    "requiredEntities": ["ts"]
  },
  {
    "frontendName": "candidate_name", 
    "displayName": "Candidate Name",
    "javaType": "String",
    "nullable": true,
    "enabled": true,
    "requiredEntities": ["ts", "tss", "tsa", "c"]
  }
]
```

### 4. Get Export Count

**Endpoint**: `POST /api/export/count`

**Request Body**:
```json
{
  "selectedFields": ["timesheet_id", "candidate_name"],
  "timesheetPeriods": true,
  "filters": {
    "candidateId": 123
  }
}
```

**Response**: `42` (number of records/periods that would be exported)

## Period Grouping Features

### 1. UTC Conversion
- Epoch times (`period_start`, `period_end`) are converted to UTC dates
- Grouping is done on the UTC date (not the original epoch)
- Display format: "1 January", "15 March", etc.

### 2. File Organization

#### Excel Format:
- **Multiple Sheets**: Each period gets its own sheet
- **Sheet Names**: Period range (e.g., "1 January - 7 January")
- **File Name**: First period range found + .xlsx extension

#### CSV Format:
- **Single File**: All periods in one file
- **Period Headers**: Each period section starts with "Period: 1 January - 7 January"
- **Column Headers**: Repeated for each period section

### 3. Filename Examples
```
# Period Grouping Enabled
1 January - 7 January.xlsx
15 March - 21 March.csv
1 December - 7 December.xlsx

# Regular Export (with timestamp)
export_data_20241201_143022.csv
export_data_20241201_143022.xlsx
```

## Sample SQL Generated

### Regular Query:
```sql
SELECT ts.id as timesheet_id, c.firstname as candidate_name
FROM cst_timesheet_t ts
LEFT JOIN cst_timesheet_setting_t tss ON ts.timesheet_setting_id = tss.id
LEFT JOIN cst_timesheet_setting_association_t tsa ON tss.association_id = tsa.id  
LEFT JOIN tblcandidate c ON tsa.contractor_id = c.id
WHERE ts.account_id = ?
ORDER BY ts.id DESC
LIMIT 1000
```

### Period Grouped Query:
```sql
SELECT ts.id as timesheet_id, 
       c.firstname as candidate_name,
       DATE_FORMAT(FROM_UNIXTIME(ts.period_start), '%d %M') as period_start_display,
       DATE_FORMAT(FROM_UNIXTIME(ts.period_end), '%d %M') as period_end_display
FROM cst_timesheet_t ts
LEFT JOIN cst_timesheet_setting_t tss ON ts.timesheet_setting_id = tss.id
LEFT JOIN cst_timesheet_setting_association_t tsa ON tss.association_id = tsa.id  
LEFT JOIN tblcandidate c ON tsa.contractor_id = c.id
WHERE ts.account_id = ?
GROUP BY DATE(FROM_UNIXTIME(ts.period_start)), DATE(FROM_UNIXTIME(ts.period_end))
ORDER BY ts.period_start ASC
```

## Testing with cURL

### Test Period Grouping:
```bash
curl -X POST http://localhost:8080/api/export/candidates-timesheets \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "selectedFields": ["timesheet_id", "candidate_name", "timesheet_period"],
    "fileFormat": "EXCEL", 
    "timesheetPeriods": true,
    "maxRecords": 100
  }' \
  --output "period_export.xlsx"
```

### Test Regular Export:
```bash
curl -X POST http://localhost:8080/api/export/candidates-timesheets \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "selectedFields": ["timesheet_id", "candidate_name"],
    "fileFormat": "CSV",
    "timesheetPeriods": false,
    "maxRecords": 100
  }' \
  --output "regular_export.csv"
```

## Notes

1. **Performance**: Period grouping queries may take longer for large datasets
2. **Limits**: `maxRecords` applies to total records, not periods
3. **Validation**: All selected fields must exist in the JSON configuration
4. **File Size**: Excel files with many sheets may be larger than CSV
5. **Timezone**: All dates are converted to UTC for consistency
