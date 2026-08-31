/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration for the on-demand rule engine evaluation thread pool. Bounds parallelism
 * to match DB connection pool capacity.
 */
@Configuration
public class OnDemandEvaluationConfig {

	@Bean("onDemandEvaluationExecutor")
	public Executor onDemandEvaluationExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(10);
		executor.setMaxPoolSize(20);
		executor.setQueueCapacity(200);
		executor.setThreadNamePrefix("on-demand-eval-");
		executor.initialize();
		return executor;
	}

}
