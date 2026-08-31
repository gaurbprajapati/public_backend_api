package io.recruitcrm.microservice.timesheet.search.filters.contractor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import io.recruitcrm.microservice.search.models.jooq.tables.Tblcandidate;
import io.recruitcrm.microservice.timesheet.search.constants.FilterTypes;
import io.recruitcrm.microservice.timesheet.search.dto.FilterDto;
import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.TableJoinSpecification;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContractorGroupBaseFilterNode Tests")
class ContractorGroupBaseFilterNodeTests {

	private FilterNodeContext filterNodeContext;

	private final Integer accountId = 1;

	private final String gmtDifference = "+00:00";

	@BeforeEach
	void setUp() {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("status");
		filterDto.setFilterType(FilterTypes.IS);
		filterDto.setFilterValue("0");
		filterDto.setGroupType("contractors");

		this.filterNodeContext = new FilterNodeContext();
		this.filterNodeContext.setAccountId(this.accountId);
		this.filterNodeContext.setGmtDifference(this.gmtDifference);
		this.filterNodeContext.setFilterDto(filterDto);
	}

	private ContractorGroupBaseFilterNode createConcreteNode() {
		// Concrete subclass that does NOT override getJoinTables so the default
		// implementation (returning getMinimalJoins) is exercised
		return new ContractorGroupBaseFilterNode(this.filterNodeContext) {
			@Override
			public List<Condition> getFilterConditions() {
				return List.of(DSL.trueCondition());
			}

			@Override
			public Field<?> getSearchField() {
				return CANDIDATE.ID;
			}
		};
	}

	@Test
	@DisplayName("Get join tables should return minimal joins by default")
	void testGetJoinTablesReturnsMinimalJoinsByDefault() {
		// Given
		ContractorGroupBaseFilterNode filterNode = this.createConcreteNode();

		// When
		List<TableJoinSpecification> joinTables = filterNode.getJoinTables();

		// Then
		assertThat(joinTables).isNotNull().isEmpty();
	}

	@Test
	@DisplayName("Get base table should return candidate table")
	void testGetBaseTableReturnsCandidateTable() {
		// Given
		ContractorGroupBaseFilterNode filterNode = this.createConcreteNode();

		// When
		Table<?> baseTable = filterNode.getBaseTable();

		// Then
		assertThat(baseTable).isNotNull().isEqualTo(Tblcandidate.TBLCANDIDATE);
	}

	@Test
	@DisplayName("Get select fields should return candidate ID field")
	void testGetSelectFieldsReturnsCandidateIdField() {
		// Given
		ContractorGroupBaseFilterNode filterNode = this.createConcreteNode();

		// When
		List<Field<?>> selectFields = filterNode.getSelectFields();

		// Then
		assertThat(selectFields).hasSize(1);
		assertThat(selectFields.get(0)).isEqualTo(Tblcandidate.TBLCANDIDATE.ID);
	}

	@Test
	@DisplayName("Get account ID filter condition should return condition with account ID")
	void testGetAccountIdFilterConditionReturnsConditionWithAccountId() {
		// Given
		ContractorGroupBaseFilterNode filterNode = this.createConcreteNode();

		// When
		Condition condition = filterNode.getAccountIdFilterCondition();

		// Then
		assertThat(condition).isNotNull();
	}

}
