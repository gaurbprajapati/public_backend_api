/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.flagsmith;

import com.flagsmith.models.Flag;
import com.flagsmith.models.Flags;
import io.recruitcrm.microservice.timesheet.configuration.Generated;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
@Generated
public interface FlagsmithMapper {

	FlagsmithMapper INSTANCE = Mappers.getMapper(FlagsmithMapper.class);

	@Mapping(target = "flags", source = "flags")
	FlagsmithFlags toFlagsmithFlags(Flags flags);

	static FlagsmithBaseFlag map(Flag flag) {
		FlagsmithBaseFlag flagsmithBaseFlag = new FlagsmithBaseFlag();
		flagsmithBaseFlag.setFeatureName(flag.getFeatureName());
		flagsmithBaseFlag.setEnabled(flag.getEnabled());
		flagsmithBaseFlag.setValue(flag.getValue());
		return flagsmithBaseFlag;
	}

}
