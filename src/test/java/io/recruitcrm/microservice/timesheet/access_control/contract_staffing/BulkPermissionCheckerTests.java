package io.recruitcrm.microservice.timesheet.access_control.contract_staffing;

import io.recruitcrm.entity.model.Candidate;
import io.recruitcrm.entity.model.Job;
import io.recruitcrm.logging.logger.Logger;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.AccessControlCheckMetadataContext;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.BulkPermissionCheckContext;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.BulkPermissionCheckRequest;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.BulkPermissionCheckResult;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.PermissionCheckContext;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.PermissionCheckInternalContext;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.permission.BasePermissionChecker;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.permission.PermissionCheckerFactory;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.dto.timesheet.TimesheetPermissionDataDto;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import io.recruitcrm.microservice.timesheet.repositories.timesheet.TimesheetRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("BulkPermissionChecker")
class BulkPermissionCheckerTests {

	@Mock
	private PermissionCheckerFactory permissionCheckerFactory;

	@Mock
	private AuthHolder authHolder;

	@Mock
	private TimesheetRepository timesheetRepository;

	@Mock
	private Logger logger;

	@Mock
	private BasePermissionChecker basePermissionChecker;

	@Captor
	private ArgumentCaptor<PermissionCheckInternalContext> internalContextCaptor;

	@Captor
	private ArgumentCaptor<BulkPermissionCheckContext> bulkContextCaptor;

	@Test
	@DisplayName("Returns empty results when request items are null")
	void testCheckBulkPermissionsReturnsEmptyWhenItemsNull() {
		BulkPermissionChecker checker = new BulkPermissionChecker(this.permissionCheckerFactory, this.authHolder,
				this.timesheetRepository, this.logger);

		BulkPermissionCheckResult result = checker.checkBulkPermissions(new BulkPermissionCheckRequest());

		assertThat(result.getResults()).isNotNull().isEmpty();
		verify(this.permissionCheckerFactory, never()).getPermissionChecker(any(Permission.class));
		verify(this.timesheetRepository, never()).getTimesheetPermissionDataBulk(any(), any());
	}

	@Test
	@DisplayName("Throws NullPointerException when request itself is null")
	void testCheckBulkPermissionsThrowsWhenRequestIsNull() {
		BulkPermissionChecker checker = new BulkPermissionChecker(this.permissionCheckerFactory, this.authHolder,
				this.timesheetRepository, this.logger);

		assertThatThrownBy(() -> checker.checkBulkPermissions(null)).isInstanceOf(NullPointerException.class);
	}

	@Test
	@DisplayName("Returns empty results when request items are empty")
	void testCheckBulkPermissionsReturnsEmptyWhenItemsEmpty() {
		BulkPermissionChecker checker = new BulkPermissionChecker(this.permissionCheckerFactory, this.authHolder,
				this.timesheetRepository, this.logger);

		BulkPermissionCheckResult result = checker
			.checkBulkPermissions(BulkPermissionCheckRequest.builder().items(Collections.emptyList()).build());

		assertThat(result.getResults()).isNotNull().isEmpty();
		verify(this.permissionCheckerFactory, never()).getPermissionChecker(any(Permission.class));
	}

	@Test
	@DisplayName("Allows when checker exists and does not throw")
	void testCheckBulkPermissionsAllowsWhenCheckerSucceeds() {
		BulkPermissionChecker checker = new BulkPermissionChecker(this.permissionCheckerFactory, this.authHolder,
				this.timesheetRepository, this.logger);

		Integer accountId = Integer.valueOf(99);
		given(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.permissionCheckerFactory.getPermissionChecker(Permission.VIEW_TIMESHEET))
			.willReturn(this.basePermissionChecker);

		TimesheetPermissionDataDto permissionData = mock(TimesheetPermissionDataDto.class);
		given(permissionData.getTimesheetId()).willReturn(Integer.valueOf(1));
		given(permissionData.getCandidate()).willReturn(mock(Candidate.class));
		given(permissionData.getJob()).willReturn(mock(Job.class));
		given(this.timesheetRepository.getTimesheetPermissionDataBulk(any(), any()))
			.willReturn(List.of(permissionData));

		BulkPermissionCheckRequest request = BulkPermissionCheckRequest.builder()
			.items(List.of(buildItemWithTimesheetId(Integer.valueOf(1), Permission.VIEW_TIMESHEET)))
			.build();

		BulkPermissionCheckResult result = checker.checkBulkPermissions(request);

		assertThat(result.getResults()).hasSize(1);
		assertThat(result.getResults().get(0).isAllowed()).isTrue();
	}

	@Test
	@DisplayName("Denies when no permission checker exists")
	void testCheckBulkPermissionsDeniesWhenCheckerMissing() {
		BulkPermissionChecker checker = new BulkPermissionChecker(this.permissionCheckerFactory, this.authHolder,
				this.timesheetRepository, this.logger);

		given(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(Integer.valueOf(1));
		given(this.permissionCheckerFactory.getPermissionChecker(Permission.VIEW_TIMESHEET)).willReturn(null);

		BulkPermissionCheckRequest request = BulkPermissionCheckRequest.builder()
			.items(List.of(buildItemWithTimesheetId(Integer.valueOf(1), Permission.VIEW_TIMESHEET)))
			.build();

		BulkPermissionCheckResult result = checker.checkBulkPermissions(request);

		assertThat(result.getResults()).hasSize(1);
		assertThat(result.getResults().get(0).isAllowed()).isFalse();
		assertThat(result.getResults().get(0).getErrorMessage()).contains("No permission checker exists for");
	}

	@Test
	@DisplayName("Denies with UnauthorizedAccessException message when checker throws UnauthorizedAccessException")
	void testCheckBulkPermissionsDeniesOnUnauthorizedAccessException() {
		BulkPermissionChecker checker = new BulkPermissionChecker(this.permissionCheckerFactory, this.authHolder,
				this.timesheetRepository, this.logger);

		given(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(Integer.valueOf(1));
		given(this.permissionCheckerFactory.getPermissionChecker(Permission.VIEW_TIMESHEET))
			.willReturn(this.basePermissionChecker);
		willThrow(new UnauthorizedAccessException("unauthorized")).given(this.basePermissionChecker)
			.checkPermissionWithBulkContext(any(PermissionCheckInternalContext.class),
					any(BulkPermissionCheckContext.class));

		BulkPermissionCheckRequest request = BulkPermissionCheckRequest.builder()
			.items(List.of(buildItemWithTimesheetId(Integer.valueOf(1), Permission.VIEW_TIMESHEET)))
			.build();

		BulkPermissionCheckResult result = checker.checkBulkPermissions(request);

		assertThat(result.getResults()).hasSize(1);
		assertThat(result.getResults().get(0).isAllowed()).isFalse();
		assertThat(result.getResults().get(0).getErrorMessage()).isEqualTo("unauthorized");
	}

	@Test
	@DisplayName("Denies with generic error message and logs when checker throws unexpected exception")
	void testCheckBulkPermissionsDeniesOnUnexpectedException() {
		BulkPermissionChecker checker = new BulkPermissionChecker(this.permissionCheckerFactory, this.authHolder,
				this.timesheetRepository, this.logger);

		given(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(Integer.valueOf(1));
		given(this.permissionCheckerFactory.getPermissionChecker(Permission.VIEW_TIMESHEET))
			.willReturn(this.basePermissionChecker);
		willThrow(new IllegalStateException("boom")).given(this.basePermissionChecker)
			.checkPermissionWithBulkContext(any(PermissionCheckInternalContext.class),
					any(BulkPermissionCheckContext.class));

		BulkPermissionCheckRequest request = BulkPermissionCheckRequest.builder()
			.items(List.of(buildItemWithTimesheetId(Integer.valueOf(1), Permission.VIEW_TIMESHEET)))
			.build();

		BulkPermissionCheckResult result = checker.checkBulkPermissions(request);

		assertThat(result.getResults()).hasSize(1);
		assertThat(result.getResults().get(0).isAllowed()).isFalse();
		assertThat(result.getResults().get(0).getErrorMessage()).contains("Unexpected error during permission check:");
		verify(this.logger).logError("Unexpected error during permission check - boom");
	}

	@Test
	@DisplayName("Preload failure falls back to empty bulk context")
	void testCheckBulkPermissionsFallsBackToEmptyContextWhenPreloadFails() {
		BulkPermissionChecker checker = new BulkPermissionChecker(this.permissionCheckerFactory, this.authHolder,
				this.timesheetRepository, this.logger);

		given(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(Integer.valueOf(10));
		given(this.permissionCheckerFactory.getPermissionChecker(Permission.VIEW_TIMESHEET))
			.willReturn(this.basePermissionChecker);
		given(this.timesheetRepository.getTimesheetPermissionDataBulk(any(), any()))
			.willThrow(new RuntimeException("db down"));

		BulkPermissionCheckRequest request = BulkPermissionCheckRequest.builder()
			.items(List.of(buildItemWithTimesheetId(Integer.valueOf(1), Permission.VIEW_TIMESHEET)))
			.explicitStrategy(BulkDataLoadingStrategy.TIMESHEET_WITH_RELATED_ENTITIES)
			.build();

		BulkPermissionCheckResult result = checker.checkBulkPermissions(request);

		assertThat(result.getResults()).hasSize(1);
		verify(this.basePermissionChecker).checkPermissionWithBulkContext(this.internalContextCaptor.capture(),
				this.bulkContextCaptor.capture());
		BulkPermissionCheckContext bulkContext = this.bulkContextCaptor.getValue();
		assertThat(bulkContext.getCandidatesByTimesheetId()).isNotNull().isEmpty();
		assertThat(bulkContext.getJobsByTimesheetId()).isNotNull().isEmpty();
		assertThat(bulkContext.getCandidatesById()).isNotNull().isEmpty();
		assertThat(bulkContext.getJobsById()).isNotNull().isEmpty();
	}

	@Test
	@DisplayName("Mixed strategy loads timesheet, candidate and job maps when IDs provided")
	void testCheckBulkPermissionsMixedStrategyLoadsAllEntityTypes() {
		BulkPermissionChecker checker = new BulkPermissionChecker(this.permissionCheckerFactory, this.authHolder,
				this.timesheetRepository, this.logger);

		Integer accountId = Integer.valueOf(7);
		given(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.permissionCheckerFactory.getPermissionChecker(Permission.VIEW_TIMESHEET))
			.willReturn(this.basePermissionChecker);
		given(this.permissionCheckerFactory.getPermissionChecker(Permission.EDIT_TIMESHEET))
			.willReturn(this.basePermissionChecker);
		given(this.permissionCheckerFactory.getPermissionChecker(Permission.DELETE_TIMESHEET))
			.willReturn(this.basePermissionChecker);

		TimesheetPermissionDataDto timesheetPermissionData = mock(TimesheetPermissionDataDto.class);
		given(timesheetPermissionData.getTimesheetId()).willReturn(Integer.valueOf(1));
		given(timesheetPermissionData.getCandidate()).willReturn(mock(Candidate.class));
		given(timesheetPermissionData.getJob()).willReturn(mock(Job.class));
		given(this.timesheetRepository.getTimesheetPermissionDataBulk(any(), any()))
			.willReturn(List.of(timesheetPermissionData));

		TimesheetPermissionDataDto candidatePermissionData = mock(TimesheetPermissionDataDto.class);
		given(candidatePermissionData.getCandidateId()).willReturn(Integer.valueOf(2));
		given(candidatePermissionData.getCandidate()).willReturn(mock(Candidate.class));
		given(this.timesheetRepository.getCandidatePermissionDataBulk(any(), any()))
			.willReturn(List.of(candidatePermissionData));

		TimesheetPermissionDataDto jobPermissionData = mock(TimesheetPermissionDataDto.class);
		given(jobPermissionData.getJobId()).willReturn(Integer.valueOf(3));
		given(jobPermissionData.getJob()).willReturn(mock(Job.class));
		given(this.timesheetRepository.getJobPermissionDataBulk(any(), any())).willReturn(List.of(jobPermissionData));

		BulkPermissionCheckRequest request = BulkPermissionCheckRequest.builder()
			.items(List.of(buildItemWithTimesheetId(Integer.valueOf(1), Permission.VIEW_TIMESHEET),
					buildItemWithCandidateId(Integer.valueOf(2), Permission.EDIT_TIMESHEET),
					buildItemWithJobId(Integer.valueOf(3), Permission.DELETE_TIMESHEET)))
			.explicitStrategy(BulkDataLoadingStrategy.MIXED_STRATEGY)
			.build();

		BulkPermissionCheckResult result = checker.checkBulkPermissions(request);

		assertThat(result.getResults()).hasSize(3);
		verify(this.timesheetRepository).getTimesheetPermissionDataBulk(any(), any());
		verify(this.timesheetRepository).getCandidatePermissionDataBulk(any(), any());
		verify(this.timesheetRepository).getJobPermissionDataBulk(any(), any());
	}

	@Test
	@DisplayName("Specific strategies load only their required data")
	void testCheckBulkPermissionsSpecificStrategiesLoadOnlyRequiredData() {
		BulkPermissionChecker checker = new BulkPermissionChecker(this.permissionCheckerFactory, this.authHolder,
				this.timesheetRepository, this.logger);

		Integer accountId = Integer.valueOf(7);
		given(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.permissionCheckerFactory.getPermissionChecker(Permission.EDIT_TIMESHEET))
			.willReturn(this.basePermissionChecker);
		given(this.permissionCheckerFactory.getPermissionChecker(Permission.DELETE_TIMESHEET))
			.willReturn(this.basePermissionChecker);

		given(this.timesheetRepository.getCandidatePermissionDataBulk(any(), any()))
			.willReturn(Collections.emptyList());
		given(this.timesheetRepository.getJobPermissionDataBulk(any(), any())).willReturn(Collections.emptyList());

		BulkPermissionCheckRequest candidateRequest = BulkPermissionCheckRequest.builder()
			.items(List.of(buildItemWithCandidateId(Integer.valueOf(2), Permission.EDIT_TIMESHEET)))
			.explicitStrategy(BulkDataLoadingStrategy.CANDIDATES_ONLY)
			.build();

		BulkPermissionCheckRequest jobRequest = BulkPermissionCheckRequest.builder()
			.items(List.of(buildItemWithJobId(Integer.valueOf(3), Permission.DELETE_TIMESHEET)))
			.explicitStrategy(BulkDataLoadingStrategy.JOBS_ONLY)
			.build();

		assertThat(checker.checkBulkPermissions(candidateRequest).getResults()).hasSize(1);
		assertThat(checker.checkBulkPermissions(jobRequest).getResults()).hasSize(1);

		verify(this.timesheetRepository, never()).getTimesheetPermissionDataBulk(any(), any());
		verify(this.timesheetRepository).getCandidatePermissionDataBulk(any(), any());
		verify(this.timesheetRepository).getJobPermissionDataBulk(any(), any());
	}

	@Test
	@DisplayName("Mixed strategy skips non-requested data loads when IDs are missing")
	void testCheckBulkPermissionsMixedStrategySkipsDataLoadsForMissingIds() {
		BulkPermissionChecker checker = new BulkPermissionChecker(this.permissionCheckerFactory, this.authHolder,
				this.timesheetRepository, this.logger);

		given(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(Integer.valueOf(7));
		given(this.permissionCheckerFactory.getPermissionChecker(Permission.VIEW_TIMESHEET))
			.willReturn(this.basePermissionChecker);

		BulkPermissionCheckRequest request = BulkPermissionCheckRequest.builder()
			.items(List.of(buildItem(null, null, null, Permission.VIEW_TIMESHEET)))
			.explicitStrategy(BulkDataLoadingStrategy.MIXED_STRATEGY)
			.build();

		BulkPermissionCheckResult result = checker.checkBulkPermissions(request);

		assertThat(result.getResults()).hasSize(1);
		verify(this.timesheetRepository, never()).getTimesheetPermissionDataBulk(any(), any());
		verify(this.timesheetRepository, never()).getCandidatePermissionDataBulk(any(), any());
		verify(this.timesheetRepository, never()).getJobPermissionDataBulk(any(), any());
	}

	@Test
	@DisplayName("Auto strategy handles null candidate and job values while building bulk maps")
	void testCheckBulkPermissionsAutoStrategyHandlesNullCandidateAndJobValues() {
		BulkPermissionChecker checker = new BulkPermissionChecker(this.permissionCheckerFactory, this.authHolder,
				this.timesheetRepository, this.logger);

		Integer accountId = Integer.valueOf(7);
		given(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(accountId);
		given(this.permissionCheckerFactory.getPermissionChecker(Permission.VIEW_TIMESHEET))
			.willReturn(this.basePermissionChecker);
		given(this.permissionCheckerFactory.getPermissionChecker(Permission.EDIT_TIMESHEET))
			.willReturn(this.basePermissionChecker);
		given(this.permissionCheckerFactory.getPermissionChecker(Permission.DELETE_TIMESHEET))
			.willReturn(this.basePermissionChecker);

		TimesheetPermissionDataDto timesheetWithNullRelatedEntities = mock(TimesheetPermissionDataDto.class);
		given(timesheetWithNullRelatedEntities.getCandidate()).willReturn(null);
		given(timesheetWithNullRelatedEntities.getJob()).willReturn(null);
		given(this.timesheetRepository.getTimesheetPermissionDataBulk(any(), any()))
			.willReturn(List.of(timesheetWithNullRelatedEntities));

		TimesheetPermissionDataDto candidateWithNullEntity = mock(TimesheetPermissionDataDto.class);
		given(candidateWithNullEntity.getCandidate()).willReturn(null);
		given(this.timesheetRepository.getCandidatePermissionDataBulk(any(), any()))
			.willReturn(List.of(candidateWithNullEntity));

		TimesheetPermissionDataDto jobWithNullEntity = mock(TimesheetPermissionDataDto.class);
		given(jobWithNullEntity.getJob()).willReturn(null);
		given(this.timesheetRepository.getJobPermissionDataBulk(any(), any())).willReturn(List.of(jobWithNullEntity));

		BulkPermissionCheckRequest request = BulkPermissionCheckRequest.builder()
			.items(List.of(buildItemWithTimesheetId(Integer.valueOf(1), Permission.VIEW_TIMESHEET),
					buildItemWithCandidateId(Integer.valueOf(2), Permission.EDIT_TIMESHEET),
					buildItemWithJobId(Integer.valueOf(3), Permission.DELETE_TIMESHEET)))
			.build();

		BulkPermissionCheckResult result = checker.checkBulkPermissions(request);

		assertThat(result.getResults()).hasSize(3);
		verify(this.timesheetRepository).getTimesheetPermissionDataBulk(any(), any());
		verify(this.timesheetRepository).getCandidatePermissionDataBulk(any(), any());
		verify(this.timesheetRepository).getJobPermissionDataBulk(any(), any());
	}

	@Test
	@DisplayName("Specific strategies skip their data load when the corresponding ID set is empty")
	void testCheckBulkPermissionsSpecificStrategiesSkipLoadsWhenIdsEmpty() {
		BulkPermissionChecker checker = new BulkPermissionChecker(this.permissionCheckerFactory, this.authHolder,
				this.timesheetRepository, this.logger);

		given(this.authHolder.getAuthenticationPrincipalOrganizationIdentifier()).willReturn(Integer.valueOf(7));
		given(this.permissionCheckerFactory.getPermissionChecker(Permission.VIEW_TIMESHEET))
			.willReturn(this.basePermissionChecker);

		BulkPermissionCheckRequest timesheetStrategyRequest = BulkPermissionCheckRequest.builder()
			.items(List.of(buildItem(null, null, null, Permission.VIEW_TIMESHEET)))
			.explicitStrategy(BulkDataLoadingStrategy.TIMESHEET_WITH_RELATED_ENTITIES)
			.build();

		BulkPermissionCheckRequest candidateStrategyRequest = BulkPermissionCheckRequest.builder()
			.items(List.of(buildItem(null, null, null, Permission.VIEW_TIMESHEET)))
			.explicitStrategy(BulkDataLoadingStrategy.CANDIDATES_ONLY)
			.build();

		BulkPermissionCheckRequest jobStrategyRequest = BulkPermissionCheckRequest.builder()
			.items(List.of(buildItem(null, null, null, Permission.VIEW_TIMESHEET)))
			.explicitStrategy(BulkDataLoadingStrategy.JOBS_ONLY)
			.build();

		assertThat(checker.checkBulkPermissions(timesheetStrategyRequest).getResults()).hasSize(1);
		assertThat(checker.checkBulkPermissions(candidateStrategyRequest).getResults()).hasSize(1);
		assertThat(checker.checkBulkPermissions(jobStrategyRequest).getResults()).hasSize(1);

		verify(this.timesheetRepository, never()).getTimesheetPermissionDataBulk(any(), any());
		verify(this.timesheetRepository, never()).getCandidatePermissionDataBulk(any(), any());
		verify(this.timesheetRepository, never()).getJobPermissionDataBulk(any(), any());
	}

	private static BulkPermissionCheckRequest.BulkPermissionCheckItem buildItemWithTimesheetId(Integer timesheetId,
			Permission permission) {
		return buildItem(timesheetId, null, null, permission);
	}

	private static BulkPermissionCheckRequest.BulkPermissionCheckItem buildItemWithCandidateId(Integer candidateId,
			Permission permission) {
		return buildItem(null, candidateId, null, permission);
	}

	private static BulkPermissionCheckRequest.BulkPermissionCheckItem buildItemWithJobId(Integer jobId,
			Permission permission) {
		return buildItem(null, null, jobId, permission);
	}

	private static BulkPermissionCheckRequest.BulkPermissionCheckItem buildItem(Integer timesheetId,
			Integer candidateId, Integer jobId, Permission permission) {
		PermissionCheckContext permissionCheckContext = PermissionCheckContext.builder()
			.permission(permission)
			.permissionLevel(PermissionLevel.YES)
			.build();
		AccessControlCheckMetadataContext metadataContext = AccessControlCheckMetadataContext.builder()
			.timesheetId(timesheetId)
			.candidateId(candidateId)
			.jobId(jobId)
			.build();
		return BulkPermissionCheckRequest.BulkPermissionCheckItem.builder()
			.entity(Entity.TIMESHEET)
			.permissionCheckContext(permissionCheckContext)
			.accessControlCheckMetadataContext(metadataContext)
			.build();
	}

}
