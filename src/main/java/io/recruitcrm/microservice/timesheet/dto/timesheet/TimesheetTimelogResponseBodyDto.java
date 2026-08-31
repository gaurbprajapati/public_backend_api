package io.recruitcrm.microservice.timesheet.dto.timesheet;

import io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.types.PayBillTypeResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.types.StatusTypeResponseBodyDto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetTimelogResponseBodyDto {

	private Integer workTime;

	private Integer calculateBreakTime;

	@NotNull(message = "Timesheet id cannot be null")
	private Integer timesheetId;

	@NotNull(message = "Start date cannot be null")
	private Integer startDate;

	@NotNull(message = "End date cannot be null")
	private Integer endDate;

	@NotNull(message = "Total bill data cannot be null")
	private String totalBillData;

	@NotNull(message = "Total pay data cannot be null")
	private String totalPayData;

	@NotNull(message = "Approval status cannot be null")
	private StatusTypeResponseBodyDto approvalStatus;

	@NotNull(message = "Payment status cannot be null")
	private PayBillTypeResponseBodyDto paymentStatus;

	private Integer paymentDate;

	private String payoutNumber;

	@NotNull(message = "Billing status cannot be null")
	private PayBillTypeResponseBodyDto billingStatus;

	private Integer billingDate;

	private String invoiceNumber;

	private TimeLogResponseBodyDto timeLog;

}
