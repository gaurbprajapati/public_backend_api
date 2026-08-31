package io.recruitcrm.microservice.timesheet.dto.time_log;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Data structure to hold batch operation values for time logs and intervals
 */
@Getter
public class BatchOperationData {

	private final List<TimeLogUpsertDto> timeLogUpsertValues;

	private final List<TimeLogIntervalUpsertDto> intervalUpsertValues;

	private final Set<Integer> timeLogIdsWithIntervals;

	public BatchOperationData() {
		this.timeLogUpsertValues = new ArrayList<>();
		this.intervalUpsertValues = new ArrayList<>();
		this.timeLogIdsWithIntervals = new HashSet<>();
	}

}
