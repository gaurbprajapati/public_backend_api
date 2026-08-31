/*
 * Copyright (c) 2026. RecruitCRM
 * All rights reserved.
 */
package io.recruitcrm.microservice.timesheet.services.export;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FixedColumnOrderingRequest {

	private List<String> newColumnOrder;

	private List<String> regularColumns;

	private Map<String, List<String>> timeColumnsByDate;

	private boolean hasWorkHours;

	private boolean hasOvertimeHours;

	private boolean hasEffectiveWorkHours;

	private boolean hasBreakIntervals;

	private boolean hasTimeLogRemarks;

	private List<String> sortedDatesForPeriod;

}
