/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth.keycloak;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for KeycloakConfigurationProperties class. Tests all getters and setters for
 * 100% line and branch coverage.
 */
@ExtendWith(MockitoExtension.class)
class KeycloakConfigurationPropertiesTests {

	private KeycloakConfigurationProperties properties;

	@BeforeEach
	void setUp() {
		this.properties = new KeycloakConfigurationProperties();
	}

	@Nested
	@DisplayName("Enabled Property Tests")
	class EnabledPropertyTests {

		@Test
		@DisplayName("Should return default enabled value as false")
		void testIsEnabledDefaultValueReturnsFalse() {
			// When
			boolean result = KeycloakConfigurationPropertiesTests.this.properties.isEnabled();

			// Then
			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("Should set and get enabled value")
		void testSetEnabledValidValueSetsEnabled() {
			// Given
			boolean enabled = true;

			// When
			KeycloakConfigurationPropertiesTests.this.properties.setEnabled(enabled);
			boolean result = KeycloakConfigurationPropertiesTests.this.properties.isEnabled();

			// Then
			assertThat(result).isTrue();
		}

	}

	@Nested
	@DisplayName("ServerUrl Property Tests")
	class ServerUrlPropertyTests {

		@Test
		@DisplayName("Should return null for default serverUrl")
		void testGetServerUrlDefaultValueReturnsNull() {
			// When
			String result = KeycloakConfigurationPropertiesTests.this.properties.getServerUrl();

			// Then
			assertThat(result).isNull();
		}

		@Test
		@DisplayName("Should set and get serverUrl value")
		void testSetServerUrlValidValueSetsServerUrl() {
			// Given
			String serverUrl = "http://localhost:8080";

			// When
			KeycloakConfigurationPropertiesTests.this.properties.setServerUrl(serverUrl);
			String result = KeycloakConfigurationPropertiesTests.this.properties.getServerUrl();

			// Then
			assertThat(result).isEqualTo(serverUrl);
		}

	}

	@Nested
	@DisplayName("Realm Property Tests")
	class RealmPropertyTests {

		@Test
		@DisplayName("Should return null for default realm")
		void testGetRealmDefaultValueReturnsNull() {
			// When
			String result = KeycloakConfigurationPropertiesTests.this.properties.getRealm();

			// Then
			assertThat(result).isNull();
		}

		@Test
		@DisplayName("Should set and get realm value")
		void testSetRealmValidValueSetsRealm() {
			// Given
			String realm = "my-realm";

			// When
			KeycloakConfigurationPropertiesTests.this.properties.setRealm(realm);
			String result = KeycloakConfigurationPropertiesTests.this.properties.getRealm();

			// Then
			assertThat(result).isEqualTo(realm);
		}

	}

	@Nested
	@DisplayName("IssuerUri Property Tests")
	class IssuerUriPropertyTests {

		@Test
		@DisplayName("Should return null for default issuerUri")
		void testGetIssuerUriDefaultValueReturnsNull() {
			// When
			String result = KeycloakConfigurationPropertiesTests.this.properties.getIssuerUri();

			// Then
			assertThat(result).isNull();
		}

		@Test
		@DisplayName("Should set and get issuerUri value")
		void testSetIssuerUriValidValueSetsIssuerUri() {
			// Given
			String issuerUri = "http://localhost:8080/realms/my-realm";

			// When
			KeycloakConfigurationPropertiesTests.this.properties.setIssuerUri(issuerUri);
			String result = KeycloakConfigurationPropertiesTests.this.properties.getIssuerUri();

			// Then
			assertThat(result).isEqualTo(issuerUri);
		}

	}

	@Nested
	@DisplayName("PublicKey Property Tests")
	class PublicKeyPropertyTests {

		@Test
		@DisplayName("Should return null for default publicKey")
		void testGetPublicKeyDefaultValueReturnsNull() {
			// When
			String result = KeycloakConfigurationPropertiesTests.this.properties.getPublicKey();

			// Then
			assertThat(result).isNull();
		}

		@Test
		@DisplayName("Should set and get publicKey value")
		void testSetPublicKeyValidValueSetsPublicKey() {
			// Given
			String publicKey = "-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----";

			// When
			KeycloakConfigurationPropertiesTests.this.properties.setPublicKey(publicKey);
			String result = KeycloakConfigurationPropertiesTests.this.properties.getPublicKey();

			// Then
			assertThat(result).isEqualTo(publicKey);
		}

	}

	@Nested
	@DisplayName("JwkSetUri Property Tests")
	class JwkSetUriPropertyTests {

		@Test
		@DisplayName("Should return null for default jwkSetUri")
		void testGetJwkSetUriDefaultValueReturnsNull() {
			// When
			String result = KeycloakConfigurationPropertiesTests.this.properties.getJwkSetUri();

			// Then
			assertThat(result).isNull();
		}

		@Test
		@DisplayName("Should set and get jwkSetUri value")
		void testSetJwkSetUriValidValueSetsJwkSetUri() {
			// Given
			String jwkSetUri = "http://localhost:8080/realms/my-realm/protocol/openid-connect/certs";

			// When
			KeycloakConfigurationPropertiesTests.this.properties.setJwkSetUri(jwkSetUri);
			String result = KeycloakConfigurationPropertiesTests.this.properties.getJwkSetUri();

			// Then
			assertThat(result).isEqualTo(jwkSetUri);
		}

	}

	@Nested
	@DisplayName("ValidateIssuer Property Tests")
	class ValidateIssuerPropertyTests {

		@Test
		@DisplayName("Should return default validateIssuer value as true")
		void testIsValidateIssuerDefaultValueReturnsTrue() {
			// When
			boolean result = KeycloakConfigurationPropertiesTests.this.properties.isValidateIssuer();

			// Then
			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("Should set and get validateIssuer value")
		void testSetValidateIssuerValidValueSetsValidateIssuer() {
			// Given
			boolean validateIssuer = false;

			// When
			KeycloakConfigurationPropertiesTests.this.properties.setValidateIssuer(validateIssuer);
			boolean result = KeycloakConfigurationPropertiesTests.this.properties.isValidateIssuer();

			// Then
			assertThat(result).isFalse();
		}

	}

	@Nested
	@DisplayName("ValidateAudience Property Tests")
	class ValidateAudiencePropertyTests {

		@Test
		@DisplayName("Should return default validateAudience value as false")
		void testIsValidateAudienceDefaultValueReturnsFalse() {
			// When
			boolean result = KeycloakConfigurationPropertiesTests.this.properties.isValidateAudience();

			// Then
			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("Should set and get validateAudience value")
		void testSetValidateAudienceValidValueSetsValidateAudience() {
			// Given
			boolean validateAudience = true;

			// When
			KeycloakConfigurationPropertiesTests.this.properties.setValidateAudience(validateAudience);
			boolean result = KeycloakConfigurationPropertiesTests.this.properties.isValidateAudience();

			// Then
			assertThat(result).isTrue();
		}

	}

	@Nested
	@DisplayName("ExpectedAudience Property Tests")
	class ExpectedAudiencePropertyTests {

		@Test
		@DisplayName("Should return default expectedAudience value as account")
		void testGetExpectedAudienceDefaultValueReturnsAccount() {
			// When
			String result = KeycloakConfigurationPropertiesTests.this.properties.getExpectedAudience();

			// Then
			assertThat(result).isEqualTo("account");
		}

		@Test
		@DisplayName("Should set and get expectedAudience value")
		void testSetExpectedAudienceValidValueSetsExpectedAudience() {
			// Given
			String expectedAudience = "my-client";

			// When
			KeycloakConfigurationPropertiesTests.this.properties.setExpectedAudience(expectedAudience);
			String result = KeycloakConfigurationPropertiesTests.this.properties.getExpectedAudience();

			// Then
			assertThat(result).isEqualTo(expectedAudience);
		}

	}

	@Nested
	@DisplayName("DetectionStrategy Property Tests")
	class DetectionStrategyPropertyTests {

		@Test
		@DisplayName("Should return default detectionStrategy value as auto")
		void testGetDetectionStrategyDefaultValueReturnsAuto() {
			// When
			String result = KeycloakConfigurationPropertiesTests.this.properties.getDetectionStrategy();

			// Then
			assertThat(result).isEqualTo("auto");
		}

		@Test
		@DisplayName("Should set and get detectionStrategy value")
		void testSetDetectionStrategyValidValueSetsDetectionStrategy() {
			// Given
			String detectionStrategy = "keycloak";

			// When
			KeycloakConfigurationPropertiesTests.this.properties.setDetectionStrategy(detectionStrategy);
			String result = KeycloakConfigurationPropertiesTests.this.properties.getDetectionStrategy();

			// Then
			assertThat(result).isEqualTo(detectionStrategy);
		}

	}

	@Nested
	@DisplayName("ConnectionTimeout Property Tests")
	class ConnectionTimeoutPropertyTests {

		@Test
		@DisplayName("Should return default connectionTimeout value as 5000")
		void testGetConnectionTimeoutDefaultValueReturns5000() {
			// When
			int result = KeycloakConfigurationPropertiesTests.this.properties.getConnectionTimeout();

			// Then
			assertThat(result).isEqualTo(5000);
		}

		@Test
		@DisplayName("Should set and get connectionTimeout value")
		void testSetConnectionTimeoutValidValueSetsConnectionTimeout() {
			// Given
			int connectionTimeout = 10000;

			// When
			KeycloakConfigurationPropertiesTests.this.properties.setConnectionTimeout(connectionTimeout);
			int result = KeycloakConfigurationPropertiesTests.this.properties.getConnectionTimeout();

			// Then
			assertThat(result).isEqualTo(connectionTimeout);
		}

	}

	@Nested
	@DisplayName("ReadTimeout Property Tests")
	class ReadTimeoutPropertyTests {

		@Test
		@DisplayName("Should return default readTimeout value as 5000")
		void testGetReadTimeoutDefaultValueReturns5000() {
			// When
			int result = KeycloakConfigurationPropertiesTests.this.properties.getReadTimeout();

			// Then
			assertThat(result).isEqualTo(5000);
		}

		@Test
		@DisplayName("Should set and get readTimeout value")
		void testSetReadTimeoutValidValueSetsReadTimeout() {
			// Given
			int readTimeout = 15000;

			// When
			KeycloakConfigurationPropertiesTests.this.properties.setReadTimeout(readTimeout);
			int result = KeycloakConfigurationPropertiesTests.this.properties.getReadTimeout();

			// Then
			assertThat(result).isEqualTo(readTimeout);
		}

	}

	@Nested
	@DisplayName("CacheTtl Property Tests")
	class CacheTtlPropertyTests {

		@Test
		@DisplayName("Should return default cacheTtl value as 300")
		void testGetCacheTtlDefaultValueReturns300() {
			// When
			int result = KeycloakConfigurationPropertiesTests.this.properties.getCacheTtl();

			// Then
			assertThat(result).isEqualTo(300);
		}

		@Test
		@DisplayName("Should set and get cacheTtl value")
		void testSetCacheTtlValidValueSetsCacheTtl() {
			// Given
			int cacheTtl = 600;

			// When
			KeycloakConfigurationPropertiesTests.this.properties.setCacheTtl(cacheTtl);
			int result = KeycloakConfigurationPropertiesTests.this.properties.getCacheTtl();

			// Then
			assertThat(result).isEqualTo(cacheTtl);
		}

	}

}
