package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.contract_staffing.entity.model.TimesheetInvoice;
import io.recruitcrm.contract_staffing.entity.model.TimesheetReimbursement;
import io.recruitcrm.contract_staffing.entity.model.TimesheetReimbursementStatusHistory;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSetting;
import io.recruitcrm.contract_staffing.entity.model.TimesheetSettingAssociation;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.CreateReimbursementRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.AddedByResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.UpdatedByResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementDocumentUploadRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementDocumentUploadResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementDocumentViewResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementListItemResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementStatusHistoryResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.UpdateReimbursementRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReopenReimbursementRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.UpdatePayableBillableRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.UpdateShareWithClientRequestBodyDto;
import io.recruitcrm.microservice.timesheet.responses.APINormalResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Test data factory for Reimbursement-related test objects.
 */
public final class ReimbursementTestDataFactory {

	private ReimbursementTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	// ===== Test Constants =====

	public static Integer getDefaultTimesheetId() {
		return 1;
	}

	public static Integer getDefaultReimbursementId() {
		return 1;
	}

	public static Integer getDefaultAccountId() {
		return 1;
	}

	public static Integer getDefaultUserId() {
		return 1;
	}

	public static Integer getDefaultUserTypeId() {
		return 1;
	}

	public static Integer getDefaultCurrencyId() {
		return 1;
	}

	public static Integer getDefaultTimesheetSettingId() {
		return 1;
	}

	public static Integer getDefaultAssociationId() {
		return 500;
	}

	public static Integer getStatusSubmitted() {
		return 1;
	}

	public static String getStatusSubmittedLabel() {
		return "Submitted";
	}

	public static Integer getAgencyUserTypeId() {
		return 2;
	}

	public static Integer getNonAgencyUserTypeId() {
		return 1;
	}

	public static Integer getStatusApproved() {
		return 2;
	}

	public static Integer getStatusRejected() {
		return 3;
	}

	public static String getStatusApprovedLabel() {
		return "Approved";
	}

	public static String getStatusPendingLabel() {
		return "Pending";
	}

	public static Integer getCurrentEpoch() {
		return 1710000000;
	}

	public static String getDefaultRemark() {
		return "Reopening for correction";
	}

	public static Integer getDefaultIsSharedWithClient() {
		return 1;
	}

	public static Integer getNotSharedWithClient() {
		return 0;
	}

	// ===== Request DTOs =====

	public static CreateReimbursementRequestBodyDto createReimbursementRequest() {
		return new CreateReimbursementRequestBodyDto("Travel expenses for client meeting", new BigDecimal("150.50"),
				"https://example.com/receipt.pdf", "receipt.pdf");
	}

	public static CreateReimbursementRequestBodyDto createReimbursementRequestWithoutFile() {
		return new CreateReimbursementRequestBodyDto("Lunch expense", new BigDecimal("25.00"), null, null);
	}

	public static CreateReimbursementRequestBodyDto createReimbursementRequestMinimalAmount() {
		return new CreateReimbursementRequestBodyDto("Parking fee", new BigDecimal("0.01"), null, null);
	}

	public static UpdateReimbursementRequestBodyDto updateReimbursementRequest() {
		return new UpdateReimbursementRequestBodyDto("Updated travel expenses", new BigDecimal("200.00"),
				"https://example.com/updated-receipt.pdf", "updated-receipt.pdf", null);
	}

	public static UpdateReimbursementRequestBodyDto updateReimbursementRequestPartial() {
		return new UpdateReimbursementRequestBodyDto("Updated description only", null, null, null, null);
	}

	public static UpdateReimbursementRequestBodyDto updateReimbursementRequestAmountOnly() {
		return new UpdateReimbursementRequestBodyDto(null, new BigDecimal("300.00"), null, null, null);
	}

	public static UpdateReimbursementRequestBodyDto updateReimbursementRequestSkipStatusHistory() {
		UpdateReimbursementRequestBodyDto request = updateReimbursementRequest();
		request.setSkipStatusHistory(true);
		return request;
	}

	// ===== Reopen Request DTOs =====

	public static ReopenReimbursementRequestBodyDto createReopenReimbursementRequest() {
		return new ReopenReimbursementRequestBodyDto(getDefaultRemark());
	}

	public static ReopenReimbursementRequestBodyDto createReopenReimbursementRequestNoRemark() {
		return new ReopenReimbursementRequestBodyDto(null);
	}

	// ===== Update Payable/Billable Request DTOs =====

	public static UpdatePayableBillableRequestBodyDto createUpdatePayableBillableRequest() {
		return new UpdatePayableBillableRequestBodyDto(1, 0);
	}

	public static UpdatePayableBillableRequestBodyDto createUpdatePayableOnlyRequest() {
		return new UpdatePayableBillableRequestBodyDto(1, null);
	}

	public static UpdatePayableBillableRequestBodyDto createUpdateBillableOnlyRequest() {
		return new UpdatePayableBillableRequestBodyDto(null, 1);
	}

	public static UpdatePayableBillableRequestBodyDto createUpdatePayableBillableEmptyRequest() {
		return new UpdatePayableBillableRequestBodyDto(null, null);
	}

	// ===== Update Share With Client Request DTOs =====

	public static UpdateShareWithClientRequestBodyDto createShareWithClientOnRequest() {
		return new UpdateShareWithClientRequestBodyDto(getDefaultIsSharedWithClient());
	}

	public static UpdateShareWithClientRequestBodyDto createShareWithClientOffRequest() {
		return new UpdateShareWithClientRequestBodyDto(getNotSharedWithClient());
	}

	// ===== Response DTOs =====

	public static ReimbursementResponseBodyDto createReimbursementResponse() {
		return new ReimbursementResponseBodyDto(getDefaultReimbursementId(), getDefaultTimesheetId(),
				"Travel expenses for client meeting", new BigDecimal("150.50"), "https://example.com/receipt.pdf",
				"receipt.pdf", getStatusSubmitted(), getStatusSubmittedLabel(), Integer.valueOf(0), Integer.valueOf(0),
				getDefaultCurrencyId(), getDefaultUserId(), getCurrentEpoch(), getDefaultUserId(), getCurrentEpoch(),
				getDefaultIsSharedWithClient());
	}

	public static ReimbursementResponseBodyDto createReimbursementResponseWithoutFile() {
		return new ReimbursementResponseBodyDto(getDefaultReimbursementId(), getDefaultTimesheetId(), "Lunch expense",
				new BigDecimal("25.00"), null, null, getStatusSubmitted(), getStatusSubmittedLabel(),
				Integer.valueOf(0), Integer.valueOf(0), getDefaultCurrencyId(), getDefaultUserId(), getCurrentEpoch(),
				getDefaultUserId(), getCurrentEpoch(), getDefaultIsSharedWithClient());
	}

	public static ReimbursementResponseBodyDto updateReimbursementResponse() {
		return new ReimbursementResponseBodyDto(getDefaultReimbursementId(), getDefaultTimesheetId(),
				"Updated travel expenses", new BigDecimal("200.00"), "https://example.com/updated-receipt.pdf",
				"updated-receipt.pdf", getStatusSubmitted(), getStatusSubmittedLabel(), Integer.valueOf(0),
				Integer.valueOf(0), getDefaultCurrencyId(), getDefaultUserId(), getCurrentEpoch(), getDefaultUserId(),
				getCurrentEpoch(), getDefaultIsSharedWithClient());
	}

	public static ReimbursementResponseBodyDto updateShareWithClientResponse(Integer isSharedWithClient) {
		return new ReimbursementResponseBodyDto(getDefaultReimbursementId(), getDefaultTimesheetId(),
				"Travel expenses for client meeting", new BigDecimal("150.50"), "https://example.com/receipt.pdf",
				"receipt.pdf", getStatusSubmitted(), getStatusSubmittedLabel(), Integer.valueOf(0), Integer.valueOf(0),
				getDefaultCurrencyId(), getDefaultUserId(), getCurrentEpoch(), getDefaultUserId(), getCurrentEpoch(),
				isSharedWithClient);
	}

	public static List<ReimbursementResponseBodyDto> createReimbursementResponseList() {
		return Arrays.asList(createReimbursementResponse(), createReimbursementResponseWithoutFile());
	}

	public static List<ReimbursementResponseBodyDto> createEmptyReimbursementResponseList() {
		return Arrays.asList();
	}

	// ===== List Item Response DTOs =====

	public static ReimbursementListItemResponseBodyDto createReimbursementListItemResponse() {
		AddedByResponseBodyDto addedBy = new AddedByResponseBodyDto(getDefaultUserId(), "Test User", null,
				getDefaultUserTypeId());
		UpdatedByResponseBodyDto updatedBy = new UpdatedByResponseBodyDto(getDefaultUserId(), "Test User", null,
				getDefaultUserTypeId());
		return ReimbursementListItemResponseBodyDto.builder()
			.id(getDefaultReimbursementId())
			.timesheetId(getDefaultTimesheetId())
			.description("Travel expenses for client meeting")
			.amount(new BigDecimal("150.50"))
			.documentToken("https://example.com/receipt.pdf")
			.fileName("receipt.pdf")
			.status(getStatusSubmitted())
			.statusLabel(getStatusSubmittedLabel())
			.isPayable(0)
			.isBillable(0)
			.isSharedWithClient(getDefaultIsSharedWithClient())
			.currencyId(getDefaultCurrencyId())
			.addedBy(addedBy)
			.addedOn(getCurrentEpoch())
			.updatedBy(updatedBy)
			.updatedOn(getCurrentEpoch())
			.build();
	}

	public static ReimbursementListItemResponseBodyDto createReimbursementListItemResponseWithoutFile() {
		AddedByResponseBodyDto addedBy = new AddedByResponseBodyDto(getDefaultUserId(), "Test User", null,
				getDefaultUserTypeId());
		UpdatedByResponseBodyDto updatedBy = new UpdatedByResponseBodyDto(getDefaultUserId(), "Test User", null,
				getDefaultUserTypeId());
		return ReimbursementListItemResponseBodyDto.builder()
			.id(getDefaultReimbursementId())
			.timesheetId(getDefaultTimesheetId())
			.description("Lunch expense")
			.amount(new BigDecimal("25.00"))
			.documentToken(null)
			.fileName(null)
			.status(getStatusSubmitted())
			.statusLabel(getStatusSubmittedLabel())
			.isPayable(0)
			.isBillable(0)
			.isSharedWithClient(getDefaultIsSharedWithClient())
			.currencyId(getDefaultCurrencyId())
			.addedBy(addedBy)
			.addedOn(getCurrentEpoch())
			.updatedBy(updatedBy)
			.updatedOn(getCurrentEpoch())
			.build();
	}

	// ===== Status History Response DTOs =====

	public static ReimbursementStatusHistoryResponseBodyDto createStatusHistoryResponse() {
		AddedByResponseBodyDto createdBy = new AddedByResponseBodyDto(getDefaultUserId(), "Test User", null,
				getDefaultUserTypeId());
		return new ReimbursementStatusHistoryResponseBodyDto(1, getStatusSubmitted(), getStatusSubmittedLabel(), null,
				createdBy, getCurrentEpoch());
	}

	public static List<ReimbursementStatusHistoryResponseBodyDto> createStatusHistoryResponseList() {
		return Arrays.asList(createStatusHistoryResponse());
	}

	// ===== Timesheet & Setting Entity Objects =====

	public static Timesheet createTimesheetEntity() {
		return createTimesheetEntity(getDefaultTimesheetId());
	}

	public static Timesheet createTimesheetEntity(Integer timesheetId) {
		Timesheet timesheet = new Timesheet();
		timesheet.setId(timesheetId);
		timesheet.setTimesheetSettingId(getDefaultTimesheetSettingId());
		timesheet.setAccountId(getDefaultAccountId());
		return timesheet;
	}

	public static TimesheetSetting createTimesheetSettingEntity(Integer isReimbursementEnabled) {
		TimesheetSetting timesheetSetting = new TimesheetSetting();
		timesheetSetting.setId(getDefaultTimesheetSettingId());
		timesheetSetting.setIsReimbursementEnabled(isReimbursementEnabled);
		timesheetSetting.setIsClientExpenseSharingEnabled(getDefaultIsSharedWithClient());
		timesheetSetting.setAccountId(getDefaultAccountId());

		TimesheetSettingAssociation association = new TimesheetSettingAssociation();
		association.setId(getDefaultAssociationId());
		timesheetSetting.setAssociation(association);

		return timesheetSetting;
	}

	public static TimesheetSetting createTimesheetSettingEntity(Integer isReimbursementEnabled,
			Integer isClientExpenseSharingEnabled) {
		TimesheetSetting timesheetSetting = createTimesheetSettingEntity(isReimbursementEnabled);
		timesheetSetting.setIsClientExpenseSharingEnabled(isClientExpenseSharingEnabled);
		return timesheetSetting;
	}

	// ===== Reimbursement Entity Objects =====

	public static TimesheetReimbursement createReimbursementEntity() {
		TimesheetReimbursement reimbursement = new TimesheetReimbursement();
		reimbursement.setId(getDefaultReimbursementId());
		reimbursement.setTimesheetId(getDefaultTimesheetId());
		reimbursement.setDescription("Travel expenses for client meeting");
		reimbursement.setAmount(new BigDecimal("150.50"));
		reimbursement.setDocumentToken("https://example.com/receipt.pdf");
		reimbursement.setFileName("receipt.pdf");
		reimbursement.setStatus(getStatusSubmitted());
		reimbursement.setIsPayable(0);
		reimbursement.setIsBillable(0);
		reimbursement.setIsSharedWithClient(getDefaultIsSharedWithClient());
		reimbursement.setCurrencyId(getDefaultCurrencyId());
		reimbursement.setAccountId(getDefaultAccountId());
		reimbursement.setAddedBy(getDefaultUserId());
		reimbursement.setAddedOn(getCurrentEpoch());
		reimbursement.setAddedByUserTypeId(getDefaultUserTypeId());
		reimbursement.setUpdatedBy(getDefaultUserId());
		reimbursement.setUpdatedOn(getCurrentEpoch());
		reimbursement.setUpdatedByUserTypeId(getDefaultUserTypeId());
		return reimbursement;
	}

	public static TimesheetReimbursement createReimbursementEntityWithoutFile() {
		TimesheetReimbursement reimbursement = new TimesheetReimbursement();
		reimbursement.setId(getDefaultReimbursementId());
		reimbursement.setTimesheetId(getDefaultTimesheetId());
		reimbursement.setDescription("Lunch expense");
		reimbursement.setAmount(new BigDecimal("25.00"));
		reimbursement.setDocumentToken(null);
		reimbursement.setFileName(null);
		reimbursement.setStatus(getStatusSubmitted());
		reimbursement.setIsPayable(0);
		reimbursement.setIsBillable(0);
		reimbursement.setIsSharedWithClient(getDefaultIsSharedWithClient());
		reimbursement.setCurrencyId(getDefaultCurrencyId());
		reimbursement.setAccountId(getDefaultAccountId());
		reimbursement.setAddedBy(getDefaultUserId());
		reimbursement.setAddedOn(getCurrentEpoch());
		reimbursement.setAddedByUserTypeId(getDefaultUserTypeId());
		reimbursement.setUpdatedBy(getDefaultUserId());
		reimbursement.setUpdatedOn(getCurrentEpoch());
		reimbursement.setUpdatedByUserTypeId(getDefaultUserTypeId());
		return reimbursement;
	}

	public static TimesheetReimbursement createReimbursementEntityWithSharedStatus(Integer isSharedWithClient) {
		TimesheetReimbursement reimbursement = createReimbursementEntity();
		reimbursement.setIsSharedWithClient(isSharedWithClient);
		return reimbursement;
	}

	public static TimesheetReimbursement createReimbursementEntityNonSubmitted() {
		TimesheetReimbursement reimbursement = createReimbursementEntity();
		reimbursement.setStatus(2);
		return reimbursement;
	}

	public static TimesheetReimbursement createReimbursementEntityWithStatus(int status) {
		TimesheetReimbursement reimbursement = createReimbursementEntity();
		reimbursement.setStatus(status);
		return reimbursement;
	}

	public static TimesheetInvoice createTimesheetInvoiceEntity() {
		TimesheetInvoice invoice = new TimesheetInvoice();
		invoice.setId(1);
		invoice.setInvoiceId(1);
		return invoice;
	}

	public static TimesheetReimbursement createApprovedReimbursementEntity() {
		TimesheetReimbursement reimbursement = createReimbursementEntity();
		reimbursement.setStatus(getStatusApproved());
		return reimbursement;
	}

	public static TimesheetReimbursementStatusHistory createStatusHistoryEntity() {
		TimesheetReimbursementStatusHistory statusHistory = new TimesheetReimbursementStatusHistory();
		statusHistory.setId(1);
		statusHistory.setTimesheetReimbursementId(getDefaultReimbursementId());
		statusHistory.setReimbursementStatusTypeId(getStatusSubmitted());
		statusHistory.setCreatedBy(getDefaultUserId());
		statusHistory.setCreatedByUserTypeId(getDefaultUserTypeId());
		statusHistory.setCreatedOn(getCurrentEpoch());
		statusHistory.setAccountId(getDefaultAccountId());
		return statusHistory;
	}

	// ===== API Response Entities =====

	public static ResponseEntity<?> createVoidSuccessResponse() {
		APINormalResponse<Void> response = new APINormalResponse<>(null);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	public static ResponseEntity<?> createReimbursementSuccessResponse(ReimbursementResponseBodyDto data) {
		APINormalResponse<ReimbursementResponseBodyDto> response = new APINormalResponse<>(data);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	public static ResponseEntity<?> updateReimbursementSuccessResponse(ReimbursementResponseBodyDto data) {
		APINormalResponse<ReimbursementResponseBodyDto> response = new APINormalResponse<>(data);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	public static ResponseEntity<?> createReimbursementListSuccessResponse(List<ReimbursementResponseBodyDto> data) {
		APINormalResponse<List<ReimbursementResponseBodyDto>> response = new APINormalResponse<>(data);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	public static List<ReimbursementListItemResponseBodyDto> createReimbursementListItemResponseList() {
		return Arrays.asList(createReimbursementListItemResponse(), createReimbursementListItemResponseWithoutFile());
	}

	public static List<ReimbursementListItemResponseBodyDto> createEmptyReimbursementListItemResponseList() {
		return Arrays.asList();
	}

	public static ResponseEntity<?> createReimbursementListItemSuccessResponse(
			List<ReimbursementListItemResponseBodyDto> data) {
		APINormalResponse<List<ReimbursementListItemResponseBodyDto>> response = new APINormalResponse<>(data);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	public static ResponseEntity<?> createStatusHistoryListSuccessResponse(
			List<ReimbursementStatusHistoryResponseBodyDto> data) {
		APINormalResponse<List<ReimbursementStatusHistoryResponseBodyDto>> response = new APINormalResponse<>(data);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	public static Integer getDefaultReimbursementCount() {
		return 5;
	}

	public static ResponseEntity<?> createReimbursementCountSuccessResponse(Integer data) {
		APINormalResponse<Integer> response = new APINormalResponse<>(data);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	public static ReimbursementResponseBodyDto createApprovedReimbursementResponse() {
		return new ReimbursementResponseBodyDto(getDefaultReimbursementId(), getDefaultTimesheetId(),
				"Travel expenses for client meeting", new BigDecimal("150.50"), "https://example.com/receipt.pdf",
				"receipt.pdf", getStatusApproved(), getStatusApprovedLabel(), 1, 0, getDefaultCurrencyId(),
				getDefaultUserId(), getCurrentEpoch(), getDefaultUserId(), getCurrentEpoch(),
				getDefaultIsSharedWithClient());
	}

	// ===== Document Upload Request DTOs =====

	public static ReimbursementDocumentUploadRequestBodyDto createDocumentUploadRequest() {
		return new ReimbursementDocumentUploadRequestBodyDto("receipt.pdf", getDefaultTimesheetId());
	}

	public static ReimbursementDocumentUploadRequestBodyDto createDocumentUploadRequestNoExtension() {
		return new ReimbursementDocumentUploadRequestBodyDto("receipt", getDefaultTimesheetId());
	}

	public static ReimbursementDocumentUploadRequestBodyDto createDocumentUploadRequestInvalidExtension() {
		return new ReimbursementDocumentUploadRequestBodyDto("virus.exe", getDefaultTimesheetId());
	}

	public static String getDefaultDocumentToken() {
		return "encrypted-s3-key-abc123";
	}

	public static String getDefaultRawS3Key() {
		return "1/timesheets/1/reimbursements/1/receipt.pdf";
	}

	public static String getDefaultReEncryptedToken() {
		return "re-encrypted-token-for-viewer";
	}

	public static String getDefaultPresignedUrl() {
		return "https://mystafflocal-mumbai.s3.ap-south-1.amazonaws.com/1/timesheets/1/reimbursements/1/receipt.pdf?signed";
	}

	// ===== Document Upload Response DTOs =====

	public static ReimbursementDocumentUploadResponseBodyDto createDocumentUploadResponse() {
		return ReimbursementDocumentUploadResponseBodyDto.builder()
			.documentToken(getDefaultDocumentToken())
			.documentFileName("receipt.pdf")
			.presignedUploadUrl(getDefaultPresignedUrl())
			.expiresInMinutes(5)
			.build();
	}

	public static ResponseEntity<?> createDocumentUploadSuccessResponse(
			ReimbursementDocumentUploadResponseBodyDto data) {
		APINormalResponse<ReimbursementDocumentUploadResponseBodyDto> response = new APINormalResponse<>(data);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	// ===== Document View Constants =====

	public static String getDefaultPresignedViewUrl() {
		return "https://mystafflocal-mumbai.s3.ap-south-1.amazonaws.com/1/timesheets/1/reimbursements/1/receipt.pdf?view-signed";
	}

	public static String getDefaultDocumentFileName() {
		return "receipt.pdf";
	}

	public static int getViewExpiresInMinutes() {
		return 15;
	}

	// ===== Document View Response DTOs =====

	public static ReimbursementDocumentViewResponseBodyDto createDocumentViewResponse() {
		return ReimbursementDocumentViewResponseBodyDto.builder()
			.documentFileName(getDefaultDocumentFileName())
			.presignedViewUrl(getDefaultPresignedViewUrl())
			.expiresInMinutes(getViewExpiresInMinutes())
			.type(new java.util.ArrayList<>())
			.build();
	}

	public static ResponseEntity<?> createDocumentViewSuccessResponse(ReimbursementDocumentViewResponseBodyDto data) {
		APINormalResponse<ReimbursementDocumentViewResponseBodyDto> response = new APINormalResponse<>(data);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	// ===== Messages =====

	public static final class Messages {

		private Messages() {
			throw new UnsupportedOperationException("Utility class");
		}

		public static final String REIMBURSEMENT_CREATED_SUCCESSFULLY = "Reimbursement created successfully";

		public static final String REIMBURSEMENT_FETCHED_SUCCESSFULLY = "Reimbursement fetched successfully";

		public static final String REIMBURSEMENTS_FETCHED_SUCCESSFULLY = "Reimbursements fetched successfully";

		public static final String REIMBURSEMENT_UPDATED_SUCCESSFULLY = "Reimbursement updated successfully";

		public static final String REIMBURSEMENT_DELETED_SUCCESSFULLY = "Reimbursement deleted successfully";

		public static final String REIMBURSEMENT_REOPENED_SUCCESSFULLY = "Reimbursement reopened successfully";

		public static final String REIMBURSEMENT_COUNT_FETCHED_SUCCESSFULLY = "Reimbursement count fetched successfully";

		public static final String UPLOAD_URL_GENERATED_SUCCESSFULLY = "Upload URL generated successfully";

		public static final String DOCUMENT_VIEW_URL_GENERATED_SUCCESSFULLY = "Document view URL generated successfully";

		public static final String TIMESHEET_NOT_FOUND = "Timesheet id %d not found.";

		public static final String TIMESHEET_SETTING_NOT_FOUND = "TimesheetSetting id %d not found.";

	}

}
