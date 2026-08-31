package io.recruitcrm.microservice.timesheet.dto.company;

import io.recruitcrm.microservice.timesheet.dto.off_limit.OffLimitInfoDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Data
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class CompanyOffLimitResponseBodyDto extends OffLimitInfoDto {

	private String name;

	private String profilePic;

	private String slug;

}
