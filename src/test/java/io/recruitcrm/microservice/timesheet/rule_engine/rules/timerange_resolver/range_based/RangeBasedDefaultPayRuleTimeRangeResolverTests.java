package io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.range_based;

import com.google.common.collect.RangeSet;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.TimeRangeResolverContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RangeBasedDefaultPayRuleTimeRangeResolverTests {

	@Mock
	private Logger logger;

	private RangeBasedDefaultPayRuleTimeRangeResolver resolver;

	@BeforeEach
	void setUp() {
		this.resolver = new RangeBasedDefaultPayRuleTimeRangeResolver(this.logger);
	}

	@Test
	@DisplayName("Resolve time range - returns empty set (Default Pay ranges are pre-computed at week end)")
	void testResolveTimeRangeReturnsEmpty() {
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(new TimeRangeResolverContext());

		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	@DisplayName("Resolve time range - null context also returns empty set")
	void testResolveTimeRangeWithNullContextReturnsEmpty() {
		RangeSet<LocalTime> result = this.resolver.resolveTimeRange(null);

		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isTrue();
	}

}
