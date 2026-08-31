package io.recruitcrm.microservice.timesheet.helpers.rule_engine;

public enum CustomRuleTypeEnum {

	RANGE_BASED_AFTER_SHIFT(1), RANGE_BASED_BEFORE_SHIFT(2), RANGE_BASED_SPECIFIC_TIME_RANGE(3),
	RANGE_BASED_DAILY_OVERTIME(4), RANGE_BASED_WEEKLY_OVERTIME(5), DURATION_BASED_SPECIFIC_HOURS_RANGE(6),
	DURATION_BASED_DAILY_OVERTIME(7), DURATION_BASED_WEEKLY_OVERTIME(8), RANGE_BASED_BREAK(9), DURATION_BASED_BREAK(10),
	RANGE_BASED_REGULAR_HOURS(11), DURATION_BASED_REGULAR_HOURS(12);

	private final Integer value;

	CustomRuleTypeEnum(Integer value) {
		this.value = value;
	}

	public Integer getValue() {
		return this.value;
	}

}
