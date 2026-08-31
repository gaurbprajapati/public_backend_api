package io.recruitcrm.microservice.timesheet.controllers.timesheet_logs;

import io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.UpdateTimeLogsRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetResponseBodyDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class TimesheetLogsControllerMockData {

	private TimesheetLogsControllerMockData() {
	}

	public static TimesheetResponseBodyDto getTimesheetResponseBodyDto() {
		TimesheetResponseBodyDto dto = new TimesheetResponseBodyDto();
		dto.setTimesheetId(1);
		dto.setApprovalStatusId(1);
		dto.setPayoutNumber("PAY123");
		dto.setInvoiceNumber("INV123");
		dto.setTimeLogs(getTimeLogResponseBodyDtoList());
		return dto;
	}

	public static List<TimeLogResponseBodyDto> getTimeLogResponseBodyDtoList() {
		TimeLogResponseBodyDto timeLog1 = new TimeLogResponseBodyDto();
		timeLog1.setId(1);
		timeLog1.setDate(1625097600); // Example Unix timestamp
		timeLog1.setDayTypeId(1);
		timeLog1.setWorkTime(8);
		timeLog1.setOverTime(1);
		timeLog1.setRemark("Regular work day");
		timeLog1.setTotalTime(9);

		TimeLogResponseBodyDto timeLog2 = new TimeLogResponseBodyDto();
		timeLog2.setId(2);
		timeLog2.setDate(1625184000); // Example Unix timestamp
		timeLog2.setDayTypeId(2);
		timeLog2.setWorkTime(4);
		timeLog2.setOverTime(0);
		timeLog2.setRemark("Half day");
		timeLog2.setTotalTime(4);

		return Arrays.asList(timeLog1, timeLog2);
	}

	public static UpdateTimeLogsRequestBodyDto getUpdateTimeLogsRequestBodyDto() {
		UpdateTimeLogsRequestBodyDto dto = new UpdateTimeLogsRequestBodyDto();
		List<TimeLogRequestBodyDto> logs = new ArrayList<>();

		TimeLogRequestBodyDto log1 = new TimeLogRequestBodyDto();
		log1.setId(1);
		log1.setWorkTime(8);
		log1.setWorkStartTime(1625122800); // Example Unix timestamp
		log1.setWorkEndTime(1625151600); // Example Unix timestamp
		log1.setBreakTime(1);
		log1.setOverTime(1);
		log1.setRemark("Regular work day");
		logs.add(log1);

		TimeLogRequestBodyDto log2 = new TimeLogRequestBodyDto();
		log2.setId(2);
		log2.setWorkTime(4);
		log2.setWorkStartTime(1625209200); // Example Unix timestamp
		log2.setWorkEndTime(1625223600); // Example Unix timestamp
		log2.setBreakTime(1);
		log2.setOverTime(0);
		log2.setRemark("Half day");
		logs.add(log2);

		dto.setLogs(logs);
		return dto;
	}

}