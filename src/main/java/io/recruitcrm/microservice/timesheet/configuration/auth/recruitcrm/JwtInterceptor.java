/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm;

import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.helper.IJwtInterceptorHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class JwtInterceptor implements HandlerInterceptor {

	private final IJwtInterceptorHelper interceptorHelper;

	private final AuthHolder authHolder;

	public JwtInterceptor(IJwtInterceptorHelper interceptorHelper,
			@Qualifier(AuthHolder.BEAN_NAME) AuthHolder authHolder) {
		this.interceptorHelper = interceptorHelper;
		this.authHolder = authHolder;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler)
			throws Exception {
		if (this.authHolder.hasPrincipal()) {
			return true;
		}
		final String authHeader = request.getHeader("Authorization");
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			// Pass request object to enable auth type detection
			return this.interceptorHelper.handleWithBearerToken(request, response, authHeader);
		}
		else {
			this.interceptorHelper.sendErrorResponse(response, "Missing bearer token in header");
			return false;
		}
	}

	@Override
	public void postHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
			@NotNull Object handler, ModelAndView modelAndView) throws Exception {
		HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
	}

	@Override
	public void afterCompletion(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
			@NotNull Object handler, Exception ex) throws Exception {
		HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
	}

}
