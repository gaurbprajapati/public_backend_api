package io.recruitcrm.microservice.timesheet.helpers.entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("EntityColumnConstants Tests")
class EntityColumnConstantsTests {

	@Test
	@DisplayName("constants should expose expected entity column values")
	void testEntityColumnConstantsShouldMatchExpectedValues() {
		// Given

		// When and Then
		assertThat(EntityColumnConstants.TIMESHEET_CONTRACTOR).isEqualTo("timesheet_contractor");
		assertThat(EntityColumnConstants.TIMESHEET_DEAL).isEqualTo("timesheet_deal");
		assertThat(EntityColumnConstants.CONTRACTOR).isEqualTo("contractor_portal");
		assertThat(EntityColumnConstants.ALL_TIMESHEET_PAGE).isEqualTo("timesheets");
		assertThat(EntityColumnConstants.ALL_CONTRACTOR_PAGE).isEqualTo("contractors");
		assertThat(EntityColumnConstants.CLIENT).isEqualTo("client_portal");
		assertThat(EntityColumnConstants.ENTITY_COLUMNS_CONTRACTOR_JSON).isEqualTo("entity_columns_contractor.json");
		assertThat(EntityColumnConstants.ENTITY_COLUMNS_DEAL_JSON).isEqualTo("entity_columns_deal.json");
		assertThat(EntityColumnConstants.ENTITY_COLUMNS_ALL_TIMESHEET_PAGE)
			.isEqualTo("entity_columns_all_timesheet_page.json");
		assertThat(EntityColumnConstants.ENTITY_COLUMNS_ALL_CONTRACTOR_PAGE)
			.isEqualTo("entity_columns_all_contractor_page.json");
		assertThat(EntityColumnConstants.ENTITY_COLUMNS_CONTRACTOR_PORTAL_JSON)
			.isEqualTo("entity_columns_contractor_portal.json");
		assertThat(EntityColumnConstants.ENTITY_COLUMNS_CLIENT_PORTAL_JSON)
			.isEqualTo("entity_columns_client_portal.json");
	}

	@Test
	@DisplayName("constructor should be private and throw unsupported operation exception")
	void testConstructorShouldBePrivateAndThrowUnsupportedOperationException() throws NoSuchMethodException {
		// Given
		Constructor<EntityColumnConstants> constructor = EntityColumnConstants.class.getDeclaredConstructor();

		// When
		constructor.setAccessible(true);

		// Then
		assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
		assertThatThrownBy(constructor::newInstance).isInstanceOf(InvocationTargetException.class)
			.hasCauseInstanceOf(UnsupportedOperationException.class)
			.hasRootCauseMessage("This is a utility class and cannot be instantiated");
	}

}
