package io.recruitcrm.microservice.timesheet.services.contractor_setting;

import io.recruitcrm.microservice.timesheet.dao.contractor_setting.TimeSlotsResultBodyDto;
import io.recruitcrm.microservice.timesheet.dto.contractor_setting.ContractorTimesheetSettingResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.contractor_setting.GetContractorListRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.BulkEmptySlotRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.EmptySlotRequestBodyDto;

import java.util.List;
import java.util.Map;

public interface ITimesheetContractorService {

	Map<Integer, ContractorTimesheetSettingResponseBodyDto> getContractorTimesheetSettings(
			GetContractorListRequestBodyDto requestDto);

	List<TimeSlotsResultBodyDto> getFreeSlots(EmptySlotRequestBodyDto requestDto, Integer timesheetFrequency);

	List<TimeSlotsResultBodyDto> getBulkFreeSlots(BulkEmptySlotRequestBodyDto requestDto);

}
