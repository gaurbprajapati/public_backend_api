package io.recruitcrm.microservice.timesheet.dto.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Represents a single row in the "Reimbursements" export sheet. Combines reimbursement
 * data with parent timesheet context (period, contractor, job) so each row is
 * self-contained without requiring cross-referencing.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReimbursementExportRowDto {

	private String timesheetId;

	private String timesheetPeriod;

	private String contractorName;

	private String jobName;

	private String companyName;

	private String jobDuration;

	private String reimbursementDescription;

	private BigDecimal amount;

	private String currencySymbol;

	private String payable;

	private String billable;

	private String status;

}
