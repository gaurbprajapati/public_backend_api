package io.recruitcrm.microservice.timesheet.configuration.jobs;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.Executor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@DisplayName("TimesheetJobAsyncConfiguration Tests")
class TimesheetJobAsyncConfigurationTests {

	private final TimesheetJobAsyncConfiguration timesheetJobAsyncConfiguration = new TimesheetJobAsyncConfiguration();

	@Test
	@DisplayName("timesheetJobExecutor should create configured ThreadPoolTaskExecutor")
	void testTimesheetJobExecutorCreatesConfiguredExecutor() {
		// When
		Executor executor = this.timesheetJobAsyncConfiguration.timesheetJobExecutor();

		// Then
		assertThat(executor).isInstanceOf(ThreadPoolTaskExecutor.class);
		ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) executor;
		assertThat(taskExecutor.getThreadNamePrefix()).isEqualTo("timesheet-job-");
		assertThat(taskExecutor.getCorePoolSize()).isEqualTo(2);
		assertThat(taskExecutor.getMaxPoolSize()).isEqualTo(2);
		assertThat(taskExecutor.getQueueCapacity()).isEqualTo(5);
	}

	@Test
	@DisplayName("TIMESHEET_JOB_EXECUTOR constant should match bean name")
	void testTimesheetJobExecutorConstantMatchesBeanName() {
		// Then
		assertThat(TimesheetJobAsyncConfiguration.TIMESHEET_JOB_EXECUTOR).isEqualTo("timesheetJobExecutor");
	}

}
