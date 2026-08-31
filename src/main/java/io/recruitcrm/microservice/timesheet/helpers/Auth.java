package io.recruitcrm.microservice.timesheet.helpers;

import io.recruitcrm.entity.model.User;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class Auth {

	User user;

	Integer accountId;

	private Integer userId;

	public void setUser(@AuthenticationPrincipal User user) {
		this.user = user;
		this.accountId = (user != null) ? user.getAccount().getId() : null;
		this.userId = (user != null) ? user.getId() : null;
	}

	public User getUser() {
		if (this.user == null) {
			throw new ResourceNotFoundException("User not found.");
		}
		return this.user;
	}

	public Integer getAccountId() {
		if (this.accountId == null) {
			throw new UnauthorizedAccessException();
		}
		return this.accountId;
	}

	public Integer getUserId() {
		if (this.userId == null) {
			throw new ResourceNotFoundException("User not found.");
		}
		return this.userId;
	}

	public User getUserOrThrow() {
		if (this.user == null) {
			throw new UnauthorizedAccessException();
		}
		return this.user;
	}

}
