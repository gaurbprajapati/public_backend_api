package io.recruitcrm.microservice.timesheet.services.portals;

import io.recruitcrm.contract_staffing.entity.model.JobTimesheetAccess;
import io.recruitcrm.contract_staffing.entity.model.Timesheet;
import io.recruitcrm.contract_staffing.entity.model.TimesheetApprover;
import io.recruitcrm.entity.model.Contact;
import io.recruitcrm.entity.model.Job;
import io.recruitcrm.microservice.timesheet.dao.contact.ContactJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.job.JobJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.job_secondary_contact.JobSecondaryContactJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.job_timesheet_access.JobTimesheetAccessJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.timesheet.TimesheetJpaRepository;
import io.recruitcrm.microservice.timesheet.dto.portal.PortalTimesheetPermissionDto;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.helpers.enums.UserTypeEnum;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import io.recruitcrm.microservice.timesheet.repositories.timesheet_approver.TimesheetApproverRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class PortalAccessControlService {

	private final JobJpaRepository jobJpaRepository;

	private final JobTimesheetAccessJpaRepository jobTimesheetAccessJpaRepository;

	private final JobSecondaryContactJpaRepository jobSecondaryContactJpaRepository;

	private final TimesheetJpaRepository timesheetJpaRepository;

	private final TimesheetApproverRepository timesheetApproverRepository;

	private final ContactJpaRepository contactJpaRepository;

	private final AuthHolder auth;

	public PortalAccessControlService(JobJpaRepository jobJpaRepository,
			JobTimesheetAccessJpaRepository jobTimesheetAccessJpaRepository,
			JobSecondaryContactJpaRepository jobSecondaryContactJpaRepository,
			TimesheetJpaRepository timesheetJpaRepository, TimesheetApproverRepository timesheetApproverRepository,
			ContactJpaRepository contactJpaRepository, AuthHolder auth) {
		this.jobJpaRepository = jobJpaRepository;
		this.jobTimesheetAccessJpaRepository = jobTimesheetAccessJpaRepository;
		this.jobSecondaryContactJpaRepository = jobSecondaryContactJpaRepository;
		this.timesheetJpaRepository = timesheetJpaRepository;
		this.timesheetApproverRepository = timesheetApproverRepository;
		this.contactJpaRepository = contactJpaRepository;
		this.auth = auth;
	}

	/**
	 * Validates portal access control for a job and client (contact) combination
	 * @param jobId The job ID to validate
	 * @param clientId The client (contact) ID to validate
	 * @return PortalTimesheetPermissionDto containing canCreate, canEdit, canDelete
	 * permissions
	 * @throws ResourceNotFoundException if job not found
	 * @throws ValidationErrorException if portal is not enabled
	 * @throws UnauthorizedAccessException if clientId doesn't match job's primary
	 * contactId or secondary contact, or access record not found
	 */
	public PortalTimesheetPermissionDto validatePortalAccessControl(Integer jobId, Integer clientId) {
		// Step 1: Find job by ID
		Optional<Job> jobOptional = this.jobJpaRepository.findById(jobId);
		if (jobOptional.isEmpty()) {
			throw new ResourceNotFoundException("Job", jobId);
		}

		Job job = getJob(clientId, jobOptional.get());

		Contact clientContact = this.contactJpaRepository.findById(clientId)
			.orElseThrow(() -> new UnauthorizedAccessException("Unauthorized access: Client ID " + clientId));

		String email = this.auth.getUnifiedPrincipal().getEmail();
		List<Integer> allContactIds = this.contactJpaRepository
			.findAllByEmailAndAccountId(email, clientContact.getAccountId())
			.stream()
			.map(Contact::getId)
			.toList();

		boolean isPrimaryContact = job.getContactId() != null && allContactIds.contains(job.getContactId());
		boolean isSecondaryContact = this.jobSecondaryContactJpaRepository.existsByJobIdAndContactIdIn(jobId,
				allContactIds);

		if (!isPrimaryContact && !isSecondaryContact) {
			throw new UnauthorizedAccessException("Unauthorized access: Client ID " + clientId);
		}

		// Step 4: Find JobTimesheetAccess record for this job
		Optional<JobTimesheetAccess> accessOptional = this.jobTimesheetAccessJpaRepository.findByJobId(jobId);
		if (accessOptional.isEmpty()) {
			throw new UnauthorizedAccessException("Unauthorized access for delete timesheet");
		}

		JobTimesheetAccess access = accessOptional.get();

		// Step 5: Return permission DTO
		return new PortalTimesheetPermissionDto(access.getCanCreate(), access.getCanEdit(), access.getCanDelete());
	}

	@NotNull
	private static Job getJob(Integer clientId, Job job) {
		if (Objects.equals(job.getJobType(), "") || Objects.equals(job.getJobType(), "fulltime")
				|| Objects.equals(job.getJobType(), "parttime")) {
			throw new UnauthorizedAccessException("Unauthorized access: Client ID " + clientId);
		}
		return job;
	}

	/**
	 * Checks if a specific permission is allowed for the given job and client
	 * @param jobId The job ID
	 * @param clientId The client (contact) ID
	 * @param permissionType The permission type to check (CREATE_TIMESHEET,
	 * EDIT_TIMESHEET, DELETE_TIMESHEET)
	 * @return true if permission is allowed (value is 1), false otherwise
	 */
	public boolean hasPermission(Integer jobId, Integer clientId, String permissionType) {
		PortalTimesheetPermissionDto permissions = this.validatePortalAccessControl(jobId, clientId);

		return switch (permissionType) {
			case "CREATE_TIMESHEET" -> permissions.getCanCreate() != null && permissions.getCanCreate() == 1;
			case "EDIT_TIMESHEET" -> permissions.getCanEdit() != null && permissions.getCanEdit() == 1;
			case "DELETE_TIMESHEET" -> permissions.getCanDelete() != null && permissions.getCanDelete() == 1;
			default -> false;
		};
	}

	/**
	 * Validates if a user is authorized to approve a timesheet Checks if the user is in
	 * the list of approvers for the timesheet's setting
	 * @param timesheetId The timesheet ID to validate
	 * @param userId The user/contact ID to validate
	 * @param userTypeId The user type ID (e.g., COMPANY_CONTACT)
	 * @param accountId The account ID
	 * @throws ResourceNotFoundException if timesheet not found
	 * @throws ValidationErrorException if user is not authorized to approve
	 */
	public void validateApproverAccess(Integer timesheetId, Integer userId, Integer userTypeId, Integer accountId) {
		// Verify timesheet exists
		Timesheet timesheet = this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId)
			.orElseThrow(() -> new ResourceNotFoundException("Timesheet", timesheetId));

		// Access control check: only approver can approve or reject timesheet
		Integer timesheetSettingId = timesheet.getTimesheetSettingId();

		// Get list of approvers for this timesheet setting
		List<TimesheetApprover> approvers = this.timesheetApproverRepository
			.findByTimesheetSettingId(timesheetSettingId);

		// For COMPANY_CONTACT approvers, expand the single userId to all contact records
		// sharing the same JWT email — the approver may have been registered with a
		// different contact ID that shares the same email as the authenticated client.
		List<Integer> approverContactIds;
		if (UserTypeEnum.COMPANY_CONTACT.getId().equals(userTypeId)) {
			String email = this.auth.getUnifiedPrincipal().getEmail();
			approverContactIds = this.contactJpaRepository.findAllByEmailAndAccountId(email, accountId)
				.stream()
				.map(Contact::getId)
				.toList();
		}
		else {
			approverContactIds = List.of(userId);
		}

		boolean isApprover = approvers.stream()
			.anyMatch((approver) -> approverContactIds.contains(approver.getEntityId())
					&& approver.getUserTypeId().equals(userTypeId));

		if (!isApprover) {
			throw new ValidationErrorException(
					"User is not authorized to approve this timesheet. Timesheet ID: " + timesheetId);
		}
	}

	/**
	 * Resolves all RCRM contact IDs sharing the JWT email within the given account. Used
	 * by callers that need to perform email-based approver checks in bulk without
	 * re-fetching the contact list per timesheet.
	 * @param accountId RCRM account ID for isolation
	 * @return list of contact IDs sharing the authenticated client's JWT email
	 */
	public List<Integer> resolveContactIds(Integer accountId) {
		String email = this.auth.getUnifiedPrincipal().getEmail();
		return this.contactJpaRepository.findAllByEmailAndAccountId(email, accountId)
			.stream()
			.map(Contact::getId)
			.toList();
	}

}
