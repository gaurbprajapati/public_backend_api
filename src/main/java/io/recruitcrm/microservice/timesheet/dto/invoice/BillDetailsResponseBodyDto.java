package io.recruitcrm.microservice.timesheet.dto.invoice;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for timesheet bill details endpoint. Contains invoice information
 * including status, file path, and other bill-related details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillDetailsResponseBodyDto {

	/**
	 * The timesheet ID associated with this invoice
	 */
	private Integer timesheetId;

	/**
	 * The bill status ID indicating the current billing status
	 */
	@JsonProperty("bilStatusId")
	private Integer billStatusId;

	/**
	 * The file path to the invoice document
	 */
	private String invoiceFile;

	/**
	 * The invoice number assigned to this timesheet
	 */
	private String invoiceNumber;

	/**
	 * Unix timestamp when the invoice was created
	 */
	private Integer invoiceCreatedOn;

	/**
	 * Additional remark or comment about the bill status
	 */
	private String remark;

}
