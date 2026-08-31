package io.recruitcrm.microservice.timesheet.helpers;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class GenericHelperTests {

	@Test
	void removeDuplicatesRemovesDuplicateElements() {
		List<String> input = List.of("a", "b", "a", "c", "b");
		List<String> expected = List.of("a", "b", "c");
		assertThat(GenericHelper.removeDuplicates(input)).isEqualTo(expected);
	}

	@Test
	void removeDuplicatesHandlesEmptyList() {
		List<String> input = List.of();
		List<String> expected = List.of();
		assertThat(GenericHelper.removeDuplicates(input)).isEqualTo(expected);
	}

	@Test
	void combineListsCombinesMultipleLists() {
		List<Integer> list1 = List.of(1, 2);
		List<Integer> list2 = List.of(3, 4);
		List<Integer> expected = List.of(1, 2, 3, 4);
		assertThat(GenericHelper.combineLists(list1, list2)).isEqualTo(expected);
	}

	@Test
	void combineListsHandlesEmptyLists() {
		List<Integer> list1 = List.of();
		List<Integer> list2 = List.of();
		List<Integer> expected = List.of();
		assertThat(GenericHelper.combineLists(list1, list2)).isEqualTo(expected);
	}

	@Test
	void combineAndRemoveDuplicatesCombinesAndRemovesDuplicates() {
		List<Integer> list1 = List.of(1, 2, 2);
		List<Integer> list2 = List.of(2, 3, 4);
		List<Integer> expected = List.of(1, 2, 3, 4);
		assertThat(GenericHelper.combineAndRemoveDuplicates(list1, list2)).isEqualTo(expected);
	}

	@Test
	void combineAndRemoveDuplicatesHandlesEmptyLists() {
		List<Integer> list1 = List.of();
		List<Integer> list2 = List.of();
		List<Integer> expected = List.of();
		assertThat(GenericHelper.combineAndRemoveDuplicates(list1, list2)).isEqualTo(expected);
	}

	@Test
	void combineAndRemoveDuplicatesWithListOfListsCombinesAndRemovesDuplicates() {
		List<List<Integer>> lists = List.of(List.of(1, 2, 2), List.of(2, 3, 4));
		List<Integer> expected = List.of(1, 2, 3, 4);
		assertThat(GenericHelper.combineAndRemoveDuplicates(lists)).isEqualTo(expected);
	}

	@Test
	void combineAndRemoveDuplicatesWithListOfListsHandlesEmptyLists() {
		List<List<Integer>> lists = List.of(List.of(), List.of());
		List<Integer> expected = List.of();
		assertThat(GenericHelper.combineAndRemoveDuplicates(lists)).isEqualTo(expected);
	}

	@Test
	void runAfterCommitOrNowRunsImmediatelyWhenNoTransactionActive() {
		AtomicBoolean ran = new AtomicBoolean(false);

		GenericHelper.runAfterCommitOrNow(() -> ran.set(true));

		assertThat(ran.get()).isTrue();
	}

	@Test
	void runAfterCommitOrNowDefersTaskUntilAfterCommitWhenTransactionActive() {
		AtomicBoolean ran = new AtomicBoolean(false);
		TransactionSynchronizationManager.initSynchronization();

		try {
			GenericHelper.runAfterCommitOrNow(() -> ran.set(true));

			assertThat(ran.get()).isFalse();
			TransactionSynchronizationManager.getSynchronizations().forEach((sync) -> sync.afterCommit());
			assertThat(ran.get()).isTrue();
		}
		finally {
			TransactionSynchronizationManager.clearSynchronization();
		}
	}

}
