/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.flagsmith;

import com.flagsmith.exceptions.FeatureNotFoundError;
import com.flagsmith.exceptions.FlagsmithClientError;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlagsmithFlags {

	private Map<String, FlagsmithBaseFlag> flags = new HashMap<>();

	public FlagsmithBaseFlag getFlag(String featureName) throws FlagsmithClientError {
		if (!this.flags.containsKey(featureName)) {
			throw new FeatureNotFoundError("Feature does not exist: " + featureName);
		}
		return this.flags.get(featureName);
	}

	public Boolean isFeatureEnabled(String key) throws FlagsmithClientError {
		FlagsmithBaseFlag flag = getFlag(key);
		return flag.getEnabled();
	}

	public Object getFeatureValue(String key) throws FlagsmithClientError {
		FlagsmithBaseFlag flag = getFlag(key);
		return flag.getValue();
	}

}
