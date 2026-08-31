package io.recruitcrm.microservice.timesheet.controllers.time_log;

import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogIntervalMigrationRequestBodyDto;
import io.recruitcrm.microservice.timesheet.dto.time_log.TimeLogIntervalMigrationResponseBodyDto;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import io.recruitcrm.microservice.timesheet.responses.APIResponder;
import io.recruitcrm.microservice.timesheet.responses.APIResponseType;
import io.recruitcrm.microservice.timesheet.services.time_log.ITimeLogIntervalMigrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.PrintWriter;
import java.io.StringWriter;

@Validated
@RestController
@RequestMapping("/v1/time-logs/interval-migration")
public class TimeLogIntervalMigrationController implements ITimeLogIntervalMigrationController {

	private static final Logger LOGGER = LoggerFactory.getLogger(TimeLogIntervalMigrationController.class);

	private static final int AUTHORIZED_ACCOUNT_ID = 728;

	private static final int AUTHORIZED_USER_ID = 20206;

	private final String env;

	private final AuthHolder auth;

	private final ITimeLogIntervalMigrationService timeLogIntervalMigrationService;

	private final APIResponder apiResponder;

	public TimeLogIntervalMigrationController(ITimeLogIntervalMigrationService timeLogIntervalMigrationService,
			APIResponder apiResponder, AuthHolder auth, @Value("${application.env}") String env) {
		this.timeLogIntervalMigrationService = timeLogIntervalMigrationService;
		this.apiResponder = apiResponder;
		this.auth = auth;
		this.env = env;
	}

	@Override
	@PostMapping
	public ResponseEntity<?> migrateTimeLogsToIntervalTable(
			@Validated @RequestBody TimeLogIntervalMigrationRequestBodyDto request) {
		try {
			Integer accountId = this.auth.getAuthenticationPrincipalOrganizationIdentifier();
			Integer userId = this.auth.getAuthenticationPrincipalUniqueIdentifier();

			if ((this.env.equals("local") || this.env.equals("production"))
					&& (!accountId.equals(AUTHORIZED_ACCOUNT_ID) || !userId.equals(AUTHORIZED_USER_ID))) {
				throw new UnauthorizedAccessException("You are not authorised to do this migration.");
			}

			TimeLogIntervalMigrationResponseBodyDto result = this.timeLogIntervalMigrationService
				.migrateTimeLogsToIntervalTable(request);
			return this.apiResponder.respond(result, "Time log interval migration completed successfully",
					APIResponseType.SUCCESS, HttpStatus.OK);
		}
		catch (Exception ex) {
			StringWriter sw = new StringWriter();
			ex.printStackTrace(new PrintWriter(sw));
			LOGGER.error("exception occurred inside migrateTimeLogsToIntervalTable() method :: {}", sw);
			return this.apiResponder.respondWithError(ex, APIResponseType.ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
