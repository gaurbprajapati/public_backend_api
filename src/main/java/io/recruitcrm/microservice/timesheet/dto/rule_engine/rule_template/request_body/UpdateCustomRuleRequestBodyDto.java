package io.recruitcrm.microservice.timesheet.dto.rule_engine.rule_template.request_body;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCustomRuleRequestBodyDto {

	private Integer id;

	private String ruleName;

	private List<Integer> workDayId;

	private Integer ruleType;

	private Integer startTime;

	private Integer endTime;

	private Integer dailyThreshold;

	private Integer weeklyThreshold;

	private Integer chargeMethod;

	private Float payRateMultiplier;

	private Float billRateMultiplier;

	private Float payRatePerHour;

	private Float billRatePerHour;

}