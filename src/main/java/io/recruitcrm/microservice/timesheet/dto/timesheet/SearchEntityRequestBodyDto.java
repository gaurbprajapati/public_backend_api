package io.recruitcrm.microservice.timesheet.dto.timesheet;

/**
 * Request DTO for searching entities (jobs, companies, deals). Multiple entity types can
 * be true at the same time.
 */
public record SearchEntityRequestBodyDto(String search, Boolean companies, Boolean jobs, Boolean deals,
		Boolean fromContractorsListPage) {
	public SearchEntityRequestBodyDto {
		// Normalize boolean values
		if (companies == null) {
			companies = false;
		}
		if (jobs == null) {
			jobs = false;
		}
		if (deals == null) {
			deals = false;
		}
		if (fromContractorsListPage == null) {
			fromContractorsListPage = false;
		}
		if (search == null) {
			search = "";
		}
	}
}
