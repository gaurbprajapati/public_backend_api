package io.recruitcrm.microservice.timesheet.dto.export;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Container for export data grouped by timesheet periods. Contains period information and
 * the list of timesheets within that period.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PeriodGroupedExportResponseBodyDto {

	/**
	 * Period start date in UTC (human readable format)
	 */
	private String periodStartUtc;

	/**
	 * Period end date in UTC (human readable format)
	 */
	private String periodEndUtc;

	/**
	 * Period range in display format (e.g., "1 January - 7 January")
	 */
	private String periodDisplayName;

	/**
	 * List of export data within this period
	 */
	private List<DynamicExportResponseBodyDto> timesheetsInPeriod;

	/**
	 * Number of timesheets in this period
	 */
	private int timesheetCount;

	/**
	 * Period start date as Unix timestamp (for sorting purposes)
	 */
	private Integer periodStart;

	/**
	 * Get period display name for file/sheet naming
	 */
	public String getPeriodDisplayName() {
		return (this.periodDisplayName != null) ? this.periodDisplayName
				: (this.periodStartUtc + " - " + this.periodEndUtc);
	}

}