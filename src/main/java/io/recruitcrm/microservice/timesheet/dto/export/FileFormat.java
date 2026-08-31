package io.recruitcrm.microservice.timesheet.dto.export;

/**
 * Enum defining the supported file formats for exports.
 */
public enum FileFormat {

	/**
	 * Comma-separated values format
	 */
	CSV("csv", "text/csv"),

	/**
	 * Microsoft Excel format (XLSX)
	 */
	EXCEL("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

	private final String fileExtension;

	private final String mimeType;

	FileFormat(String fileExtension, String mimeType) {
		this.fileExtension = fileExtension;
		this.mimeType = mimeType;
	}

	/**
	 * Gets the file extension for this format
	 */
	public String getFileExtension() {
		return this.fileExtension;
	}

	/**
	 * Gets the MIME type for this format
	 */
	public String getMimeType() {
		return this.mimeType;
	}

}
