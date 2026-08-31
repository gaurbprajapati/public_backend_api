/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.kafka.webhook_events.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetApprovedWebhookEvent {

	@NotBlank(message = "Event name cannot be null or empty")
	@JsonProperty("event")
	private String eventName;

	@Valid
	@NotNull(message = "Payload cannot be null")
	@JsonProperty("payload")
	private Payload payload;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Payload {

		@NotNull(message = "Account ID cannot be null")
		@JsonProperty("account_id")
		private Integer accountId;

		@NotEmpty(message = "Timesheet IDs cannot be null or empty")
		@JsonProperty("id")
		private List<Integer> id;

	}

}
