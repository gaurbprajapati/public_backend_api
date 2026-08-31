# Timesheet Export API Examples

## Overview
The Timesheet Export API allows users to export candidate and timesheet data in CSV format. The API supports two main export types:

1. **CANDIDATES_ONLY** - Export only candidate information
2. **CANDIDATES_WITH_TIMESHEETS** - Export candidates along with their timesheet data

## API Endpoint
```
POST /api/export/candidates-timesheets
```

## Request Structure

### Basic Request Fields
- `exportType` (required): Type of export (`CANDIDATES_ONLY` or `CANDIDATES_WITH_TIMESHEETS`)
- `selectedColumns` (required): Array of column names to include in export
- `candidateIds` (optional): Array of specific candidate IDs to export
- `fileFormat` (optional): File format (`CSV` or `EXCEL`, defaults to `CSV`)
- `maxRecords` (optional): Maximum records to export (defaults to 100, max 50000)

## Available Columns

### Candidate Columns
- `candidate_id` - Candidate ID
- `candidate_name` - Full name of candidate
- `candidate_email` - Email address
- `candidate_phone` - Phone number
- `candidate_added_on` - Date when candidate was added
- `candidate_owner` - Owner/recruiter name

### Timesheet Columns (only for CANDIDATES_WITH_TIMESHEETS)
- `timesheet_id` - Timesheet ID
- `timesheet_period` - Timesheet period (start - end date)
- `timesheet_status` - Status (DRAFT, SUBMITTED, APPROVED, REJECTED)
- `total_hours` - Total hours worked
- `total_work_hours` - Regular work hours
- `total_overtime_hours` - Overtime hours
- `pay_amount` - Pay amount
- `bill_amount` - Bill amount
- `pay_status` - Payment status (PAID, UNPAID)
- `bill_status` - Billing status (BILLED, UNBILLED)

## Example Requests

### 1. Export All Candidates (Basic Info)
```json
{
  "exportType": "CANDIDATES_ONLY",
  "selectedColumns": [
    "candidate_id",
    "candidate_name", 
    "candidate_email",
    "candidate_phone"
  ]
}
```

**Expected CSV Output:**
```csv
Candidate ID,Candidate Name,Email,Phone
123,John Doe,john.doe@email.com,+1234567890
456,Jane Smith,jane.smith@email.com,+9876543210
```

**Expected Excel Output:**
- Professional Excel file (.xlsx) with:
  - **Header row** with blue background and white bold text
  - **Data rows** with proper borders and formatting
  - **Auto-sized columns** for optimal readability
  - **Numeric formatting** for ID and amount fields

### 2. Export Specific Candidates with Owner Info
```json
{
  "exportType": "CANDIDATES_ONLY",
  "selectedColumns": [
    "candidate_name",
    "candidate_email",
    "candidate_added_on",
    "candidate_owner"
  ],
  "candidateIds": [123, 456, 789],
  "maxRecords": 100
}
```

### 3. Export Candidates with Timesheet Summary
```json
{
  "exportType": "CANDIDATES_WITH_TIMESHEETS",
  "selectedColumns": [
    "candidate_name",
    "candidate_email",
    "timesheet_period",
    "timesheet_status",
    "total_hours",
    "pay_amount"
  ]
}
```

**Expected CSV Output:**
```csv
Candidate Name,Email,Timesheet Period,Status,Total Hours,Pay Amount
John Doe,john.doe@email.com,01 Jan 2024 - 07 Jan 2024,APPROVED,40.0,2000.00
John Doe,john.doe@email.com,08 Jan 2024 - 14 Jan 2024,SUBMITTED,35.5,1775.00
Jane Smith,jane.smith@email.com,01 Jan 2024 - 07 Jan 2024,APPROVED,40.0,2400.00
```

### 4. Export Detailed Timesheet Data
```json
{
  "exportType": "CANDIDATES_WITH_TIMESHEETS",
  "selectedColumns": [
    "candidate_id",
    "candidate_name",
    "timesheet_id",
    "timesheet_period",
    "timesheet_status",
    "total_work_hours",
    "total_overtime_hours",
    "pay_amount",
    "bill_amount",
    "pay_status",
    "bill_status"
  ],
  "candidateIds": [123],
  "maxRecords": 50
}
```

## Response Format

### Success Response
- **Status**: 200 OK
- **Headers**: 
  - `Content-Disposition: attachment; filename="candidates_export_20240115_143022.csv"`
  - `Content-Type: text/plain`
  - `Content-Encoding: UTF-8`
- **Body**: CSV file content

### Error Responses

#### 400 Bad Request - Validation Error
```json
{
  "result": "ERROR",
  "message": "Invalid columns for candidates-only export: timesheet_period, total_hours",
  "data": null
}
```

#### 404 Not Found - No Data
```json
{
  "result": "ERROR", 
  "message": "No data found for the specified criteria",
  "data": null
}
```

## Validation Rules

1. **Export Type Validation**:
   - Must be either `CANDIDATES_ONLY` or `CANDIDATES_WITH_TIMESHEETS`

2. **Column Validation**:
   - For `CANDIDATES_ONLY`: Only candidate columns are allowed
   - For `CANDIDATES_WITH_TIMESHEETS`: Both candidate and timesheet columns are allowed
   - At least one column must be selected

3. **Record Limits**:
   - Maximum 50,000 records per export
   - Default limit is 10,000 records

4. **File Format**:
   - **CSV format**: Plain text with comma separators, UTF-8 encoded
   - **Excel format**: XLSX files with professional formatting and styling

## Excel Format Examples

### **Example 5: Export Candidates to Excel**
```json
{
  "exportType": "CANDIDATES_ONLY",
  "selectedColumns": [
    "candidate_id",
    "candidate_name",
    "candidate_email",
    "candidate_phone"
  ],
  "fileFormat": "EXCEL"
}
```

### **Example 6: Export Timesheets to Excel**
```json
{
  "exportType": "CANDIDATES_WITH_TIMESHEETS",
  "selectedColumns": [
    "candidate_name",
    "timesheet_period",
    "total_hours",
    "pay_amount",
    "bill_amount"
  ],
  "fileFormat": "EXCEL",
  "maxRecords": 100
}
```

## Implementation Notes

- Exports are processed synchronously (not background jobs)
- Data is filtered by the current user's account ID automatically
- **Uses OpenCSV library** for robust CSV generation and automatic escaping
- **Uses Apache POI library** for professional Excel file generation
- CSV values containing commas, quotes, or newlines are properly escaped by OpenCSV
- Excel files include professional formatting with headers, borders, and auto-sized columns
- Null values are displayed as empty strings
- Dates are formatted as "dd MMM yyyy" (e.g., "15 Jan 2024")
- BigDecimal values are displayed in plain string format without scientific notation

## File Naming Convention

Generated files follow this pattern:
- **CSV files**: `candidates_export_YYYYMMDD_HHMMSS.csv`
- **Excel files**: `candidates_export_YYYYMMDD_HHMMSS.xlsx`
- **With timesheets**: `candidates_timesheets_export_YYYYMMDD_HHMMSS.csv/xlsx`

Examples: 
- `candidates_export_20240115_143022.csv`
- `candidates_timesheets_export_20240115_143022.xlsx`
