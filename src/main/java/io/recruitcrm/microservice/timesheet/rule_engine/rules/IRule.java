/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.rules;

import io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEvaluationContext;
import io.recruitcrm.microservice.timesheet.rule_engine.dto.RuleEvaluationResult;

public interface IRule {

	RuleEvaluationResult evaluate(RuleEvaluationContext ruleEvaluationContext);

	void validate();

	String getName();

}
