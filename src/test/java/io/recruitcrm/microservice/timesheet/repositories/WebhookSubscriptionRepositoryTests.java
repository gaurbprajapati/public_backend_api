package io.recruitcrm.microservice.timesheet.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import io.recruitcrm.entity.model.WebhookSubscription;
import io.recruitcrm.logging.logger.Logger;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WebhookSubscriptionRepositoryTests {

	private static final String JPQL = "SELECT ws FROM WebhookSubscription ws WHERE ws.accountId = :accountId AND ws.event = :event";

	@Mock
	private EntityManager entityManager;

	@Mock
	private Logger logger;

	@InjectMocks
	private WebhookSubscriptionRepository webhookSubscriptionRepository;

	@Test
	@DisplayName("findByAccountIdAndEvent should return first subscription when results exist")
	void testFindByAccountIdAndEventResultsFoundReturnsFirst() {
		// Given
		Integer accountId = 5;
		String event = "timesheet.approved";
		WebhookSubscription subscription = mock(WebhookSubscription.class);
		@SuppressWarnings("unchecked")
		TypedQuery<WebhookSubscription> mockQuery = mock(TypedQuery.class);
		given(this.entityManager.createQuery(JPQL, WebhookSubscription.class)).willReturn(mockQuery);
		given(mockQuery.setParameter("accountId", accountId)).willReturn(mockQuery);
		given(mockQuery.setParameter("event", event)).willReturn(mockQuery);
		given(mockQuery.setMaxResults(1)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(List.of(subscription));

		// When
		Optional<WebhookSubscription> result = this.webhookSubscriptionRepository.findByAccountIdAndEvent(accountId,
				event);

		// Then
		assertThat(result).contains(subscription);
		then(this.entityManager).should().createQuery(JPQL, WebhookSubscription.class);
		then(mockQuery).should().setParameter("accountId", accountId);
		then(mockQuery).should().setParameter("event", event);
		then(mockQuery).should().setMaxResults(1);
		then(mockQuery).should().getResultList();
		then(this.logger).should()
			.logInfo(String.format("Found %d webhook subscriptions for account ID: %d and event: %s", 1, accountId,
					event));
	}

	@Test
	@DisplayName("findByAccountIdAndEvent should return empty when no results")
	void testFindByAccountIdAndEventNoResultsReturnsEmpty() {
		// Given
		Integer accountId = 5;
		String event = "timesheet.approved";
		@SuppressWarnings("unchecked")
		TypedQuery<WebhookSubscription> mockQuery = mock(TypedQuery.class);
		given(this.entityManager.createQuery(JPQL, WebhookSubscription.class)).willReturn(mockQuery);
		given(mockQuery.setParameter("accountId", accountId)).willReturn(mockQuery);
		given(mockQuery.setParameter("event", event)).willReturn(mockQuery);
		given(mockQuery.setMaxResults(1)).willReturn(mockQuery);
		given(mockQuery.getResultList()).willReturn(Collections.emptyList());

		// When
		Optional<WebhookSubscription> result = this.webhookSubscriptionRepository.findByAccountIdAndEvent(accountId,
				event);

		// Then
		assertThat(result).isEmpty();
		then(mockQuery).should().getResultList();
		then(this.logger).should()
			.logInfo(String.format("Found %d webhook subscriptions for account ID: %d and event: %s", 0, accountId,
					event));
	}

}
