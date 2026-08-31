package io.recruitcrm.microservice.timesheet.dao.contractor_setting;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeSlotsResultBodyDto {

	Integer startDate;

	Integer endDate;

	// Debug fields - REMOVE AFTER DEVELOPMENT
	String startDateFormatted;

	String endDateFormatted;

	public TimeSlotsResultBodyDto(Integer startDate, Integer endDate) {
		this.startDate = startDate;
		this.endDate = endDate;
		// Populate debug fields
		this.startDateFormatted = formatTimestamp(startDate);
		this.endDateFormatted = formatTimestamp(endDate);
	}

	/**
	 * DEBUG METHOD - REMOVE AFTER DEVELOPMENT Converts epoch timestamp to readable date
	 * string.
	 */
	private String formatTimestamp(Integer epochSeconds) {
		if (epochSeconds == null) {
			return null;
		}
		LocalDate date = Instant.ofEpochSecond(epochSeconds).atZone(ZoneOffset.UTC).toLocalDate();
		return date.format(DateTimeFormatter.ofPattern("MMM d, yyyy (EEE)"));
	}

}
