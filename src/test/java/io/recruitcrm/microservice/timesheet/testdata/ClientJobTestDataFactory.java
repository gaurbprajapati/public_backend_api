package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.microservice.timesheet.dto.portal.client.ClientJobResponseBodyDto;
import io.recruitcrm.microservice.timesheet.responses.APINormalResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Test data factory for ClientJob-related test objects. Provides factory methods to
 * create consistent test data across all client-job tests.
 */
public final class ClientJobTestDataFactory {

	private ClientJobTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static Integer getDefaultAccountId() {
		return 42;
	}

	public static String getDefaultEmail() {
		return "jane@example.com";
	}

	public static ClientJobResponseBodyDto createJobResponse() {
		return new ClientJobResponseBodyDto(501, "Senior Engineer", "contract", "Acme Corp", getDefaultEmail(),
				"primary", true);
	}

	public static List<ClientJobResponseBodyDto> createJobResponseList() {
		return List.of(createJobResponse());
	}

	public static ResponseEntity<APINormalResponse<List<ClientJobResponseBodyDto>>> createSuccessResponse(
			List<ClientJobResponseBodyDto> data) {
		APINormalResponse<List<ClientJobResponseBodyDto>> response = new APINormalResponse<>(data);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * Message constants for test assertions. Note: Inner types must be placed at the end
	 * to comply with InnerTypeLast checkstyle rule.
	 */
	public static final class Messages {

		public static final String JOBS_FETCHED_SUCCESSFULLY = "Jobs fetched successfully";

		private Messages() {
		}

	}

}
