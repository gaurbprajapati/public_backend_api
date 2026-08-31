package io.recruitcrm.microservice.timesheet.search.filters.deal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@DisplayName("DealFilterConditions Tests")
class DealFilterConditionsTests {

	private final Condition accountDealCondition = DSL.trueCondition();

	private final Field<Integer> dealIdField = DSL.field("deal_id", Integer.class);

	@Test
	@DisplayName("containsAtLeast should return false condition when deal IDs are empty")
	void testContainsAtLeastEmptyDealIdsReturnsFalseCondition() {
		assertThat(DealFilterConditions.containsAtLeast(List.of(), this.accountDealCondition, this.dealIdField, null)
			.get(0)
			.toString()).containsIgnoringCase("false");
	}

	@Test
	@DisplayName("containsAtLeast should return IN condition when deal IDs are present")
	void testContainsAtLeastWithDealIdsReturnsInCondition() {
		assertThat(
				DealFilterConditions.containsAtLeast(List.of(1, 2), this.accountDealCondition, this.dealIdField, null))
			.hasSize(1)
			.first()
			.isNotEqualTo(DSL.falseCondition());
	}

	@Test
	@DisplayName("containsAtLeast should append additional condition when provided")
	void testContainsAtLeastWithAdditionalCondition() {
		Condition additional = DSL.field("active").eq(1);
		assertThat(DealFilterConditions.containsAtLeast(List.of(1), this.accountDealCondition, this.dealIdField,
				additional))
			.hasSize(1);
	}

	@Test
	@DisplayName("containsAllHaving should return false condition when deal IDs are empty")
	void testContainsAllHavingEmptyDealIdsReturnsFalseCondition() {
		assertThat(DealFilterConditions.containsAllHaving(List.of(), this.dealIdField)).isEqualTo(DSL.falseCondition());
	}

	@Test
	@DisplayName("containsAllHaving should return count distinct condition when deal IDs are present")
	void testContainsAllHavingWithDealIdsReturnsCountDistinctCondition() {
		assertThat(DealFilterConditions.containsAllHaving(List.of(1, 2), this.dealIdField))
			.isNotEqualTo(DSL.falseCondition());
	}

	@Test
	@DisplayName("utility accessors should return expected defaults")
	void testUtilityAccessors() {
		assertThat(DealFilterConditions.emptyDealIdsMatchesAll()).isEmpty();
		assertThat(DealFilterConditions.emptyDealIdsMatchesNothing()).containsExactly(DSL.falseCondition());
		assertThat(DealFilterConditions.noGroupByFields()).isEmpty();
		assertThat(DealFilterConditions.noHavingCondition()).isEqualTo(DSL.noCondition());
		assertThat(DealFilterConditions.selectDistinctTrue()).isTrue();
		assertThat(DealFilterConditions.selectDistinctFalse()).isFalse();
	}

}
