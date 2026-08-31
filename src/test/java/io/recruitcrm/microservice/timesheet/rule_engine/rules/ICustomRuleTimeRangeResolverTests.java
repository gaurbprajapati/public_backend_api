package io.recruitcrm.microservice.timesheet.rule_engine.rules;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import io.recruitcrm.microservice.timesheet.rule_engine.rules.timerange_resolver.TimeRangeResolverContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ICustomRuleTimeRangeResolver Tests")
class ICustomRuleTimeRangeResolverTests {

	@Test
	@DisplayName("ICustomRuleTimeRangeResolver implementation - basic functionality")
	void testICustomRuleTimeRangeResolverImplementationBasicFunctionality() {
		// Arrange
		ICustomRuleTimeRangeResolver resolver = createMockResolver();

		// Act
		RangeSet<LocalTime> result = resolver.resolveTimeRange(new TimeRangeResolverContext());

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.isEmpty()).isFalse();
	}

	@Test
	@DisplayName("ICustomRuleTimeRangeResolver implementation - with null context")
	void testICustomRuleTimeRangeResolverImplementationWithNullContext() {
		// Arrange
		ICustomRuleTimeRangeResolver resolver = createMockResolver();

		// Act & Assert
		assertThatThrownBy(() -> resolver.resolveTimeRange(null)).isInstanceOf(IllegalArgumentException.class);
	}

	private ICustomRuleTimeRangeResolver createMockResolver() {
		return new ICustomRuleTimeRangeResolver() {
			@Override
			public RangeSet<LocalTime> resolveTimeRange(TimeRangeResolverContext timeRangeResolverContext) {
				if (timeRangeResolverContext == null) {
					throw new IllegalArgumentException("Context cannot be null");
				}

				RangeSet<LocalTime> rangeSet = TreeRangeSet.create();
				rangeSet.add(Range.closedOpen(LocalTime.of(9, 0), LocalTime.of(17, 0)));
				return rangeSet;
			}
		};
	}

}