package io.recruitcrm.microservice.timesheet.flagsmith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.flagsmith.exceptions.FeatureNotFoundError;
import com.flagsmith.exceptions.FlagsmithClientError;

@DisplayName("FlagsmithFlags Tests")
class FlagsmithFlagsTests {

	private static final String FEATURE_NAME = "contract-staffing";

	private FlagsmithFlags flagsmithFlags;

	@BeforeEach
	void setUp() {
		Map<String, FlagsmithBaseFlag> flags = new HashMap<>();
		flags.put(FEATURE_NAME, new FlagsmithBaseFlag(Boolean.TRUE, "enabled-value", FEATURE_NAME));
		this.flagsmithFlags = new FlagsmithFlags(flags);
	}

	@Test
	@DisplayName("Get flag should return the flag when the feature exists")
	void testGetFlagExistingFeatureReturnsFlag() throws FlagsmithClientError {
		// Given and When
		FlagsmithBaseFlag flag = this.flagsmithFlags.getFlag(FEATURE_NAME);

		// Then
		assertThat(flag).isNotNull();
		assertThat(flag.getFeatureName()).isEqualTo(FEATURE_NAME);
	}

	@Test
	@DisplayName("Get flag should throw when the feature does not exist")
	void testGetFlagMissingFeatureThrowsFeatureNotFoundError() {
		// Given and When and Then
		assertThatThrownBy(() -> this.flagsmithFlags.getFlag("missing")).isInstanceOf(FeatureNotFoundError.class)
			.hasMessageContaining("Feature does not exist");
	}

	@Test
	@DisplayName("Is feature enabled should return the flag enabled state")
	void testIsFeatureEnabledReturnsEnabledState() throws FlagsmithClientError {
		// Given and When
		Boolean enabled = this.flagsmithFlags.isFeatureEnabled(FEATURE_NAME);

		// Then
		assertThat(enabled).isTrue();
	}

	@Test
	@DisplayName("Get feature value should return the flag value")
	void testGetFeatureValueReturnsFlagValue() throws FlagsmithClientError {
		// Given and When
		Object value = this.flagsmithFlags.getFeatureValue(FEATURE_NAME);

		// Then
		assertThat(value).isEqualTo("enabled-value");
	}

	@Test
	@DisplayName("No-args constructor should initialize an empty flags map")
	void testNoArgsConstructorInitializesEmptyFlagsMap() {
		// Given and When
		FlagsmithFlags emptyFlags = new FlagsmithFlags();

		// Then
		assertThat(emptyFlags.getFlags()).isEmpty();
	}

	@Test
	@DisplayName("Set flags should replace the flags map")
	void testSetFlagsReplacesFlagsMap() {
		// Given
		FlagsmithFlags emptyFlags = new FlagsmithFlags();
		Map<String, FlagsmithBaseFlag> replacement = new HashMap<>();
		replacement.put(FEATURE_NAME, new FlagsmithBaseFlag(Boolean.FALSE, "v", FEATURE_NAME));

		// When
		emptyFlags.setFlags(replacement);

		// Then
		assertThat(emptyFlags.getFlags()).containsKey(FEATURE_NAME);
	}

}
