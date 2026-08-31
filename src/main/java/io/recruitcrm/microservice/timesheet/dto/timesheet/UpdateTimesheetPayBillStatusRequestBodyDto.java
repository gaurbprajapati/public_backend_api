package io.recruitcrm.microservice.timesheet.dto.timesheet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTimesheetPayBillStatusRequestBodyDto {

	private Integer payBillType;

	private Integer payStatusId;

	private String payoutNumber;

	private String payoutFile;

	private Integer payoutPaidOn;

	private Integer billStatusId;

	private String invoiceNumber;

	private String invoiceFile;

	private Integer invoiceCreatedOn;

}
