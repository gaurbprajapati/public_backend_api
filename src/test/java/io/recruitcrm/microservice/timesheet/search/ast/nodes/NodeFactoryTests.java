package io.recruitcrm.microservice.timesheet.search.ast.nodes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.recruitcrm.microservice.timesheet.search.ast.nodes.conjoints.group.GroupANDNode;
import io.recruitcrm.microservice.timesheet.search.ast.nodes.conjoints.group.GroupConjointNode;
import io.recruitcrm.microservice.timesheet.search.ast.nodes.conjoints.group.GroupORNode;
import io.recruitcrm.microservice.timesheet.search.ast.nodes.conjoints.subgroup.SubGroupANDNode;
import io.recruitcrm.microservice.timesheet.search.ast.nodes.conjoints.subgroup.SubGroupConjointNode;
import io.recruitcrm.microservice.timesheet.search.ast.nodes.conjoints.subgroup.SubGroupORNode;
import io.recruitcrm.microservice.timesheet.search.ast.nodes.filters.FilterNode;
import io.recruitcrm.microservice.timesheet.search.constants.FilterTypes;
import io.recruitcrm.microservice.timesheet.search.dto.FilterDto;

import java.util.stream.Stream;

@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@DisplayName("NodeFactory Tests")
class NodeFactoryTests {

	private NodeFactory nodeFactory;

	private final Integer accountId = 1;

	private final String gmtDifference = "+05:30";

	@BeforeEach
	void setUp() {
		this.nodeFactory = new NodeFactory(this.accountId, this.gmtDifference);
	}

	@ParameterizedTest(name = "[{index}] operator={0} -> {1}")
	@MethodSource("groupConjointNodeCases")
	@DisplayName("createGroupConjointNode should create the expected node (case-insensitive, defaulting to AND)")
	void testCreateGroupConjointNode(String operator, Class<? extends GroupConjointNode> expectedType) {
		GroupConjointNode result = this.nodeFactory.createGroupConjointNode(operator);

		assertThat(result).isNotNull().isInstanceOf(expectedType);
	}

	private static Stream<Arguments> groupConjointNodeCases() {
		return Stream.of(Arguments.of("AND", GroupANDNode.class), Arguments.of("OR", GroupORNode.class),
				Arguments.of("and", GroupANDNode.class), Arguments.of("invalid", GroupANDNode.class));
	}

	@ParameterizedTest(name = "[{index}] operator={0} -> {1}")
	@MethodSource("subGroupConjointNodeCases")
	@DisplayName("createSubGroupConjointNode should create the expected node (case-insensitive, defaulting to AND)")
	void testCreateSubGroupConjointNode(String operator, Class<? extends SubGroupConjointNode> expectedType) {
		SubGroupConjointNode result = this.nodeFactory.createSubGroupConjointNode(operator);

		assertThat(result).isNotNull().isInstanceOf(expectedType);
	}

	private static Stream<Arguments> subGroupConjointNodeCases() {
		return Stream.of(Arguments.of("AND", SubGroupANDNode.class), Arguments.of("OR", SubGroupORNode.class),
				Arguments.of("and", SubGroupANDNode.class), Arguments.of("invalid", SubGroupANDNode.class));
	}

	@ParameterizedTest(name = "[{index}] dbField={0}, filterType={1}, groupType={3} -> {4}")
	@MethodSource("createFilterNodeCases")
	@DisplayName("createFilterNode should create the expected filter node implementation")
	void testCreateFilterNodeCreatesExpectedImpl(String dbField, FilterTypes filterType, String filterValue,
			String groupType, String expectedSimpleName) {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField(dbField);
		filterDto.setFilterType(filterType);
		filterDto.setFilterValue(filterValue);
		filterDto.setGroupType(groupType);

		FilterNode result = this.nodeFactory.createFilterNode(filterDto);

		assertThat(result).isNotNull();
		assertThat(result.getFilterNodeImpl().getClass().getSimpleName()).isEqualTo(expectedSimpleName);
	}

	private static Stream<Arguments> createFilterNodeCases() {
		return Stream.of(
				// timesheet IS / IS_NOT / multi-value / has-any / is-empty /
				// does-not-contain
				// across all supported db fields and group-type routing
				Arguments.of("added_on", FilterTypes.IS, "TODAY", "AND", "IsFilterNode"),
				Arguments.of("timesheetPeriod", FilterTypes.IS, "TODAY", "AND", "IsFilterNode"),
				Arguments.of("companyName", FilterTypes.IS, "1,2,3", "AND", "IsFilterNode"),
				Arguments.of("associatedDeal", FilterTypes.IS, "1,2,3", "AND", "IsFilterNode"),
				Arguments.of("dealName", FilterTypes.IS, "1,2,3", "AND", "IsFilterNode"),
				Arguments.of("jobName", FilterTypes.IS, "1,2,3", "AND", "IsFilterNode"),
				Arguments.of("job_name", FilterTypes.IS, "1,2,3", "AND", "IsFilterNode"),
				Arguments.of("job", FilterTypes.IS, "1,2,3", "AND", "IsFilterNode"),
				Arguments.of("timesheetStatus", FilterTypes.IS, "1,2,3", "AND", "IsFilterNode"),
				Arguments.of("timesheet_status", FilterTypes.IS, "1,2,3", "AND", "IsFilterNode"),
				Arguments.of("timesheet_status_id", FilterTypes.IS, "1,2,3", "AND", "IsFilterNode"),
				Arguments.of("timesheetStatusId", FilterTypes.IS, "1,2,3", "AND", "IsFilterNode"),
				Arguments.of("status", FilterTypes.IS, "0", "contractors", "IsFilterNode"),
				Arguments.of("status", FilterTypes.IS, "0", "AND", "IsFilterNode"),
				Arguments.of("dealName", FilterTypes.IS, "1,2,3", "contractors", "IsFilterNode"),
				Arguments.of("deal", FilterTypes.IS, "1,2,3", "contractors", "IsFilterNode"),
				Arguments.of("jobName", FilterTypes.IS, "1,2,3", "contractors", "IsFilterNode"),
				Arguments.of("added_on", FilterTypes.IS_NOT, "TODAY", "AND", "IsNotFilterNode"),
				Arguments.of("companyName", FilterTypes.IS_NOT, "1,2,3", "AND", "IsNotFilterNode"),
				Arguments.of("associatedDeal", FilterTypes.IS_NOT, "1,2,3", "AND", "IsNotFilterNode"),
				Arguments.of("jobName", FilterTypes.IS_NOT, "1,2,3", "AND", "IsNotFilterNode"),
				Arguments.of("timesheetStatus", FilterTypes.IS_NOT, "1,2,3", "AND", "IsNotFilterNode"),
				Arguments.of("dealName", FilterTypes.IS_NOT, "1,2,3", "contractors", "IsNotFilterNode"),
				Arguments.of("jobName", FilterTypes.IS_NOT, "1,2,3", "contractors", "IsNotFilterNode"),
				Arguments.of("associatedDeal", FilterTypes.CONTAINS_AT_LEAST_ONE, "1,2,3", "AND",
						"ContainsAtLeastFilterNode"),
				Arguments.of("companyName", FilterTypes.CONTAINS_AT_LEAST_ONE, "1,2,3", "AND",
						"ContainsAtLeastFilterNode"),
				Arguments.of("jobName", FilterTypes.CONTAINS_AT_LEAST_ONE, "1,2,3", "AND", "ContainsAtLeastFilterNode"),
				Arguments.of("timesheetStatus", FilterTypes.CONTAINS_AT_LEAST_ONE, "1,2,3", "AND",
						"ContainsAtLeastFilterNode"),
				Arguments.of("dealName", FilterTypes.CONTAINS_AT_LEAST_ONE, "1,2,3", "contractors",
						"ContainsAtLeastFilterNode"),
				Arguments.of("jobName", FilterTypes.CONTAINS_AT_LEAST_ONE, "1,2,3", "contractors",
						"ContainsAtLeastFilterNode"),
				Arguments.of("associatedDeal", FilterTypes.HAS_ANY_VALUE, "", "AND", "HasAnyValueFilterNode"),
				Arguments.of("companyName", FilterTypes.HAS_ANY_VALUE, "", "AND", "HasAnyValueFilterNode"),
				Arguments.of("timesheetPeriod", FilterTypes.HAS_ANY_VALUE, "", "AND", "HasAnyValueFilterNode"),
				Arguments.of("jobName", FilterTypes.HAS_ANY_VALUE, "", "AND", "HasAnyValueFilterNode"),
				Arguments.of("timesheetStatus", FilterTypes.HAS_ANY_VALUE, "", "AND", "HasAnyValueFilterNode"),
				Arguments.of("dealName", FilterTypes.HAS_ANY_VALUE, "", "contractors", "HasAnyValueFilterNode"),
				Arguments.of("jobName", FilterTypes.HAS_ANY_VALUE, "", "contractors", "HasAnyValueFilterNode"),
				Arguments.of("associatedDeal", FilterTypes.IS_EMPTY, "", "AND", "IsEmptyFilterNode"),
				Arguments.of("companyName", FilterTypes.IS_EMPTY, "", "AND", "IsEmptyFilterNode"),
				Arguments.of("timesheetPeriod", FilterTypes.IS_EMPTY, "", "AND", "IsEmptyFilterNode"),
				Arguments.of("jobName", FilterTypes.IS_EMPTY, "", "AND", "IsEmptyFilterNode"),
				Arguments.of("timesheetStatus", FilterTypes.IS_EMPTY, "", "AND", "IsEmptyFilterNode"),
				Arguments.of("dealName", FilterTypes.IS_EMPTY, "", "contractors", "IsEmptyFilterNode"),
				Arguments.of("jobName", FilterTypes.IS_EMPTY, "", "contractors", "IsEmptyFilterNode"),
				Arguments.of("companyName", FilterTypes.DOES_NOT_CONTAIN, "1,2,3", "AND", "DoesNotContainFilterNode"),
				Arguments.of("associatedDeal", FilterTypes.DOES_NOT_CONTAIN, "1,2,3", "AND",
						"DoesNotContainFilterNode"),
				Arguments.of("jobName", FilterTypes.DOES_NOT_CONTAIN, "1,2,3", "AND", "DoesNotContainFilterNode"),
				Arguments.of("timesheetStatus", FilterTypes.DOES_NOT_CONTAIN, "1,2,3", "AND",
						"DoesNotContainFilterNode"),
				Arguments.of("dealName", FilterTypes.DOES_NOT_CONTAIN, "1,2,3", "contractors",
						"DoesNotContainFilterNode"),
				Arguments.of("jobName", FilterTypes.DOES_NOT_CONTAIN, "1,2,3", "contractors",
						"DoesNotContainFilterNode"),
				// CONTAINS routing (contractor job, contractor deal, associated deal)
				Arguments.of("jobName", FilterTypes.CONTAINS, "1,2,3", "contractors", "ContainsFilterNode"),
				Arguments.of("dealName", FilterTypes.CONTAINS, "1,2,3", "contractors", "ContainsFilterNode"),
				Arguments.of("associatedDeal", FilterTypes.CONTAINS, "1,2,3", "AND", "ContainsFilterNode"),
				// added_on date filters
				Arguments.of("added_on", FilterTypes.IS_BEFORE, "TODAY", "AND", "IsBeforeFilterNode"),
				Arguments.of("added_on", FilterTypes.IS_AFTER, "TODAY", "AND", "IsAfterFilterNode"),
				Arguments.of("added_on", FilterTypes.IS_BETWEEN, "{\"start\":\"1633046400\",\"end\":\"1635724800\"}",
						"AND", "IsBetweenFilterNode"),
				// timesheetPeriod date / numeric filters
				Arguments.of("timesheetPeriod", FilterTypes.IS_LESS_THAN, "10", "AND", "IsLessThanFilterNode"),
				Arguments.of("timesheetPeriod", FilterTypes.IS_MORE_THAN, "10", "AND", "IsMoreThanFilterNode"),
				Arguments.of("timesheetPeriod", FilterTypes.IS_EQUAL_TO, "1633046400", "AND", "IsEqualToFilterNode"),
				Arguments.of("timesheetPeriod", FilterTypes.IS_BEFORE, "1633046400", "AND", "IsBeforeFilterNode"),
				Arguments.of("timesheetPeriod", FilterTypes.IS_AFTER, "1633046400", "AND", "IsAfterFilterNode"),
				Arguments.of("timesheetPeriod", FilterTypes.IS_BETWEEN, "{\"start\":1633046400,\"end\":1635724800}",
						"AND", "IsBetweenFilterNode"),
				Arguments.of("timesheetPeriod", FilterTypes.IS_NOT_BETWEEN, "{\"start\":1633046400,\"end\":1635724800}",
						"AND", "IsNotBetweenFilterNode"));
	}

	@ParameterizedTest(name = "[{index}] dbField={0}, filterType={1}, groupType={2}")
	@MethodSource("createFilterNodeThrowsCases")
	@DisplayName("createFilterNode should throw IllegalArgumentException for unsupported or unknown configurations")
	void testCreateFilterNodeThrowsIllegalArgument(String dbField, FilterTypes filterType, String groupType,
			String expectedMessageSubstring) {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField(dbField);
		filterDto.setFilterType(filterType);
		filterDto.setFilterValue("test");
		filterDto.setGroupType(groupType);

		assertThatThrownBy(() -> this.nodeFactory.createFilterNode(filterDto))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(expectedMessageSubstring);
	}

	private static Stream<Arguments> createFilterNodeThrowsCases() {
		return Stream.of(
				Arguments.of("associatedDeal", FilterTypes.IS_BEFORE, "AND",
						"Unsupported filter type for associated_deal"),
				Arguments.of("timesheetPeriod", FilterTypes.IS_NOT, "AND",
						"Unsupported filter type for timesheet_period"),
				Arguments.of("companyName", FilterTypes.IS_BEFORE, "AND", "Unsupported filter type for company"),
				Arguments.of("jobName", FilterTypes.IS_BEFORE, "AND", "Unsupported filter type for job"),
				Arguments.of("timesheetStatus", FilterTypes.IS_BEFORE, "AND",
						"Unsupported filter type for timesheet_status"),
				Arguments.of("dealName", FilterTypes.IS_BEFORE, "contractors",
						"Unsupported filter type for contractor deal"),
				Arguments.of("added_on", FilterTypes.IS_EMPTY, "AND", "Unsupported filter type for added_on"),
				Arguments.of("status", FilterTypes.IS_NOT, "contractors", "Unsupported filter type for status"),
				Arguments.of("jobName", FilterTypes.IS_BEFORE, "contractors",
						"Unsupported filter type for contractor job"),
				Arguments.of("unknown_field", FilterTypes.IS, "AND", "Unknown timesheet dbField: unknown_field"),
				Arguments.of("unknown_contractor_field", FilterTypes.IS, "contractors",
						"Unknown contractor dbField: unknown_contractor_field"));
	}

	@Test
	@DisplayName("createFilterNode should set accountId and gmtDifference in FilterNodeContext")
	void testCreateFilterNodeSetsContext() {
		FilterDto filterDto = new FilterDto();
		filterDto.setDbField("added_on");
		filterDto.setFilterType(FilterTypes.IS);
		filterDto.setFilterValue("TODAY");
		filterDto.setGroupType("AND");

		FilterNode result = this.nodeFactory.createFilterNode(filterDto);

		assertThat(result).isNotNull();
		assertThat(result.getFilterNodeContext().getAccountId()).isEqualTo(this.accountId);
		assertThat(result.getFilterNodeContext().getGmtDifference()).isEqualTo(this.gmtDifference);
		assertThat(result.getFilterNodeContext().getFilterDto()).isEqualTo(filterDto);
	}

}
