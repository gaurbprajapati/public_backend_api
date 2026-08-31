package io.recruitcrm.microservice.timesheet.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetDealListQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetJobAndContractorListQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetListResponseBodyDto;
import io.recruitcrm.microservice.timesheet.testdata.TimesheetTestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class CustomTimeSheetMapperTests {

	private final CustomTimeSheetMapper customTimeSheetMapper = new CustomTimeSheetMapper();

	// ===== listTimeSheetRequestToResponseBodyDto (Deal-based) Tests =====

	@Test
	@DisplayName("Deal list mapping should set isReimbursementEnabled to 0 when projection value is null")
	void testDealListMappingIsReimbursementEnabledNullDefaultsToZero() {
		// Given
		List<TimesheetDealListQueryResultDto> projections = TimesheetTestDataFactory
			.createTimesheetDealListQueryResultList();
		projections.get(0).setIsReimbursementEnabled(null);

		// When
		List<TimesheetListResponseBodyDto> result = this.customTimeSheetMapper
			.listTimeSheetRequestToResponseBodyDto(projections);

		// Then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getIsReimbursementEnabled()).isZero();
	}

	@Test
	@DisplayName("Deal list mapping should preserve isReimbursementEnabled when projection value is 1")
	void testDealListMappingIsReimbursementEnabledOneReturnsOne() {
		// Given
		List<TimesheetDealListQueryResultDto> projections = TimesheetTestDataFactory
			.createTimesheetDealListQueryResultList();
		projections.get(0).setIsReimbursementEnabled(1);

		// When
		List<TimesheetListResponseBodyDto> result = this.customTimeSheetMapper
			.listTimeSheetRequestToResponseBodyDto(projections);

		// Then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getIsReimbursementEnabled()).isEqualTo(1);
	}

	@Test
	@DisplayName("Deal list mapping should preserve isReimbursementEnabled when projection value is 0")
	void testDealListMappingIsReimbursementEnabledZeroReturnsZero() {
		// Given
		List<TimesheetDealListQueryResultDto> projections = TimesheetTestDataFactory
			.createTimesheetDealListQueryResultList();
		projections.get(0).setIsReimbursementEnabled(0);

		// When
		List<TimesheetListResponseBodyDto> result = this.customTimeSheetMapper
			.listTimeSheetRequestToResponseBodyDto(projections);

		// Then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getIsReimbursementEnabled()).isZero();
	}

	@Test
	@DisplayName("Deal list mapping should set contractor off-limit status data on the response")
	void testDealListMappingSetsContractorOffLimitData() {
		// Given
		List<TimesheetDealListQueryResultDto> projections = TimesheetTestDataFactory
			.createTimesheetDealListQueryResultList();

		// When
		List<TimesheetListResponseBodyDto> result = this.customTimeSheetMapper
			.listTimeSheetRequestToResponseBodyDto(projections);

		// Then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getContractor()).isNotNull();
		assertThat(result.get(0).getContractor().getOffLimitStatusId()).isEqualTo(20);
		assertThat(result.get(0).getContractor().getStatusLabel()).isEqualTo("Do Not Contact");
		assertThat(result.get(0).getContractor().getBackgroundColorHex()).isEqualTo("#00FF00");
		assertThat(result.get(0).getContractor().getTextColorHex()).isEqualTo("#000000");
		assertThat(result.get(0).getContractor().getOffLimitReason()).isEqualTo("Non-compete agreement");
		assertThat(result.get(0).getContractor().getMarkedByName()).isEqualTo("John Doe");
		assertThat(result.get(0).getContractor().getOffLimitStartDate()).isEqualTo(1717200000);
		assertThat(result.get(0).getContractor().getOffLimitEndDate()).isEqualTo(1719792000);
	}

	// ===== listTimeSheetJobAndContractorRequestToResponseBodyDto Tests =====

	@Test
	@DisplayName("Job and contractor list mapping should set isReimbursementEnabled to 0 when projection value is null")
	void testJobAndContractorListMappingIsReimbursementEnabledNullDefaultsToZero() {
		// Given
		List<TimesheetJobAndContractorListQueryResultDto> projections = TimesheetTestDataFactory
			.createTimesheetJobAndContractorListQueryResultList();
		projections.get(0).setIsReimbursementEnabled(null);

		// When
		List<TimesheetListResponseBodyDto> result = this.customTimeSheetMapper
			.listTimeSheetJobAndContractorRequestToResponseBodyDto(projections);

		// Then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getIsReimbursementEnabled()).isZero();
	}

	@Test
	@DisplayName("Job and contractor list mapping should preserve isReimbursementEnabled when projection value is 1")
	void testJobAndContractorListMappingIsReimbursementEnabledOneReturnsOne() {
		// Given
		List<TimesheetJobAndContractorListQueryResultDto> projections = TimesheetTestDataFactory
			.createTimesheetJobAndContractorListQueryResultList();
		projections.get(0).setIsReimbursementEnabled(1);

		// When
		List<TimesheetListResponseBodyDto> result = this.customTimeSheetMapper
			.listTimeSheetJobAndContractorRequestToResponseBodyDto(projections);

		// Then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getIsReimbursementEnabled()).isEqualTo(1);
	}

	@Test
	@DisplayName("Job and contractor list mapping should preserve isReimbursementEnabled when projection value is 0")
	void testJobAndContractorListMappingIsReimbursementEnabledZeroReturnsZero() {
		// Given
		List<TimesheetJobAndContractorListQueryResultDto> projections = TimesheetTestDataFactory
			.createTimesheetJobAndContractorListQueryResultList();
		projections.get(0).setIsReimbursementEnabled(0);

		// When
		List<TimesheetListResponseBodyDto> result = this.customTimeSheetMapper
			.listTimeSheetJobAndContractorRequestToResponseBodyDto(projections);

		// Then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getIsReimbursementEnabled()).isZero();
	}

}
