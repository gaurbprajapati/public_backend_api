package io.recruitcrm.microservice.timesheet.controllers.contractor_setting;

import io.recruitcrm.microservice.timesheet.dto.contractor_setting.GetContractorListRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.BulkEmptySlotRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.EmptySlotRequestBodyDto;
import org.springframework.http.ResponseEntity;

public interface ITimesheetContractorSettingController {

	ResponseEntity<?> getContractorTimesheetSettings(GetContractorListRequestBodyDto requestDto);

	ResponseEntity<?> getFreeSlots(EmptySlotRequestBodyDto requestDto);

	ResponseEntity<?> getBulkFreeSlots(BulkEmptySlotRequestBodyDto requestDto);

}
