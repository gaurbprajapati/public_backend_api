/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core;

import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.AccessControlDto;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.exceptions.access_control.UnknownAccessLevelException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds queries based on access control rules. This class is responsible for
 * constructing queries that respect the access control settings.
 */
@Component
public class QueryBuilder {

	private final AccessLevelHandler accessLevelHandler;

	private final EntityManager entityManager;

	private final AuthHolder authHolder;

	public QueryBuilder(AccessLevelHandler accessLevelHandler, EntityManager entityManager, AuthHolder authHolder) {
		this.accessLevelHandler = accessLevelHandler;
		this.entityManager = entityManager;
		this.authHolder = authHolder;
	}

	/**
	 * Builds a query that respects the access control settings for the given entity and
	 * permission.
	 * @param entityClass The entity class to query
	 * @param entity The entity type to check access for
	 * @param accessControlDto The access control data transfer object
	 * @param permission The permission to check
	 * @param <T> The entity type
	 * @return A criteria query that respects access control
	 * @throws UnknownAccessLevelException if the entity or permission is not supported
	 */
	public <T> CriteriaQuery<T> buildQuery(Class<T> entityClass, Entity entity, AccessControlDto accessControlDto,
			Permission permission) {
		PermissionLevel level = getAccessLevel(entity, accessControlDto, permission);
		Integer currentUserId = this.authHolder.getAuthenticationPrincipalUniqueIdentifier();

		CriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
		CriteriaQuery<T> query = cb.createQuery(entityClass);
		Root<T> root = query.from(entityClass);

		List<Predicate> predicates = new ArrayList<>();

		switch (level) {
			case OWNED_ONLY -> predicates.add(cb.equal(root.get("ownerId"), currentUserId));
			case TEAM_ONLY -> {
				// Get all team members
				List<Integer> teamUserIds = getTeamUserIds(currentUserId);
				predicates.add(root.get("ownerId").in(teamUserIds));
			}
			case YES, EVERYTHING -> {
				// No additional predicates needed - full access
			}
			case NO -> predicates.add(cb.disjunction()); // Always false
			default -> throw new UnknownAccessLevelException(String.format("Unsupported permission level: %s", level));
		}

		query.where(predicates.toArray(new Predicate[0]));
		return query;
	}

	/**
	 * Gets the access level for a given entity and action. Delegates to
	 * AccessLevelHandler for the actual access level determination.
	 * @param entity The entity to check access for
	 * @param accessControlDto The access control data transfer object
	 * @param permission The action to check access for
	 * @return The permission level for the entity and action
	 * @throws UnknownAccessLevelException if the entity or action is not supported
	 */
	public PermissionLevel getAccessLevel(Entity entity, AccessControlDto accessControlDto, Permission permission) {
		return this.accessLevelHandler.getAccessLevel(entity, accessControlDto, permission, null);
	}

	private List<Integer> getTeamUserIds(Integer userId) {
		String queryStr = """
				SELECT DISTINCT tm.userId
				FROM TeamMember tm
				WHERE tm.teamId IN (
				    SELECT t.teamId
				    FROM TeamMember t
				    WHERE t.userId = :userId
				)
				""";

		return this.entityManager.createQuery(queryStr, Integer.class).setParameter("userId", userId).getResultList();
	}

}
