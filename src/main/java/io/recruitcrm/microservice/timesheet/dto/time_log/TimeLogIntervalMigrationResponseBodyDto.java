package io.recruitcrm.microservice.timesheet.dto.time_log;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for time log interval migration endpoint. Contains migrated count, whether
 * more records remain for migration, and lists of successful and failed time log IDs.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeLogIntervalMigrationResponseBodyDto {

	/**
	 * Number of time logs successfully migrated in this batch.
	 */
	private int migratedCount;

	/**
	 * Whether more time logs remain to be migrated (for pagination).
	 */
	private boolean hasMore;

	/**
	 * List of time log IDs that were successfully migrated.
	 */
	private List<Integer> migratedTimeLogIds;

	/**
	 * List of time log IDs that failed to migrate.
	 */
	private List<Integer> failedTimeLogIds;

	/**
	 * The offset to use for the next migration batch.
	 */
	private int nextOffset;

	/**
	 * Number of time logs remaining to be migrated (unmigrated count).
	 */
	private long remainingRecords;

	/**
	 * Number of time logs fetched in this batch (for debugging).
	 */
	private int totalInBatch;

	/**
	 * Number of time logs skipped because their timesheet setting uses WORK_HOUR (type 1)
	 * work log type, which does not have start/end times to migrate.
	 */
	private int skippedCount;

	/**
	 * Constructor for backward compatibility when only count and hasMore are needed.
	 */
	public TimeLogIntervalMigrationResponseBodyDto(int migratedCount, boolean hasMore) {
		this.migratedCount = migratedCount;
		this.hasMore = hasMore;
		this.migratedTimeLogIds = new ArrayList<>();
		this.failedTimeLogIds = new ArrayList<>();
		this.nextOffset = 0;
		this.remainingRecords = 0L;
	}

}
