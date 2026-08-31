/*
 * Copyright (c) 2026. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.dto.portal.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ClientPortalStatusEnum}.
 */
class ClientPortalStatusEnumTests {

	@Test
	@DisplayName("getValue returns the numeric value for each enum constant")
	void testGetValueReturnsNumericValueForEachConstant() {
		assertThat(ClientPortalStatusEnum.NOT_SENT.getValue()).isZero();
		assertThat(ClientPortalStatusEnum.INVITATION_SENT.getValue()).isEqualTo(1);
		assertThat(ClientPortalStatusEnum.PORTAL_ENABLED.getValue()).isEqualTo(2);
		assertThat(ClientPortalStatusEnum.PORTAL_DISABLED.getValue()).isEqualTo(3);
	}

	@Test
	@DisplayName("getLabel returns the human readable label for each enum constant")
	void testGetLabelReturnsLabelForEachConstant() {
		assertThat(ClientPortalStatusEnum.NOT_SENT.getLabel()).isEqualTo("Not Sent");
		assertThat(ClientPortalStatusEnum.INVITATION_SENT.getLabel()).isEqualTo("Invitation Sent");
		assertThat(ClientPortalStatusEnum.PORTAL_ENABLED.getLabel()).isEqualTo("Portal Enabled");
		assertThat(ClientPortalStatusEnum.PORTAL_DISABLED.getLabel()).isEqualTo("Portal Disabled");
	}

	@Test
	@DisplayName("getLabelByValue returns the matching label for each known value")
	void testGetLabelByValueReturnsMatchingLabelForKnownValues() {
		assertThat(ClientPortalStatusEnum.getLabelByValue(0)).isEqualTo("Not Sent");
		assertThat(ClientPortalStatusEnum.getLabelByValue(1)).isEqualTo("Invitation Sent");
		assertThat(ClientPortalStatusEnum.getLabelByValue(2)).isEqualTo("Portal Enabled");
		assertThat(ClientPortalStatusEnum.getLabelByValue(3)).isEqualTo("Portal Disabled");
	}

	@Test
	@DisplayName("getLabelByValue returns Not Sent label when value is null")
	void testGetLabelByValueNullValueReturnsNotSentLabel() {
		assertThat(ClientPortalStatusEnum.getLabelByValue(null)).isEqualTo("Not Sent");
	}

	@Test
	@DisplayName("getLabelByValue returns Not Sent label when value is unknown")
	void testGetLabelByValueUnknownValueReturnsNotSentLabel() {
		assertThat(ClientPortalStatusEnum.getLabelByValue(99)).isEqualTo("Not Sent");
	}

	@Test
	@DisplayName("valueOf resolves each enum constant by name")
	void testValueOfResolvesEachConstantByName() {
		assertThat(ClientPortalStatusEnum.valueOf("NOT_SENT")).isEqualTo(ClientPortalStatusEnum.NOT_SENT);
		assertThat(ClientPortalStatusEnum.values()).hasSize(4);
	}

}
