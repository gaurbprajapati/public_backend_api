package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.TaskMeetings;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Test data factory for {@link TaskMeetings} objects.
 */
public final class TaskMeetingsTestDataFactory {

	public static final String DEFAULT_CAN_ADD = "1";

	public static final String DEFAULT_CAN_EDIT = "0";

	public static final String DEFAULT_CAN_VIEW = "1";

	public static final String DEFAULT_CAN_DELETE = "0";

	private TaskMeetingsTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Creates a TaskMeetings instance with default permission values.
	 * @return task meetings with default access values
	 */
	public static TaskMeetings createTaskMeetings() {
		TaskMeetings taskMeetings = new TaskMeetings();
		taskMeetings.setCanAdd(DEFAULT_CAN_ADD);
		taskMeetings.setCanEdit(DEFAULT_CAN_EDIT);
		taskMeetings.setCanView(DEFAULT_CAN_VIEW);
		taskMeetings.setCanDelete(DEFAULT_CAN_DELETE);
		return taskMeetings;
	}

	/**
	 * Creates a TaskMeetings instance with custom permission values.
	 * @param canAdd add permission
	 * @param canEdit edit permission
	 * @param canView view permission
	 * @param canDelete delete permission
	 * @return task meetings with custom access values
	 */
	public static TaskMeetings createTaskMeetings(String canAdd, String canEdit, String canView, String canDelete) {
		TaskMeetings taskMeetings = new TaskMeetings();
		taskMeetings.setCanAdd(canAdd);
		taskMeetings.setCanEdit(canEdit);
		taskMeetings.setCanView(canView);
		taskMeetings.setCanDelete(canDelete);
		return taskMeetings;
	}

	/**
	 * Creates default additional properties for TaskMeetings tests.
	 * @return map with deterministic property insertion order
	 */
	public static Map<String, Object> createAdditionalProperties() {
		Map<String, Object> additionalProperties = new LinkedHashMap<>();
		additionalProperties.put("customFlag", Boolean.TRUE);
		additionalProperties.put("permissionGroup", "task_meetings");
		return additionalProperties;
	}

}
