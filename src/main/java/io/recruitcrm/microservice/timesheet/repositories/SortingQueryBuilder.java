package io.recruitcrm.microservice.timesheet.repositories;

import io.recruitcrm.microservice.search.dto.sorting.SortPriorityRequestBodyDto;
import org.jooq.Field;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.SelectOrderByStep;
import org.jooq.SortField;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SortingQueryBuilder {

	/**
	 * Add sorting to query using a single table reference for all fields.
	 * @param searchSelectQuery The base query to add sorting to
	 * @param sortPriority List of sort priority fields
	 * @param table The table to use for qualifying field names
	 * @return Query with sorting applied
	 */
	public SelectOrderByStep<?> addSortingQuery(SelectConditionStep<?> searchSelectQuery,
			List<SortPriorityRequestBodyDto> sortPriority, Table<?> table) {
		/**
		 * Filter out null or empty field names to prevent invalid SQL. Qualify field
		 * names with the table reference to avoid ambiguous column errors when multiple
		 * tables have columns with the same name (e.g., updated_by, updated_on).
		 */
		Set<String> selectColumnNames = collectSelectColumnNames(searchSelectQuery);
		List<? extends SortField<?>> sortFields = sortPriority.stream()
			.filter((sort) -> sort.getField() != null && !sort.getField().trim().isEmpty())
			.map((sort) -> resolveSortField(sort, table, selectColumnNames))
			.filter(Objects::nonNull)
			.toList();

		// If no valid sort fields, return query without ordering
		if (sortFields.isEmpty()) {
			return searchSelectQuery;
		}

		return (SelectOrderByStep<?>) searchSelectQuery.orderBy(withIdTiebreaker(sortFields, table));
	}

	/**
	 * Add sorting to query using a field-to-table mapping for multi-table queries. This
	 * method allows different fields to be qualified by their respective tables, enabling
	 * sorting across multiple joined tables.
	 * @param searchSelectQuery The base query to add sorting to
	 * @param sortPriority List of sort priority fields
	 * @param fieldTableMapping Map of field names to their corresponding tables
	 * @param defaultTable Default table to use if field is not found in the mapping
	 * @return Query with sorting applied
	 */
	public SelectOrderByStep<?> addSortingQuery(SelectConditionStep<?> searchSelectQuery,
			List<SortPriorityRequestBodyDto> sortPriority, Map<String, Table<?>> fieldTableMapping,
			Table<?> defaultTable) {
		/**
		 * Filter out null or empty field names to prevent invalid SQL. For each field,
		 * lookup its corresponding table from the mapping. If not found, use the default
		 * table. This allows sorting across multiple joined tables with proper
		 * qualification.
		 */
		Set<String> selectColumnNames = collectSelectColumnNames(searchSelectQuery);
		List<? extends SortField<?>> sortFields = sortPriority.stream()
			.filter((sort) -> sort.getField() != null && !sort.getField().trim().isEmpty())
			.map((sort) -> {
				/**
				 * Lookup the table for this field from the mapping. If the field is not
				 * in the mapping, use the default table. This ensures all fields are
				 * properly qualified with their table reference.
				 */
				Table<?> table = fieldTableMapping.getOrDefault(sort.getField(), defaultTable);
				return resolveSortField(sort, table, selectColumnNames);
			})
			.filter(Objects::nonNull)
			.toList();

		// If no valid sort fields, return query without ordering
		if (sortFields.isEmpty()) {
			return searchSelectQuery;
		}

		return (SelectOrderByStep<?>) searchSelectQuery.orderBy(withIdTiebreaker(sortFields, defaultTable));
	}

	/**
	 * Append the table's {@code id} column as a descending tiebreaker to the sort list,
	 * making the ordering total so LIMIT/OFFSET pagination is deterministic. Without it,
	 * rows that tie on the requested (non-unique) sort columns can come back in a
	 * different order for each page, so the same row surfaces on two consecutive pages
	 * (or is skipped) — the cause of the infinite-scroll skeleton/data flicker on the
	 * timesheet list. Skipped when the caller already sorts by {@code id} or when the
	 * resolved table has no {@code id} column.
	 * @param sortFields user-specified sort fields (already resolved and non-empty)
	 * @param table table whose {@code id} column is used as the tiebreaker
	 * @return new list with {@code id DESC} appended (unless id is already present or
	 * unavailable)
	 */
	private List<SortField<?>> withIdTiebreaker(List<? extends SortField<?>> sortFields, Table<?> table) {
		List<SortField<?>> withTiebreaker = new ArrayList<>(sortFields);
		boolean alreadySortingById = sortFields.stream().anyMatch((sf) -> "id".equalsIgnoreCase(sf.getName()));
		if (alreadySortingById) {
			return withTiebreaker;
		}
		Field<?> idField = table.field("id");
		if (idField == null) {
			return withTiebreaker;
		}
		withTiebreaker.add(idField.desc());
		return withTiebreaker;
	}

	/**
	 * Resolve a single sort priority into the JOOQ {@link SortField} to apply, or
	 * {@code null} if the requested field cannot be safely sorted on.
	 */
	private SortField<?> resolveSortField(SortPriorityRequestBodyDto sort, Table<?> table,
			Set<String> selectColumnNames) {
		Field<?> field = resolveField(sort.getField(), table, selectColumnNames);
		if (field == null) {
			return null;
		}
		return "asc".equalsIgnoreCase(sort.getOrder()) ? field.asc() : field.desc();
	}

	/**
	 * Resolve a field name to a JOOQ {@link Field}, or {@code null} if it cannot be
	 * safely sorted on. List-page sort fields arrive as the AG-Grid {@code colId}, which
	 * names a SELECT-clause alias, so we sort by that alias first (matching the displayed
	 * value). Only when the field is not in the projection do we fall back to a physical
	 * column on the resolved table, returning {@code null} if it is neither so the caller
	 * drops it from the ORDER BY instead of emitting an invalid
	 * {@code ORDER BY "<unknown>"}.
	 */
	private Field<?> resolveField(String fieldName, Table<?> table, Set<String> selectColumnNames) {
		if (selectColumnNames.contains(fieldName.toLowerCase(Locale.ROOT))) {
			// Sort by the projection alias (the value actually shown in that column).
			return DSL.field(DSL.name(fieldName));
		}
		// Not in the projection — fall back to a physical column on the resolved table,
		// or
		// null if it is not one (avoids an invalid ORDER BY).
		return table.field(fieldName);
	}

	/**
	 * Collect the (lower-cased) output column names of the query projection so sort
	 * fields can be validated against the columns the query actually returns.
	 */
	private Set<String> collectSelectColumnNames(Select<?> query) {
		return query.getSelect()
			.stream()
			.map(Field::getName)
			.filter(Objects::nonNull)
			.map((name) -> name.toLowerCase(Locale.ROOT))
			.collect(Collectors.toSet());
	}

}
