package io.recruitcrm.microservice.timesheet.mapper;

import io.recruitcrm.contract_staffing.entity.model.TimesheetReimbursement;
import io.recruitcrm.contract_staffing.entity.model.TimesheetReimbursementStatusHistory;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.CreateReimbursementRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementStatusHistoryResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.UpdateReimbursementRequestBodyDto;
import io.recruitcrm.microservice.timesheet.testdata.ReimbursementTestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class TimesheetReimbursementMapperTests {

	private final TimesheetReimbursementMapper timesheetReimbursementMapper = new TimesheetReimbursementMapperImpl();

	@Test
	@DisplayName("Map CreateReimbursementRequestBodyDto to TimesheetReimbursement entity")
	void testToEntityValidDtoReturnsEntity() {
		// Given
		CreateReimbursementRequestBodyDto dto = ReimbursementTestDataFactory.createReimbursementRequest();

		// When
		TimesheetReimbursement entity = this.timesheetReimbursementMapper.toEntity(dto);

		// Then
		assertThat(entity).isNotNull();
		assertThat(entity.getDescription()).isEqualTo(dto.getDescription());
		assertThat(entity.getAmount()).isEqualTo(dto.getAmount());
		assertThat(entity.getDocumentToken()).isEqualTo(dto.getDocumentToken());
		assertThat(entity.getFileName()).isEqualTo(dto.getFileName());
		assertThat(entity.getId()).isNull();
		assertThat(entity.getTimesheetId()).isNull();
		assertThat(entity.getStatus()).isNull();
		assertThat(entity.getIsPayable()).isNull();
		assertThat(entity.getIsBillable()).isNull();
	}

	@Test
	@DisplayName("Map CreateReimbursementRequestBodyDto without file to TimesheetReimbursement entity")
	void testToEntityWithoutFileValidDtoReturnsEntity() {
		// Given
		CreateReimbursementRequestBodyDto dto = ReimbursementTestDataFactory.createReimbursementRequestWithoutFile();

		// When
		TimesheetReimbursement entity = this.timesheetReimbursementMapper.toEntity(dto);

		// Then
		assertThat(entity).isNotNull();
		assertThat(entity.getDescription()).isEqualTo(dto.getDescription());
		assertThat(entity.getAmount()).isEqualTo(dto.getAmount());
		assertThat(entity.getDocumentToken()).isNull();
		assertThat(entity.getFileName()).isNull();
	}

	@Test
	@DisplayName("Map TimesheetReimbursement entity to ReimbursementResponseBodyDto")
	void testToResponseDtoValidEntityReturnsDto() {
		// Given
		TimesheetReimbursement entity = ReimbursementTestDataFactory.createReimbursementEntity();
		String statusLabel = ReimbursementTestDataFactory.getStatusSubmittedLabel();

		// When
		ReimbursementResponseBodyDto dto = this.timesheetReimbursementMapper.toResponseDto(entity, statusLabel);

		// Then
		assertThat(dto).isNotNull();
		assertThat(dto.getId()).isEqualTo(entity.getId());
		assertThat(dto.getTimesheetId()).isEqualTo(entity.getTimesheetId());
		assertThat(dto.getDescription()).isEqualTo(entity.getDescription());
		assertThat(dto.getAmount()).isEqualTo(entity.getAmount());
		assertThat(dto.getDocumentToken()).isEqualTo(entity.getDocumentToken());
		assertThat(dto.getFileName()).isEqualTo(entity.getFileName());
		assertThat(dto.getStatus()).isEqualTo(entity.getStatus());
		assertThat(dto.getStatusLabel()).isEqualTo(statusLabel);
		assertThat(dto.getCurrencyId()).isEqualTo(entity.getCurrencyId());
		assertThat(dto.getAddedBy()).isEqualTo(entity.getAddedBy());
		assertThat(dto.getAddedOn()).isEqualTo(entity.getAddedOn());
		assertThat(dto.getUpdatedBy()).isEqualTo(entity.getUpdatedBy());
		assertThat(dto.getUpdatedOn()).isEqualTo(entity.getUpdatedOn());
		assertThat(dto.getIsPayable()).isZero();
		assertThat(dto.getIsBillable()).isZero();
	}

	@Test
	@DisplayName("Map TimesheetReimbursement entity without file to ReimbursementResponseBodyDto")
	void testToResponseDtoWithoutFileValidEntityReturnsDto() {
		// Given
		TimesheetReimbursement entity = ReimbursementTestDataFactory.createReimbursementEntityWithoutFile();
		String statusLabel = ReimbursementTestDataFactory.getStatusSubmittedLabel();

		// When
		ReimbursementResponseBodyDto dto = this.timesheetReimbursementMapper.toResponseDto(entity, statusLabel);

		// Then
		assertThat(dto).isNotNull();
		assertThat(dto.getId()).isEqualTo(entity.getId());
		assertThat(dto.getTimesheetId()).isEqualTo(entity.getTimesheetId());
		assertThat(dto.getDescription()).isEqualTo(entity.getDescription());
		assertThat(dto.getAmount()).isEqualTo(entity.getAmount());
		assertThat(dto.getDocumentToken()).isNull();
		assertThat(dto.getFileName()).isNull();
		assertThat(dto.getStatus()).isEqualTo(entity.getStatus());
		assertThat(dto.getStatusLabel()).isEqualTo(statusLabel);
	}

	@Test
	@DisplayName("Map entity with isPayable as 1 to DTO with isPayable as 1")
	void testToResponseDtoIsPayableOneReturnsOne() {
		// Given
		TimesheetReimbursement entity = ReimbursementTestDataFactory.createReimbursementEntity();
		entity.setIsPayable(1);
		String statusLabel = ReimbursementTestDataFactory.getStatusSubmittedLabel();

		// When
		ReimbursementResponseBodyDto dto = this.timesheetReimbursementMapper.toResponseDto(entity, statusLabel);

		// Then
		assertThat(dto.getIsPayable()).isEqualTo(Integer.valueOf(1));
	}

	@Test
	@DisplayName("Map entity with isBillable as 1 to DTO with isBillable as 1")
	void testToResponseDtoIsBillableOneReturnsOne() {
		// Given
		TimesheetReimbursement entity = ReimbursementTestDataFactory.createReimbursementEntity();
		entity.setIsBillable(1);
		String statusLabel = ReimbursementTestDataFactory.getStatusSubmittedLabel();

		// When
		ReimbursementResponseBodyDto dto = this.timesheetReimbursementMapper.toResponseDto(entity, statusLabel);

		// Then
		assertThat(dto.getIsBillable()).isEqualTo(Integer.valueOf(1));
	}

	@Test
	@DisplayName("Update entity from UpdateReimbursementRequestBodyDto with all fields")
	void testUpdateEntityFromDtoWithAllFieldsUpdatesEntity() {
		// Given
		UpdateReimbursementRequestBodyDto dto = ReimbursementTestDataFactory.updateReimbursementRequest();
		TimesheetReimbursement entity = ReimbursementTestDataFactory.createReimbursementEntity();
		Integer originalId = entity.getId();

		// When
		this.timesheetReimbursementMapper.updateEntityFromDto(dto, entity);

		// Then
		assertThat(entity.getDescription()).isEqualTo(dto.getDescription());
		assertThat(entity.getAmount()).isEqualTo(dto.getAmount());
		assertThat(entity.getDocumentToken()).isEqualTo(dto.getDocumentToken());
		assertThat(entity.getFileName()).isEqualTo(dto.getFileName());
		assertThat(entity.getId()).isEqualTo(originalId); // Should not be updated
		assertThat(entity.getStatus()).isNotNull(); // Should not be updated
	}

	@Test
	@DisplayName("Update entity from UpdateReimbursementRequestBodyDto with partial fields - null values ignored")
	void testUpdateEntityFromDtoWithPartialFieldsIgnoresNull() {
		// Given
		UpdateReimbursementRequestBodyDto dto = ReimbursementTestDataFactory.updateReimbursementRequestPartial();
		TimesheetReimbursement entity = ReimbursementTestDataFactory.createReimbursementEntity();
		BigDecimal originalAmount = entity.getAmount();

		// When
		this.timesheetReimbursementMapper.updateEntityFromDto(dto, entity);

		// Then
		assertThat(entity.getDescription()).isEqualTo(dto.getDescription());
		assertThat(entity.getAmount()).isEqualTo(originalAmount);
		assertThat(entity.getDocumentToken()).isNull();
		assertThat(entity.getFileName()).isNull();
	}

	@Test
	@DisplayName("Update entity from UpdateReimbursementRequestBodyDto with amount only")
	void testUpdateEntityFromDtoWithAmountOnly() {
		// Given
		UpdateReimbursementRequestBodyDto dto = ReimbursementTestDataFactory.updateReimbursementRequestAmountOnly();
		TimesheetReimbursement entity = ReimbursementTestDataFactory.createReimbursementEntity();
		String originalDescription = entity.getDescription();

		// When
		this.timesheetReimbursementMapper.updateEntityFromDto(dto, entity);

		// Then
		assertThat(entity.getAmount()).isEqualTo(dto.getAmount());
		assertThat(entity.getDescription()).isEqualTo(originalDescription);
		assertThat(entity.getDocumentToken()).isNull();
	}

	@Test
	@DisplayName("Update entity should not affect id, timesheetId, status, isPayable, isBillable")
	void testUpdateEntityFromDtoPreservesIgnoredFields() {
		// Given
		UpdateReimbursementRequestBodyDto dto = ReimbursementTestDataFactory.updateReimbursementRequest();
		TimesheetReimbursement entity = ReimbursementTestDataFactory.createReimbursementEntity();
		Integer originalId = entity.getId();
		Integer originalTimesheetId = entity.getTimesheetId();
		Integer originalStatus = entity.getStatus();
		Integer originalIsPayable = entity.getIsPayable();
		Integer originalIsBillable = entity.getIsBillable();
		Integer originalAccountId = entity.getAccountId();
		Integer originalAddedBy = entity.getAddedBy();
		Integer originalAddedOn = entity.getAddedOn();

		// When
		this.timesheetReimbursementMapper.updateEntityFromDto(dto, entity);

		// Then
		assertThat(entity.getId()).isEqualTo(originalId);
		assertThat(entity.getTimesheetId()).isEqualTo(originalTimesheetId);
		assertThat(entity.getStatus()).isEqualTo(originalStatus);
		assertThat(entity.getIsPayable()).isEqualTo(originalIsPayable);
		assertThat(entity.getIsBillable()).isEqualTo(originalIsBillable);
		assertThat(entity.getAccountId()).isEqualTo(originalAccountId);
		assertThat(entity.getAddedBy()).isEqualTo(originalAddedBy);
		assertThat(entity.getAddedOn()).isEqualTo(originalAddedOn);
	}

	@Test
	@DisplayName("Map TimesheetReimbursementStatusHistory to ReimbursementStatusHistoryResponseBodyDto")
	void testToStatusHistoryResponseDtoValidEntityReturnsDto() {
		// Given
		TimesheetReimbursementStatusHistory history = ReimbursementTestDataFactory.createStatusHistoryEntity();
		String statusLabel = ReimbursementTestDataFactory.getStatusSubmittedLabel();

		// When
		ReimbursementStatusHistoryResponseBodyDto dto = this.timesheetReimbursementMapper
			.toStatusHistoryResponseDto(history, statusLabel);

		// Then
		assertThat(dto).isNotNull();
		assertThat(dto.getId()).isEqualTo(history.getId());
		assertThat(dto.getStatus()).isEqualTo(history.getReimbursementStatusTypeId());
		assertThat(dto.getStatusLabel()).isEqualTo(statusLabel);
		assertThat(dto.getRemark()).isEqualTo(history.getRemark());
		assertThat(dto.getCreatedBy()).isNull();
		assertThat(dto.getCreatedOn()).isEqualTo(history.getCreatedOn());
	}

	@Test
	@DisplayName("Map TimesheetReimbursementStatusHistory with null remark")
	void testToStatusHistoryResponseDtoWithNullRemark() {
		// Given
		TimesheetReimbursementStatusHistory history = ReimbursementTestDataFactory.createStatusHistoryEntity();
		history.setRemark(null);
		String statusLabel = "Submitted";

		// When
		ReimbursementStatusHistoryResponseBodyDto dto = this.timesheetReimbursementMapper
			.toStatusHistoryResponseDto(history, statusLabel);

		// Then
		assertThat(dto).isNotNull();
		assertThat(dto.getRemark()).isNull();
		assertThat(dto.getStatusLabel()).isEqualTo(statusLabel);
	}

	@Test
	@DisplayName("Map TimesheetReimbursementStatusHistory with approved status")
	void testToStatusHistoryResponseDtoWithApprovedStatus() {
		// Given
		TimesheetReimbursementStatusHistory history = ReimbursementTestDataFactory.createStatusHistoryEntity();
		history.setReimbursementStatusTypeId(ReimbursementTestDataFactory.getStatusApproved());
		String statusLabel = ReimbursementTestDataFactory.getStatusApprovedLabel();

		// When
		ReimbursementStatusHistoryResponseBodyDto dto = this.timesheetReimbursementMapper
			.toStatusHistoryResponseDto(history, statusLabel);

		// Then
		assertThat(dto.getStatus()).isEqualTo(ReimbursementTestDataFactory.getStatusApproved());
		assertThat(dto.getStatusLabel()).isEqualTo(statusLabel);
	}

	@Test
	@DisplayName("Map approved TimesheetReimbursement entity to DTO with approved status label")
	void testToResponseDtoWithApprovedStatusLabel() {
		// Given
		TimesheetReimbursement entity = ReimbursementTestDataFactory.createApprovedReimbursementEntity();
		String statusLabel = ReimbursementTestDataFactory.getStatusApprovedLabel();

		// When
		ReimbursementResponseBodyDto dto = this.timesheetReimbursementMapper.toResponseDto(entity, statusLabel);

		// Then
		assertThat(dto.getStatus()).isEqualTo(ReimbursementTestDataFactory.getStatusApproved());
		assertThat(dto.getStatusLabel()).isEqualTo(statusLabel);
	}

}
