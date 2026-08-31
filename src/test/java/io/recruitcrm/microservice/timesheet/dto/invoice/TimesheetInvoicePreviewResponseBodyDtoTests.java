package io.recruitcrm.microservice.timesheet.dto.invoice;

import static org.assertj.core.api.Assertions.assertThat;

import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetPeriodResponseBodyDto;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("TimesheetInvoicePreviewResponseBodyDto Tests")
class TimesheetInvoicePreviewResponseBodyDtoTests {

	@Test
	@DisplayName("No-args constructor creates empty DTO")
	void testNoArgsConstructor() {
		TimesheetInvoicePreviewResponseBodyDto dto = new TimesheetInvoicePreviewResponseBodyDto();

		assertThat(dto.getTimesheetId()).isNull();
		assertThat(dto.getTimesheetPeriod()).isNull();
		assertThat(dto.getCurrencyId()).isNull();
		assertThat(dto.getAssociations()).isNull();
	}

	@Test
	@DisplayName("All-args constructor sets all fields")
	void testAllArgsConstructor() {
		TimesheetPeriodResponseBodyDto period = new TimesheetPeriodResponseBodyDto(Integer.valueOf(1704067200),
				Integer.valueOf(1704672000));
		AssociationsResponseBodyDto associations = new AssociationsResponseBodyDto(
				Map.of(Integer.valueOf(11), List.of(Integer.valueOf(500), Integer.valueOf(501))));

		TimesheetInvoicePreviewResponseBodyDto dto = new TimesheetInvoicePreviewResponseBodyDto(Integer.valueOf(10),
				period, Integer.valueOf(1), Double.valueOf(980.25), "$", "USD", "none", "Jane Doe",
				Integer.valueOf(401), "jane-doe", "job-slug", "https://img/pic.png", Integer.valueOf(123),
				Integer.valueOf(4), Integer.valueOf(600), Integer.valueOf(400), Integer.valueOf(300), associations,
				"USD", "$", Integer.valueOf(1));

		assertThat(dto.getTimesheetId()).isEqualTo(Integer.valueOf(10));
		assertThat(dto.getTimesheetPeriod()).isEqualTo(period);
		assertThat(dto.getCurrencyId()).isEqualTo(Integer.valueOf(1));
		assertThat(dto.getBillAmount()).isEqualTo(Double.valueOf(980.25));
		assertThat(dto.getBillCurrencySymbol()).isEqualTo("$");
		assertThat(dto.getBillCurrencyCode()).isEqualTo("USD");
		assertThat(dto.getErrorKey()).isEqualTo("none");
		assertThat(dto.getContractorName()).isEqualTo("Jane Doe");
		assertThat(dto.getContractorOwnerId()).isEqualTo(Integer.valueOf(401));
		assertThat(dto.getContractorSlug()).isEqualTo("jane-doe");
		assertThat(dto.getJobSlug()).isEqualTo("job-slug");
		assertThat(dto.getContractorProfilePicUrl()).isEqualTo("https://img/pic.png");
		assertThat(dto.getContractorSerialNumber()).isEqualTo(Integer.valueOf(123));
		assertThat(dto.getTimesheetApprovalStatusTypeId()).isEqualTo(Integer.valueOf(4));
		assertThat(dto.getContractorJobAssignmentId()).isEqualTo(Integer.valueOf(600));
		assertThat(dto.getContractorId()).isEqualTo(Integer.valueOf(400));
		assertThat(dto.getJobId()).isEqualTo(Integer.valueOf(300));
		assertThat(dto.getAssociations()).isEqualTo(associations);
		assertThat(dto.getPayCurrencyCode()).isEqualTo("USD");
		assertThat(dto.getPayCurrencySymbol()).isEqualTo("$");
		assertThat(dto.getIsReimbursementEnabled()).isEqualTo(Integer.valueOf(1));
	}

	@Test
	@DisplayName("Setters, equals, hashCode and toString work")
	void testSettersEqualsHashCodeAndToString() {
		TimesheetPeriodResponseBodyDto period = new TimesheetPeriodResponseBodyDto(Integer.valueOf(1704067200),
				Integer.valueOf(1704672000));

		TimesheetInvoicePreviewResponseBodyDto dto1 = new TimesheetInvoicePreviewResponseBodyDto();
		dto1.setTimesheetId(Integer.valueOf(77));
		dto1.setTimesheetPeriod(period);
		dto1.setBillCurrencyCode("EUR");

		TimesheetInvoicePreviewResponseBodyDto dto2 = new TimesheetInvoicePreviewResponseBodyDto();
		dto2.setTimesheetId(Integer.valueOf(77));
		dto2.setTimesheetPeriod(period);
		dto2.setBillCurrencyCode("EUR");

		assertThat(dto1).isEqualTo(dto2).hasSameHashCodeAs(dto2);
		assertThat(dto1.toString()).contains("timesheetId=77").contains("billCurrencyCode=EUR");
	}

}
