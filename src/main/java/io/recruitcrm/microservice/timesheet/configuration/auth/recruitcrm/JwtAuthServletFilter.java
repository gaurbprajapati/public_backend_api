/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm;

import io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.helper.IJwtInterceptorHelper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthServletFilter extends OncePerRequestFilter {

	private final IJwtInterceptorHelper interceptorHelper;

	public JwtAuthServletFilter(IJwtInterceptorHelper interceptorHelper) {
		this.interceptorHelper = interceptorHelper;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String authHeader = request.getHeader("Authorization");
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			Boolean success = this.interceptorHelper.handleWithBearerToken(request, response, authHeader);
			if (!Boolean.TRUE.equals(success)) {
				return;
			}
		}
		chain.doFilter(request, response);
	}

}
