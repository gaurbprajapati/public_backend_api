package io.recruitcrm.microservice.timesheet.helpers.constants;

import java.util.Set;

public final class S3FileUploadConstants {

	private S3FileUploadConstants() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}

	public static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "jpg", "jpeg", "png");

	public static final String ACL_PRIVATE = "private";

	public static final String UPLOAD_DURATION_MINUTES = "5";

	public static final String VIEW_DURATION_MINUTES = "15";

	public static final int EXPIRES_IN_MINUTES = 5;

	public static final int VIEW_EXPIRES_IN_MINUTES = 15;

}
