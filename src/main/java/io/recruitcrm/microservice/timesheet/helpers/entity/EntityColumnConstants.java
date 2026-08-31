package io.recruitcrm.microservice.timesheet.helpers.entity;

public final class EntityColumnConstants {

	private EntityColumnConstants() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}

	public static final String TIMESHEET_CONTRACTOR = "timesheet_contractor";

	public static final String TIMESHEET_DEAL = "timesheet_deal";

	public static final String CONTRACTOR = "contractor_portal";

	public static final String ALL_TIMESHEET_PAGE = "timesheets";

	public static final String ALL_CONTRACTOR_PAGE = "contractors";

	public static final String CLIENT = "client_portal";

	public static final String ENTITY_COLUMNS_CONTRACTOR_JSON = "entity_columns_contractor.json";

	public static final String ENTITY_COLUMNS_DEAL_JSON = "entity_columns_deal.json";

	public static final String ENTITY_COLUMNS_ALL_TIMESHEET_PAGE = "entity_columns_all_timesheet_page.json";

	public static final String ENTITY_COLUMNS_ALL_CONTRACTOR_PAGE = "entity_columns_all_contractor_page.json";

	public static final String ENTITY_COLUMNS_CONTRACTOR_PORTAL_JSON = "entity_columns_contractor_portal.json";

	public static final String ENTITY_COLUMNS_CLIENT_PORTAL_JSON = "entity_columns_client_portal.json";

	public static final String OTHERS_VIEW_SUFFIX = "_others_view";

}
