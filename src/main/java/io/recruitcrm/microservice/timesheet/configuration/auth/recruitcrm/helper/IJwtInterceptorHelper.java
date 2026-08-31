/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.helper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface IJwtInterceptorHelper {

	Boolean handleWithBearerToken(HttpServletResponse response, String authHeader) throws IOException;

	Boolean handleWithBearerToken(HttpServletRequest request, HttpServletResponse response, String authHeader)
			throws IOException;

	Boolean handleWithUserId(String userId);

	void sendErrorResponse(HttpServletResponse response, String errorMessage) throws IOException;

}
