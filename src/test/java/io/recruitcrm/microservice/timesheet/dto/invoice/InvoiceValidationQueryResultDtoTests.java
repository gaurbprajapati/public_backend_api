package io.recruitcrm.microservice.timesheet.dto.invoice;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("InvoiceValidationQueryResultDto Tests")
class InvoiceValidationQueryResultDtoTests {

	@Test
	@DisplayName("No-args constructor creates empty DTO")
	void testNoArgsConstructor() {
		InvoiceValidationQueryResultDto dto = new InvoiceValidationQueryResultDto();

		assertThat(dto.getTimesheetId()).isNull();
		assertThat(dto.getTimesheetApprovalStatusTypeId()).isNull();
		assertThat(dto.getCompanyName()).isNull();
	}

	@Test
	@DisplayName("All-args constructor sets all fields")
	void testAllArgsConstructor() {
		InvoiceValidationQueryResultDto dto = new InvoiceValidationQueryResultDto(Integer.valueOf(10),
				Integer.valueOf(4), "Recruit CRM", Integer.valueOf(1704067200), Integer.valueOf(1704672000),
				Integer.valueOf(1), Double.valueOf(1234.50), "$", "USD", "Jane Doe", "https://img/pic.png",
				Integer.valueOf(9001), Integer.valueOf(200), Integer.valueOf(300), "senior-java-dev",
				Integer.valueOf(301), Integer.valueOf(400), Integer.valueOf(401), "jane-doe", Integer.valueOf(500),
				Integer.valueOf(600), "USD", "$", Integer.valueOf(1));

		assertThat(dto.getTimesheetId()).isEqualTo(Integer.valueOf(10));
		assertThat(dto.getTimesheetApprovalStatusTypeId()).isEqualTo(Integer.valueOf(4));
		assertThat(dto.getCompanyName()).isEqualTo("Recruit CRM");
		assertThat(dto.getPeriodStart()).isEqualTo(Integer.valueOf(1704067200));
		assertThat(dto.getPeriodEnd()).isEqualTo(Integer.valueOf(1704672000));
		assertThat(dto.getCurrencyId()).isEqualTo(Integer.valueOf(1));
		assertThat(dto.getBillAmount()).isEqualTo(Double.valueOf(1234.50));
		assertThat(dto.getCurrencySymbol()).isEqualTo("$");
		assertThat(dto.getCurrencyCode()).isEqualTo("USD");
		assertThat(dto.getContractorName()).isEqualTo("Jane Doe");
		assertThat(dto.getContractorProfilePicUrl()).isEqualTo("https://img/pic.png");
		assertThat(dto.getContractorSerialNumber()).isEqualTo(Integer.valueOf(9001));
		assertThat(dto.getCompanyId()).isEqualTo(Integer.valueOf(200));
		assertThat(dto.getJobId()).isEqualTo(Integer.valueOf(300));
		assertThat(dto.getJobSlug()).isEqualTo("senior-java-dev");
		assertThat(dto.getJobContactId()).isEqualTo(Integer.valueOf(301));
		assertThat(dto.getContractorId()).isEqualTo(Integer.valueOf(400));
		assertThat(dto.getContractorOwnerId()).isEqualTo(Integer.valueOf(401));
		assertThat(dto.getContractorSlug()).isEqualTo("jane-doe");
		assertThat(dto.getDealId()).isEqualTo(Integer.valueOf(500));
		assertThat(dto.getContractorJobAssignmentId()).isEqualTo(Integer.valueOf(600));
		assertThat(dto.getPayCurrencyCode()).isEqualTo("USD");
		assertThat(dto.getPayCurrencySymbol()).isEqualTo("$");
		assertThat(dto.getIsReimbursementEnabled()).isEqualTo(Integer.valueOf(1));
	}

	@Test
	@DisplayName("Setters, equals, hashCode and toString work")
	void testSettersEqualsHashCodeAndToString() {
		InvoiceValidationQueryResultDto dto1 = new InvoiceValidationQueryResultDto();
		dto1.setTimesheetId(Integer.valueOf(11));
		dto1.setCurrencyCode("EUR");
		dto1.setBillAmount(Double.valueOf(250.75));

		InvoiceValidationQueryResultDto dto2 = new InvoiceValidationQueryResultDto();
		dto2.setTimesheetId(Integer.valueOf(11));
		dto2.setCurrencyCode("EUR");
		dto2.setBillAmount(Double.valueOf(250.75));

		assertThat(dto1).isEqualTo(dto2).hasSameHashCodeAs(dto2);
		assertThat(dto1.toString()).contains("timesheetId=11").contains("currencyCode=EUR");
	}

}
