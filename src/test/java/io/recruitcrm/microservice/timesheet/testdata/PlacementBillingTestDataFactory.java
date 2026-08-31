package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.PlacementBilling;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Test data factory for {@link PlacementBilling} objects.
 */
public final class PlacementBillingTestDataFactory {

	public static final String DEFAULT_CAN_ADD = "1";

	public static final String DEFAULT_CAN_EDIT = "0";

	public static final String DEFAULT_CAN_VIEW = "1";

	public static final String DEFAULT_CAN_DELETE = "0";

	private PlacementBillingTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Creates a PlacementBilling instance with default permission values.
	 * @return placement billing with default access values
	 */
	public static PlacementBilling createPlacementBilling() {
		PlacementBilling placementBilling = new PlacementBilling();
		placementBilling.setCanAdd(DEFAULT_CAN_ADD);
		placementBilling.setCanEdit(DEFAULT_CAN_EDIT);
		placementBilling.setCanView(DEFAULT_CAN_VIEW);
		placementBilling.setCanDelete(DEFAULT_CAN_DELETE);
		return placementBilling;
	}

	/**
	 * Creates a PlacementBilling instance with custom permission values.
	 * @param canAdd add permission
	 * @param canEdit edit permission
	 * @param canView view permission
	 * @param canDelete delete permission
	 * @return placement billing with custom access values
	 */
	public static PlacementBilling createPlacementBilling(String canAdd, String canEdit, String canView,
			String canDelete) {
		PlacementBilling placementBilling = new PlacementBilling();
		placementBilling.setCanAdd(canAdd);
		placementBilling.setCanEdit(canEdit);
		placementBilling.setCanView(canView);
		placementBilling.setCanDelete(canDelete);
		return placementBilling;
	}

	/**
	 * Creates default additional properties for PlacementBilling tests.
	 * @return map with deterministic property insertion order
	 */
	public static Map<String, Object> createAdditionalProperties() {
		Map<String, Object> additionalProperties = new LinkedHashMap<>();
		additionalProperties.put("customFlag", Boolean.TRUE);
		additionalProperties.put("permissionGroup", "placement_billing");
		return additionalProperties;
	}

}
