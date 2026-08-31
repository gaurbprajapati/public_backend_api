/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

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
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Component
public class MultiFactorAuthenticationJwtInterceptor implements HandlerInterceptor {

	private static final String UNAUTHORISED_MESSAGE = "Unauthorised";

	private static final String UNAUTHORISED_ACCESS_MESSAGE = "Unauthorised access";

	private static final int MFA_GRACE_PERIOD_DAYS = 7;

	private final IAuthHolder<Integer, User> auth;

	private final UserMfaSettingsService userMfaSettingsService;

	private final IJwtInterceptorHelper interceptorHelper;

	private final Flagsmith flagsmith;

	public MultiFactorAuthenticationJwtInterceptor(IAuthHolder<Integer, User> auth,
			UserMfaSettingsService userMfaSettingsService, IJwtInterceptorHelper interceptorHelper,
			Flagsmith flagsmith) {
		this.auth = auth;
		this.userMfaSettingsService = userMfaSettingsService;
		this.interceptorHelper = interceptorHelper;
		this.flagsmith = flagsmith;
	}

	@Override
	public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
			@NotNull Object handler) throws Exception {
		IAuthenticationPrincipal<Integer, User> authenticationPrincipal = this.auth.getAuthenticationPrincipal();
		User authUser = authenticationPrincipal.getUser();
		Integer userId = authUser.getId();
		Boolean flagForMfa = this.flagsmith.isFeatureEnabled(Flags.MULTI_FACTOR_AUTHENTICATION, authUser);

		Boolean mfaEnabled = Optional.ofNullable(authUser.getUserDetails().getMfaEnabled()).orElse(Boolean.FALSE);
		Byte mfaEnforcedRaw = Optional.ofNullable(authUser.getUserDetails().getMfaEnforced()).orElse((byte) 0);
		boolean mfaEnforced = mfaEnforcedRaw != 0;

		if (Boolean.TRUE.equals(flagForMfa) && (mfaEnabled || mfaEnforced)) {
			return handleMfaSettings(userId, authUser, response);
		}
		return true;
	}

	public boolean handleMfaSettings(Integer userId, User userDetails, HttpServletResponse response)
			throws IOException {
		UserMfaSettingsResponseBodyDto mfaSettings = this.userMfaSettingsService.getUserMfaSettings(userId);
		if (isMfaEnforcedButNotEnabled(userDetails, mfaSettings)) {
			this.interceptorHelper.sendErrorResponse(response, UNAUTHORISED_MESSAGE);
			return false;
		}
		else if (isMfaEnforcedActive(userDetails.getUserDetails().getMfaEnforced())) {
			return handleMfaLogin(mfaSettings, response);
		}
		return true;
	}

	public boolean isMfaEnforcedButNotEnabled(User userDetails, UserMfaSettingsResponseBodyDto mfaSettings) {
		if (isMfaEnforcedActive(userDetails.getUserDetails().getMfaEnforced())
				&& Boolean.FALSE.equals(userDetails.getUserDetails().getMfaEnabled())) {
			Integer mfaEnforcedDateEpoch = Optional.ofNullable(mfaSettings.getMfaEnforcedDate()).orElse(0);
			LocalDateTime mfaEnforcedDate = LocalDateTime.ofEpochSecond(mfaEnforcedDateEpoch, 0, ZoneOffset.UTC);
			LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(MFA_GRACE_PERIOD_DAYS);
			return mfaEnforcedDate.isBefore(sevenDaysAgo);
		}
		return false;
	}

	public boolean handleMfaLogin(UserMfaSettingsResponseBodyDto mfaSettings, HttpServletResponse response)
			throws IOException {
		if (mfaSettings.getWebMfaLogin() || mfaSettings.getMobileMfaLogin()) {
			return true;
		}
		else {
			this.interceptorHelper.sendErrorResponse(response, UNAUTHORISED_ACCESS_MESSAGE);
			return false;
		}
	}

	private static boolean isMfaEnforcedActive(Byte mfaEnforced) {
		return mfaEnforced != null && mfaEnforced != 0;
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
