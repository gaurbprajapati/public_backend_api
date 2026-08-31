package io.recruitcrm.microservice.timesheet.dto.timesheet;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("TimesheetSearchRequestBodyDto Tests")
class TimesheetSearchRequestBodyDtoTests {

	@Test
	@DisplayName("No-args constructor creates empty DTO")
	void testNoArgsConstructor() {
		TimesheetSearchRequestBodyDto dto = new TimesheetSearchRequestBodyDto();

		assertThat(dto.getAdvancedSearchContext()).isNull();
		assertThat(dto.getDefaultFilterList()).isNull();
		assertThat(dto.getFilterSearchList()).isNull();
		assertThat(dto.getBooleanSearchList()).isNull();
		assertThat(dto.getSortPriorityList()).isNull();
		assertThat(dto.getTimesheetIds()).isNull();
		assertThat(dto.getIsSubmitted()).isNull();
		assertThat(dto.getIsReimbursement()).isNull();
	}

	@Test
	@DisplayName("All-args constructor sets all fields including isReimbursement")
	void testAllArgsConstructor() {
		TimesheetSearchRequestBodyDto dto = new TimesheetSearchRequestBodyDto("context", null, null, null, List.of(),
				List.of(1, 2), Boolean.TRUE, Boolean.TRUE);

		assertThat(dto.getAdvancedSearchContext()).isEqualTo("context");
		assertThat(dto.getTimesheetIds()).containsExactly(1, 2);
		assertThat(dto.getIsSubmitted()).isTrue();
		assertThat(dto.getIsReimbursement()).isTrue();
	}

	@Test
	@DisplayName("Setters, equals, hashCode and toString work for isReimbursement")
	void testSettersEqualsHashCodeAndToString() {
		TimesheetSearchRequestBodyDto dto1 = new TimesheetSearchRequestBodyDto();
		dto1.setIsReimbursement(true);
		dto1.setIsSubmitted(false);

		TimesheetSearchRequestBodyDto dto2 = new TimesheetSearchRequestBodyDto();
		dto2.setIsReimbursement(true);
		dto2.setIsSubmitted(false);

		assertThat(dto1).isEqualTo(dto2).hasSameHashCodeAs(dto2);
		assertThat(dto1.toString()).contains("isReimbursement=true").contains("isSubmitted=false");
	}

	@Test
	@DisplayName("isReimbursement false is preserved when explicitly set")
	void testIsReimbursementFalseIsPreserved() {
		TimesheetSearchRequestBodyDto dto = new TimesheetSearchRequestBodyDto();
		dto.setIsReimbursement(false);

		assertThat(dto.getIsReimbursement()).isFalse();
	}

}
