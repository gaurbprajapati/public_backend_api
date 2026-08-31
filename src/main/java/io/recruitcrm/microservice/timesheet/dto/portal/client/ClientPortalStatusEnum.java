package io.recruitcrm.microservice.timesheet.dto.portal.client;

public enum ClientPortalStatusEnum {

	NOT_SENT(0, "Not Sent"), INVITATION_SENT(1, "Invitation Sent"), PORTAL_ENABLED(2, "Portal Enabled"),
	PORTAL_DISABLED(3, "Portal Disabled");

	private final Integer value;

	private final String label;

	ClientPortalStatusEnum(Integer value, String label) {
		this.value = value;
		this.label = label;
	}

	public Integer getValue() {
		return this.value;
	}

	public String getLabel() {
		return this.label;
	}

	public static String getLabelByValue(Integer value) {
		if (value != null) {
			for (ClientPortalStatusEnum status : values()) {
				if (status.value.equals(value)) {
					return status.label;
				}
			}
		}
		return NOT_SENT.label;
	}

}
