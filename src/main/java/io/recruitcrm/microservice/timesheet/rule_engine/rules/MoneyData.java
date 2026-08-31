/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.rule_engine.rules;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Container for monetary data (pay and bill amounts) Used to aggregate financial
 * information from rule evaluations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoneyData {

	/**
	 * Pay amount for the worker
	 */
	@PositiveOrZero(message = "Pay amount must be zero or positive")
	private BigDecimal payAmount;

	/**
	 * Bill amount for the client
	 */
	@PositiveOrZero(message = "Bill amount must be zero or positive")
	private BigDecimal billAmount;

	/**
	 * Creates a MoneyData instance with zero values
	 * @return new MoneyData with zero amounts
	 */
	public static MoneyData zero() {
		return MoneyData.builder().payAmount(BigDecimal.ZERO).billAmount(BigDecimal.ZERO).build();
	}

	/**
	 * Adds another MoneyData to this instance
	 * @param other the MoneyData to add
	 * @return new MoneyData with summed values
	 */
	public MoneyData add(MoneyData other) {
		if (other == null) {
			return this;
		}

		BigDecimal newPayAmount = (this.payAmount != null) ? this.payAmount : BigDecimal.ZERO;
		BigDecimal newBillAmount = (this.billAmount != null) ? this.billAmount : BigDecimal.ZERO;

		return MoneyData.builder()
			.payAmount(newPayAmount.add((other.payAmount != null) ? other.payAmount : BigDecimal.ZERO))
			.billAmount(newBillAmount.add((other.billAmount != null) ? other.billAmount : BigDecimal.ZERO))
			.build();
	}

	/**
	 * Subtracts another MoneyData from this instance
	 * @param other the MoneyData to subtract
	 * @return new MoneyData with subtracted values
	 */
	public MoneyData subtract(MoneyData other) {
		if (other == null) {
			return this;
		}

		BigDecimal newPayAmount = (this.payAmount != null) ? this.payAmount : BigDecimal.ZERO;
		BigDecimal newBillAmount = (this.billAmount != null) ? this.billAmount : BigDecimal.ZERO;

		return MoneyData.builder()
			.payAmount(newPayAmount.subtract((other.payAmount != null) ? other.payAmount : BigDecimal.ZERO))
			.billAmount(newBillAmount.subtract((other.billAmount != null) ? other.billAmount : BigDecimal.ZERO))
			.build();
	}

	/**
	 * Multiplies this MoneyData by a factor
	 * @param factor the factor to multiply by
	 * @return new MoneyData with multiplied values
	 */
	public MoneyData multiply(BigDecimal factor) {
		if (factor == null) {
			return this;
		}

		return MoneyData.builder()
			.payAmount((this.payAmount != null) ? this.payAmount.multiply(factor) : BigDecimal.ZERO)
			.billAmount((this.billAmount != null) ? this.billAmount.multiply(factor) : BigDecimal.ZERO)
			.build();
	}

	/**
	 * Gets the total monetary value (pay + bill)
	 * @return total monetary value
	 */
	public BigDecimal getTotalValue() {
		BigDecimal total = BigDecimal.ZERO;

		if (this.payAmount != null) {
			total = total.add(this.payAmount);
		}

		if (this.billAmount != null) {
			total = total.add(this.billAmount);
		}

		return total;
	}

	/**
	 * Checks if this MoneyData has any non-zero values
	 * @return true if either pay or bill amount is greater than zero
	 */
	public boolean hasValues() {
		return (this.payAmount != null && this.payAmount.compareTo(BigDecimal.ZERO) > 0)
				|| (this.billAmount != null && this.billAmount.compareTo(BigDecimal.ZERO) > 0);
	}

	/**
	 * Gets the pay amount, returning zero if null
	 * @return pay amount or zero
	 */
	public BigDecimal getPayAmountOrZero() {
		return (this.payAmount != null) ? this.payAmount : BigDecimal.ZERO;
	}

	/**
	 * Gets the bill amount, returning zero if null
	 * @return bill amount or zero
	 */
	public BigDecimal getBillAmountOrZero() {
		return (this.billAmount != null) ? this.billAmount : BigDecimal.ZERO;
	}

}
