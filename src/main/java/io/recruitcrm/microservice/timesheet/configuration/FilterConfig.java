package io.recruitcrm.microservice.timesheet.configuration;

import io.recruitcrm.logging.ecs.EcsAuthContextProvider;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.JwtAuthServletFilter;
import io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.helper.IJwtInterceptorHelper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

	@Bean
	public JwtAuthServletFilter jwtAuthServletFilter(IJwtInterceptorHelper interceptorHelper) {
		return new JwtAuthServletFilter(interceptorHelper);
	}

	@Bean
	public FilterRegistrationBean<JwtAuthServletFilter> jwtAuthServletFilterRegistration(
			JwtAuthServletFilter jwtAuthServletFilter) {
		FilterRegistrationBean<JwtAuthServletFilter> bean = new FilterRegistrationBean<>();
		bean.setFilter(jwtAuthServletFilter);
		bean.setOrder(-1);
		return bean;
	}

	@Bean
	public EcsAuthContextProvider ecsAuthContextProvider(AuthHolder authHolder) {
		return new EcsAuthContextProvider() {
			@Override
			public Integer getAccountId() {
				return authHolder.getAuthenticationPrincipalOrganizationIdentifier();
			}

			@Override
			public Integer getUserId() {
				return authHolder.getAuthenticationPrincipalUniqueIdentifier();
			}

			@Override
			public String getUserEmail() {
				try {
					return authHolder.getUnifiedPrincipal().getEmail();
				}
				catch (RuntimeException ignored) {
					return null;
				}
			}
		};
	}

}
