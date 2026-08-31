package io.recruitcrm.microservice.timesheet.helpers.constants;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("S3FileUploadConstants Tests")
class S3FileUploadConstantsTests {

	@Test
	@DisplayName("Constants should expose expected file upload values")
	void testConstantsExposeExpectedValues() {
		// Given and When and Then
		assertThat(S3FileUploadConstants.ALLOWED_EXTENSIONS).containsExactlyInAnyOrder("pdf", "jpg", "jpeg", "png");
		assertThat(S3FileUploadConstants.ACL_PRIVATE).isEqualTo("private");
		assertThat(S3FileUploadConstants.UPLOAD_DURATION_MINUTES).isEqualTo("5");
		assertThat(S3FileUploadConstants.VIEW_DURATION_MINUTES).isEqualTo("15");
		assertThat(S3FileUploadConstants.EXPIRES_IN_MINUTES).isEqualTo(5);
		assertThat(S3FileUploadConstants.VIEW_EXPIRES_IN_MINUTES).isEqualTo(15);
	}

	@Test
	@DisplayName("Constructor should be private and throw unsupported operation exception")
	void testConstructorShouldBePrivateAndThrowUnsupportedOperationException() throws NoSuchMethodException {
		// Given
		Constructor<S3FileUploadConstants> constructor = S3FileUploadConstants.class.getDeclaredConstructor();

		// When
		constructor.setAccessible(true);

		// Then
		assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
		assertThatThrownBy(constructor::newInstance).isInstanceOf(InvocationTargetException.class)
			.hasCauseInstanceOf(UnsupportedOperationException.class);
	}

}
