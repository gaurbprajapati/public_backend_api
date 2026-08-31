package io.recruitcrm.microservice.timesheet.helpers;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public final class GenericHelper {

	private GenericHelper() {
	}

	public static List<String> removeDuplicates(List<String> list) {
		return list.stream().distinct().toList();
	}

	@SafeVarargs
	public static <T> List<T> combineLists(List<T>... lists) {
		return Stream.of(lists).flatMap(List::stream).toList();
	}

	@SafeVarargs
	public static <T> List<T> combineAndRemoveDuplicates(List<T>... lists) {
		Set<T> combinedSet = new LinkedHashSet<>();

		for (List<T> list : lists) {
			combinedSet.addAll(list);
		}

		return new ArrayList<>(combinedSet);
	}

	public static <T> List<T> combineAndRemoveDuplicates(List<List<T>> lists) {
		Set<T> combinedSet = new LinkedHashSet<>();
		for (List<T> list : lists) {
			combinedSet.addAll(list);
		}
		return new ArrayList<>(combinedSet);
	}

	public static void runAfterCommitOrNow(final Runnable task) {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					task.run();
				}
			});
		}
		else {
			task.run();
		}
	}

}
