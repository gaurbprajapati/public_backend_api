/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm;

import io.recruitcrm.entity.model.User;
import io.recruitcrm.microservice.timesheet.configuration.auth.IAuthenticationPrincipal;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@RequestScope
@Component
public class AuthenticationPrincipal implements IAuthenticationPrincipal<Integer, User> {

	private User user;

	@Override
	public Integer getUniqueIdentifier() {
		return this.user.getId();
	}

	@Override
	public String getUserName() {
		return this.user.getUsername();
	}

	@Override
	public Integer getRoleIdentifier() {
		return this.user.getRoleId();
	}

	@Override
	public String getRoleIdentifierLabel() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Integer getOrganizationIdentifier() {
		return this.user.getAccount().getId();
	}

	@Override
	public User getUser() {
		return this.user;
	}

	@Override
	public void setUser(User authenticationPrincipal) {
		this.user = authenticationPrincipal;
	}

}
