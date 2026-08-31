package io.recruitcrm.microservice.timesheet.services.reimbursement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;

import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.contract_staffing.entity.model.TimesheetInvoice;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.dao.timesheet.TimesheetJpaRepository;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementDocumentUploadRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementDocumentUploadResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementDocumentViewResponseBodyDto;
import io.recruitcrm.microservice.timesheet.exceptions.ConflictException;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import io.recruitcrm.microservice.timesheet.helpers.auth.ReimbursementAccessValidator;
import io.recruitcrm.microservice.timesheet.helpers.constants.ExceptionMessageConstants;
import io.recruitcrm.microservice.timesheet.helpers.constants.S3FileUploadConstants;
import io.recruitcrm.microservice.timesheet.repositories.invoice.ITimesheetInvoiceRepository;
import io.recruitcrm.microservice.timesheet.testdata.ReimbursementTestDataFactory;
import io.recruitcrm.s3.dto.PresignedUrlResponse;
import io.recruitcrm.s3.service.IS3Service;
import io.recruitcrm.s3.util.FileKeyEncryptionUtil;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class S3ReimbursementServiceTests {

	@InjectMocks
	private S3ReimbursementService s3ReimbursementService;

	@Mock
	private AuthHolder auth;

	@Mock
	private IS3Service s3Service;

	@Mock
	private TimesheetJpaRepository timesheetJpaRepository;

	@Mock
	private FileKeyEncryptionUtil encryptionUtil;

	@Mock
	private ReimbursementAccessValidator reimbursementAccessValidator;

	@Mock
	private ITimesheetInvoiceRepository timesheetInvoiceRepository;

	private Integer accountId;

	private Integer userId;

	@BeforeEach
	void setUp() {
		this.accountId = ReimbursementTestDataFactory.getDefaultAccountId();
		this.userId = ReimbursementTestDataFactory.getDefaultUserId();

		lenient().when(this.auth.getAuthenticationPrincipalOrganizationIdentifier()).thenReturn(this.accountId);
		lenient().when(this.auth.getAuthenticationPrincipalUniqueIdentifier()).thenReturn(this.userId);
	}

	@Test
	@DisplayName("Generate upload URL should return success response")
	void testGenerateUploadUrlValidRequestReturnsSuccess() {
		// Given
		ReimbursementDocumentUploadRequestBodyDto request = ReimbursementTestDataFactory.createDocumentUploadRequest();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(request.getTimesheetId());
		String keyPattern = this.accountId + "/timesheets/" + request.getTimesheetId()
				+ "/reimbursements/[0-9a-f\\-]{36}/" + request.getFileName();
		PresignedUrlResponse presignedUrlResponse = PresignedUrlResponse.success(
				ReimbursementTestDataFactory.getDefaultPresignedUrl(),
				ReimbursementTestDataFactory.getDefaultDocumentToken());

		given(this.timesheetJpaRepository.findByIdAndAccountId(request.getTimesheetId(), this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.s3Service.generatePresignedPutUrl(eq(this.accountId.longValue()), eq(this.userId.longValue()),
				eq(request.getFileName()), eq(S3FileUploadConstants.ACL_PRIVATE),
				eq(S3FileUploadConstants.UPLOAD_DURATION_MINUTES), matches(keyPattern)))
			.willReturn(presignedUrlResponse);

		// When
		ReimbursementDocumentUploadResponseBodyDto result = this.s3ReimbursementService.generateUploadUrl(request);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getDocumentToken()).isEqualTo(ReimbursementTestDataFactory.getDefaultDocumentToken());
		assertThat(result.getDocumentFileName()).isEqualTo(request.getFileName());
		assertThat(result.getPresignedUploadUrl()).isEqualTo(ReimbursementTestDataFactory.getDefaultPresignedUrl());
		assertThat(result.getExpiresInMinutes()).isEqualTo(S3FileUploadConstants.EXPIRES_IN_MINUTES);
		then(this.timesheetJpaRepository).should().findByIdAndAccountId(request.getTimesheetId(), this.accountId);
		then(this.s3Service).should()
			.generatePresignedPutUrl(eq(this.accountId.longValue()), eq(this.userId.longValue()),
					eq(request.getFileName()), eq(S3FileUploadConstants.ACL_PRIVATE),
					eq(S3FileUploadConstants.UPLOAD_DURATION_MINUTES), matches(keyPattern));
	}

	@Test
	@DisplayName("Generate upload URL should throw ResourceNotFoundException when timesheet not found")
	void testGenerateUploadUrlTimesheetNotFoundThrowsResourceNotFoundException() {
		// Given
		ReimbursementDocumentUploadRequestBodyDto request = ReimbursementTestDataFactory.createDocumentUploadRequest();

		given(this.timesheetJpaRepository.findByIdAndAccountId(request.getTimesheetId(), this.accountId))
			.willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> this.s3ReimbursementService.generateUploadUrl(request))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("Timesheet")
			.hasMessageContaining(request.getTimesheetId().toString());

		then(this.timesheetJpaRepository).should().findByIdAndAccountId(request.getTimesheetId(), this.accountId);
		then(this.s3Service).should(never())
			.generatePresignedPutUrl(anyLong(), anyLong(), anyString(), anyString(), anyString(), anyString());
	}

	@Test
	@DisplayName("Generate upload URL should throw ValidationErrorException when file has no extension")
	void testGenerateUploadUrlNoExtensionThrowsValidationErrorException() {
		// Given
		ReimbursementDocumentUploadRequestBodyDto request = ReimbursementTestDataFactory
			.createDocumentUploadRequestNoExtension();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(request.getTimesheetId());

		given(this.timesheetJpaRepository.findByIdAndAccountId(request.getTimesheetId(), this.accountId))
			.willReturn(Optional.of(timesheet));

		// When & Then
		assertThatThrownBy(() -> this.s3ReimbursementService.generateUploadUrl(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("File must have an extension");

		then(this.timesheetJpaRepository).should().findByIdAndAccountId(request.getTimesheetId(), this.accountId);
		then(this.s3Service).should(never())
			.generatePresignedPutUrl(anyLong(), anyLong(), anyString(), anyString(), anyString(), anyString());
	}

	@Test
	@DisplayName("Generate upload URL should throw ValidationErrorException when file extension not allowed")
	void testGenerateUploadUrlInvalidExtensionThrowsValidationErrorException() {
		// Given
		ReimbursementDocumentUploadRequestBodyDto request = ReimbursementTestDataFactory
			.createDocumentUploadRequestInvalidExtension();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(request.getTimesheetId());

		given(this.timesheetJpaRepository.findByIdAndAccountId(request.getTimesheetId(), this.accountId))
			.willReturn(Optional.of(timesheet));

		// When & Then
		assertThatThrownBy(() -> this.s3ReimbursementService.generateUploadUrl(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("exe")
			.hasMessageContaining("is not allowed");

		then(this.timesheetJpaRepository).should().findByIdAndAccountId(request.getTimesheetId(), this.accountId);
		then(this.s3Service).should(never())
			.generatePresignedPutUrl(anyLong(), anyLong(), anyString(), anyString(), anyString(), anyString());
	}

	@Test
	@DisplayName("Generate upload URL should throw ValidationErrorException when S3 service fails")
	void testGenerateUploadUrlS3FailureThrowsValidationErrorException() {
		// Given
		ReimbursementDocumentUploadRequestBodyDto request = ReimbursementTestDataFactory.createDocumentUploadRequest();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(request.getTimesheetId());
		String keyPattern = this.accountId + "/timesheets/" + request.getTimesheetId()
				+ "/reimbursements/[0-9a-f\\-]{36}/" + request.getFileName();
		PresignedUrlResponse errorResponse = PresignedUrlResponse.error("S3 bucket not accessible");

		given(this.timesheetJpaRepository.findByIdAndAccountId(request.getTimesheetId(), this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.s3Service.generatePresignedPutUrl(eq(this.accountId.longValue()), eq(this.userId.longValue()),
				eq(request.getFileName()), eq(S3FileUploadConstants.ACL_PRIVATE),
				eq(S3FileUploadConstants.UPLOAD_DURATION_MINUTES), matches(keyPattern)))
			.willReturn(errorResponse);

		// When & Then
		assertThatThrownBy(() -> this.s3ReimbursementService.generateUploadUrl(request))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Failed to generate upload URL")
			.hasMessageContaining("S3 bucket not accessible");

		then(this.timesheetJpaRepository).should().findByIdAndAccountId(request.getTimesheetId(), this.accountId);
		then(this.s3Service).should()
			.generatePresignedPutUrl(eq(this.accountId.longValue()), eq(this.userId.longValue()),
					eq(request.getFileName()), eq(S3FileUploadConstants.ACL_PRIVATE),
					eq(S3FileUploadConstants.UPLOAD_DURATION_MINUTES), matches(keyPattern));
	}

	@Test
	@DisplayName("View file should decrypt token, re-encrypt for current user, and return presigned view URL")
	void testViewFileValidRequestReturnsSuccess() {
		// Given
		String documentToken = ReimbursementTestDataFactory.getDefaultDocumentToken();
		String rawKey = ReimbursementTestDataFactory.getDefaultRawS3Key();
		String reEncryptedToken = ReimbursementTestDataFactory.getDefaultReEncryptedToken();
		String fileName = ReimbursementTestDataFactory.getDefaultDocumentFileName();
		Boolean download = false;
		PresignedUrlResponse presignedUrlResponse = PresignedUrlResponse.success(
				ReimbursementTestDataFactory.getDefaultPresignedViewUrl(),
				ReimbursementTestDataFactory.getDefaultDocumentToken());

		given(this.encryptionUtil.decryptFileKeyUnsafe(documentToken)).willReturn(rawKey);
		given(this.encryptionUtil.encryptFileKey(rawKey, this.accountId.longValue(), this.userId.longValue()))
			.willReturn(reEncryptedToken);
		given(this.s3Service.generatePresignedGetUrl(reEncryptedToken, this.accountId.longValue(),
				this.userId.longValue(), fileName, download, S3FileUploadConstants.VIEW_DURATION_MINUTES))
			.willReturn(presignedUrlResponse);

		// When
		ReimbursementDocumentViewResponseBodyDto result = this.s3ReimbursementService.viewFile(documentToken, fileName,
				download);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getDocumentFileName()).isEqualTo(fileName);
		assertThat(result.getPresignedViewUrl()).isEqualTo(ReimbursementTestDataFactory.getDefaultPresignedViewUrl());
		assertThat(result.getExpiresInMinutes()).isEqualTo(S3FileUploadConstants.VIEW_EXPIRES_IN_MINUTES);
		then(this.encryptionUtil).should().decryptFileKeyUnsafe(documentToken);
		then(this.encryptionUtil).should().encryptFileKey(rawKey, this.accountId.longValue(), this.userId.longValue());
		then(this.s3Service).should()
			.generatePresignedGetUrl(reEncryptedToken, this.accountId.longValue(), this.userId.longValue(), fileName,
					download, S3FileUploadConstants.VIEW_DURATION_MINUTES);
	}

	@Test
	@DisplayName("View file should return presigned view URL with download flag true")
	void testViewFileDownloadTrueReturnsSuccess() {
		// Given
		String documentToken = ReimbursementTestDataFactory.getDefaultDocumentToken();
		String rawKey = ReimbursementTestDataFactory.getDefaultRawS3Key();
		String reEncryptedToken = ReimbursementTestDataFactory.getDefaultReEncryptedToken();
		String fileName = ReimbursementTestDataFactory.getDefaultDocumentFileName();
		Boolean download = true;
		PresignedUrlResponse presignedUrlResponse = PresignedUrlResponse.success(
				ReimbursementTestDataFactory.getDefaultPresignedViewUrl(),
				ReimbursementTestDataFactory.getDefaultDocumentToken());

		given(this.encryptionUtil.decryptFileKeyUnsafe(documentToken)).willReturn(rawKey);
		given(this.encryptionUtil.encryptFileKey(rawKey, this.accountId.longValue(), this.userId.longValue()))
			.willReturn(reEncryptedToken);
		given(this.s3Service.generatePresignedGetUrl(reEncryptedToken, this.accountId.longValue(),
				this.userId.longValue(), fileName, download, S3FileUploadConstants.VIEW_DURATION_MINUTES))
			.willReturn(presignedUrlResponse);

		// When
		ReimbursementDocumentViewResponseBodyDto result = this.s3ReimbursementService.viewFile(documentToken, fileName,
				download);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getPresignedViewUrl()).isEqualTo(ReimbursementTestDataFactory.getDefaultPresignedViewUrl());
		then(this.s3Service).should()
			.generatePresignedGetUrl(reEncryptedToken, this.accountId.longValue(), this.userId.longValue(), fileName,
					download, S3FileUploadConstants.VIEW_DURATION_MINUTES);
	}

	@Test
	@DisplayName("View file should throw ValidationErrorException when S3 service fails")
	void testViewFileS3FailureThrowsValidationErrorException() {
		// Given
		String documentToken = ReimbursementTestDataFactory.getDefaultDocumentToken();
		String rawKey = ReimbursementTestDataFactory.getDefaultRawS3Key();
		String reEncryptedToken = ReimbursementTestDataFactory.getDefaultReEncryptedToken();
		String fileName = ReimbursementTestDataFactory.getDefaultDocumentFileName();
		Boolean download = false;
		PresignedUrlResponse errorResponse = PresignedUrlResponse.error("Key decryption failed");

		given(this.encryptionUtil.decryptFileKeyUnsafe(documentToken)).willReturn(rawKey);
		given(this.encryptionUtil.encryptFileKey(rawKey, this.accountId.longValue(), this.userId.longValue()))
			.willReturn(reEncryptedToken);
		given(this.s3Service.generatePresignedGetUrl(reEncryptedToken, this.accountId.longValue(),
				this.userId.longValue(), fileName, download, S3FileUploadConstants.VIEW_DURATION_MINUTES))
			.willReturn(errorResponse);

		// When & Then
		assertThatThrownBy(() -> this.s3ReimbursementService.viewFile(documentToken, fileName, download))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Failed to generate view URL")
			.hasMessageContaining("Key decryption failed");

		then(this.s3Service).should()
			.generatePresignedGetUrl(reEncryptedToken, this.accountId.longValue(), this.userId.longValue(), fileName,
					download, S3FileUploadConstants.VIEW_DURATION_MINUTES);
	}

	@Test
	@DisplayName("View file should pass null fileName to S3 service when not provided")
	void testViewFileNullFileNameReturnsSuccess() {
		// Given
		String documentToken = ReimbursementTestDataFactory.getDefaultDocumentToken();
		String rawKey = ReimbursementTestDataFactory.getDefaultRawS3Key();
		String reEncryptedToken = ReimbursementTestDataFactory.getDefaultReEncryptedToken();
		Boolean download = false;
		PresignedUrlResponse presignedUrlResponse = PresignedUrlResponse.success(
				ReimbursementTestDataFactory.getDefaultPresignedViewUrl(),
				ReimbursementTestDataFactory.getDefaultDocumentToken());

		given(this.encryptionUtil.decryptFileKeyUnsafe(documentToken)).willReturn(rawKey);
		given(this.encryptionUtil.encryptFileKey(rawKey, this.accountId.longValue(), this.userId.longValue()))
			.willReturn(reEncryptedToken);
		given(this.s3Service.generatePresignedGetUrl(reEncryptedToken, this.accountId.longValue(),
				this.userId.longValue(), null, download, S3FileUploadConstants.VIEW_DURATION_MINUTES))
			.willReturn(presignedUrlResponse);

		// When
		ReimbursementDocumentViewResponseBodyDto result = this.s3ReimbursementService.viewFile(documentToken, null,
				download);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getDocumentFileName()).isNull();
		assertThat(result.getPresignedViewUrl()).isEqualTo(ReimbursementTestDataFactory.getDefaultPresignedViewUrl());
		then(this.s3Service).should()
			.generatePresignedGetUrl(reEncryptedToken, this.accountId.longValue(), this.userId.longValue(), null,
					download, S3FileUploadConstants.VIEW_DURATION_MINUTES);
	}

	@Test
	@DisplayName("View file should throw ValidationErrorException when decryption returns null")
	void testViewFileInvalidTokenThrowsValidationErrorException() {
		// Given
		String documentToken = "invalid-corrupted-token";
		String fileName = ReimbursementTestDataFactory.getDefaultDocumentFileName();
		Boolean download = false;

		given(this.encryptionUtil.decryptFileKeyUnsafe(documentToken)).willReturn(null);

		// When & Then
		assertThatThrownBy(() -> this.s3ReimbursementService.viewFile(documentToken, fileName, download))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Invalid or corrupted document token");

		then(this.encryptionUtil).should().decryptFileKeyUnsafe(documentToken);
		then(this.encryptionUtil).should(never()).encryptFileKey(anyString(), anyLong(), anyLong());
		then(this.s3Service).should(never())
			.generatePresignedGetUrl(anyString(), anyLong(), anyLong(), anyString(), any(Boolean.class), anyString());
	}

	@Test
	@DisplayName("View file should throw ValidationErrorException when decryption returns empty string")
	void testViewFileEmptyDecryptedKeyThrowsValidationErrorException() {
		// Given
		String documentToken = ReimbursementTestDataFactory.getDefaultDocumentToken();
		String fileName = ReimbursementTestDataFactory.getDefaultDocumentFileName();
		Boolean download = false;

		given(this.encryptionUtil.decryptFileKeyUnsafe(documentToken)).willReturn("");

		// When & Then
		assertThatThrownBy(() -> this.s3ReimbursementService.viewFile(documentToken, fileName, download))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Invalid or corrupted document token");

		then(this.encryptionUtil).should().decryptFileKeyUnsafe(documentToken);
		then(this.encryptionUtil).should(never()).encryptFileKey(anyString(), anyLong(), anyLong());
	}

	@Test
	@DisplayName("Delete reimbursement file should decrypt token and call S3 delete")
	void testDeleteReimbursementFileValidTokenDeletesFile() {
		// Given
		String documentToken = ReimbursementTestDataFactory.getDefaultDocumentToken();
		String rawKey = ReimbursementTestDataFactory.getDefaultRawS3Key();

		given(this.encryptionUtil.decryptFileKeyUnsafe(documentToken)).willReturn(rawKey);
		given(this.s3Service.deleteFile(rawKey, null, null, this.accountId.longValue())).willReturn(true);

		// When
		this.s3ReimbursementService.deleteReimbursementFile(documentToken);

		// Then
		then(this.encryptionUtil).should().decryptFileKeyUnsafe(documentToken);
		then(this.s3Service).should().deleteFile(rawKey, null, null, this.accountId.longValue());
	}

	@Test
	@DisplayName("Delete reimbursement file should do nothing when token is null")
	void testDeleteReimbursementFileNullTokenDoesNothing() {
		// When
		this.s3ReimbursementService.deleteReimbursementFile(null);

		// Then
		then(this.encryptionUtil).should(never()).decryptFileKeyUnsafe(anyString());
		then(this.s3Service).should(never()).deleteFile(anyString(), anyString(), anyString(), anyLong());
	}

	@Test
	@DisplayName("Delete reimbursement file should do nothing when token is empty")
	void testDeleteReimbursementFileEmptyTokenDoesNothing() {
		// When
		this.s3ReimbursementService.deleteReimbursementFile("");

		// Then
		then(this.encryptionUtil).should(never()).decryptFileKeyUnsafe(anyString());
		then(this.s3Service).should(never()).deleteFile(anyString(), anyString(), anyString(), anyLong());
	}

	@Test
	@DisplayName("Delete reimbursement file should do nothing when decryption returns null")
	void testDeleteReimbursementFileDecryptionReturnsNullDoesNothing() {
		// Given
		String documentToken = "corrupted-token";

		given(this.encryptionUtil.decryptFileKeyUnsafe(documentToken)).willReturn(null);

		// When
		this.s3ReimbursementService.deleteReimbursementFile(documentToken);

		// Then
		then(this.encryptionUtil).should().decryptFileKeyUnsafe(documentToken);
		then(this.s3Service).should(never()).deleteFile(anyString(), anyString(), anyString(), anyLong());
	}

	// ===== Access Control: validateTimesheetCreateAccess Tests =====

	@Test
	@DisplayName("Generate upload URL should throw UnauthorizedAccessException when USER create access denied")
	void testGenerateUploadUrlUserCreateAccessDeniedThrowsUnauthorizedAccessException() {
		// Given
		ReimbursementDocumentUploadRequestBodyDto request = ReimbursementTestDataFactory.createDocumentUploadRequest();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(request.getTimesheetId());

		given(this.timesheetJpaRepository.findByIdAndAccountId(request.getTimesheetId(), this.accountId))
			.willReturn(Optional.of(timesheet));
		willThrow(new UnauthorizedAccessException("Create access denied")).given(this.reimbursementAccessValidator)
			.validateTimesheetCreateAccess(request.getTimesheetId());

		// When & Then
		assertThatThrownBy(() -> this.s3ReimbursementService.generateUploadUrl(request))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("Create access denied");

		then(this.s3Service).should(never())
			.generatePresignedPutUrl(anyLong(), anyLong(), anyString(), anyString(), anyString(), anyString());
	}

	@Test
	@DisplayName("Generate upload URL should throw UnauthorizedAccessException for non-USER principal")
	void testGenerateUploadUrlNonUserPrincipalThrowsUnauthorizedAccessException() {
		// Given
		ReimbursementDocumentUploadRequestBodyDto request = ReimbursementTestDataFactory.createDocumentUploadRequest();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(request.getTimesheetId());

		given(this.timesheetJpaRepository.findByIdAndAccountId(request.getTimesheetId(), this.accountId))
			.willReturn(Optional.of(timesheet));
		willThrow(new UnauthorizedAccessException("Unknown persona type")).given(this.reimbursementAccessValidator)
			.validateTimesheetCreateAccess(request.getTimesheetId());

		// When & Then
		assertThatThrownBy(() -> this.s3ReimbursementService.generateUploadUrl(request))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("Unknown persona type");

		then(this.s3Service).should(never())
			.generatePresignedPutUrl(anyLong(), anyLong(), anyString(), anyString(), anyString(), anyString());
	}

	@Test
	@DisplayName("Generate upload URL should throw ConflictException when invoice is linked to timesheet")
	void testGenerateUploadUrlInvoiceLinkedThrowsConflictException() {
		// Given
		ReimbursementDocumentUploadRequestBodyDto request = ReimbursementTestDataFactory.createDocumentUploadRequest();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(request.getTimesheetId());
		TimesheetInvoice timesheetInvoice = new TimesheetInvoice();
		timesheetInvoice.setInvoiceId(123);

		given(this.timesheetJpaRepository.findByIdAndAccountId(request.getTimesheetId(), this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetInvoiceRepository.findByTimesheetId(request.getTimesheetId(), this.accountId))
			.willReturn(timesheetInvoice);

		// When & Then
		assertThatThrownBy(() -> this.s3ReimbursementService.generateUploadUrl(request))
			.isInstanceOf(ConflictException.class)
			.hasMessageContaining(ExceptionMessageConstants.REIMBURSEMENT_INVOICE_LINKED);

		then(this.timesheetJpaRepository).should().findByIdAndAccountId(request.getTimesheetId(), this.accountId);
		then(this.timesheetInvoiceRepository).should().findByTimesheetId(request.getTimesheetId(), this.accountId);
		then(this.reimbursementAccessValidator).should(never()).validateTimesheetCreateAccess(request.getTimesheetId());
		then(this.s3Service).should(never())
			.generatePresignedPutUrl(anyLong(), anyLong(), anyString(), anyString(), anyString(), anyString());
	}

	@Test
	@DisplayName("Generate upload URL should succeed when timesheet invoice exists but invoiceId is null")
	void testGenerateUploadUrlTimesheetInvoiceExistsButInvoiceIdNullReturnsSuccess() {
		// Given
		ReimbursementDocumentUploadRequestBodyDto request = ReimbursementTestDataFactory.createDocumentUploadRequest();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(request.getTimesheetId());
		TimesheetInvoice timesheetInvoice = new TimesheetInvoice();
		timesheetInvoice.setInvoiceId(null);
		String keyPattern = this.accountId + "/timesheets/" + request.getTimesheetId()
				+ "/reimbursements/[0-9a-f\\-]{36}/" + request.getFileName();
		PresignedUrlResponse presignedUrlResponse = PresignedUrlResponse.success(
				ReimbursementTestDataFactory.getDefaultPresignedUrl(),
				ReimbursementTestDataFactory.getDefaultDocumentToken());

		given(this.timesheetJpaRepository.findByIdAndAccountId(request.getTimesheetId(), this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetInvoiceRepository.findByTimesheetId(request.getTimesheetId(), this.accountId))
			.willReturn(timesheetInvoice);
		given(this.s3Service.generatePresignedPutUrl(eq(this.accountId.longValue()), eq(this.userId.longValue()),
				eq(request.getFileName()), eq(S3FileUploadConstants.ACL_PRIVATE),
				eq(S3FileUploadConstants.UPLOAD_DURATION_MINUTES), matches(keyPattern)))
			.willReturn(presignedUrlResponse);

		// When
		ReimbursementDocumentUploadResponseBodyDto result = this.s3ReimbursementService.generateUploadUrl(request);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getDocumentToken()).isEqualTo(ReimbursementTestDataFactory.getDefaultDocumentToken());
		assertThat(result.getDocumentFileName()).isEqualTo(request.getFileName());
		assertThat(result.getPresignedUploadUrl()).isEqualTo(ReimbursementTestDataFactory.getDefaultPresignedUrl());
		then(this.timesheetInvoiceRepository).should().findByTimesheetId(request.getTimesheetId(), this.accountId);
		then(this.reimbursementAccessValidator).should().validateTimesheetCreateAccess(request.getTimesheetId());
	}

	@Test
	@DisplayName("Generate upload URL should succeed when no invoice exists for timesheet")
	void testGenerateUploadUrlNoInvoiceExistsReturnsSuccess() {
		// Given
		ReimbursementDocumentUploadRequestBodyDto request = ReimbursementTestDataFactory.createDocumentUploadRequest();
		Timesheet timesheet = ReimbursementTestDataFactory.createTimesheetEntity(request.getTimesheetId());
		String keyPattern = this.accountId + "/timesheets/" + request.getTimesheetId()
				+ "/reimbursements/[0-9a-f\\-]{36}/" + request.getFileName();
		PresignedUrlResponse presignedUrlResponse = PresignedUrlResponse.success(
				ReimbursementTestDataFactory.getDefaultPresignedUrl(),
				ReimbursementTestDataFactory.getDefaultDocumentToken());

		given(this.timesheetJpaRepository.findByIdAndAccountId(request.getTimesheetId(), this.accountId))
			.willReturn(Optional.of(timesheet));
		given(this.timesheetInvoiceRepository.findByTimesheetId(request.getTimesheetId(), this.accountId))
			.willReturn(null);
		given(this.s3Service.generatePresignedPutUrl(eq(this.accountId.longValue()), eq(this.userId.longValue()),
				eq(request.getFileName()), eq(S3FileUploadConstants.ACL_PRIVATE),
				eq(S3FileUploadConstants.UPLOAD_DURATION_MINUTES), matches(keyPattern)))
			.willReturn(presignedUrlResponse);

		// When
		ReimbursementDocumentUploadResponseBodyDto result = this.s3ReimbursementService.generateUploadUrl(request);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getDocumentToken()).isEqualTo(ReimbursementTestDataFactory.getDefaultDocumentToken());
		then(this.timesheetInvoiceRepository).should().findByTimesheetId(request.getTimesheetId(), this.accountId);
		then(this.reimbursementAccessValidator).should().validateTimesheetCreateAccess(request.getTimesheetId());
	}

}
