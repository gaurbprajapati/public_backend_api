package io.recruitcrm.microservice.timesheet.dto.off_limit;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class OffLimitInfoDto {

	private Integer offLimitStatusId;

	private String offLimitReason;

	private Integer offLimitEndDate;

	private Integer offLimitStartDate;

	private String statusLabel;

	private String backgroundColorHex;

	private String textColorHex;

	private String markedByName;

}
