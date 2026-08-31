package io.recruitcrm.microservice.timesheet.dto.time_log;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for time log interval migration endpoint. Supports batch migration with
 * configurable batch size and offset.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeLogIntervalMigrationRequestBodyDto {

	/**
	 * Number of time logs to migrate in this batch.
	 */
	@NotNull(message = "Batch size cannot be null")
	@Min(value = 1, message = "Batch size must be at least 1")
	@Max(value = 5000, message = "Batch size cannot exceed 5000")
	private Integer batchSize;

	/**
	 * Number of time logs to skip before starting migration (for pagination).
	 */
	@NotNull(message = "Offset cannot be null")
	@Min(value = 0, message = "Offset must be non-negative")
	private Integer offset;

}
