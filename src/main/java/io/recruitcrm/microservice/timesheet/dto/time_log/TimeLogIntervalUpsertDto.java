package io.recruitcrm.microservice.timesheet.dto.time_log;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for time log interval upsert operations Contains values for batch upsert: [id,
 * timeLogId, workStartTime, workEndTime, rangeBasedRemark, breakIntervalJson]
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeLogIntervalUpsertDto {

	private Integer id;

	private Integer timeLogId;

	private Integer workStartTime;

	private Integer workEndTime;

	private String rangeBasedRemark;

	private String breakIntervalJson;

}
