package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.microservice.timesheet.dto.portal.client.BulkValidateQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.portal.client.BulkValidateRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.portal.client.BulkValidateResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.portal.client.ContactBulkValidateItemDto;
import io.recruitcrm.microservice.timesheet.dto.portal.client.ContactValidationResultDto;
import io.recruitcrm.microservice.timesheet.responses.APINormalResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Test data factory for BulkValidate-related test objects. Provides factory methods to
 * create consistent test data across all bulk-validate tests.
 */
public final class BulkValidateTestDataFactory {

	private BulkValidateTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static Integer getDefaultAccountId() {
		return 42;
	}

	public static Integer getDefaultContactId() {
		return 1234;
	}

	public static String getDefaultContactEmail() {
		return "jane@example.com";
	}

	public static ContactBulkValidateItemDto createContactItem() {
		return new ContactBulkValidateItemDto(getDefaultContactId(), getDefaultContactEmail());
	}

	public static ContactBulkValidateItemDto createContactItemNullEmail() {
		return new ContactBulkValidateItemDto(1235, null);
	}

	public static BulkValidateRequestBodyDto createBulkValidateRequest() {
		return new BulkValidateRequestBodyDto(List.of(createContactItem()));
	}

	public static BulkValidateRequestBodyDto createBulkValidateRequestAllNullEmails() {
		return new BulkValidateRequestBodyDto(List.of(createContactItemNullEmail()));
	}

	public static BulkValidateRequestBodyDto createBulkValidateRequestMixed() {
		return new BulkValidateRequestBodyDto(List.of(createContactItem(), createContactItemNullEmail()));
	}

	public static ContactValidationResultDto createValidResult() {
		return new ContactValidationResultDto(getDefaultContactId(), getDefaultContactEmail(), true, null);
	}

	public static ContactValidationResultDto createEmailMissingResult() {
		return new ContactValidationResultDto(1235, null, false, "email_missing");
	}

	public static ContactValidationResultDto createEmailTakenResult() {
		return new ContactValidationResultDto(getDefaultContactId(), getDefaultContactEmail(), false, "email_taken");
	}

	public static ContactValidationResultDto createPortalActiveResult() {
		return new ContactValidationResultDto(getDefaultContactId(), getDefaultContactEmail(), false, "portal_active");
	}

	public static ContactValidationResultDto createRateLimitResult() {
		return new ContactValidationResultDto(getDefaultContactId(), getDefaultContactEmail(), false, "rate_limit");
	}

	public static BulkValidateResponseBodyDto createBulkValidateResponse() {
		return new BulkValidateResponseBodyDto(List.of(createValidResult()), 1, 0);
	}

	public static BulkValidateResponseBodyDto createBulkValidateResponseAllInvalid() {
		return new BulkValidateResponseBodyDto(List.of(createEmailMissingResult()), 0, 1);
	}

	public static BulkValidateQueryResultDto createQueryResultSameAccount() {
		return new BulkValidateQueryResultDto(getDefaultContactEmail(), 0, getDefaultAccountId().longValue(), 0L, 0L);
	}

	public static BulkValidateQueryResultDto createQueryResultDifferentAccount() {
		return new BulkValidateQueryResultDto(getDefaultContactEmail(), 0, 999L, 0L, 0L);
	}

	public static BulkValidateQueryResultDto createQueryResultPortalActive() {
		return new BulkValidateQueryResultDto(getDefaultContactEmail(), 2, getDefaultAccountId().longValue(), 0L, 0L);
	}

	public static BulkValidateQueryResultDto createQueryResultRateLimited() {
		long startOfToday = LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC).toEpochSecond();
		return new BulkValidateQueryResultDto(getDefaultContactEmail(), 0, getDefaultAccountId().longValue(), 3L,
				startOfToday);
	}

	public static ResponseEntity<APINormalResponse<BulkValidateResponseBodyDto>> createBulkValidateSuccessResponse(
			BulkValidateResponseBodyDto data) {
		APINormalResponse<BulkValidateResponseBodyDto> response = new APINormalResponse<>(data);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * Message constants for test assertions. Note: Inner types must be placed at the end
	 * to comply with InnerTypeLast checkstyle rule.
	 */
	public static final class Messages {

		public static final String BULK_VALIDATION_COMPLETED = "Bulk validation completed";

		private Messages() {
		}

	}

}
