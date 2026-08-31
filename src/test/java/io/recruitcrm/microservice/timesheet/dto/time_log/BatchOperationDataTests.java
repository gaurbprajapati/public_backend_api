package io.recruitcrm.microservice.timesheet.dto.time_log;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link BatchOperationData} with 100% coverage. Tests constructor,
 * getters, and collection mutability.
 */
class BatchOperationDataTests {

	@Test
	@DisplayName("Constructor and all getters are covered")
	void testConstructorAndGettersCoverage() {
		// When - constructor executes all three field initializations
		BatchOperationData data = new BatchOperationData();
		// Then - invoke all three getters to cover Lombok-generated code
		List<TimeLogUpsertDto> timeLogs = data.getTimeLogUpsertValues();
		List<TimeLogIntervalUpsertDto> intervals = data.getIntervalUpsertValues();
		Set<Integer> idsWithIntervals = data.getTimeLogIdsWithIntervals();
		assertThat(timeLogs).isNotNull().isEmpty();
		assertThat(intervals).isNotNull().isEmpty();
		assertThat(idsWithIntervals).isNotNull().isEmpty();
	}

	@Test
	@DisplayName("Constructor initializes empty collections")
	void testConstructorInitializesEmptyCollections() {
		// When
		BatchOperationData data = new BatchOperationData();

		// Then
		assertThat(data.getTimeLogUpsertValues()).isNotNull().isEmpty();
		assertThat(data.getIntervalUpsertValues()).isNotNull().isEmpty();
		assertThat(data.getTimeLogIdsWithIntervals()).isNotNull().isEmpty();
	}

	@Test
	@DisplayName("Getter returns mutable list for timeLogUpsertValues")
	void testGetTimeLogUpsertValuesReturnsMutableList() {
		// Given
		BatchOperationData data = new BatchOperationData();

		// When
		List<TimeLogUpsertDto> list = data.getTimeLogUpsertValues();
		assertThat(list).isEmpty();
		list.add(null); // Mutate to ensure we get the same list reference

		// Then
		assertThat(data.getTimeLogUpsertValues()).hasSize(1);
	}

	@Test
	@DisplayName("Getter returns mutable list for intervalUpsertValues")
	void testGetIntervalUpsertValuesReturnsMutableList() {
		// Given
		BatchOperationData data = new BatchOperationData();

		// When
		List<TimeLogIntervalUpsertDto> list = data.getIntervalUpsertValues();
		assertThat(list).isEmpty();
		list.add(null);

		// Then
		assertThat(data.getIntervalUpsertValues()).hasSize(1);
	}

	@Test
	@DisplayName("Getter returns mutable set for timeLogIdsWithIntervals")
	void testGetTimeLogIdsWithIntervalsReturnsMutableSet() {
		// Given
		BatchOperationData data = new BatchOperationData();

		// When
		Set<Integer> set = data.getTimeLogIdsWithIntervals();
		assertThat(set).isEmpty();
		set.add(1);

		// Then
		assertThat(data.getTimeLogIdsWithIntervals()).containsExactlyInAnyOrder(1);
	}

	@Test
	@DisplayName("Multiple instances have independent collections")
	void testMultipleInstancesHaveIndependentCollections() {
		// Given
		BatchOperationData data1 = new BatchOperationData();
		BatchOperationData data2 = new BatchOperationData();

		// When
		data1.getTimeLogUpsertValues().add(new TimeLogUpsertDto());
		data1.getIntervalUpsertValues().add(new TimeLogIntervalUpsertDto());
		data1.getTimeLogIdsWithIntervals().add(100);

		// Then
		assertThat(data1.getTimeLogUpsertValues()).hasSize(1);
		assertThat(data1.getIntervalUpsertValues()).hasSize(1);
		assertThat(data1.getTimeLogIdsWithIntervals()).hasSize(1);
		assertThat(data2.getTimeLogUpsertValues()).isEmpty();
		assertThat(data2.getIntervalUpsertValues()).isEmpty();
		assertThat(data2.getTimeLogIdsWithIntervals()).isEmpty();
	}

	@Test
	@DisplayName("TimeLogUpsertValues can hold multiple items")
	void testTimeLogUpsertValuesCanHoldMultipleItems() {
		// Given
		BatchOperationData data = new BatchOperationData();
		TimeLogUpsertDto dto1 = new TimeLogUpsertDto();
		TimeLogUpsertDto dto2 = new TimeLogUpsertDto();

		// When
		data.getTimeLogUpsertValues().add(dto1);
		data.getTimeLogUpsertValues().add(dto2);

		// Then
		assertThat(data.getTimeLogUpsertValues()).hasSize(2).containsExactly(dto1, dto2);
	}

	@Test
	@DisplayName("IntervalUpsertValues can hold multiple items")
	void testIntervalUpsertValuesCanHoldMultipleItems() {
		// Given
		BatchOperationData data = new BatchOperationData();
		TimeLogIntervalUpsertDto dto1 = new TimeLogIntervalUpsertDto();
		TimeLogIntervalUpsertDto dto2 = new TimeLogIntervalUpsertDto();

		// When
		data.getIntervalUpsertValues().add(dto1);
		data.getIntervalUpsertValues().add(dto2);

		// Then
		assertThat(data.getIntervalUpsertValues()).hasSize(2).containsExactly(dto1, dto2);
	}

	@Test
	@DisplayName("TimeLogIdsWithIntervals set prevents duplicates")
	void testTimeLogIdsWithIntervalsSetPreventsDuplicates() {
		// Given
		BatchOperationData data = new BatchOperationData();

		// When
		data.getTimeLogIdsWithIntervals().add(1);
		data.getTimeLogIdsWithIntervals().add(1);
		data.getTimeLogIdsWithIntervals().add(2);

		// Then
		assertThat(data.getTimeLogIdsWithIntervals()).hasSize(2).containsExactlyInAnyOrder(1, 2);
	}

	@Test
	@DisplayName("Collections are ArrayList and HashSet instances")
	void testCollectionsAreArrayListAndHashSetInstances() {
		// Given
		BatchOperationData data = new BatchOperationData();

		// When & Then
		assertThat(data.getTimeLogUpsertValues()).isInstanceOf(ArrayList.class);
		assertThat(data.getIntervalUpsertValues()).isInstanceOf(ArrayList.class);
		assertThat(data.getTimeLogIdsWithIntervals()).isInstanceOf(HashSet.class);
	}

	@Test
	@DisplayName("Getter returns same reference for multiple calls")
	void testGetterReturnsSameReferenceForMultipleCalls() {
		// Given
		BatchOperationData data = new BatchOperationData();

		// When
		List<TimeLogUpsertDto> list1 = data.getTimeLogUpsertValues();
		List<TimeLogUpsertDto> list2 = data.getTimeLogUpsertValues();
		List<TimeLogIntervalUpsertDto> intervals1 = data.getIntervalUpsertValues();
		List<TimeLogIntervalUpsertDto> intervals2 = data.getIntervalUpsertValues();
		Set<Integer> set1 = data.getTimeLogIdsWithIntervals();
		Set<Integer> set2 = data.getTimeLogIdsWithIntervals();

		// Then
		assertThat(list1).isSameAs(list2);
		assertThat(intervals1).isSameAs(intervals2);
		assertThat(set1).isSameAs(set2);
	}

	@Test
	@DisplayName("Collections can be cleared and reused")
	void testCollectionsCanBeClearedAndReused() {
		// Given
		BatchOperationData data = new BatchOperationData();
		data.getTimeLogUpsertValues().add(new TimeLogUpsertDto());
		data.getIntervalUpsertValues().add(new TimeLogIntervalUpsertDto());
		data.getTimeLogIdsWithIntervals().add(1);

		// When
		data.getTimeLogUpsertValues().clear();
		data.getIntervalUpsertValues().clear();
		data.getTimeLogIdsWithIntervals().clear();

		// Then
		assertThat(data.getTimeLogUpsertValues()).isEmpty();
		assertThat(data.getIntervalUpsertValues()).isEmpty();
		assertThat(data.getTimeLogIdsWithIntervals()).isEmpty();
	}

	@Test
	@DisplayName("Collections can contain null values")
	void testCollectionsCanContainNullValues() {
		// Given
		BatchOperationData data = new BatchOperationData();

		// When
		data.getTimeLogUpsertValues().add(null);
		data.getIntervalUpsertValues().add(null);
		data.getTimeLogIdsWithIntervals().add(null);

		// Then
		assertThat(data.getTimeLogUpsertValues()).containsNull();
		assertThat(data.getIntervalUpsertValues()).containsNull();
		assertThat(data.getTimeLogIdsWithIntervals()).containsNull();
	}

}
