/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import io.recruitcrm.entity.model.WebhookSubscription;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import io.recruitcrm.logging.config.LoggerConfiguration;
import io.recruitcrm.logging.logger.Logger;
import org.springframework.beans.factory.annotation.Qualifier;

@Repository
public class WebhookSubscriptionRepository {

	private final EntityManager entityManager;

	private final Logger logger;

	public WebhookSubscriptionRepository(EntityManager entityManager,
			@Qualifier(LoggerConfiguration.ASYNC_CONTEXT_LOGGER) Logger logger) {
		this.entityManager = entityManager;
		this.logger = logger;
	}

	/**
	 * Finds a webhook subscription by account and event. the database returns at most one
	 * row; if multiple exist, the first match is returned.
	 */
	public Optional<WebhookSubscription> findByAccountIdAndEvent(Integer accountId, String event) {
		TypedQuery<WebhookSubscription> query = this.entityManager.createQuery(
				"SELECT ws FROM WebhookSubscription ws WHERE ws.accountId = :accountId AND ws.event = :event",
				WebhookSubscription.class);
		query.setParameter("accountId", accountId);
		query.setParameter("event", event);
		query.setMaxResults(1);
		List<WebhookSubscription> results = query.getResultList();
		this.logger.logInfo(String.format("Found %d webhook subscriptions for account ID: %d and event: %s",
				results.size(), accountId, event));
		return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
	}

}
