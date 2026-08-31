package io.recruitcrm.microservice.timesheet.configuration;

import io.recruitcrm.logging.ecs.EcsAuthContextProvider;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.AuthPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.JwtAuthServletFilter;
import io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.helper.IJwtInterceptorHelper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import static org.assertj.core.api.Assertions.assertThat;

class FilterConfigTests {

	@Test
	void shouldCreateJwtAuthServletFilter() {
		FilterConfig filterConfig = new FilterConfig();
		IJwtInterceptorHelper interceptorHelper = Mockito.mock(IJwtInterceptorHelper.class);

		JwtAuthServletFilter filter = filterConfig.jwtAuthServletFilter(interceptorHelper);

		assertThat(filter).isNotNull();
	}

	@Test
	void shouldCreateJwtAuthServletFilterRegistration() {
		FilterConfig filterConfig = new FilterConfig();
		JwtAuthServletFilter jwtAuthServletFilter = Mockito.mock(JwtAuthServletFilter.class);

		FilterRegistrationBean<JwtAuthServletFilter> registration = filterConfig
			.jwtAuthServletFilterRegistration(jwtAuthServletFilter);

		assertThat(registration.getFilter()).isEqualTo(jwtAuthServletFilter);
		assertThat(registration.getOrder()).isEqualTo(-1);
	}

	@Test
	void shouldCreateEcsAuthContextProvider() {
		FilterConfig filterConfig = new FilterConfig();
		AuthHolder authHolder = Mockito.mock(AuthHolder.class);
		AuthPrincipal principal = Mockito.mock(AuthPrincipal.class);
		Mockito.when(authHolder.getAuthenticationPrincipalOrganizationIdentifier()).thenReturn(123);
		Mockito.when(authHolder.getAuthenticationPrincipalUniqueIdentifier()).thenReturn(456);
		Mockito.when(authHolder.getUnifiedPrincipal()).thenReturn(principal);
		Mockito.when(principal.getEmail()).thenReturn("user@example.com");

		EcsAuthContextProvider provider = filterConfig.ecsAuthContextProvider(authHolder);

		assertThat(provider.getAccountId()).isEqualTo(123);
		assertThat(provider.getUserId()).isEqualTo(456);
		assertThat(provider.getUserEmail()).isEqualTo("user@example.com");
	}

	@Test
	void shouldSkipUserEmailWhenAuthPrincipalIsNull() {
		FilterConfig filterConfig = new FilterConfig();
		AuthHolder authHolder = Mockito.mock(AuthHolder.class);
		Mockito.when(authHolder.getUnifiedPrincipal()).thenReturn(null);

		EcsAuthContextProvider provider = filterConfig.ecsAuthContextProvider(authHolder);

		assertThat(provider.getUserEmail()).isNull();
	}

	@Test
	void shouldSkipUserEmailWhenAuthPrincipalIsUnavailable() {
		FilterConfig filterConfig = new FilterConfig();
		AuthHolder authHolder = Mockito.mock(AuthHolder.class);
		Mockito.when(authHolder.getUnifiedPrincipal()).thenThrow(new RuntimeException("missing principal"));

		EcsAuthContextProvider provider = filterConfig.ecsAuthContextProvider(authHolder);

		assertThat(provider.getUserEmail()).isNull();
	}

}
