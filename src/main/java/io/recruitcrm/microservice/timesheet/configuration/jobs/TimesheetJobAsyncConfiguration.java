package io.recruitcrm.microservice.timesheet.configuration.jobs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Async execution for Lambda-triggered jobs so HTTP handlers can return quickly.
 */
@Configuration
@EnableAsync
public class TimesheetJobAsyncConfiguration {

	public static final String TIMESHEET_JOB_EXECUTOR = "timesheetJobExecutor";

	private static final int TIMESHEET_CORE_POOL_SIZE = 2;

	private static final int TIMESHEET_MAX_POOL_SIZE = 2;

	private static final int TIMESHEET_QUEUE_CAPACITY = 5;

	private static final int TIMESHEET_AWAIT_TERMINATION_SECONDS = 30;

	@Bean(name = TIMESHEET_JOB_EXECUTOR)
	public Executor timesheetJobExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setThreadNamePrefix("timesheet-job-");
		executor.setCorePoolSize(TIMESHEET_CORE_POOL_SIZE);
		executor.setMaxPoolSize(TIMESHEET_MAX_POOL_SIZE);
		executor.setQueueCapacity(TIMESHEET_QUEUE_CAPACITY);
		executor.setWaitForTasksToCompleteOnShutdown(true);
		executor.setAwaitTerminationSeconds(TIMESHEET_AWAIT_TERMINATION_SECONDS);
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
		executor.initialize();
		return executor;
	}

}
