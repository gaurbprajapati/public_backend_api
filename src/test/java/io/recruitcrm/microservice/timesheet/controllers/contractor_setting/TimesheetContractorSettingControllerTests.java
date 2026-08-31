package io.recruitcrm.microservice.timesheet.controllers.contractor_setting;

import io.recruitcrm.microservice.timesheet.dao.contractor_setting.TimeSlotsResultBodyDto;
import io.recruitcrm.microservice.timesheet.dto.contractor_setting.ContractorTimesheetSettingResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.contractor_setting.GetContractorListRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.EmptySlotRequestBodyDto;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APINormalResponse;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.contractor_setting.TimesheetContractorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class TimesheetContractorSettingControllerTests {

	@Mock
	private TimesheetContractorService timesheetContractorService;

	@Mock
	private APIResponder apiResponder;

	@InjectMocks
	private TimesheetContractorSettingController timesheetContractorSettingController;

	@BeforeEach
	void setUp() {
		// @InjectMocks handles dependency injection automatically
	}

	@Test
	@DisplayName("Get contractor timesheet settings successfully")
	void testGetContractorTimesheetSettingsValidRequestReturnsSettings() {
		// Arrange
		GetContractorListRequestBodyDto requestDto = new GetContractorListRequestBodyDto();
		requestDto.setJobId(1);
		requestDto.setContractorIds(Arrays.asList(1, 2, 3));

		Map<Integer, ContractorTimesheetSettingResponseBodyDto> expectedSettings = new HashMap<>();
		ContractorTimesheetSettingResponseBodyDto setting1 = new ContractorTimesheetSettingResponseBodyDto();
		setting1.setTimesheetSettingId(1);
		setting1.setStartDate(1704067200); // 2024-01-01 00:00:00 UTC
		setting1.setEndDate(1704153600); // 2024-01-02 00:00:00 UTC
		setting1.setTimesheetStartDate(1704067200);
		setting1.setTimesheetFrequency(1);
		expectedSettings.put(1, setting1);

		APINormalResponse<Map<Integer, ContractorTimesheetSettingResponseBodyDto>> apiResponse = new APINormalResponse<>(
				expectedSettings);
		ResponseEntity<APINormalResponse<Map<Integer, ContractorTimesheetSettingResponseBodyDto>>> expectedResponseEntity = new ResponseEntity<>(
				apiResponse, HttpStatus.OK);

		Mockito.when(this.timesheetContractorService.getContractorTimesheetSettings(requestDto))
			.thenReturn(expectedSettings);
		Mockito
			.when(this.apiResponder.respond(expectedSettings, "Contractor timesheet settings fetched successfully",
					APIResponseType.SUCCESS, HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetContractorSettingController
			.getContractorTimesheetSettings(requestDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetContractorService).getContractorTimesheetSettings(requestDto);
		Mockito.verify(this.apiResponder)
			.respond(expectedSettings, "Contractor timesheet settings fetched successfully", APIResponseType.SUCCESS,
					HttpStatus.OK);
	}

	@Test
	@DisplayName("Get free slots successfully")
	void testGetFreeSlotsValidRequestReturnsSlots() {
		// Arrange
		EmptySlotRequestBodyDto requestDto = new EmptySlotRequestBodyDto();
		requestDto.setContractorIds(Arrays.asList(1, 2, 3));
		requestDto.setStartDate(1704067200); // 2024-01-01 00:00:00 UTC
		requestDto.setEndDate(1704153600); // 2024-01-02 00:00:00 UTC
		requestDto.setTimesheetFrequencyId(1);
		requestDto.setTimesheetStartDay(1);
		requestDto.setJobId(1);

		List<TimeSlotsResultBodyDto> expectedSlots = Arrays.asList(new TimeSlotsResultBodyDto(1704067200, 1704153600),
				new TimeSlotsResultBodyDto(1704153600, 1704240000));

		APINormalResponse<List<TimeSlotsResultBodyDto>> apiResponse = new APINormalResponse<>(expectedSlots);
		ResponseEntity<APINormalResponse<List<TimeSlotsResultBodyDto>>> expectedResponseEntity = new ResponseEntity<>(
				apiResponse, HttpStatus.OK);

		Mockito.when(this.timesheetContractorService.getFreeSlots(requestDto, requestDto.getTimesheetFrequencyId()))
			.thenReturn(expectedSlots);
		Mockito
			.when(this.apiResponder.respond(expectedSlots, "Empty slots fetched successfully", APIResponseType.SUCCESS,
					HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetContractorSettingController.getFreeSlots(requestDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetContractorService).getFreeSlots(requestDto, requestDto.getTimesheetFrequencyId());
		Mockito.verify(this.apiResponder)
			.respond(expectedSlots, "Empty slots fetched successfully", APIResponseType.SUCCESS, HttpStatus.OK);
	}

	@Test
	@DisplayName("Get bulk free slots successfully")
	void testGetBulkFreeSlotsValidRequestReturnsSlots() {
		// Arrange
		io.recruitcrm.microservice.timesheet.dto.timesheet.BulkEmptySlotRequestBodyDto requestDto = io.recruitcrm.microservice.timesheet.testdata.ContractorTestDataFactory
			.createBulkEmptySlotRequest();
		List<TimeSlotsResultBodyDto> expectedSlots = Arrays.asList(new TimeSlotsResultBodyDto(1704067200, 1704153600),
				new TimeSlotsResultBodyDto(1704153600, 1704240000));

		APINormalResponse<List<TimeSlotsResultBodyDto>> apiResponse = new APINormalResponse<>(expectedSlots);
		ResponseEntity<APINormalResponse<List<TimeSlotsResultBodyDto>>> expectedResponseEntity = new ResponseEntity<>(
				apiResponse, HttpStatus.OK);

		Mockito.when(this.timesheetContractorService.getBulkFreeSlots(requestDto)).thenReturn(expectedSlots);
		Mockito
			.when(this.apiResponder.respond(expectedSlots, "Common free slots fetched successfully",
					APIResponseType.SUCCESS, HttpStatus.OK))
			.thenReturn(expectedResponseEntity);

		// Act
		ResponseEntity<?> response = this.timesheetContractorSettingController.getBulkFreeSlots(requestDto);

		// Assert
		assertThat(response).isEqualTo(expectedResponseEntity);
		Mockito.verify(this.timesheetContractorService).getBulkFreeSlots(requestDto);
		Mockito.verify(this.apiResponder)
			.respond(expectedSlots, "Common free slots fetched successfully", APIResponseType.SUCCESS, HttpStatus.OK);
	}

}