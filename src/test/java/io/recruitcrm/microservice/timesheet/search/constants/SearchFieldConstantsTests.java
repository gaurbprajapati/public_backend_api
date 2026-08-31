package io.recruitcrm.microservice.timesheet.search.constants;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("SearchFieldConstants Tests")
class SearchFieldConstantsTests {

	@Test
	@DisplayName("constants should expose expected search field values")
	void testSearchFieldConstantsShouldMatchExpectedValues() {
		// Given

		// When and Then
		assertThat(SearchFieldConstants.GROUP_TYPE_CONTRACTORS).isEqualTo("contractors");
		assertThat(SearchFieldConstants.FIELD_STATUS).isEqualTo("status");
		assertThat(SearchFieldConstants.FIELD_ADDED_ON).isEqualTo("added_on");
		assertThat(SearchFieldConstants.FIELD_TIMESHEET_PERIOD).isEqualTo("timesheetPeriod");
		assertThat(SearchFieldConstants.FIELD_COMPANY_NAME).isEqualTo("companyName");
		assertThat(SearchFieldConstants.FIELD_DEAL_NAME).isEqualTo("dealName");
		assertThat(SearchFieldConstants.FIELD_ASSOCIATED_DEAL).isEqualTo("associatedDeal");
		assertThat(SearchFieldConstants.FIELD_DEAL).isEqualTo("deal");
		assertThat(SearchFieldConstants.FIELD_JOB_NAME_SNAKE).isEqualTo("job_name");
		assertThat(SearchFieldConstants.FIELD_JOB_NAME_CAMEL).isEqualTo("jobName");
		assertThat(SearchFieldConstants.FIELD_JOB).isEqualTo("job");
		assertThat(SearchFieldConstants.FIELD_TIMESHEET_STATUS_SNAKE).isEqualTo("timesheet_status");
		assertThat(SearchFieldConstants.FIELD_TIMESHEET_STATUS_CAMEL).isEqualTo("timesheetStatus");
		assertThat(SearchFieldConstants.FIELD_TIMESHEET_STATUS_ID_SNAKE).isEqualTo("timesheet_status_id");
		assertThat(SearchFieldConstants.FIELD_TIMESHEET_STATUS_ID_CAMEL).isEqualTo("timesheetStatusId");
	}

	@Test
	@DisplayName("constructor should be private and throw unsupported operation exception")
	void testConstructorShouldBePrivateAndThrowUnsupportedOperationException() throws NoSuchMethodException {
		// Given
		Constructor<SearchFieldConstants> constructor = SearchFieldConstants.class.getDeclaredConstructor();

		// When
		constructor.setAccessible(true);

		// Then
		assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
		assertThatThrownBy(constructor::newInstance).isInstanceOf(InvocationTargetException.class)
			.hasCauseInstanceOf(UnsupportedOperationException.class)
			.hasRootCauseMessage("Utility class");
	}

}
