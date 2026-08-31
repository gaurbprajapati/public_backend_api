package io.recruitcrm.microservice.timesheet.helpers;

import io.recruitcrm.contract_staffing.entity.model.UserTypeEnum;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class FetchUserAndContactUserIds {

	public void addUserToAppropriateSet(Integer userTypeId, Integer userId, Set<Integer> agencyUserIds,
			Set<Integer> contactUserIds, Set<Integer> contractorUserIds) {
		if (userTypeId.equals(UserTypeEnum.AGENCY_RECRUITER.getId())) {
			agencyUserIds.add(userId);
		}
		else if (userTypeId.equals(UserTypeEnum.COMPANY_CONTACT.getId())) {
			contactUserIds.add(userId);
		}
		else {
			contractorUserIds.add(userId);
		}
	}

}
