package io.recruitcrm.microservice.timesheet.services.reimbursement;

import io.recruitcrm.contract_staffing.entity.model.TimesheetInvoice;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.dao.timesheet.TimesheetJpaRepository;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementDocumentUploadRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementDocumentUploadResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementDocumentViewResponseBodyDto;
import io.recruitcrm.microservice.timesheet.exceptions.ConflictException;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import io.recruitcrm.microservice.timesheet.helpers.auth.ReimbursementAccessValidator;
import io.recruitcrm.microservice.timesheet.helpers.constants.ExceptionMessageConstants;
import io.recruitcrm.microservice.timesheet.helpers.constants.S3FileUploadConstants;
import io.recruitcrm.microservice.timesheet.repositories.invoice.ITimesheetInvoiceRepository;
import io.recruitcrm.s3.dto.PresignedUrlResponse;
import io.recruitcrm.s3.service.IS3Service;
import io.recruitcrm.s3.util.FileKeyEncryptionUtil;

import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class S3ReimbursementService implements IS3ReimbursementService {

	private final AuthHolder auth;

	private final IS3Service s3Service;

	private final TimesheetJpaRepository timesheetJpaRepository;

	private final FileKeyEncryptionUtil encryptionUtil;

	private final ReimbursementAccessValidator reimbursementAccessValidator;

	private final ITimesheetInvoiceRepository timesheetInvoiceRepository;

	public S3ReimbursementService(AuthHolder auth, IS3Service s3Service, TimesheetJpaRepository timesheetJpaRepository,
			FileKeyEncryptionUtil encryptionUtil, ReimbursementAccessValidator reimbursementAccessValidator,
			ITimesheetInvoiceRepository timesheetInvoiceRepository) {
		this.auth = auth;
		this.s3Service = s3Service;
		this.timesheetJpaRepository = timesheetJpaRepository;
		this.encryptionUtil = encryptionUtil;
		this.reimbursementAccessValidator = reimbursementAccessValidator;
		this.timesheetInvoiceRepository = timesheetInvoiceRepository;
	}

	@Override
	public ReimbursementDocumentUploadResponseBodyDto generateUploadUrl(
			ReimbursementDocumentUploadRequestBodyDto request) {
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		Integer userId = this.auth.getAuthenticationPrincipalUniqueIdentifier();

		this.timesheetJpaRepository.findByIdAndAccountId(request.getTimesheetId(), accountId)
			.orElseThrow(() -> new ResourceNotFoundException("Timesheet", request.getTimesheetId()));

		validateInvoiceNotLinked(request.getTimesheetId(), accountId);

		this.reimbursementAccessValidator.validateTimesheetCreateAccess(request.getTimesheetId());

		String fileName = request.getFileName().trim();
		validateFileExtension(fileName);

		String key = accountId + "/timesheets/" + request.getTimesheetId() + "/reimbursements/"
				+ UUID.randomUUID().toString() + "/" + fileName;

		PresignedUrlResponse response = this.s3Service.generatePresignedPutUrl(accountId.longValue(),
				userId.longValue(), fileName, S3FileUploadConstants.ACL_PRIVATE,
				S3FileUploadConstants.UPLOAD_DURATION_MINUTES, key);

		if (!response.isSuccess()) {
			throw new ValidationErrorException("Failed to generate upload URL: " + response.getErrorMessage());
		}

		return ReimbursementDocumentUploadResponseBodyDto.builder()
			.documentToken(response.getKey())
			.documentFileName(fileName)
			.presignedUploadUrl(response.getPreSignedUrl())
			.expiresInMinutes(S3FileUploadConstants.EXPIRES_IN_MINUTES)
			.build();
	}

	@Override
	public ReimbursementDocumentViewResponseBodyDto viewFile(String documentToken, String fileName, Boolean download) {
		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
		Integer userId = this.auth.getAuthenticationPrincipalUniqueIdentifier();

		String rawKey = this.encryptionUtil.decryptFileKeyUnsafe(documentToken);
		if (rawKey == null || rawKey.isEmpty()) {
			throw new ValidationErrorException("Invalid or corrupted document token");
		}

		if (fileName != null && !fileName.isEmpty()) {
			String tokenFileName = rawKey.substring(rawKey.lastIndexOf('/') + 1);
			if (!tokenFileName.equals(fileName)) {
				throw new ValidationErrorException("File name does not match the document token");
			}
		}

		String reEncryptedKey = this.encryptionUtil.encryptFileKey(rawKey, accountId.longValue(), userId.longValue());

		PresignedUrlResponse response = this.s3Service.generatePresignedGetUrl(reEncryptedKey, accountId.longValue(),
				userId.longValue(), fileName, download, S3FileUploadConstants.VIEW_DURATION_MINUTES);

		if (!response.isSuccess()) {
			throw new ValidationErrorException("Failed to generate view URL: " + response.getErrorMessage());
		}

		return ReimbursementDocumentViewResponseBodyDto.builder()
			.documentFileName(fileName)
			.presignedViewUrl(response.getPreSignedUrl())
			.expiresInMinutes(S3FileUploadConstants.VIEW_EXPIRES_IN_MINUTES)
			.type(response.getType())
			.build();
	}

	@Override
	public void deleteReimbursementFile(String documentToken) {
		if (documentToken == null || documentToken.isEmpty()) {
			return;
		}

		Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();

		String rawKey = this.encryptionUtil.decryptFileKeyUnsafe(documentToken);
		if (rawKey == null || rawKey.isEmpty()) {
			return;
		}

		this.s3Service.deleteFile(rawKey, null, null, accountId.longValue());
	}

	private void validateInvoiceNotLinked(Integer timesheetId, Integer accountId) {
		TimesheetInvoice timesheetInvoice = this.timesheetInvoiceRepository.findByTimesheetId(timesheetId, accountId);
		if (timesheetInvoice != null && timesheetInvoice.getInvoiceId() != null) {
			throw new ConflictException(ExceptionMessageConstants.REIMBURSEMENT_INVOICE_LINKED);
		}
	}

	private void validateFileExtension(String fileName) {
		int lastDot = fileName.lastIndexOf('.');
		if (lastDot < 0) {
			throw new ValidationErrorException("File must have an extension. Allowed: "
					+ String.join(", ", S3FileUploadConstants.ALLOWED_EXTENSIONS));
		}
		String extension = fileName.substring(lastDot + 1).toLowerCase(Locale.ROOT);
		if (!S3FileUploadConstants.ALLOWED_EXTENSIONS.contains(extension)) {
			throw new ValidationErrorException("File extension '" + extension + "' is not allowed. Allowed: "
					+ String.join(", ", S3FileUploadConstants.ALLOWED_EXTENSIONS));
		}
	}

}
