/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.microservice.timesheet.dto.validator.TimesheetAndSettingValidatorQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.validator.contractor.ContractorTimesheetValidatorQueryResultDto;
import java.util.Collections;
import java.util.List;

/**
 * Test data factory for
 * {@link io.recruitcrm.microservice.timesheet.repositories.validator.ValidatorRepository}
 * tests.
 */
public final class ValidatorRepositoryTestDataFactory {

	private ValidatorRepositoryTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static Integer getPrimaryTimesheetId() {
		return 9001;
	}

	public static Timesheet createTimesheetWithId(int id) {
		Timesheet timesheet = new Timesheet();
		timesheet.setId(id);
		return timesheet;
	}

	public static List<Integer> getSampleTimesheetIdsForValidator() {
		return List.of(101, 102);
	}

	public static TimesheetAndSettingValidatorQueryResultDto createTimesheetAndSettingValidatorRow() {
		return new TimesheetAndSettingValidatorQueryResultDto(1, 2, 3, 20240101, 20240107, "Ada Lovelace", "pic.png",
				"Engineer role", 55, 101, "https://logo.example/cm.png", 2, true, Collections.emptyList(), 999);
	}

	public static ContractorTimesheetValidatorQueryResultDto createContractorValidatorRow() {
		return new ContractorTimesheetValidatorQueryResultDto(101, 55, 1, false, Collections.emptyList());
	}

}
