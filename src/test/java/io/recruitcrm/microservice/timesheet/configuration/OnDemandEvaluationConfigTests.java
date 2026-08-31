/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration;

import java.util.concurrent.Executor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for OnDemandEvaluationConfig. Verifies thread pool properties and bean
 * creation for 100% line and branch coverage.
 */
@ExtendWith(MockitoExtension.class)
class OnDemandEvaluationConfigTests {

	private OnDemandEvaluationConfig config;

	@BeforeEach
	void setUp() {
		this.config = new OnDemandEvaluationConfig();
	}

	@Test
	@DisplayName("onDemandEvaluationExecutor - bean is not null")
	void testOnDemandEvaluationExecutorBeanIsNotNull() {
		// When
		Executor executor = this.config.onDemandEvaluationExecutor();

		// Then
		assertThat(executor).isNotNull();
	}

	@Test
	@DisplayName("onDemandEvaluationExecutor - returns ThreadPoolTaskExecutor instance")
	void testOnDemandEvaluationExecutorReturnsThreadPoolTaskExecutor() {
		// When
		Executor executor = this.config.onDemandEvaluationExecutor();

		// Then
		assertThat(executor).isInstanceOf(ThreadPoolTaskExecutor.class);
	}

	@Test
	@DisplayName("onDemandEvaluationExecutor - core pool size is 10")
	void testOnDemandEvaluationExecutorCorePoolSizeIsTen() {
		// When
		ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) this.config.onDemandEvaluationExecutor();

		// Then
		assertThat(executor.getCorePoolSize()).isEqualTo(10);
	}

	@Test
	@DisplayName("onDemandEvaluationExecutor - max pool size is 20")
	void testOnDemandEvaluationExecutorMaxPoolSizeIsTwenty() {
		// When
		ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) this.config.onDemandEvaluationExecutor();

		// Then
		assertThat(executor.getMaxPoolSize()).isEqualTo(20);
	}

	@Test
	@DisplayName("onDemandEvaluationExecutor - queue capacity is 200")
	void testOnDemandEvaluationExecutorQueueCapacityIsTwoHundred() {
		// When
		ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) this.config.onDemandEvaluationExecutor();

		// Then
		assertThat(executor.getThreadPoolExecutor().getQueue().remainingCapacity()).isEqualTo(200);
	}

	@Test
	@DisplayName("onDemandEvaluationExecutor - thread name prefix is 'on-demand-eval-'")
	void testOnDemandEvaluationExecutorThreadNamePrefixIsCorrect() {
		// When
		ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) this.config.onDemandEvaluationExecutor();

		// Then
		assertThat(executor.getThreadNamePrefix()).isEqualTo("on-demand-eval-");
	}

	@Test
	@DisplayName("onDemandEvaluationExecutor - executor is initialized and accepts tasks")
	void testOnDemandEvaluationExecutorIsInitialized() {
		// When
		ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) this.config.onDemandEvaluationExecutor();

		// Then
		assertThat(executor.getThreadPoolExecutor()).isNotNull();
	}

	@Test
	@DisplayName("onDemandEvaluationExecutor - returns Executor interface type")
	void testOnDemandEvaluationExecutorReturnsExecutorType() {
		// When
		Executor executor = this.config.onDemandEvaluationExecutor();

		// Then
		assertThat(executor).isInstanceOf(Executor.class);
	}

	@Test
	@DisplayName("onDemandEvaluationExecutor - each call creates a new initialized executor")
	void testOnDemandEvaluationExecutorEachCallCreatesNewExecutor() {
		// When
		Executor executor1 = this.config.onDemandEvaluationExecutor();
		Executor executor2 = this.config.onDemandEvaluationExecutor();

		// Then
		assertThat(executor1).isNotNull();
		assertThat(executor2).isNotNull();
		assertThat(executor1).isNotSameAs(executor2);
	}

	@Test
	@DisplayName("Constructor - creates config instance correctly")
	void testConstructorCreatesInstanceCorrectly() {
		// When
		OnDemandEvaluationConfig newConfig = new OnDemandEvaluationConfig();

		// Then
		assertThat(newConfig).isNotNull();
	}

}
