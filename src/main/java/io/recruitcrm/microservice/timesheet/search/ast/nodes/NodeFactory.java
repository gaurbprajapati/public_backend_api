package io.recruitcrm.microservice.timesheet.search.ast.nodes;

import java.util.Locale;

import io.recruitcrm.microservice.timesheet.search.ast.nodes.conjoints.group.GroupANDNode;
import io.recruitcrm.microservice.timesheet.search.ast.nodes.conjoints.group.GroupConjointNode;
import io.recruitcrm.microservice.timesheet.search.ast.nodes.conjoints.group.GroupORNode;
import io.recruitcrm.microservice.timesheet.search.ast.nodes.conjoints.subgroup.SubGroupANDNode;
import io.recruitcrm.microservice.timesheet.search.ast.nodes.conjoints.subgroup.SubGroupConjointNode;
import io.recruitcrm.microservice.timesheet.search.ast.nodes.conjoints.subgroup.SubGroupORNode;
import io.recruitcrm.microservice.timesheet.search.ast.nodes.filters.FilterNode;
import io.recruitcrm.microservice.timesheet.search.constants.FilterTypes;
import io.recruitcrm.microservice.timesheet.search.constants.GroupTypes;
import io.recruitcrm.microservice.timesheet.search.constants.SearchFieldConstants;
import io.recruitcrm.microservice.timesheet.search.dto.FilterDto;
import io.recruitcrm.microservice.timesheet.search.filters.FilterNodeContext;
import io.recruitcrm.microservice.timesheet.search.filters.IFilterNode;
import io.recruitcrm.microservice.timesheet.search.filters.timesheet.added_on.IsAfterFilterNode;
import io.recruitcrm.microservice.timesheet.search.filters.timesheet.added_on.IsBeforeFilterNode;
import io.recruitcrm.microservice.timesheet.search.filters.timesheet.added_on.IsBetweenFilterNode;
import io.recruitcrm.microservice.timesheet.search.filters.timesheet.added_on.IsFilterNode;
import io.recruitcrm.microservice.timesheet.search.filters.timesheet.added_on.IsNotFilterNode;

public final class NodeFactory {

	private final Integer accountId;

	private final String gmtDifference;

	public NodeFactory(Integer accountId, String gmtDifference) {
		this.accountId = accountId;
		this.gmtDifference = gmtDifference;
	}

	public GroupConjointNode createGroupConjointNode(String groupJoinOperator) {
		try {
			GroupTypes groupType = GroupTypes.valueOf(groupJoinOperator.toUpperCase(Locale.ROOT));
			if (groupType == GroupTypes.OR) {
				return new GroupORNode();
			}
			return new GroupANDNode();
		}
		catch (IllegalArgumentException ex) {
			// Default to AND if invalid value
			return new GroupANDNode();
		}
	}

	public SubGroupConjointNode createSubGroupConjointNode(String subGroupJoinOperator) {
		try {
			GroupTypes groupType = GroupTypes.valueOf(subGroupJoinOperator.toUpperCase(Locale.ROOT));
			if (groupType == GroupTypes.OR) {
				return new SubGroupORNode();
			}
			return new SubGroupANDNode();
		}
		catch (IllegalArgumentException ex) {
			// Default to AND if invalid value
			return new SubGroupANDNode();
		}
	}

	public FilterNode createFilterNode(FilterDto filterDto) {
		FilterNodeContext filterNodeContext = new FilterNodeContext();
		filterNodeContext.setFilterDto(filterDto);
		filterNodeContext.setAccountId(this.accountId);
		filterNodeContext.setGmtDifference(this.gmtDifference);

		IFilterNode filterNodeImpl = this.createFilterNodeImpl(filterDto, filterNodeContext);
		return new FilterNode(filterNodeImpl, filterNodeContext);
	}

	private IFilterNode createFilterNodeImpl(FilterDto filterDto, FilterNodeContext filterNodeContext) {
		FilterTypes filterType = filterDto.getFilterType();
		String dbField = filterDto.getDbField();
		String groupType = filterDto.getGroupType();

		// Route to appropriate filter node based on dbField, filterType, and groupType
		// Check groupType first to distinguish between contractor and timesheet filters
		if (SearchFieldConstants.GROUP_TYPE_CONTRACTORS.equalsIgnoreCase(groupType)) {
			return this.createContractorFilterNode(dbField, filterType, filterNodeContext);
		}

		// Timesheet-specific filters (default)
		return this.createTimesheetFilterNode(dbField, filterType, filterNodeContext);
	}

	private IFilterNode createContractorFilterNode(String dbField, FilterTypes filterType,
			FilterNodeContext filterNodeContext) {
		if (this.isDealField(dbField)) {
			return this.createContractorDealFilterNode(filterType, filterNodeContext);
		}
		if (this.isJobField(dbField)) {
			return this.createContractorJobFilterNode(filterType, filterNodeContext);
		}
		if (SearchFieldConstants.FIELD_STATUS.equals(dbField)) {
			return this.createStatusFilterNode(filterType, filterNodeContext);
		}
		throw new IllegalArgumentException("Unknown contractor dbField: " + dbField);
	}

	private IFilterNode createTimesheetFilterNode(String dbField, FilterTypes filterType,
			FilterNodeContext filterNodeContext) {
		if (SearchFieldConstants.FIELD_ADDED_ON.equals(dbField)) {
			return this.createAddedOnFilterNode(filterType, filterNodeContext);
		}
		if (SearchFieldConstants.FIELD_TIMESHEET_PERIOD.equals(dbField)) {
			return this.createTimesheetPeriodFilterNode(filterType, filterNodeContext);
		}
		if (this.isDealField(dbField)) {
			return this.createAssociatedDealFilterNode(filterType, filterNodeContext);
		}
		if (this.isJobField(dbField)) {
			return this.createJobFilterNode(filterType, filterNodeContext);
		}
		if (this.isTimesheetStatusField(dbField)) {
			return this.createTimesheetStatusFilterNode(filterType, filterNodeContext);
		}
		if (SearchFieldConstants.FIELD_COMPANY_NAME.equals(dbField)) {
			return this.createCompanyFilterNode(filterType, filterNodeContext);
		}
		if (SearchFieldConstants.FIELD_STATUS.equals(dbField)) {
			return this.createStatusFilterNode(filterType, filterNodeContext);
		}
		throw new IllegalArgumentException("Unknown timesheet dbField: " + dbField);
	}

	private boolean isDealField(String dbField) {
		return SearchFieldConstants.FIELD_DEAL_NAME.equals(dbField)
				|| SearchFieldConstants.FIELD_ASSOCIATED_DEAL.equals(dbField)
				|| SearchFieldConstants.FIELD_DEAL.equals(dbField);
	}

	private boolean isJobField(String dbField) {
		return SearchFieldConstants.FIELD_JOB_NAME_SNAKE.equals(dbField)
				|| SearchFieldConstants.FIELD_JOB_NAME_CAMEL.equals(dbField)
				|| SearchFieldConstants.FIELD_JOB.equals(dbField);
	}

	private boolean isTimesheetStatusField(String dbField) {
		return SearchFieldConstants.FIELD_TIMESHEET_STATUS_SNAKE.equals(dbField)
				|| SearchFieldConstants.FIELD_TIMESHEET_STATUS_CAMEL.equals(dbField)
				|| SearchFieldConstants.FIELD_TIMESHEET_STATUS_ID_SNAKE.equals(dbField)
				|| SearchFieldConstants.FIELD_TIMESHEET_STATUS_ID_CAMEL.equals(dbField);
	}

	private IFilterNode createAddedOnFilterNode(FilterTypes filterType, FilterNodeContext filterNodeContext) {
		return switch (filterType) {
			case IS -> new IsFilterNode(filterNodeContext);
			case IS_NOT -> new IsNotFilterNode(filterNodeContext);
			case IS_BEFORE -> new IsBeforeFilterNode(filterNodeContext);
			case IS_AFTER -> new IsAfterFilterNode(filterNodeContext);
			case IS_BETWEEN -> new IsBetweenFilterNode(filterNodeContext);
			default -> throw new IllegalArgumentException("Unsupported filter type for added_on: " + filterType);
		};
	}

	private IFilterNode createTimesheetPeriodFilterNode(FilterTypes filterType, FilterNodeContext filterNodeContext) {
		return switch (filterType) {
			case IS -> new io.recruitcrm.microservice.timesheet.search.filters.timesheet.timesheet_period.IsFilterNode(
					filterNodeContext);
			case IS_EQUAL_TO ->
				new io.recruitcrm.microservice.timesheet.search.filters.timesheet.timesheet_period.IsEqualToFilterNode(
						filterNodeContext);
			case IS_BEFORE ->
				new io.recruitcrm.microservice.timesheet.search.filters.timesheet.timesheet_period.IsBeforeFilterNode(
						filterNodeContext);
			case IS_AFTER ->
				new io.recruitcrm.microservice.timesheet.search.filters.timesheet.timesheet_period.IsAfterFilterNode(
						filterNodeContext);
			case IS_BETWEEN ->
				new io.recruitcrm.microservice.timesheet.search.filters.timesheet.timesheet_period.IsBetweenFilterNode(
						filterNodeContext);
			case IS_NOT_BETWEEN ->
				new io.recruitcrm.microservice.timesheet.search.filters.timesheet.timesheet_period.IsNotBetweenFilterNode(
						filterNodeContext);
			case IS_LESS_THAN ->
				new io.recruitcrm.microservice.timesheet.search.filters.timesheet.timesheet_period.IsLessThanFilterNode(
						filterNodeContext);
			case IS_MORE_THAN ->
				new io.recruitcrm.microservice.timesheet.search.filters.timesheet.timesheet_period.IsMoreThanFilterNode(
						filterNodeContext);
			case HAS_ANY_VALUE ->
				new io.recruitcrm.microservice.timesheet.search.filters.timesheet.timesheet_period.HasAnyValueFilterNode(
						filterNodeContext);
			case IS_EMPTY ->
				new io.recruitcrm.microservice.timesheet.search.filters.timesheet.timesheet_period.IsEmptyFilterNode(
						filterNodeContext);
			default ->
				throw new IllegalArgumentException("Unsupported filter type for timesheet_period: " + filterType);
		};
	}

	private IFilterNode createAssociatedDealFilterNode(FilterTypes filterType, FilterNodeContext filterNodeContext) {
		return switch (filterType) {
			case IS -> new io.recruitcrm.microservice.timesheet.search.filters.timesheet.associated_deal.IsFilterNode(
					filterNodeContext);
			case IS_NOT ->
				new io.recruitcrm.microservice.timesheet.search.filters.timesheet.associated_deal.IsNotFilterNode(
						filterNodeContext);
			case CONTAINS_AT_LEAST_ONE ->
				new io.recruitcrm.microservice.timesheet.search.filters.timesheet.associated_deal.ContainsAtLeastFilterNode(
						filterNodeContext);
			case CONTAINS ->
				new io.recruitcrm.microservice.timesheet.search.filters.timesheet.associated_deal.ContainsFilterNode(
						filterNodeContext);
			case DOES_NOT_CONTAIN ->
				new io.recruitcrm.microservice.timesheet.search.filters.timesheet.associated_deal.DoesNotContainFilterNode(
						filterNodeContext);
			case HAS_ANY_VALUE ->
				new io.recruitcrm.microservice.timesheet.search.filters.timesheet.associated_deal.HasAnyValueFilterNode(
						filterNodeContext);
			case IS_EMPTY ->
				new io.recruitcrm.microservice.timesheet.search.filters.timesheet.associated_deal.IsEmptyFilterNode(
						filterNodeContext);
			default -> throw new IllegalArgumentException("Unsupported filter type for associated_deal: " + filterType);
		};
	}

	private IFilterNode createJobFilterNode(FilterTypes filterType, FilterNodeContext filterNodeContext) {
		return switch (filterType) {
			case IS ->
				new io.recruitcrm.microservice.timesheet.search.filters.timesheet.job.IsFilterNode(filterNodeContext);
			case IS_NOT -> new io.recruitcrm.microservice.timesheet.search.filters.timesheet.job.IsNotFilterNode(
					filterNodeContext);
			case CONTAINS_AT_LEAST_ONE ->
				new io.recruitcrm.microservice.timesheet.search.filters.timesheet.job.ContainsAtLeastFilterNode(
						filterNodeContext);
			case DOES_NOT_CONTAIN ->
				new io.recruitcrm.microservice.timesheet.search.filters.timesheet.job.DoesNotContainFilterNode(
						filterNodeContext);
			case HAS_ANY_VALUE ->
				new io.recruitcrm.microservice.timesheet.search.filters.timesheet.job.HasAnyValueFilterNode(
						filterNodeContext);
			case IS_EMPTY -> new io.recruitcrm.microservice.timesheet.search.filters.timesheet.job.IsEmptyFilterNode(
					filterNodeContext);
			default -> throw new IllegalArgumentException("Unsupported filter type for job: " + filterType);
		};
	}

	private IFilterNode createTimesheetStatusFilterNode(FilterTypes filterType, FilterNodeContext filterNodeContext) {
		return switch (filterType) {
			case IS -> new io.recruitcrm.microservice.timesheet.search.filters.timesheet.timesheet_status.IsFilterNode(
					filterNodeContext);
			case IS_NOT ->
				new io.recruitcrm.microservice.timesheet.search.filters.timesheet.timesheet_status.IsNotFilterNode(
						filterNodeContext);
			case CONTAINS_AT_LEAST_ONE ->
				new io.recruitcrm.microservice.timesheet.search.filters.timesheet.timesheet_status.ContainsAtLeastFilterNode(
						filterNodeContext);
			case DOES_NOT_CONTAIN ->
				new io.recruitcrm.microservice.timesheet.search.filters.timesheet.timesheet_status.DoesNotContainFilterNode(
						filterNodeContext);
			case HAS_ANY_VALUE ->
				new io.recruitcrm.microservice.timesheet.search.filters.timesheet.timesheet_status.HasAnyValueFilterNode(
						filterNodeContext);
			case IS_EMPTY ->
				new io.recruitcrm.microservice.timesheet.search.filters.timesheet.timesheet_status.IsEmptyFilterNode(
						filterNodeContext);
			default ->
				throw new IllegalArgumentException("Unsupported filter type for timesheet_status: " + filterType);
		};
	}

	private IFilterNode createCompanyFilterNode(FilterTypes filterType, FilterNodeContext filterNodeContext) {
		return switch (filterType) {
			case IS -> new io.recruitcrm.microservice.timesheet.search.filters.timesheet.company.IsFilterNode(
					filterNodeContext);
			case IS_NOT -> new io.recruitcrm.microservice.timesheet.search.filters.timesheet.company.IsNotFilterNode(
					filterNodeContext);
			case CONTAINS_AT_LEAST_ONE ->
				new io.recruitcrm.microservice.timesheet.search.filters.timesheet.company.ContainsAtLeastFilterNode(
						filterNodeContext);
			case DOES_NOT_CONTAIN ->
				new io.recruitcrm.microservice.timesheet.search.filters.timesheet.company.DoesNotContainFilterNode(
						filterNodeContext);
			case HAS_ANY_VALUE ->
				new io.recruitcrm.microservice.timesheet.search.filters.timesheet.company.HasAnyValueFilterNode(
						filterNodeContext);
			case IS_EMPTY ->
				new io.recruitcrm.microservice.timesheet.search.filters.timesheet.company.IsEmptyFilterNode(
						filterNodeContext);
			default -> throw new IllegalArgumentException("Unsupported filter type for company: " + filterType);
		};
	}

	private IFilterNode createStatusFilterNode(FilterTypes filterType, FilterNodeContext filterNodeContext) {
		return switch (filterType) {
			case IS -> new io.recruitcrm.microservice.timesheet.search.filters.contractor.status.IsFilterNode(
					filterNodeContext);
			default -> throw new IllegalArgumentException("Unsupported filter type for status: " + filterType);
		};
	}

	private IFilterNode createContractorDealFilterNode(FilterTypes filterType, FilterNodeContext filterNodeContext) {
		return switch (filterType) {
			case IS ->
				new io.recruitcrm.microservice.timesheet.search.filters.contractor.deal.IsFilterNode(filterNodeContext);
			case IS_NOT -> new io.recruitcrm.microservice.timesheet.search.filters.contractor.deal.IsNotFilterNode(
					filterNodeContext);
			case CONTAINS_AT_LEAST_ONE ->
				new io.recruitcrm.microservice.timesheet.search.filters.contractor.deal.ContainsAtLeastFilterNode(
						filterNodeContext);
			case CONTAINS -> new io.recruitcrm.microservice.timesheet.search.filters.contractor.deal.ContainsFilterNode(
					filterNodeContext);
			case DOES_NOT_CONTAIN ->
				new io.recruitcrm.microservice.timesheet.search.filters.contractor.deal.DoesNotContainFilterNode(
						filterNodeContext);
			case HAS_ANY_VALUE ->
				new io.recruitcrm.microservice.timesheet.search.filters.contractor.deal.HasAnyValueFilterNode(
						filterNodeContext);
			case IS_EMPTY -> new io.recruitcrm.microservice.timesheet.search.filters.contractor.deal.IsEmptyFilterNode(
					filterNodeContext);
			default -> throw new IllegalArgumentException("Unsupported filter type for contractor deal: " + filterType);
		};
	}

	private IFilterNode createContractorJobFilterNode(FilterTypes filterType, FilterNodeContext filterNodeContext) {
		return switch (filterType) {
			case IS ->
				new io.recruitcrm.microservice.timesheet.search.filters.contractor.job.IsFilterNode(filterNodeContext);
			case IS_NOT -> new io.recruitcrm.microservice.timesheet.search.filters.contractor.job.IsNotFilterNode(
					filterNodeContext);
			case CONTAINS_AT_LEAST_ONE ->
				new io.recruitcrm.microservice.timesheet.search.filters.contractor.job.ContainsAtLeastFilterNode(
						filterNodeContext);
			case DOES_NOT_CONTAIN ->
				new io.recruitcrm.microservice.timesheet.search.filters.contractor.job.DoesNotContainFilterNode(
						filterNodeContext);
			case HAS_ANY_VALUE ->
				new io.recruitcrm.microservice.timesheet.search.filters.contractor.job.HasAnyValueFilterNode(
						filterNodeContext);
			case IS_EMPTY -> new io.recruitcrm.microservice.timesheet.search.filters.contractor.job.IsEmptyFilterNode(
					filterNodeContext);
			case CONTAINS -> new io.recruitcrm.microservice.timesheet.search.filters.contractor.job.ContainsFilterNode(
					filterNodeContext);
			default -> throw new IllegalArgumentException("Unsupported filter type for contractor job: " + filterType);
		};
	}

}
