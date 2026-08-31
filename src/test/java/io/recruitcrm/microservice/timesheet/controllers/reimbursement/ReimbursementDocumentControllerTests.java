package io.recruitcrm.microservice.timesheet.controllers.reimbursement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementDocumentUploadRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementDocumentUploadResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.reimbursement.ReimbursementDocumentViewResponseBodyDto;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.reimbursement.IS3ReimbursementService;
import io.recruitcrm.microservice.timesheet.testdata.ReimbursementTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class ReimbursementDocumentControllerTests {

	@Mock
	private IS3ReimbursementService s3ReimbursementService;

	@Mock
	private APIResponder apiResponder;

	@InjectMocks
	private ReimbursementDocumentController reimbursementDocumentController;

	@BeforeEach
	void setUp() {
		// Mockito annotations handle all setup; intentionally empty
	}

	@Test
	@DisplayName("Generate upload URL successfully")
	void testGenerateUploadUrlValidRequestReturnsUploadUrl() {
		// Arrange
		ReimbursementDocumentUploadRequestBodyDto requestDto = ReimbursementTestDataFactory
			.createDocumentUploadRequest();
		ReimbursementDocumentUploadResponseBodyDto expectedResponse = ReimbursementTestDataFactory
			.createDocumentUploadResponse();
		ResponseEntity<?> expectedResponseEntity = ReimbursementTestDataFactory
			.createDocumentUploadSuccessResponse(expectedResponse);

		Mockito.when(this.s3ReimbursementService.generateUploadUrl(requestDto)).thenReturn(expectedResponse);
		Mockito
			.when(this.apiResponder.respond(expectedResponse,
					ReimbursementTestDataFactory.Messages.UPLOAD_URL_GENERATED_SUCCESSFULLY, APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn((ResponseEntity) expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.reimbursementDocumentController.generateUploadUrl(requestDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.s3ReimbursementService).generateUploadUrl(requestDto);
		Mockito.verify(this.apiResponder)
			.respond(expectedResponse, ReimbursementTestDataFactory.Messages.UPLOAD_URL_GENERATED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("View document should return presigned view URL")
	void testViewDocumentValidRequestReturnsViewUrl() {
		// Given
		String documentToken = ReimbursementTestDataFactory.getDefaultDocumentToken();
		String fileName = ReimbursementTestDataFactory.getDefaultDocumentFileName();
		Boolean download = false;
		ReimbursementDocumentViewResponseBodyDto expectedResponse = ReimbursementTestDataFactory
			.createDocumentViewResponse();
		ResponseEntity<?> expectedResponseEntity = ReimbursementTestDataFactory
			.createDocumentViewSuccessResponse(expectedResponse);

		given(this.s3ReimbursementService.viewFile(documentToken, fileName, download)).willReturn(expectedResponse);
		given(this.apiResponder.respond(expectedResponse,
				ReimbursementTestDataFactory.Messages.DOCUMENT_VIEW_URL_GENERATED_SUCCESSFULLY, APIResponseType.SUCCESS,
				HttpStatus.OK))
			.willReturn((ResponseEntity) expectedResponseEntity);

		// When
		ResponseEntity<?> response = this.reimbursementDocumentController.viewDocument(documentToken, fileName,
				download);

		// Then
		assertThat(response).isEqualTo(expectedResponseEntity);
		then(this.s3ReimbursementService).should().viewFile(documentToken, fileName, download);
		then(this.apiResponder).should()
			.respond(expectedResponse, ReimbursementTestDataFactory.Messages.DOCUMENT_VIEW_URL_GENERATED_SUCCESSFULLY,
					APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("View document with download flag true should return presigned download URL")
	void testViewDocumentDownloadTrueReturnsDownloadUrl() {
		// Given
		String documentToken = ReimbursementTestDataFactory.getDefaultDocumentToken();
		String fileName = ReimbursementTestDataFactory.getDefaultDocumentFileName();
		Boolean download = true;
		ReimbursementDocumentViewResponseBodyDto expectedResponse = ReimbursementTestDataFactory
			.createDocumentViewResponse();
		ResponseEntity<?> expectedResponseEntity = ReimbursementTestDataFactory
			.createDocumentViewSuccessResponse(expectedResponse);

		given(this.s3ReimbursementService.viewFile(documentToken, fileName, download)).willReturn(expectedResponse);
		given(this.apiResponder.respond(expectedResponse,
				ReimbursementTestDataFactory.Messages.DOCUMENT_VIEW_URL_GENERATED_SUCCESSFULLY, APIResponseType.SUCCESS,
				HttpStatus.OK))
			.willReturn((ResponseEntity) expectedResponseEntity);

		// When
		ResponseEntity<?> response = this.reimbursementDocumentController.viewDocument(documentToken, fileName,
				download);

		// Then
		assertThat(response).isEqualTo(expectedResponseEntity);
		then(this.s3ReimbursementService).should().viewFile(documentToken, fileName, download);
	}

	@Test
	@DisplayName("View document with null fileName should return presigned view URL")
	void testViewDocumentNullFileNameReturnsViewUrl() {
		// Given
		String documentToken = ReimbursementTestDataFactory.getDefaultDocumentToken();
		Boolean download = false;
		ReimbursementDocumentViewResponseBodyDto expectedResponse = ReimbursementTestDataFactory
			.createDocumentViewResponse();
		ResponseEntity<?> expectedResponseEntity = ReimbursementTestDataFactory
			.createDocumentViewSuccessResponse(expectedResponse);

		given(this.s3ReimbursementService.viewFile(documentToken, null, download)).willReturn(expectedResponse);
		given(this.apiResponder.respond(expectedResponse,
				ReimbursementTestDataFactory.Messages.DOCUMENT_VIEW_URL_GENERATED_SUCCESSFULLY, APIResponseType.SUCCESS,
				HttpStatus.OK))
			.willReturn((ResponseEntity) expectedResponseEntity);

		// When
		ResponseEntity<?> response = this.reimbursementDocumentController.viewDocument(documentToken, null, download);

		// Then
		assertThat(response).isEqualTo(expectedResponseEntity);
		then(this.s3ReimbursementService).should().viewFile(documentToken, null, download);
	}

}
