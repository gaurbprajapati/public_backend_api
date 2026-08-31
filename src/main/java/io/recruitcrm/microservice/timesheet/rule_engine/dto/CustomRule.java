/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.dto;

import io.recruitcrm.microservice.timesheet.rule_engine.constants.ChargeMethodType;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.RuleType;
import io.recruitcrm.microservice.timesheet.rule_engine.constants.WorkDay;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomRule implements IEvaluatableRule {

	private Integer id;

	private String ruleName;

	private List<WorkDay> workDays;

	private RuleType ruleType;

	private LocalTime startTime;

	private LocalTime endTime;

	private Duration dailyThreshold;

	private Duration weeklyThreshold;

	private ChargeMethodType chargeMethod;

	private Float payRateMultiplier;

	private Float billRateMultiplier;

	private Float payRatePerHour;

	private Float billRatePerHour;

	private Duration startDuration;

	private Duration endDuration;

	@Override
	public boolean isApplicableOnDay(WorkDay timeLogDay) {
		// If workDays is null or empty, rule applies to all days
		if (this.workDays == null || this.workDays.isEmpty()) {
			return true;
		}

		// Check if the rule's workDays contains this day
		return this.workDays.contains(timeLogDay);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CustomRule that)) {
			return false;
		}

		return Objects.equals(this.id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id);
	}

}
