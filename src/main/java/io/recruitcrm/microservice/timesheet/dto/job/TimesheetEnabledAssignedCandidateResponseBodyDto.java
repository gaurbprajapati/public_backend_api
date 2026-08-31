package io.recruitcrm.microservice.timesheet.dto.job;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetEnabledAssignedCandidateResponseBodyDto {

	@JsonProperty("email")
	private String email;

	@JsonProperty("entitytype")
	private String entityType;

	@JsonProperty("id")
	private Integer id;

	@JsonProperty("photo")
	private String photo;

	@JsonProperty("slug")
	private String slug;

	@JsonProperty("srno")
	private Integer srno;

	@JsonProperty("title")
	private String title;

	@JsonProperty("jobId")
	private Integer jobId;

	@JsonProperty("jobStartDate")
	private Integer jobStartDate;

	@JsonProperty("jobEndDate")
	private Integer jobEndDate;

	@JsonProperty("timesheetFrequency")
	private Integer timesheetFrequency;

	@JsonProperty("timesheetStartDay")
	private Integer timesheetStartDay;

}
