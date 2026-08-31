
package io.recruitcrm.microservice.timesheet.configuration.auth.keycloak;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Keycloak authentication.
 */
@Configuration
@ConfigurationProperties(prefix = "security.keycloak")
public class KeycloakConfigurationProperties {

	private boolean enabled = false;

	private String serverUrl;

	private String realm;

	private String issuerUri;

	private String publicKey;

	private String jwkSetUri;

	private boolean validateIssuer = true;

	private boolean validateAudience = false;

	private String expectedAudience = "account";

	private String detectionStrategy = "auto";

	private int connectionTimeout = 5000;

	private int readTimeout = 5000;

	private int cacheTtl = 300;

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getServerUrl() {
		return this.serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	public String getRealm() {
		return this.realm;
	}

	public void setRealm(String realm) {
		this.realm = realm;
	}

	public String getIssuerUri() {
		return this.issuerUri;
	}

	public void setIssuerUri(String issuerUri) {
		this.issuerUri = issuerUri;
	}

	public String getPublicKey() {
		return this.publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public String getJwkSetUri() {
		return this.jwkSetUri;
	}

	public void setJwkSetUri(String jwkSetUri) {
		this.jwkSetUri = jwkSetUri;
	}

	public boolean isValidateIssuer() {
		return this.validateIssuer;
	}

	public void setValidateIssuer(boolean validateIssuer) {
		this.validateIssuer = validateIssuer;
	}

	public boolean isValidateAudience() {
		return this.validateAudience;
	}

	public void setValidateAudience(boolean validateAudience) {
		this.validateAudience = validateAudience;
	}

	public String getExpectedAudience() {
		return this.expectedAudience;
	}

	public void setExpectedAudience(String expectedAudience) {
		this.expectedAudience = expectedAudience;
	}

	public String getDetectionStrategy() {
		return this.detectionStrategy;
	}

	public void setDetectionStrategy(String detectionStrategy) {
		this.detectionStrategy = detectionStrategy;
	}

	public int getConnectionTimeout() {
		return this.connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public int getReadTimeout() {
		return this.readTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	public int getCacheTtl() {
		return this.cacheTtl;
	}

	public void setCacheTtl(int cacheTtl) {
		this.cacheTtl = cacheTtl;
	}

}
