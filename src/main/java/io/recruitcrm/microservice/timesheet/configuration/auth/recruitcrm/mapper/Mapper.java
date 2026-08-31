/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.mapper;

import io.recruitcrm.entity.model.UserMfaSettings;
import io.recruitcrm.microservice.timesheet.configuration.Generated;
import io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.dto.UserMfaSettingsResponseBodyDto;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@org.mapstruct.Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
@Generated
@Component
public interface Mapper {

	Mapper INSTANCE = Mappers.getMapper(Mapper.class);

	UserMfaSettingsResponseBodyDto toUserMfaSettingsResultBodyDto(UserMfaSettings userMfaSettings);

}
