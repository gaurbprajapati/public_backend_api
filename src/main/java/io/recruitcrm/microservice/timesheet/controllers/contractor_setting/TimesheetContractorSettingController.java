package io.recruitcrm.microservice.timesheet.controllers.contractor_setting;

import io.recruitcrm.microservice.timesheet.dao.contractor_setting.TimeSlotsResultBodyDto;
import io.recruitcrm.microservice.timesheet.dto.contractor_setting.ContractorTimesheetSettingResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.contractor_setting.GetContractorListRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.BulkEmptySlotRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.EmptySlotRequestBodyDto;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.contractor_setting.TimesheetContractorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/timesheets")
public class TimesheetContractorSettingController implements ITimesheetContractorSettingController {

	private final TimesheetContractorService timesheetContractorService;

	private final APIResponder apiResponder;

	public TimesheetContractorSettingController(TimesheetContractorService timesheetContractorService,
			APIResponder apiResponder) {
		this.timesheetContractorService = timesheetContractorService;
		this.apiResponder = apiResponder;
	}

	@Override
	@PostMapping("/contractor-settings")
	public ResponseEntity<?> getContractorTimesheetSettings(@RequestBody GetContractorListRequestBodyDto requestDto) {
		Map<Integer, ContractorTimesheetSettingResponseBodyDto> settings = this.timesheetContractorService
			.getContractorTimesheetSettings(requestDto);
		return this.apiResponder.respond(settings, "Contractor timesheet settings fetched successfully",
				APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Override
	@PostMapping("/free-slots")
	public ResponseEntity<?> getFreeSlots(@RequestBody EmptySlotRequestBodyDto requestDto) {
		List<TimeSlotsResultBodyDto> emptySlots = this.timesheetContractorService.getFreeSlots(requestDto,
				requestDto.getTimesheetFrequencyId());
		return this.apiResponder.respond(emptySlots, "Empty slots fetched successfully", APIResponseType.SUCCESS,
				HttpStatus.OK);
	}

	@Override
	@PostMapping("/bulk-free-slots")
	public ResponseEntity<?> getBulkFreeSlots(@RequestBody BulkEmptySlotRequestBodyDto requestDto) {
		List<TimeSlotsResultBodyDto> commonSlots = this.timesheetContractorService.getBulkFreeSlots(requestDto);
		return this.apiResponder.respond(commonSlots, "Common free slots fetched successfully", APIResponseType.SUCCESS,
				HttpStatus.OK);
	}

}
