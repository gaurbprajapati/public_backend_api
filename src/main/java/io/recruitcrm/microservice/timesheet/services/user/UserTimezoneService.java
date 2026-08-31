package io.recruitcrm.microservice.timesheet.services.user;

import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.repositories.user.UserRepository;
import org.springframework.stereotype.Service;

/**
 * Service implementation for user timezone operations. Retrieves user's configured
 * timezone from the database.
 */
@Service
public class UserTimezoneService implements IUserTimezoneService {

	private static final String DEFAULT_GMT_DIFFERENCE = "+00:00";

	private final UserRepository userRepository;

	private final AuthHolder auth;

	public UserTimezoneService(UserRepository userRepository, AuthHolder auth) {
		this.userRepository = userRepository;
		this.auth = auth;
	}

	@Override
	public String getCurrentUserGmtDifference() {
		Integer userId = this.auth.getAuthenticationPrincipalUniqueIdentifier();
		return this.getGmtDifferenceByUserId(userId);
	}

	@Override
	public String getGmtDifferenceByUserId(Integer userId) {
		if (userId == null) {
			return DEFAULT_GMT_DIFFERENCE;
		}

		String gmtDifference = this.userRepository.getGMTDifferenceByUserId(userId);

		if (gmtDifference == null || gmtDifference.isBlank()) {
			return DEFAULT_GMT_DIFFERENCE;
		}

		return gmtDifference;
	}

}
