package io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm;

import io.recruitcrm.entity.model.User;
import io.recruitcrm.microservice.timesheet.configuration.auth.IAuthHolder;
import io.recruitcrm.microservice.timesheet.configuration.auth.IAuthenticationPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.dto.UserMfaSettingsResponseBodyDto;
import io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.helper.IJwtInterceptorHelper;
import io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.service.UserMfaSettingsService;
import io.recruitcrm.microservice.timesheet.flagsmith.Flags;
import io.recruitcrm.microservice.timesheet.flagsmith.Flagsmith;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("MultiFactorAuthenticationJwtInterceptor Tests")
class MultiFactorAuthenticationJwtInterceptorTests {

	@Mock
	private IAuthHolder<Integer, User> auth;

	@Mock
	private UserMfaSettingsService userMfaSettingsService;

	@Mock
	private IJwtInterceptorHelper interceptorHelper;

	@Mock
	private Flagsmith flagsmith;

	@Mock
	private IAuthenticationPrincipal<Integer, User> authenticationPrincipal;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@InjectMocks
	private MultiFactorAuthenticationJwtInterceptor interceptor;

	private User user;

	@BeforeEach
	void setUp() {
		this.user = Mockito.mock(User.class, Mockito.RETURNS_DEEP_STUBS);
		lenient().when(this.user.getId()).thenReturn(10);
		lenient().when(this.auth.getAuthenticationPrincipal()).thenReturn(this.authenticationPrincipal);
		lenient().when(this.authenticationPrincipal.getUser()).thenReturn(this.user);
	}

	@Test
	@DisplayName("PreHandle returns true when MFA feature flag is disabled")
	void testPreHandleReturnsTrueWhenFeatureFlagDisabled() throws Exception {
		given(this.flagsmith.isFeatureEnabled(Flags.MULTI_FACTOR_AUTHENTICATION, this.user)).willReturn(false);

		assertThat(this.interceptor.preHandle(this.request, this.response, new Object())).isTrue();
		then(this.userMfaSettingsService).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("PreHandle returns true when MFA flag on but MFA neither enabled nor enforced on user")
	void testPreHandleReturnsTrueWhenMfaNotActiveOnUser() throws Exception {
		given(this.flagsmith.isFeatureEnabled(Flags.MULTI_FACTOR_AUTHENTICATION, this.user)).willReturn(true);
		given(this.user.getUserDetails().getMfaEnabled()).willReturn(null);
		given(this.user.getUserDetails().getMfaEnforced()).willReturn(null);

		assertThat(this.interceptor.preHandle(this.request, this.response, new Object())).isTrue();
		then(this.userMfaSettingsService).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("PreHandle returns true when user details are null")
	void testPreHandleReturnsTrueWhenUserDetailsNull() throws Exception {
		given(this.flagsmith.isFeatureEnabled(Flags.MULTI_FACTOR_AUTHENTICATION, this.user)).willReturn(true);
		given(this.user.getUserDetails()).willReturn(null);

		assertThat(this.interceptor.preHandle(this.request, this.response, new Object())).isTrue();
		then(this.userMfaSettingsService).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("PreHandle returns true when MFA enabled and web login satisfied")
	void testPreHandleReturnsTrueWhenWebMfaLoginSatisfied() throws Exception {
		given(this.flagsmith.isFeatureEnabled(Flags.MULTI_FACTOR_AUTHENTICATION, this.user)).willReturn(true);
		given(this.user.getUserDetails().getMfaEnabled()).willReturn(true);
		given(this.user.getUserDetails().getMfaEnforced()).willReturn((byte) 0);
		UserMfaSettingsResponseBodyDto settings = new UserMfaSettingsResponseBodyDto();
		settings.setWebMfaLogin(true);
		settings.setMobileMfaLogin(false);
		given(this.userMfaSettingsService.getUserMfaSettings(10)).willReturn(settings);

		assertThat(this.interceptor.preHandle(this.request, this.response, new Object())).isTrue();
	}

	@Test
	@DisplayName("PreHandle returns false when MFA enforced, grace OK, but neither web nor mobile login active")
	void testPreHandleReturnsFalseWhenNoMfaLoginChannel() throws Exception {
		given(this.flagsmith.isFeatureEnabled(Flags.MULTI_FACTOR_AUTHENTICATION, this.user)).willReturn(true);
		given(this.user.getUserDetails().getMfaEnabled()).willReturn(false);
		given(this.user.getUserDetails().getMfaEnforced()).willReturn((byte) 1);
		UserMfaSettingsResponseBodyDto settings = new UserMfaSettingsResponseBodyDto();
		settings.setMfaEnforcedDate((int) Instant.now().getEpochSecond());
		settings.setWebMfaLogin(false);
		settings.setMobileMfaLogin(false);
		given(this.userMfaSettingsService.getUserMfaSettings(10)).willReturn(settings);

		assertThat(this.interceptor.preHandle(this.request, this.response, new Object())).isFalse();
		then(this.interceptorHelper).should().sendErrorResponse(this.response, "Unauthorised access");
	}

	@Test
	@DisplayName("PreHandle returns false when MFA enforced but not enabled and grace period elapsed")
	void testPreHandleReturnsFalseWhenEnforcedButNotEnabledBeyondGrace() throws Exception {
		given(this.flagsmith.isFeatureEnabled(Flags.MULTI_FACTOR_AUTHENTICATION, this.user)).willReturn(true);
		given(this.user.getUserDetails().getMfaEnabled()).willReturn(false);
		given(this.user.getUserDetails().getMfaEnforced()).willReturn((byte) 1);
		UserMfaSettingsResponseBodyDto settings = new UserMfaSettingsResponseBodyDto();
		settings.setMfaEnforcedDate(0);
		given(this.userMfaSettingsService.getUserMfaSettings(10)).willReturn(settings);

		assertThat(this.interceptor.preHandle(this.request, this.response, new Object())).isFalse();
		then(this.interceptorHelper).should().sendErrorResponse(this.response, "Unauthorised");
	}

	@Test
	@DisplayName("PreHandle proceeds to login check when enforced but not enabled within grace window")
	void testPreHandleWithinGraceUsesHandleMfaLogin() throws Exception {
		given(this.flagsmith.isFeatureEnabled(Flags.MULTI_FACTOR_AUTHENTICATION, this.user)).willReturn(true);
		given(this.user.getUserDetails().getMfaEnabled()).willReturn(false);
		given(this.user.getUserDetails().getMfaEnforced()).willReturn((byte) 1);
		UserMfaSettingsResponseBodyDto settings = new UserMfaSettingsResponseBodyDto();
		settings.setMfaEnforcedDate((int) Instant.now().getEpochSecond());
		settings.setWebMfaLogin(false);
		settings.setMobileMfaLogin(true);
		given(this.userMfaSettingsService.getUserMfaSettings(10)).willReturn(settings);

		assertThat(this.interceptor.preHandle(this.request, this.response, new Object())).isTrue();
	}

	@Test
	@DisplayName("Handle MFA login returns true when mobile MFA login is true")
	void testHandleMfaLoginReturnsTrueForMobile() throws Exception {
		UserMfaSettingsResponseBodyDto settings = new UserMfaSettingsResponseBodyDto();
		settings.setWebMfaLogin(false);
		settings.setMobileMfaLogin(true);

		assertThat(this.interceptor.handleMfaLogin(settings, this.response)).isTrue();
		then(this.interceptorHelper).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("Is MFA enforced but not enabled returns false when MFA not enforced")
	void testIsMfaEnforcedButNotEnabledFalseWhenNotEnforced() {
		UserMfaSettingsResponseBodyDto settings = new UserMfaSettingsResponseBodyDto();
		given(this.user.getUserDetails().getMfaEnforced()).willReturn((byte) 0);

		assertThat(this.interceptor.isMfaEnforcedButNotEnabled(this.user, settings)).isFalse();
	}

	@Test
	@DisplayName("Is MFA enforced but not enabled returns false when user details are null")
	void testIsMfaEnforcedButNotEnabledFalseWhenUserDetailsNull() {
		given(this.user.getUserDetails()).willReturn(null);

		assertThat(this.interceptor.isMfaEnforcedButNotEnabled(this.user, new UserMfaSettingsResponseBodyDto()))
			.isFalse();
	}

	@Test
	@DisplayName("PostHandle and afterCompletion delegate without error")
	void testPostHandleAndAfterCompletionDelegate() {
		assertThatCode(() -> {
			this.interceptor.postHandle(this.request, this.response, new Object(), null);
			this.interceptor.afterCompletion(this.request, this.response, new Object(), null);
		}).doesNotThrowAnyException();
	}

}
