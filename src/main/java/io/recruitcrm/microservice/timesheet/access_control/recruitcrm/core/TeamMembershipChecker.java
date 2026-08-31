/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Helper class for checking team membership between users.
 */
@Component(TeamMembershipChecker.BEAN_NAME)
public class TeamMembershipChecker {

	public static final String BEAN_NAME = "recruitcrmTeamMembershipChecker";

	private final EntityManager entityManager;

	public TeamMembershipChecker(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	/**
	 * Checks if two users are in the same team.
	 * @param userId1 The ID of the first user
	 * @param userId2 The ID of the second user
	 * @return true if the users are in the same team, false otherwise
	 */
	public boolean isInSameTeam(Integer userId1, Integer userId2) {
		List<Integer> teamUserIds = getAllUserIdsOfTeam(userId1);
		return !teamUserIds.isEmpty() && teamUserIds.contains(userId2);
	}

	private List<Integer> getAllUserIdsOfTeam(Integer userId) {
		String queryStr = """
				SELECT DISTINCT tm.userId
				FROM TeamMember tm
				WHERE tm.teamId IN (
				    SELECT t.teamId
				    FROM TeamMember t
				    WHERE t.userId = :userId
				)
				""";

		TypedQuery<Integer> query = this.entityManager.createQuery(queryStr, Integer.class);
		query.setParameter("userId", userId);
		return query.getResultList();
	}

}