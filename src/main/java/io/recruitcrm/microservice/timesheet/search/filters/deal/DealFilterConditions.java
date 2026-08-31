package io.recruitcrm.microservice.timesheet.search.filters.deal;

import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;

/**
 * Shared condition builders for contractor-deal and timesheet-associated-deal filter
 * nodes.
 */
public final class DealFilterConditions {

	private DealFilterConditions() {
		// Utility class - prevent instantiation
	}

	public static List<Condition> containsAtLeast(List<Integer> dealIds, Condition accountDealCondition,
			Field<Integer> dealIdField, Condition additionalAnd) {
		if (dealIds.isEmpty()) {
			return List.of(accountDealCondition.and(DSL.falseCondition()));
		}
		Condition dealCondition = accountDealCondition.and(dealIdField.in(dealIds));
		if (additionalAnd != null) {
			dealCondition = dealCondition.and(additionalAnd);
		}
		return List.of(dealCondition);
	}

	public static Condition containsAllHaving(List<Integer> dealIds, Field<Integer> dealIdField) {
		if (dealIds.isEmpty()) {
			return DSL.falseCondition();
		}
		return DSL.countDistinct(dealIdField).eq(dealIds.size());
	}

	public static List<Condition> emptyDealIdsMatchesAll() {
		return List.of();
	}

	public static List<Condition> emptyDealIdsMatchesNothing() {
		return List.of(DSL.falseCondition());
	}

	public static List<Field<?>> noGroupByFields() {
		return List.of();
	}

	public static Condition noHavingCondition() {
		return DSL.noCondition();
	}

	public static Boolean selectDistinctTrue() {
		return Boolean.TRUE;
	}

	public static Boolean selectDistinctFalse() {
		return Boolean.FALSE;
	}

}
