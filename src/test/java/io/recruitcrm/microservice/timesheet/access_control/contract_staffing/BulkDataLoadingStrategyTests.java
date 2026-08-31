/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.access_control.contract_staffing;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
@DisplayName("BulkDataLoadingStrategy Tests")
class BulkDataLoadingStrategyTests {

	private static Stream<Arguments> determineStrategyCases() {
		return Stream.of(
				// hasTimesheetIds, hasCandidateIds, hasJobIds, expected
				Arguments.of(true, true, false, BulkDataLoadingStrategy.MIXED_STRATEGY),
				Arguments.of(true, false, true, BulkDataLoadingStrategy.MIXED_STRATEGY),
				Arguments.of(false, true, true, BulkDataLoadingStrategy.MIXED_STRATEGY),
				Arguments.of(true, true, true, BulkDataLoadingStrategy.MIXED_STRATEGY),
				Arguments.of(true, false, false, BulkDataLoadingStrategy.TIMESHEET_WITH_RELATED_ENTITIES),
				Arguments.of(false, true, false, BulkDataLoadingStrategy.CANDIDATES_ONLY),
				Arguments.of(false, false, true, BulkDataLoadingStrategy.JOBS_ONLY),
				Arguments.of(false, false, false, BulkDataLoadingStrategy.TIMESHEET_WITH_RELATED_ENTITIES));
	}

	@ParameterizedTest(name = "ts={0}, cand={1}, job={2} -> {3}")
	@MethodSource("determineStrategyCases")
	@DisplayName("determineStrategy should return the appropriate strategy for each combination")
	void testDetermineStrategy(boolean hasTimesheetIds, boolean hasCandidateIds, boolean hasJobIds,
			BulkDataLoadingStrategy expected) {
		// When
		BulkDataLoadingStrategy result = BulkDataLoadingStrategy.determineStrategy(hasTimesheetIds, hasCandidateIds,
				hasJobIds);

		// Then
		assertThat(result).isEqualTo(expected);
	}

	@Test
	@DisplayName("requires methods should reflect single-type strategy semantics")
	void testRequiresSingleTypeSemantics() {
		// Then
		assertThat(BulkDataLoadingStrategy.TIMESHEET_WITH_RELATED_ENTITIES.requiresTimesheetData()).isTrue();
		assertThat(BulkDataLoadingStrategy.CANDIDATES_ONLY.requiresTimesheetData()).isFalse();

		assertThat(BulkDataLoadingStrategy.CANDIDATES_ONLY.requiresCandidateData()).isTrue();
		assertThat(BulkDataLoadingStrategy.JOBS_ONLY.requiresCandidateData()).isFalse();

		assertThat(BulkDataLoadingStrategy.JOBS_ONLY.requiresJobData()).isTrue();
		assertThat(BulkDataLoadingStrategy.CANDIDATES_ONLY.requiresJobData()).isFalse();
	}

	@Test
	@DisplayName("requires-for-mixed methods should include MIXED_STRATEGY")
	void testRequiresForMixedSemantics() {
		// Then
		assertThat(BulkDataLoadingStrategy.TIMESHEET_WITH_RELATED_ENTITIES.requiresTimesheetDataForMixed()).isTrue();
		assertThat(BulkDataLoadingStrategy.MIXED_STRATEGY.requiresTimesheetDataForMixed()).isTrue();
		assertThat(BulkDataLoadingStrategy.JOBS_ONLY.requiresTimesheetDataForMixed()).isFalse();

		assertThat(BulkDataLoadingStrategy.CANDIDATES_ONLY.requiresCandidateDataForMixed()).isTrue();
		assertThat(BulkDataLoadingStrategy.MIXED_STRATEGY.requiresCandidateDataForMixed()).isTrue();
		assertThat(BulkDataLoadingStrategy.JOBS_ONLY.requiresCandidateDataForMixed()).isFalse();

		assertThat(BulkDataLoadingStrategy.JOBS_ONLY.requiresJobDataForMixed()).isTrue();
		assertThat(BulkDataLoadingStrategy.MIXED_STRATEGY.requiresJobDataForMixed()).isTrue();
		assertThat(BulkDataLoadingStrategy.CANDIDATES_ONLY.requiresJobDataForMixed()).isFalse();
	}

	@Test
	@DisplayName("toString should include name, code and description")
	void testToStringContainsCodeAndDescription() {
		// When
		String result = BulkDataLoadingStrategy.MIXED_STRATEGY.toString();

		// Then
		assertThat(result).contains("MIXED_STRATEGY").contains("mixed_strategy");
		assertThat(BulkDataLoadingStrategy.MIXED_STRATEGY.getCode()).isEqualTo("mixed_strategy");
		assertThat(BulkDataLoadingStrategy.MIXED_STRATEGY.getDescription()).isNotEmpty();
	}

}
