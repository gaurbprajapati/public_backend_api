package io.recruitcrm.microservice.timesheet.rule_engine.rules;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MoneyData Tests")
class MoneyDataTests {

	@Test
	@DisplayName("Zero - should create MoneyData with zero amounts")
	void testZero() {
		// Act
		MoneyData moneyData = MoneyData.zero();

		// Assert
		assertThat(moneyData.getPayAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(moneyData.getBillAmount()).isEqualTo(BigDecimal.ZERO);
	}

	@Test
	@DisplayName("Add with null - should return original MoneyData")
	void testAddWithNull() {
		// Arrange
		MoneyData original = MoneyData.builder()
			.payAmount(new BigDecimal("10.00"))
			.billAmount(new BigDecimal("20.00"))
			.build();

		// Act
		MoneyData result = original.add(null);

		// Assert
		assertThat(result).isEqualTo(original);
	}

	@Test
	@DisplayName("Add with valid MoneyData - should add amounts correctly")
	void testAddWithValidMoneyData() {
		// Arrange
		MoneyData first = MoneyData.builder()
			.payAmount(new BigDecimal("10.00"))
			.billAmount(new BigDecimal("20.00"))
			.build();

		MoneyData second = MoneyData.builder()
			.payAmount(new BigDecimal("15.00"))
			.billAmount(new BigDecimal("25.00"))
			.build();

		// Act
		MoneyData result = first.add(second);

		// Assert
		assertThat(result.getPayAmount()).isEqualTo(new BigDecimal("25.00"));
		assertThat(result.getBillAmount()).isEqualTo(new BigDecimal("45.00"));
	}

	@Test
	@DisplayName("Add with null amounts - should handle null values")
	void testAddWithNullAmounts() {
		// Arrange
		MoneyData first = MoneyData.builder().payAmount(null).billAmount(null).build();

		MoneyData second = MoneyData.builder()
			.payAmount(new BigDecimal("15.00"))
			.billAmount(new BigDecimal("25.00"))
			.build();

		// Act
		MoneyData result = first.add(second);

		// Assert
		assertThat(result.getPayAmount()).isEqualTo(new BigDecimal("15.00"));
		assertThat(result.getBillAmount()).isEqualTo(new BigDecimal("25.00"));
	}

	@Test
	@DisplayName("Add with both null amounts - should handle all null values")
	void testAddWithBothNullAmounts() {
		// Arrange
		MoneyData first = MoneyData.builder().payAmount(null).billAmount(null).build();

		MoneyData second = MoneyData.builder().payAmount(null).billAmount(null).build();

		// Act
		MoneyData result = first.add(second);

		// Assert
		assertThat(result.getPayAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getBillAmount()).isEqualTo(BigDecimal.ZERO);
	}

	@Test
	@DisplayName("Subtract with null - should return original MoneyData")
	void testSubtractWithNull() {
		// Arrange
		MoneyData original = MoneyData.builder()
			.payAmount(new BigDecimal("10.00"))
			.billAmount(new BigDecimal("20.00"))
			.build();

		// Act
		MoneyData result = original.subtract(null);

		// Assert
		assertThat(result).isEqualTo(original);
	}

	@Test
	@DisplayName("Subtract with valid MoneyData - should subtract amounts correctly")
	void testSubtractWithValidMoneyData() {
		// Arrange
		MoneyData first = MoneyData.builder()
			.payAmount(new BigDecimal("25.00"))
			.billAmount(new BigDecimal("45.00"))
			.build();

		MoneyData second = MoneyData.builder()
			.payAmount(new BigDecimal("10.00"))
			.billAmount(new BigDecimal("20.00"))
			.build();

		// Act
		MoneyData result = first.subtract(second);

		// Assert
		assertThat(result.getPayAmount()).isEqualTo(new BigDecimal("15.00"));
		assertThat(result.getBillAmount()).isEqualTo(new BigDecimal("25.00"));
	}

	@Test
	@DisplayName("Subtract with null amounts - should handle null values")
	void testSubtractWithNullAmounts() {
		// Arrange
		MoneyData first = MoneyData.builder()
			.payAmount(new BigDecimal("25.00"))
			.billAmount(new BigDecimal("45.00"))
			.build();

		MoneyData second = MoneyData.builder().payAmount(null).billAmount(null).build();

		// Act
		MoneyData result = first.subtract(second);

		// Assert
		assertThat(result.getPayAmount()).isEqualTo(new BigDecimal("25.00"));
		assertThat(result.getBillAmount()).isEqualTo(new BigDecimal("45.00"));
	}

	@Test
	@DisplayName("Subtract with this.payAmount or this.billAmount null")
	void testSubtractWithThisNullAmounts() {
		MoneyData first = MoneyData.builder().payAmount(null).billAmount(null).build();
		MoneyData second = MoneyData.builder()
			.payAmount(new BigDecimal("5.00"))
			.billAmount(new BigDecimal("7.00"))
			.build();
		MoneyData result = first.subtract(second);
		assertThat(result.getPayAmount()).isEqualTo(new BigDecimal("-5.00"));
		assertThat(result.getBillAmount()).isEqualTo(new BigDecimal("-7.00"));
	}

	@Test
	@DisplayName("Multiply with null factor - should return original MoneyData")
	void testMultiplyWithNullFactor() {
		// Arrange
		MoneyData original = MoneyData.builder()
			.payAmount(new BigDecimal("10.00"))
			.billAmount(new BigDecimal("20.00"))
			.build();

		// Act
		MoneyData result = original.multiply(null);

		// Assert
		assertThat(result).isEqualTo(original);
	}

	@Test
	@DisplayName("Multiply with valid factor - should multiply amounts correctly")
	void testMultiplyWithValidFactor() {
		// Arrange
		MoneyData moneyData = MoneyData.builder()
			.payAmount(new BigDecimal("10.00"))
			.billAmount(new BigDecimal("20.00"))
			.build();

		BigDecimal factor = new BigDecimal("1.5");

		// Act
		MoneyData result = moneyData.multiply(factor);

		// Assert
		// BigDecimal multiplication preserves scale: 10.00 * 1.5 = 15.000 (scale 3)
		assertThat(result.getPayAmount()).isEqualTo(new BigDecimal("15.000"));
		assertThat(result.getBillAmount()).isEqualTo(new BigDecimal("30.000"));
	}

	@Test
	@DisplayName("Multiply with null amounts - should handle null values")
	void testMultiplyWithNullAmounts() {
		// Arrange
		MoneyData moneyData = MoneyData.builder().payAmount(null).billAmount(null).build();

		BigDecimal factor = new BigDecimal("1.5");

		// Act
		MoneyData result = moneyData.multiply(factor);

		// Assert
		assertThat(result.getPayAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(result.getBillAmount()).isEqualTo(BigDecimal.ZERO);
	}

	@Test
	@DisplayName("GetTotalValue with both amounts - should return sum")
	void testGetTotalValueWithBothAmounts() {
		// Arrange
		MoneyData moneyData = MoneyData.builder()
			.payAmount(new BigDecimal("10.00"))
			.billAmount(new BigDecimal("20.00"))
			.build();

		// Act
		BigDecimal total = moneyData.getTotalValue();

		// Assert
		assertThat(total).isEqualTo(new BigDecimal("30.00"));
	}

	@Test
	@DisplayName("GetTotalValue with null amounts - should return zero")
	void testGetTotalValueWithNullAmounts() {
		// Arrange
		MoneyData moneyData = MoneyData.builder().payAmount(null).billAmount(null).build();

		// Act
		BigDecimal total = moneyData.getTotalValue();

		// Assert
		assertThat(total).isEqualTo(BigDecimal.ZERO);
	}

	@Test
	@DisplayName("GetTotalValue with one null amount - should return non-null amount")
	void testGetTotalValueWithOneNullAmount() {
		// Arrange
		MoneyData moneyData = MoneyData.builder().payAmount(new BigDecimal("10.00")).billAmount(null).build();

		// Act
		BigDecimal total = moneyData.getTotalValue();

		// Assert
		assertThat(total).isEqualTo(new BigDecimal("10.00"));
	}

	@Test
	@DisplayName("HasValues with positive amounts - should return true")
	void testHasValuesWithPositiveAmounts() {
		// Arrange
		MoneyData moneyData = MoneyData.builder()
			.payAmount(new BigDecimal("10.00"))
			.billAmount(new BigDecimal("20.00"))
			.build();

		// Act
		boolean hasValues = moneyData.hasValues();

		// Assert
		assertThat(hasValues).isTrue();
	}

	@Test
	@DisplayName("HasValues with zero amounts - should return false")
	void testHasValuesWithZeroAmounts() {
		// Arrange
		MoneyData moneyData = MoneyData.builder().payAmount(BigDecimal.ZERO).billAmount(BigDecimal.ZERO).build();

		// Act
		boolean hasValues = moneyData.hasValues();

		// Assert
		assertThat(hasValues).isFalse();
	}

	@Test
	@DisplayName("HasValues with null amounts - should return false")
	void testHasValuesWithNullAmounts() {
		// Arrange
		MoneyData moneyData = MoneyData.builder().payAmount(null).billAmount(null).build();

		// Act
		boolean hasValues = moneyData.hasValues();

		// Assert
		assertThat(hasValues).isFalse();
	}

	@Test
	@DisplayName("HasValues with one positive amount - should return true")
	void testHasValuesWithOnePositiveAmount() {
		// Arrange
		MoneyData moneyData = MoneyData.builder()
			.payAmount(new BigDecimal("10.00"))
			.billAmount(BigDecimal.ZERO)
			.build();

		// Act
		boolean hasValues = moneyData.hasValues();

		// Assert
		assertThat(hasValues).isTrue();
	}

	@Test
	@DisplayName("HasValues with only payAmount positive and billAmount null/zero")
	void testHasValuesWithOnlyPayAmountPositive() {
		MoneyData data = MoneyData.builder().payAmount(new BigDecimal("10.00")).billAmount(null).build();
		assertThat(data.hasValues()).isTrue();
		data = MoneyData.builder().payAmount(new BigDecimal("10.00")).billAmount(BigDecimal.ZERO).build();
		assertThat(data.hasValues()).isTrue();
	}

	@Test
	@DisplayName("HasValues with only billAmount positive and payAmount null/zero")
	void testHasValuesWithOnlyBillAmountPositive() {
		MoneyData data = MoneyData.builder().payAmount(null).billAmount(new BigDecimal("10.00")).build();
		assertThat(data.hasValues()).isTrue();
		data = MoneyData.builder().payAmount(BigDecimal.ZERO).billAmount(new BigDecimal("10.00")).build();
		assertThat(data.hasValues()).isTrue();
	}

	@Test
	@DisplayName("GetPayAmountOrZero with valid amount - should return amount")
	void testGetPayAmountOrZeroWithValidAmount() {
		// Arrange
		MoneyData moneyData = MoneyData.builder()
			.payAmount(new BigDecimal("10.00"))
			.billAmount(new BigDecimal("20.00"))
			.build();

		// Act
		BigDecimal result = moneyData.getPayAmountOrZero();

		// Assert
		assertThat(result).isEqualTo(new BigDecimal("10.00"));
	}

	@Test
	@DisplayName("GetPayAmountOrZero with null amount - should return zero")
	void testGetPayAmountOrZeroWithNullAmount() {
		// Arrange
		MoneyData moneyData = MoneyData.builder().payAmount(null).billAmount(new BigDecimal("20.00")).build();

		// Act
		BigDecimal result = moneyData.getPayAmountOrZero();

		// Assert
		assertThat(result).isEqualTo(BigDecimal.ZERO);
	}

	@Test
	@DisplayName("GetBillAmountOrZero with valid amount - should return amount")
	void testGetBillAmountOrZeroWithValidAmount() {
		// Arrange
		MoneyData moneyData = MoneyData.builder()
			.payAmount(new BigDecimal("10.00"))
			.billAmount(new BigDecimal("20.00"))
			.build();

		// Act
		BigDecimal result = moneyData.getBillAmountOrZero();

		// Assert
		assertThat(result).isEqualTo(new BigDecimal("20.00"));
	}

	@Test
	@DisplayName("GetBillAmountOrZero with null amount - should return zero")
	void testGetBillAmountOrZeroWithNullAmount() {
		// Arrange
		MoneyData moneyData = MoneyData.builder().payAmount(new BigDecimal("10.00")).billAmount(null).build();

		// Act
		BigDecimal result = moneyData.getBillAmountOrZero();

		// Assert
		assertThat(result).isEqualTo(BigDecimal.ZERO);
	}

	@Test
	@DisplayName("Builder pattern - should create MoneyData correctly")
	void testBuilderPattern() {
		// Arrange
		BigDecimal payAmount = new BigDecimal("10.00");
		BigDecimal billAmount = new BigDecimal("20.00");

		// Act
		MoneyData moneyData = MoneyData.builder().payAmount(payAmount).billAmount(billAmount).build();

		// Assert
		assertThat(moneyData.getPayAmount()).isEqualTo(payAmount);
		assertThat(moneyData.getBillAmount()).isEqualTo(billAmount);
	}

	@Test
	@DisplayName("NoArgsConstructor - should create MoneyData with null amounts")
	void testNoArgsConstructor() {
		// Act
		MoneyData moneyData = new MoneyData();

		// Assert
		assertThat(moneyData.getPayAmount()).isNull();
		assertThat(moneyData.getBillAmount()).isNull();
	}

	@Test
	@DisplayName("AllArgsConstructor - should create MoneyData with provided amounts")
	void testAllArgsConstructor() {
		// Arrange
		BigDecimal payAmount = new BigDecimal("10.00");
		BigDecimal billAmount = new BigDecimal("20.00");

		// Act
		MoneyData moneyData = new MoneyData(payAmount, billAmount);

		// Assert
		assertThat(moneyData.getPayAmount()).isEqualTo(payAmount);
		assertThat(moneyData.getBillAmount()).isEqualTo(billAmount);
	}

	@Test
	@DisplayName("Equals and hashCode - should work correctly")
	void testEqualsAndHashCode() {
		// Arrange
		MoneyData moneyData1 = MoneyData.builder()
			.payAmount(new BigDecimal("10.00"))
			.billAmount(new BigDecimal("20.00"))
			.build();

		MoneyData moneyData2 = MoneyData.builder()
			.payAmount(new BigDecimal("10.00"))
			.billAmount(new BigDecimal("20.00"))
			.build();

		MoneyData moneyData3 = MoneyData.builder()
			.payAmount(new BigDecimal("15.00"))
			.billAmount(new BigDecimal("25.00"))
			.build();

		// Act & Assert
		assertThat(moneyData1).isEqualTo(moneyData2)
			.isNotEqualTo(moneyData3)
			.hasSameHashCodeAs(moneyData2)
			.doesNotHaveSameHashCodeAs(moneyData3);
	}

	@Test
	@DisplayName("ToString - should contain amount information")
	void testToString() {
		// Arrange
		MoneyData moneyData = MoneyData.builder()
			.payAmount(new BigDecimal("10.00"))
			.billAmount(new BigDecimal("20.00"))
			.build();

		// Act
		String result = moneyData.toString();

		// Assert
		assertThat(result).contains("payAmount=10.00").contains("billAmount=20.00");
	}

}