package io.recruitcrm.microservice.timesheet.access_control.contract_staffing.examples;

import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.AccessControlChecker;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Entity;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Permission;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.PermissionLevel;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.AccessControlCheckMetadataContext;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.BulkPermissionCheckRequest;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.BulkPermissionCheckResult;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.PermissionCheckContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccessControlCheckerExample")
class AccessControlCheckerExampleTests {

	@Mock
	private AccessControlChecker accessControlChecker;

	@Mock
	private Logger logger;

	@Captor
	private ArgumentCaptor<Entity> entityCaptor;

	@Captor
	private ArgumentCaptor<PermissionCheckContext> permissionContextCaptor;

	@Captor
	private ArgumentCaptor<AccessControlCheckMetadataContext> metadataContextCaptor;

	@Captor
	private ArgumentCaptor<BulkPermissionCheckRequest> bulkRequestCaptor;

	@Test
	@DisplayName("checkIndividualTimesheetPermission logs info when allowed")
	void testCheckIndividualTimesheetPermissionLogsInfoWhenAllowed() {
		AccessControlCheckerExample example = new AccessControlCheckerExample(this.accessControlChecker, this.logger);
		Integer timesheetId = Integer.valueOf(1);

		example.checkIndividualTimesheetPermission(timesheetId);

		verify(this.accessControlChecker).allows(this.entityCaptor.capture(), this.permissionContextCaptor.capture(),
				this.metadataContextCaptor.capture());
		assertThat(this.entityCaptor.getValue()).isEqualTo(Entity.TIMESHEET);
		assertThat(this.permissionContextCaptor.getValue())
			.extracting(PermissionCheckContext::getPermission, PermissionCheckContext::getPermissionLevel)
			.containsExactly(Permission.VIEW_TIMESHEET, PermissionLevel.YES);
		assertThat(this.metadataContextCaptor.getValue().getTimesheetId()).isEqualTo(timesheetId);
		verify(this.logger).logInfo("User has permission to view timesheet: " + timesheetId);
	}

	@Test
	@DisplayName("checkIndividualTimesheetPermission logs error when denied")
	void testCheckIndividualTimesheetPermissionLogsErrorWhenDenied() {
		AccessControlCheckerExample example = new AccessControlCheckerExample(this.accessControlChecker, this.logger);
		Integer timesheetId = Integer.valueOf(2);

		willThrow(new RuntimeException("denied")).given(this.accessControlChecker)
			.allows(any(Entity.class), any(PermissionCheckContext.class), any(AccessControlCheckMetadataContext.class));

		example.checkIndividualTimesheetPermission(timesheetId);

		verify(this.logger).logError("Permission denied for timesheet " + timesheetId + ": denied");
	}

	@Test
	@DisplayName("checkIndividualCandidatePermission logs info when allowed")
	void testCheckIndividualCandidatePermissionLogsInfoWhenAllowed() {
		AccessControlCheckerExample example = new AccessControlCheckerExample(this.accessControlChecker, this.logger);
		Integer candidateId = Integer.valueOf(3);

		example.checkIndividualCandidatePermission(candidateId);

		verify(this.accessControlChecker).allows(this.entityCaptor.capture(), this.permissionContextCaptor.capture(),
				this.metadataContextCaptor.capture());
		assertThat(this.entityCaptor.getValue()).isEqualTo(Entity.TIMESHEET);
		assertThat(this.permissionContextCaptor.getValue())
			.extracting(PermissionCheckContext::getPermission, PermissionCheckContext::getPermissionLevel)
			.containsExactly(Permission.EDIT_TIMESHEET, PermissionLevel.YES);
		assertThat(this.metadataContextCaptor.getValue().getCandidateId()).isEqualTo(candidateId);
		verify(this.logger).logInfo("User has permission to edit timesheets for candidate: " + candidateId);
	}

	@Test
	@DisplayName("checkIndividualCandidatePermission logs error when denied")
	void testCheckIndividualCandidatePermissionLogsErrorWhenDenied() {
		AccessControlCheckerExample example = new AccessControlCheckerExample(this.accessControlChecker, this.logger);
		Integer candidateId = Integer.valueOf(4);

		willThrow(new RuntimeException("nope")).given(this.accessControlChecker)
			.allows(any(Entity.class), any(PermissionCheckContext.class), any(AccessControlCheckMetadataContext.class));

		example.checkIndividualCandidatePermission(candidateId);

		verify(this.logger).logError("Permission denied for candidate " + candidateId + ": nope");
	}

	@Test
	@DisplayName("checkIndividualJobPermission logs info when allowed")
	void testCheckIndividualJobPermissionLogsInfoWhenAllowed() {
		AccessControlCheckerExample example = new AccessControlCheckerExample(this.accessControlChecker, this.logger);
		Integer jobId = Integer.valueOf(5);

		example.checkIndividualJobPermission(jobId);

		verify(this.accessControlChecker).allows(this.entityCaptor.capture(), this.permissionContextCaptor.capture(),
				this.metadataContextCaptor.capture());
		assertThat(this.entityCaptor.getValue()).isEqualTo(Entity.TIMESHEET);
		assertThat(this.permissionContextCaptor.getValue())
			.extracting(PermissionCheckContext::getPermission, PermissionCheckContext::getPermissionLevel)
			.containsExactly(Permission.DELETE_TIMESHEET, PermissionLevel.YES);
		assertThat(this.metadataContextCaptor.getValue().getJobId()).isEqualTo(jobId);
		verify(this.logger).logInfo("User has permission to delete timesheets for job: " + jobId);
	}

	@Test
	@DisplayName("checkIndividualJobPermission logs error when denied")
	void testCheckIndividualJobPermissionLogsErrorWhenDenied() {
		AccessControlCheckerExample example = new AccessControlCheckerExample(this.accessControlChecker, this.logger);
		Integer jobId = Integer.valueOf(6);

		willThrow(new RuntimeException("blocked")).given(this.accessControlChecker)
			.allows(any(Entity.class), any(PermissionCheckContext.class), any(AccessControlCheckMetadataContext.class));

		example.checkIndividualJobPermission(jobId);

		verify(this.logger).logError("Permission denied for job " + jobId + ": blocked");
	}

	@Test
	@DisplayName("Bulk permission methods build requests and log summaries")
	void testBulkPermissionMethodsBuildRequestsAndLogSummaries() {
		AccessControlCheckerExample example = new AccessControlCheckerExample(this.accessControlChecker, this.logger);

		List<Integer> ids = List.of(Integer.valueOf(10), Integer.valueOf(11));
		BulkPermissionCheckResult result = BulkPermissionCheckResult.builder()
			.results(List.of(BulkPermissionCheckResult.BulkPermissionCheckResultItem.builder().allowed(true).build(),
					BulkPermissionCheckResult.BulkPermissionCheckResultItem.builder().allowed(false).build()))
			.build();
		given(this.accessControlChecker.allowsBulk(any(BulkPermissionCheckRequest.class))).willReturn(result);

		BulkPermissionCheckResult timesheetResult = example.checkBulkTimesheetPermissions(ids);
		BulkPermissionCheckResult candidateResult = example.checkBulkCandidatePermissions(ids);
		BulkPermissionCheckResult jobResult = example.checkBulkJobPermissions(ids);
		BulkPermissionCheckResult mixedResult = example.checkMixedBulkPermissions(ids, ids, ids);
		BulkPermissionCheckResult multiPermissionResult = example
			.checkMultiplePermissionsForTimesheet(Integer.valueOf(1));

		assertThat(timesheetResult).isSameAs(result);
		assertThat(candidateResult).isSameAs(result);
		assertThat(jobResult).isSameAs(result);
		assertThat(mixedResult).isSameAs(result);
		assertThat(multiPermissionResult).isSameAs(result);

		verify(this.accessControlChecker, times(5)).allowsBulk(this.bulkRequestCaptor.capture());
		assertThat(this.bulkRequestCaptor.getAllValues()).hasSize(5);
		assertThat(this.bulkRequestCaptor.getAllValues().get(0).getItems()).isNotNull();

		verify(this.logger).logInfo("Checking bulk permissions for " + ids.size() + " timesheets");
		verify(this.logger).logInfo("Checking bulk permissions for " + ids.size() + " candidates");
		verify(this.logger).logInfo("Checking bulk permissions for " + ids.size() + " jobs");
		verify(this.logger).logInfo("Checking multiple permissions for timesheet: 1");
	}

}
