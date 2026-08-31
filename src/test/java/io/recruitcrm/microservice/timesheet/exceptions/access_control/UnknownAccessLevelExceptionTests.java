/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.exceptions.access_control;

import static org.assertj.core.api.Assertions.assertThat;

import io.recruitcrm.microservice.timesheet.testdata.UnknownAccessLevelExceptionTestDataFactory;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit tests for {@link UnknownAccessLevelException}.
 */
class UnknownAccessLevelExceptionTests {

	static Stream<Arguments> messageCases() {
		return Stream.of(Arguments.arguments((String) null), Arguments.arguments(""),
				Arguments.arguments(UnknownAccessLevelExceptionTestDataFactory.getSampleUnknownAccessLevelMessage()),
				Arguments
					.arguments(UnknownAccessLevelExceptionTestDataFactory.getSampleMessageWithSpecialCharacters()));
	}

	@ParameterizedTest(name = "message = {0}")
	@MethodSource("messageCases")
	@DisplayName("Constructor stores message and type is runtime unchecked exception")
	void testConstructorPreservesMessageAndIsRuntimeException(String message) {
		// Given - message supplied by parameterized source

		// When
		UnknownAccessLevelException exception = new UnknownAccessLevelException(message);

		// Then
		assertThat(exception).isInstanceOf(RuntimeException.class);
		assertThat(exception.getMessage()).isEqualTo(message);
	}

}
