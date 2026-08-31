# Dynamic Export API Documentation

## Overview

The Dynamic Export API provides a flexible way to export data with customizable field selection using JPQL-based dynamic queries. This implementation supports any combination of fields without requiring fixed DTOs or JOOQ table regeneration.

## Key Features

✅ **Dynamic Field Selection**: Choose any combination of available fields  
✅ **No Fixed DTOs**: Uses `Map<String, Object>` for maximum flexibility  
✅ **JPQL-Based**: No dependency on JOOQ table regeneration  
✅ **Multiple Formats**: Support for CSV and Excel exports  
✅ **Future-Proof**: Add new fields by simply updating the ExportFieldRegistry  
✅ **Performance Optimized**: Only necessary joins are added based on selected fields  

## Available Endpoints

### 1. Export Dynamic Data
**POST** `/api/export/dynamic`

Exports data based on dynamic field selection.

### 2. Get Available Fields
**GET** `/api/export/available-fields`

Returns all available fields that can be exported.

### 3. Get Export Count
**POST** `/api/export/count`

Returns the count of records that would be exported for given criteria.

## Available Export Fields

| Field Name | Display Name | Description | Data Type | Required Entities |
|------------|--------------|-------------|-----------|-------------------|
| `timesheet_id` | Timesheet ID | Unique identifier | Integer | t |
| `timesheet_period` | Period | Formatted date range | String | t |
| `timesheet_status` | Status | Approval status | String | t, ta |
| `created_date` | Created Date | Creation date | String | t |
| `total_hours` | Total Hours | Total worked hours | BigDecimal | t |
| `billing_rate` | Billing Rate | Hourly billing rate | BigDecimal | t |
| `candidate_id` | Candidate ID | Candidate identifier | Integer | t, c |
| `candidate_name` | Candidate Name | Full candidate name | String | t, c |
| `candidate_email` | Candidate Email | Email address | String | t, c |
| `candidate_phone` | Candidate Phone | Phone number | String | t, c |
| `candidate_owner` | Candidate Owner | Owner/recruiter name | String | t, c, u |
| `job_id` | Job ID | Job identifier | Integer | t, c, j |
| `job_title` | Job Title | Position title | String | t, c, j |
| `job_reference` | Job Reference | Job reference number | String | t, c, j |
| `company_id` | Company ID | Company identifier | Integer | t, c, j, comp |
| `company_name` | Company Name | Company name | String | t, c, j, comp |
| `approved_by` | Approved By | Approver name | String | t, ta, approver |
| `approval_date` | Approval Date | Approval date | String | t, ta |

*Entity Aliases: t=Timesheet, c=Candidate, u=User, j=Job, comp=Company, ta=TimesheetApproval, approver=Approver*

## Request/Response Examples

### 1. Basic Export Request

```json
POST /api/export/dynamic

{
  "selectedFields": [
    "timesheet_id",
    "candidate_name", 
    "timesheet_period",
    "total_hours"
  ],
  "fileFormat": "CSV",
  "maxRecords": 1000
}
```

**Response**: Downloads CSV file with selected fields.

### 2. Advanced Export with Filters

```json
POST /api/export/dynamic

{
  "selectedFields": [
    "timesheet_id",
    "candidate_name",
    "candidate_email", 
    "job_title",
    "company_name",
    "timesheet_status",
    "approved_by",
    "billing_rate"
  ],
  "fileFormat": "EXCEL",
  "maxRecords": 5000,
  "filters": {
    "status": "APPROVED",
    "candidateId": 123
  },
  "timesheetIds": [1001, 1002, 1003]
}
```

**Response**: Downloads Excel file with filtered data.

### 3. Get Available Fields

```json
GET /api/export/available-fields

Response:
[
  {
    "frontendName": "timesheet_id",
    "jpqlExpression": "t.id",
    "displayName": "Timesheet ID",
    "javaType": "java.lang.Integer",
    "requiredEntities": ["t"],
    "nullable": false,
    "enabled": true
  },
  {
    "frontendName": "candidate_name",
    "jpqlExpression": "CASE WHEN c.lastName IS NULL OR c.lastName = '' THEN c.firstName ELSE CONCAT(c.firstName, ' ', c.lastName) END",
    "displayName": "Candidate Name", 
    "javaType": "java.lang.String",
    "requiredEntities": ["t", "c"],
    "nullable": true,
    "enabled": true
  },
  // ... more fields
]
```

### 4. Get Export Count

```json
POST /api/export/count

{
  "selectedFields": ["timesheet_id", "candidate_name"],
  "filters": {
    "status": "APPROVED"
  }
}

Response: 1247
```

## Supported Filters

| Filter Key | Description | Example |
|------------|-------------|---------|
| `status` | Filter by approval status | `"APPROVED"`, `"DRAFT"`, `"REJECTED"` |
| `candidateId` | Filter by specific candidate | `123` |
| `periodStartAfter` | Filter by period start date | `1640995200` (Unix timestamp) |
| `periodEndBefore` | Filter by period end date | `1672531200` (Unix timestamp) |

## File Formats

### CSV Format
- **MIME Type**: `text/csv`
- **Extension**: `.csv`
- **Encoding**: UTF-8
- **Features**: Automatic escaping, proper quoting

### Excel Format  
- **MIME Type**: `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
- **Extension**: `.xlsx`
- **Features**: Header styling, auto-sized columns, type-aware cell values

## Error Responses

### Validation Errors
```json
{
  "result": "ERROR",
  "message": "Invalid export fields: invalid_field_name",
  "data": null
}
```

### No Data Found
```json
{
  "result": "ERROR", 
  "message": "No data found matching the specified criteria",
  "data": null
}
```

## Adding New Fields

To add a new export field:

1. **Add to ExportFieldRegistry**:
```java
// In ExportFieldRegistry.createFieldDefinitions()
this.addField(fields, "new_field", "t.newColumn", "New Field", String.class, true, "t");
```

2. **Available Immediately**: No code changes needed elsewhere!

### Complex Field Example
```java
// Adding calculated field with joins
this.addField(fields, "total_pay", 
    "t.totalHours * t.billingRate", 
    "Total Pay", 
    BigDecimal.class, 
    true, 
    "t");

// Adding field requiring new entity join
this.addField(fields, "department_name", 
    "dept.name", 
    "Department", 
    String.class, 
    true, 
    "t", "c", "dept");
```

3. **Update FROM clause** in `DynamicExportRepository.buildFromClause()`:
```java
if (requiredEntities.contains("dept")) {
    fromClause.append(" LEFT JOIN Department dept ON c.departmentId = dept.id");
}
```

## Performance Considerations

- **Smart Joins**: Only required entities are joined based on selected fields
- **Query Optimization**: Uses JPQL with proper indexing
- **Memory Efficient**: Streams results instead of loading all in memory
- **Configurable Limits**: Maximum 50,000 records per export

## Testing Examples

### Using cURL

```bash
# Export basic fields as CSV
curl -X POST "http://localhost:8080/api/export/dynamic" \
  -H "Content-Type: application/json" \
  -d '{
    "selectedFields": ["timesheet_id", "candidate_name", "total_hours"],
    "fileFormat": "CSV",
    "maxRecords": 100
  }' \
  --output export.csv

# Get available fields
curl -X GET "http://localhost:8080/api/export/available-fields"

# Get export count
curl -X POST "http://localhost:8080/api/export/count" \
  -H "Content-Type: application/json" \
  -d '{
    "selectedFields": ["timesheet_id", "candidate_name"],
    "filters": {"status": "APPROVED"}
  }'
```

### Using Postman

1. **Set Method**: POST
2. **URL**: `{{baseUrl}}/api/export/dynamic`
3. **Headers**: `Content-Type: application/json`
4. **Body** (raw JSON):
```json
{
  "selectedFields": ["timesheet_id", "candidate_name", "job_title"],
  "fileFormat": "EXCEL",
  "maxRecords": 500
}
```
5. **Response**: Save response as file

## Implementation Architecture

```
Frontend Request → Controller → Service → Repository → EntityManager
       ↓              ↓          ↓          ↓            ↓
   Field Names → Validation → Business → Dynamic → Generated
                              Logic     JPQL     SQL Query
                                ↓          ↓         ↓
                           File Gen ← Export ← Query Results
                               ↓       Data      
                          Download File
```

The system is designed to be:
- **Maintainable**: Clear separation of concerns
- **Extensible**: Easy to add new fields and formats  
- **Performant**: Optimized queries and memory usage
- **Reliable**: Comprehensive validation and error handling

This implementation fulfills all your requirements:
1. ✅ **Column Mapping**: Frontend names mapped to JPQL expressions
2. ✅ **Dynamic DTOs**: Uses `Map<String, Object>` instead of fixed DTOs  
3. ✅ **Repository Optimization**: Dynamic JPQL with smart joins
4. ✅ **Future-Proof**: Add fields without changing existing code
