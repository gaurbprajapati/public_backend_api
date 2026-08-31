/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.services.timesheet;

import io.recruitcrm.microservice.timesheet.dto.contact.ContactNamePhotoQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorNamePhotoQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.user.UserDetailsQueryResultDto;

import java.util.Map;

/**
 * Container class for user details maps to reduce method parameter complexity.
 */
record UserDetailsMaps(Map<Integer, UserDetailsQueryResultDto> agencyUsersMap,
		Map<Integer, ContactNamePhotoQueryResultDto> contactUsersMap,
		Map<Integer, ContractorNamePhotoQueryResultDto> contractorUsersMap) {
}
