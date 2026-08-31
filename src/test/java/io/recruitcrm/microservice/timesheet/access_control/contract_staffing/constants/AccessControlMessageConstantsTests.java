/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.access_control.contract_staffing.constants;

import io.recruitcrm.microservice.timesheet.testdata.AccessControlMessageConstantsTestDataFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AccessControlMessageConstants Tests")
class AccessControlMessageConstantsTests {

	@Test
	@DisplayName("BULK_PERMISSION_CHECK_COMPLETED_PREFIX constant has expected value")
	void testBulkPermissionCheckCompletedPrefixConstantHasExpectedValue() {
		// Given

		// When and Then
		assertThat(AccessControlMessageConstantsTestDataFactory.getBulkPermissionCheckCompletedPrefix())
			.isEqualTo(AccessControlMessageConstants.BULK_PERMISSION_CHECK_COMPLETED_PREFIX);
	}

	@Test
	@DisplayName("constructor should be private and throw unsupported operation exception")
	void testConstructorShouldBePrivateAndThrowUnsupportedOperationException() throws NoSuchMethodException {
		// Given
		Constructor<AccessControlMessageConstants> constructor = AccessControlMessageConstants.class
			.getDeclaredConstructor();

		// When
		constructor.setAccessible(true);

		// Then
		assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
		assertThatThrownBy(constructor::newInstance).isInstanceOf(InvocationTargetException.class)
			.hasCauseInstanceOf(UnsupportedOperationException.class)
			.hasRootCauseMessage(
					AccessControlMessageConstantsTestDataFactory.getUtilityClassCannotBeInstantiatedMessage());
	}

}
