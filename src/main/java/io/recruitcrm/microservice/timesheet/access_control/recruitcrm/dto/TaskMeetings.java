/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.BasicAccessLevel;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "canadd", "canedit", "canview", "candelete" })
@Generated("jsonschema2pojo")
public class TaskMeetings implements BasicAccessLevel {

	@JsonProperty("canadd")
	private String canAdd;

	@JsonProperty("canedit")
	private String canEdit;

	@JsonProperty("canview")
	private String canView;

	@JsonProperty("candelete")
	private String canDelete;

	@JsonIgnore
	@Valid
	private final Map<String, Object> additionalProperties = new LinkedHashMap<>();

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

}