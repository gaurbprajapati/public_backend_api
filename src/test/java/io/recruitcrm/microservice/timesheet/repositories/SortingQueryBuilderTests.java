/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.withSettings;

import io.recruitcrm.microservice.search.dto.sorting.SortPriorityRequestBodyDto;
import io.recruitcrm.microservice.search.models.jooq.tables.CstTimesheetT;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SQLDialect;
import org.jooq.SelectConditionStep;
import org.jooq.SelectOrderByStep;
import org.jooq.SelectSeekStepN;
import org.jooq.SortField;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for SortingQueryBuilder class. Tests all methods for 100% line and branch
 * coverage.
 */
@ExtendWith(MockitoExtension.class)
class SortingQueryBuilderTests {

	@Mock
	private SelectConditionStep<?> selectConditionStep;

	@Mock
	private Table<?> table;

	@Mock
	private Table<?> defaultTable;

	@Mock
	private Field<Object> field;

	private SortingQueryBuilder sortingQueryBuilder;

	@BeforeEach
	void setUp() {
		this.sortingQueryBuilder = new SortingQueryBuilder();
	}

	@Nested
	@DisplayName("addSortingQuery with Single Table Tests")
	class AddSortingQuerySingleTableTests {

		@Test
		@DisplayName("Should return query without ordering when sort priority is empty")
		void testAddSortingQueryEmptySortPriorityReturnsQueryWithoutOrdering() {
			// Given
			List<SortPriorityRequestBodyDto> sortPriority = new ArrayList<>();

			// When
			SelectOrderByStep<?> result = SortingQueryBuilderTests.this.sortingQueryBuilder.addSortingQuery(
					SortingQueryBuilderTests.this.selectConditionStep, sortPriority,
					SortingQueryBuilderTests.this.table);

			// Then
			assertThat(result).isEqualTo(SortingQueryBuilderTests.this.selectConditionStep);
		}

		@Test
		@DisplayName("Should return query without ordering when all field names are null")
		void testAddSortingQueryNullFieldNamesReturnsQueryWithoutOrdering() {
			// Given
			SortPriorityRequestBodyDto sortDto = new SortPriorityRequestBodyDto();
			sortDto.setField(null);
			sortDto.setOrder("asc");
			List<SortPriorityRequestBodyDto> sortPriority = List.of(sortDto);

			// When
			SelectOrderByStep<?> result = SortingQueryBuilderTests.this.sortingQueryBuilder.addSortingQuery(
					SortingQueryBuilderTests.this.selectConditionStep, sortPriority,
					SortingQueryBuilderTests.this.table);

			// Then
			assertThat(result).isEqualTo(SortingQueryBuilderTests.this.selectConditionStep);
		}

		@Test
		@DisplayName("Should return query without ordering when all field names are empty")
		void testAddSortingQueryEmptyFieldNamesReturnsQueryWithoutOrdering() {
			// Given
			SortPriorityRequestBodyDto sortDto = new SortPriorityRequestBodyDto();
			sortDto.setField("   ");
			sortDto.setOrder("asc");
			List<SortPriorityRequestBodyDto> sortPriority = List.of(sortDto);

			// When
			SelectOrderByStep<?> result = SortingQueryBuilderTests.this.sortingQueryBuilder.addSortingQuery(
					SortingQueryBuilderTests.this.selectConditionStep, sortPriority,
					SortingQueryBuilderTests.this.table);

			// Then
			assertThat(result).isEqualTo(SortingQueryBuilderTests.this.selectConditionStep);
		}

	}

	@Nested
	@DisplayName("addSortingQuery with Field Table Mapping Tests")
	class AddSortingQueryFieldTableMappingTests {

		@Test
		@DisplayName("Should return query without ordering when sort priority is empty")
		void testAddSortingQueryEmptySortPriorityReturnsQueryWithoutOrdering() {
			// Given
			List<SortPriorityRequestBodyDto> sortPriority = new ArrayList<>();
			Map<String, Table<?>> fieldTableMapping = new HashMap<>();

			// When
			SelectOrderByStep<?> result = SortingQueryBuilderTests.this.sortingQueryBuilder.addSortingQuery(
					SortingQueryBuilderTests.this.selectConditionStep, sortPriority, fieldTableMapping,
					SortingQueryBuilderTests.this.defaultTable);

			// Then
			assertThat(result).isEqualTo(SortingQueryBuilderTests.this.selectConditionStep);
		}

		@Test
		@DisplayName("Should return query without ordering when all field names are null")
		void testAddSortingQueryNullFieldNamesReturnsQueryWithoutOrdering() {
			// Given
			SortPriorityRequestBodyDto sortDto = new SortPriorityRequestBodyDto();
			sortDto.setField(null);
			sortDto.setOrder("asc");
			List<SortPriorityRequestBodyDto> sortPriority = List.of(sortDto);
			Map<String, Table<?>> fieldTableMapping = new HashMap<>();

			// When
			SelectOrderByStep<?> result = SortingQueryBuilderTests.this.sortingQueryBuilder.addSortingQuery(
					SortingQueryBuilderTests.this.selectConditionStep, sortPriority, fieldTableMapping,
					SortingQueryBuilderTests.this.defaultTable);

			// Then
			assertThat(result).isEqualTo(SortingQueryBuilderTests.this.selectConditionStep);
		}

		@Test
		@DisplayName("Should return query without ordering when all field names are empty")
		void testAddSortingQueryEmptyFieldNamesReturnsQueryWithoutOrdering() {
			// Given
			SortPriorityRequestBodyDto sortDto = new SortPriorityRequestBodyDto();
			sortDto.setField("  ");
			sortDto.setOrder("asc");
			List<SortPriorityRequestBodyDto> sortPriority = List.of(sortDto);
			Map<String, Table<?>> fieldTableMapping = new HashMap<>();

			// When
			SelectOrderByStep<?> result = SortingQueryBuilderTests.this.sortingQueryBuilder.addSortingQuery(
					SortingQueryBuilderTests.this.selectConditionStep, sortPriority, fieldTableMapping,
					SortingQueryBuilderTests.this.defaultTable);

			// Then
			assertThat(result).isEqualTo(SortingQueryBuilderTests.this.selectConditionStep);
		}

	}

	@Nested
	@DisplayName("addSortingQuery Ordering Behaviour Tests")
	class AddSortingQueryOrderingTests {

		/**
		 * Creates an ordered-query mock that implements both {@link SelectSeekStepN} (the
		 * declared return type of {@code orderBy(Collection)}) and
		 * {@link SelectOrderByStep} (the type the production code casts the result to).
		 */
		@SuppressWarnings("rawtypes")
		private SelectSeekStepN newOrderedQuery() {
			return (SelectSeekStepN) mock(SelectSeekStepN.class,
					withSettings().extraInterfaces(SelectOrderByStep.class));
		}

		@Test
		@DisplayName("Should apply ascending order for a field found in the table and append id tiebreaker")
		@SuppressWarnings({ "unchecked", "rawtypes" })
		void testAddSortingQueryAscendingFieldFoundAppliesOrderByWithIdTiebreaker() {
			// Given
			SelectSeekStepN orderedQuery = this.newOrderedQuery();
			Field<Object> nameField = mock(Field.class);
			SortField<Object> nameAsc = mock(SortField.class);
			Field<Object> idField = mock(Field.class);
			SortField<Object> idDesc = mock(SortField.class);
			given(SortingQueryBuilderTests.this.table.field("name")).willReturn((Field) nameField);
			given(nameField.asc()).willReturn((SortField) nameAsc);
			given(nameAsc.getName()).willReturn("name");
			given(SortingQueryBuilderTests.this.table.field("id")).willReturn((Field) idField);
			given(idField.desc()).willReturn((SortField) idDesc);
			given(SortingQueryBuilderTests.this.selectConditionStep.orderBy(any(Collection.class)))
				.willReturn(orderedQuery);

			// When
			SelectOrderByStep<?> result = SortingQueryBuilderTests.this.sortingQueryBuilder.addSortingQuery(
					SortingQueryBuilderTests.this.selectConditionStep,
					List.of(new SortPriorityRequestBodyDto("name", "asc")), SortingQueryBuilderTests.this.table);

			// Then
			assertThat(result).isSameAs(orderedQuery);
			then(SortingQueryBuilderTests.this.selectConditionStep).should().orderBy(List.of(nameAsc, idDesc));
		}

		@Test
		@DisplayName("Should drop the field and skip ordering when it is neither a projected alias nor a physical column on the table")
		@SuppressWarnings({ "unchecked", "rawtypes" })
		void testAddSortingQueryUnknownAliasFieldIsDropped() {
			// Given
			given(SortingQueryBuilderTests.this.table.field("portalStatusId")).willReturn(null);

			// When
			SelectOrderByStep<?> result = SortingQueryBuilderTests.this.sortingQueryBuilder.addSortingQuery(
					SortingQueryBuilderTests.this.selectConditionStep,
					List.of(new SortPriorityRequestBodyDto("portalStatusId", "desc")),
					SortingQueryBuilderTests.this.table);

			// Then
			assertThat(result).isSameAs(SortingQueryBuilderTests.this.selectConditionStep);
			then(SortingQueryBuilderTests.this.selectConditionStep).should(never()).orderBy(any(Collection.class));
		}

		@Test
		@DisplayName("Should skip id tiebreaker when already sorting by id")
		@SuppressWarnings({ "unchecked", "rawtypes" })
		void testAddSortingQueryAlreadySortingByIdSkipsTiebreaker() {
			// Given
			SelectSeekStepN orderedQuery = this.newOrderedQuery();
			Field<Object> idField = mock(Field.class);
			SortField<Object> idAsc = mock(SortField.class);
			given(SortingQueryBuilderTests.this.table.field("id")).willReturn((Field) idField);
			given(idField.asc()).willReturn((SortField) idAsc);
			given(idAsc.getName()).willReturn("id");
			given(SortingQueryBuilderTests.this.selectConditionStep.orderBy(any(Collection.class)))
				.willReturn(orderedQuery);

			// When
			SelectOrderByStep<?> result = SortingQueryBuilderTests.this.sortingQueryBuilder.addSortingQuery(
					SortingQueryBuilderTests.this.selectConditionStep,
					List.of(new SortPriorityRequestBodyDto("id", "asc")), SortingQueryBuilderTests.this.table);

			// Then
			assertThat(result).isSameAs(orderedQuery);
			then(SortingQueryBuilderTests.this.selectConditionStep).should().orderBy(List.of(idAsc));
		}

		@Test
		@DisplayName("Should use the mapped table for a field present in the field-table mapping")
		@SuppressWarnings({ "unchecked", "rawtypes" })
		void testAddSortingQueryMappingFieldInMappingUsesMappedTable() {
			// Given
			SelectSeekStepN orderedQuery = this.newOrderedQuery();
			Table<?> mappedTable = mock(Table.class);
			Field<Object> mappedField = mock(Field.class);
			SortField<Object> mappedAsc = mock(SortField.class);
			Field<Object> idField = mock(Field.class);
			SortField<Object> idDesc = mock(SortField.class);
			given(mappedTable.field("companyName")).willReturn((Field) mappedField);
			given(mappedField.asc()).willReturn((SortField) mappedAsc);
			given(mappedAsc.getName()).willReturn("companyName");
			given(SortingQueryBuilderTests.this.defaultTable.field("id")).willReturn((Field) idField);
			given(idField.desc()).willReturn((SortField) idDesc);
			given(SortingQueryBuilderTests.this.selectConditionStep.orderBy(any(Collection.class)))
				.willReturn(orderedQuery);
			Map<String, Table<?>> mapping = Map.of("companyName", mappedTable);

			// When
			SelectOrderByStep<?> result = SortingQueryBuilderTests.this.sortingQueryBuilder.addSortingQuery(
					SortingQueryBuilderTests.this.selectConditionStep,
					List.of(new SortPriorityRequestBodyDto("companyName", "asc")), mapping,
					SortingQueryBuilderTests.this.defaultTable);

			// Then
			assertThat(result).isSameAs(orderedQuery);
			then(SortingQueryBuilderTests.this.selectConditionStep).should().orderBy(List.of(mappedAsc, idDesc));
			then(mappedTable).should().field("companyName");
		}

		@Test
		@DisplayName("Should fall back to the default table when a field is absent from the mapping")
		@SuppressWarnings({ "unchecked", "rawtypes" })
		void testAddSortingQueryMappingFieldNotInMappingUsesDefaultTable() {
			// Given
			SelectSeekStepN orderedQuery = this.newOrderedQuery();
			Field<Object> nameField = mock(Field.class);
			SortField<Object> nameDesc = mock(SortField.class);
			Field<Object> idField = mock(Field.class);
			SortField<Object> idDesc = mock(SortField.class);
			given(SortingQueryBuilderTests.this.defaultTable.field("name")).willReturn((Field) nameField);
			given(nameField.desc()).willReturn((SortField) nameDesc);
			given(nameDesc.getName()).willReturn("name");
			given(SortingQueryBuilderTests.this.defaultTable.field("id")).willReturn((Field) idField);
			given(idField.desc()).willReturn((SortField) idDesc);
			given(SortingQueryBuilderTests.this.selectConditionStep.orderBy(any(Collection.class)))
				.willReturn(orderedQuery);

			// When
			SelectOrderByStep<?> result = SortingQueryBuilderTests.this.sortingQueryBuilder.addSortingQuery(
					SortingQueryBuilderTests.this.selectConditionStep,
					List.of(new SortPriorityRequestBodyDto("name", "desc")), Map.of(),
					SortingQueryBuilderTests.this.defaultTable);

			// Then
			assertThat(result).isSameAs(orderedQuery);
			then(SortingQueryBuilderTests.this.selectConditionStep).should().orderBy(List.of(nameDesc, idDesc));
			then(SortingQueryBuilderTests.this.defaultTable).should().field("name");
		}

		@Test
		@DisplayName("Should omit id tiebreaker when the table has no id field")
		@SuppressWarnings({ "unchecked", "rawtypes" })
		void testAddSortingQueryNullIdFieldOmitsTiebreaker() {
			// Given
			SelectSeekStepN orderedQuery = this.newOrderedQuery();
			Field<Object> nameField = mock(Field.class);
			SortField<Object> nameAsc = mock(SortField.class);
			given(SortingQueryBuilderTests.this.defaultTable.field("name")).willReturn((Field) nameField);
			given(nameField.asc()).willReturn((SortField) nameAsc);
			given(nameAsc.getName()).willReturn("name");
			given(SortingQueryBuilderTests.this.defaultTable.field("id")).willReturn(null);
			given(SortingQueryBuilderTests.this.selectConditionStep.orderBy(any(Collection.class)))
				.willReturn(orderedQuery);

			// When
			SelectOrderByStep<?> result = SortingQueryBuilderTests.this.sortingQueryBuilder.addSortingQuery(
					SortingQueryBuilderTests.this.selectConditionStep,
					List.of(new SortPriorityRequestBodyDto("name", "asc")), Map.of(),
					SortingQueryBuilderTests.this.defaultTable);

			// Then
			assertThat(result).isSameAs(orderedQuery);
			then(SortingQueryBuilderTests.this.selectConditionStep).should().orderBy(List.of(nameAsc));
		}

	}

	/**
	 * Tests that render real SQL (no DB connection) to verify how sort fields are
	 * resolved against the query projection. These guard against the list-page sort
	 * regressions: unknown colIds must be dropped (not produce {@code ORDER BY
	 * "<unknown>"} → HTTP 500), and a colId that names a SELECT alias must order by that
	 * alias rather than a same-named physical column.
	 */
	@Nested
	@DisplayName("addSortingQuery projection-aware resolution")
	class ProjectionAwareSortingTests {

		private final DSLContext dsl = DSL.using(SQLDialect.MYSQL);

		private SortPriorityRequestBodyDto sort(String fieldName, String order) {
			SortPriorityRequestBodyDto dto = new SortPriorityRequestBodyDto();
			dto.setField(fieldName);
			dto.setOrder(order);
			return dto;
		}

		/**
		 * Projection aliases "id" and "timesheetId"; physical "status" lives on the
		 * table.
		 */
		private SelectConditionStep<?> baseQuery() {
			Table<?> ts = DSL.table(DSL.name("cst_timesheet_t")).as("ts");
			return this.dsl
				.select(DSL.field(DSL.name("ts", "id")).as("id"), DSL.field(DSL.name("ts", "id")).as("timesheetId"),
						DSL.field(DSL.name("ts", "off_limit_status_id")).as("status"))
				.from(ts)
				.where(DSL.trueCondition());
		}

		@Test
		@DisplayName("Orders by the SELECT alias when the colId matches a projected column")
		void testSortsByProjectionAlias() {
			SelectOrderByStep<?> result = SortingQueryBuilderTests.this.sortingQueryBuilder
				.addSortingQuery(this.baseQuery(), List.of(this.sort("timesheetId", "asc")), DSL.table(DSL.name("ts")));

			String sql = result.toString().toLowerCase();
			assertThat(sql).contains("order by").contains("timesheetid");
		}

		@Test
		@DisplayName("Drops unknown colIds instead of emitting an invalid ORDER BY")
		void testSkipsUnknownField() {
			SelectOrderByStep<?> result = SortingQueryBuilderTests.this.sortingQueryBuilder.addSortingQuery(
					this.baseQuery(), List.of(this.sort("doesNotExist", "desc")), DSL.table(DSL.name("ts")));

			assertThat(result.toString().toLowerCase()).doesNotContain("order by");
			assertThat(result.toString().toLowerCase()).doesNotContain("doesnotexist");
		}

		@Test
		@DisplayName("Field-table-mapping overload also orders by the projected alias")
		void testMappingOverloadSortsByProjectionAlias() {
			Map<String, Table<?>> mapping = new HashMap<>();
			SelectOrderByStep<?> result = SortingQueryBuilderTests.this.sortingQueryBuilder.addSortingQuery(
					this.baseQuery(), List.of(this.sort("status", "desc")), mapping, DSL.table(DSL.name("ts")));

			String sql = result.toString().toLowerCase();
			assertThat(sql).contains("order by").contains("status");
		}

		@Test
		@DisplayName("Field-table-mapping overload drops unknown colIds")
		void testMappingOverloadSkipsUnknownField() {
			Map<String, Table<?>> mapping = new HashMap<>();
			SelectOrderByStep<?> result = SortingQueryBuilderTests.this.sortingQueryBuilder.addSortingQuery(
					this.baseQuery(), List.of(this.sort("ghostColumn", "asc")), mapping, DSL.table(DSL.name("ts")));

			assertThat(result.toString().toLowerCase()).doesNotContain("order by");
		}

	}

	/**
	 * Guards stable pagination for the infinite-scroll timesheet list. When the requested
	 * sort columns are not unique (e.g. period start, company name), MySQL is free to
	 * return tied rows in a different order for each LIMIT/OFFSET page, so the same
	 * timesheet can surface on two consecutive pages (or be skipped). Appending the
	 * table's {@code id} as a final tiebreaker makes the ordering total and the
	 * pagination deterministic. See the v1/timesheets/search/get flicker bug.
	 */
	@Nested
	@DisplayName("addSortingQuery id tiebreaker for stable pagination")
	class IdTiebreakerTests {

		private final DSLContext dsl = DSL.using(SQLDialect.MYSQL);

		private final CstTimesheetT ts = CstTimesheetT.CST_TIMESHEET_T;

		private SortPriorityRequestBodyDto sort(String fieldName, String order) {
			SortPriorityRequestBodyDto dto = new SortPriorityRequestBodyDto();
			dto.setField(fieldName);
			dto.setOrder(order);
			return dto;
		}

		/** Projects the {@code period_start} alias plus the physical {@code id}. */
		private SelectConditionStep<?> baseQuery() {
			return this.dsl.select(this.ts.ID.as("id"), this.ts.PERIOD_START.as("period_start"))
				.from(this.ts)
				.where(DSL.trueCondition());
		}

		private String orderByClause(SelectOrderByStep<?> result) {
			String sql = result.toString().toLowerCase();
			int idx = sql.indexOf("order by");
			assertThat(idx).as("query must have an ORDER BY").isGreaterThan(-1);
			return sql.substring(idx);
		}

		@Test
		@DisplayName("Single-table overload appends id after a non-unique sort column")
		void testSingleTableAppendsIdTiebreaker() {
			SelectOrderByStep<?> result = SortingQueryBuilderTests.this.sortingQueryBuilder
				.addSortingQuery(this.baseQuery(), List.of(this.sort("period_start", "desc")), this.ts);

			String orderBy = this.orderByClause(result);
			assertThat(orderBy).contains("period_start");
			assertThat(orderBy.substring(orderBy.indexOf("period_start")))
				.as("id must be appended as a tiebreaker after the requested sort column")
				.contains("id");
		}

		@Test
		@DisplayName("Field-table-mapping overload appends id after a non-unique sort column")
		void testMappingOverloadAppendsIdTiebreaker() {
			Map<String, Table<?>> mapping = new HashMap<>();
			mapping.put("period_start", this.ts);
			SelectOrderByStep<?> result = SortingQueryBuilderTests.this.sortingQueryBuilder
				.addSortingQuery(this.baseQuery(), List.of(this.sort("period_start", "desc")), mapping, this.ts);

			String orderBy = this.orderByClause(result);
			assertThat(orderBy).contains("period_start");
			assertThat(orderBy.substring(orderBy.indexOf("period_start")))
				.as("id must be appended as a tiebreaker after the requested sort column")
				.contains("id");
		}

		@Test
		@DisplayName("Does not append a duplicate id when the sort already orders by id")
		void testNoDuplicateIdWhenAlreadySortingById() {
			SelectOrderByStep<?> result = SortingQueryBuilderTests.this.sortingQueryBuilder
				.addSortingQuery(this.baseQuery(), List.of(this.sort("id", "asc")), this.ts);

			String orderBy = this.orderByClause(result);
			int first = orderBy.indexOf("id");
			int last = orderBy.lastIndexOf("id");
			assertThat(first).as("id must be present in ORDER BY").isGreaterThan(-1);
			assertThat(last).as("id must not be duplicated as a tiebreaker").isEqualTo(first);
		}

	}

}
