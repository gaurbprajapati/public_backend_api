package io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class JwtServiceTests {

	@InjectMocks
	private JwtService jwtService;

	private static final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

	private static final long EXPIRATION_TIME = 86400000; // 24 hours in milliseconds

	private static final Integer USER_ID = 123;

	private static final String USER_ID_STRING = USER_ID.toString();

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(this.jwtService, "secretKey", SECRET_KEY);
		ReflectionTestUtils.setField(this.jwtService, "jwtExpiration", EXPIRATION_TIME);
	}

	@Test
	@DisplayName("Should generate token with extra claims successfully")
	void shouldGenerateTokenWithExtraClaimsSuccessfully() {
		// given
		Map<String, Object> extraClaims = new HashMap<>();
		extraClaims.put("role", "ADMIN");
		extraClaims.put("permissions", List.of("READ", "WRITE"));

		// when
		String token = this.jwtService.generateToken(extraClaims, USER_ID);

		// then
		assertThat(token).isNotNull();
		assertThat(this.jwtService.extractUsername(token)).isEqualTo(USER_ID_STRING);

		String role = this.jwtService.extractClaim(token, (claims) -> claims.get("role", String.class));
		assertThat(role).isEqualTo("ADMIN");

		@SuppressWarnings("unchecked")
		List<String> permissions = this.jwtService.extractClaim(token,
				(claims) -> claims.get("permissions", List.class));
		assertThat(permissions).containsExactly("READ", "WRITE");
	}

	@Test
	@DisplayName("Should validate token successfully")
	void shouldValidateTokenSuccessfully() {
		// given
		String token = this.jwtService.generateToken(new HashMap<>(), USER_ID);

		// when
		boolean isValid = this.jwtService.isTokenValid(token, USER_ID);

		// then
		assertThat(isValid).isTrue();
	}

	@Test
	@DisplayName("Should reject token with different user ID")
	void shouldRejectTokenWithDifferentUserId() {
		// given
		String token = this.jwtService.generateToken(new HashMap<>(), USER_ID);

		// when
		boolean isValid = this.jwtService.isTokenValid(token, 456);

		// then
		assertThat(isValid).isFalse();
	}

	@Test
	@DisplayName("Should reject expired token")
	void shouldRejectExpiredToken() {
		// given
		Map<String, Object> extraClaims = new HashMap<>();
		String token = this.jwtService.buildToken(extraClaims, USER_ID, -1000); // Token
																				// expired
																				// 1
																				// second
																				// ago

		// when & then
		assertThatThrownBy(() -> this.jwtService.isTokenExpired(token)).isInstanceOf(ExpiredJwtException.class)
			.hasMessageContaining("JWT expired");
	}

	@Test
	@DisplayName("Should reject token with invalid signature")
	void shouldRejectTokenWithInvalidSignature() {
		// given
		String token = this.jwtService.generateToken(new HashMap<>(), USER_ID);
		String invalidKey = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970404E635266556A586E3272357538782F413F4428472B4B6250645367566B5971";
		ReflectionTestUtils.setField(this.jwtService, "secretKey", invalidKey);

		// when & then
		assertThatThrownBy(() -> this.jwtService.extractAllClaims(token)).isInstanceOf(SignatureException.class);
	}

	@Test
	@DisplayName("Should get expiration time successfully")
	void shouldGetExpirationTimeSuccessfully() {
		// when
		long expirationTime = this.jwtService.getExpirationTime();

		// then
		assertThat(expirationTime).isEqualTo(EXPIRATION_TIME);
	}

	@Test
	@DisplayName("Should generate token without extra claims successfully")
	void shouldGenerateTokenWithoutExtraClaimsSuccessfully() {
		// when
		String token = this.jwtService.generateToken(new HashMap<>(), USER_ID);

		// then
		assertThat(token).isNotNull();
		assertThat(this.jwtService.extractUsername(token)).isEqualTo(USER_ID_STRING);
	}

	@Test
	@DisplayName("Should extract username successfully")
	void shouldExtractUsernameSuccessfully() {
		// given
		String token = this.jwtService.generateToken(new HashMap<>(), USER_ID);

		// when
		String username = this.jwtService.extractUsername(token);

		// then
		assertThat(username).isEqualTo(USER_ID_STRING);
	}

	@Test
	@DisplayName("Should extract custom claim successfully")
	void shouldExtractCustomClaimSuccessfully() {
		// given
		Map<String, Object> extraClaims = new HashMap<>();
		extraClaims.put("customClaim", "customValue");
		String token = this.jwtService.generateToken(extraClaims, USER_ID);

		// when
		String customClaim = this.jwtService.extractClaim(token, (claims) -> claims.get("customClaim", String.class));

		// then
		assertThat(customClaim).isEqualTo("customValue");
	}

	@Test
	@DisplayName("Should extract expiration successfully")
	void shouldExtractExpirationSuccessfully() {
		// given
		String token = this.jwtService.generateToken(new HashMap<>(), USER_ID);

		// when
		Date expiration = this.jwtService.extractExpiration(token);

		// then
		assertThat(expiration).isAfter(new Date());
	}

	@Test
	@DisplayName("Should get sign in key successfully")
	void shouldGetSignInKeySuccessfully() {
		// when
		SecretKey signInKey = this.jwtService.getSignInKey();

		// then
		assertThat(signInKey).isNotNull();
	}

	@Test
	@DisplayName("Should extract all claims successfully")
	void shouldExtractAllClaimsSuccessfully() {
		// given
		Map<String, Object> extraClaims = new HashMap<>();
		extraClaims.put("claim1", "value1");
		extraClaims.put("claim2", "value2");
		String token = this.jwtService.generateToken(extraClaims, USER_ID);

		// when
		Claims claims = this.jwtService.extractAllClaims(token);

		// then
		assertThat(claims.getSubject()).isEqualTo(USER_ID_STRING);
		assertThat(claims.get("claim1", String.class)).isEqualTo("value1");
		assertThat(claims.get("claim2", String.class)).isEqualTo("value2");
		assertThat(claims.getExpiration()).isAfter(new Date());
	}

}