/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.helpers.access_control;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.recruitcrm.entity.model.UserRole;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.AccessControlDto;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.CallLog;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.Candidates;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.Companies;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.Contacts;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.Deals;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.EmailTemplates;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.Files;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.Invoices;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.Jobs;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.Notes;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.PlacementBilling;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.TaskMeetings;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.PermissionLevel;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.dao.team_member.TeamMemberJpaRepository;
import io.recruitcrm.microservice.timesheet.dao.user_role.UserRoleJpaRepository;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.text.MessageFormat;
import java.util.List;

/**
 * Helper class for fetching access control data for different entities. This class
 * queries the tbluserrole table based on the current authenticated user's roleId and
 * returns entity-specific access control information.
 */
@Component
@RequestScope
public class AccessControlHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(AccessControlHelper.class);

	private static final String COLLABORATOR_JOBS_TABLE = "collaborator_jobs_t";

	private static final String JOB_ID_COLUMN = "job_id";

	private static final String COLLABORATOR_ID_COLUMN = "collaborator_id";

	private static final String COLLABORATOR_TYPE_COLUMN = "collaborator_type";

	private static final byte COLLABORATOR_TYPE_TEAM = 1;

	private final AuthHolder authHolder;

	private final UserRoleJpaRepository userRoleJpaRepository;

	private final TeamMemberJpaRepository teamMemberJpaRepository;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private AccessControlDto cachedAccessControlDto;

	public AccessControlHelper(AuthHolder authHolder, UserRoleJpaRepository userRoleJpaRepository,
			TeamMemberJpaRepository teamMemberJpaRepository) {
		this.authHolder = authHolder;
		this.userRoleJpaRepository = userRoleJpaRepository;
		this.teamMemberJpaRepository = teamMemberJpaRepository;
	}

	/**
	 * Fetches and caches the access control JSON from UserRole table.
	 * @return AccessControlDto containing all access control data
	 * @throws ResourceNotFoundException if UserRole not found
	 * @throws UnauthorizedAccessException if JSON parsing fails
	 */
	private AccessControlDto getAccessControlDto() {
		if (this.cachedAccessControlDto != null) {
			return this.cachedAccessControlDto;
		}

		Integer roleId = this.authHolder.getAuthenticationPrincipalRoleIdentifier();

		UserRole userRole = this.userRoleJpaRepository.findById(roleId)
			.orElseThrow(() -> new ResourceNotFoundException(
					MessageFormat.format("UserRole not found for roleId: {0}", roleId)));

		String userAccessJson = userRole.getUserAccessJson();
		if (userAccessJson == null || userAccessJson.isEmpty()) {
			throw new UnauthorizedAccessException("User access JSON is empty or null");
		}

		try {
			this.cachedAccessControlDto = this.objectMapper.readValue(userAccessJson, AccessControlDto.class);
			this.cachedAccessControlDto.initializeGlobalPermissions();
			return this.cachedAccessControlDto;
		}
		catch (Exception ex) {
			throw new UnauthorizedAccessException(
					MessageFormat.format("Error while parsing access control JSON: {0}", ex.getMessage()));
		}
	}

	/**
	 * Gets access control data for candidates entity.
	 * @return Candidates access control data
	 */
	public Candidates getCandidatesAccessControl() {
		AccessControlDto accessControlDto = getAccessControlDto();
		return accessControlDto.getCandidates();
	}

	/**
	 * Gets access control data for contacts entity.
	 * @return Contacts access control data
	 */
	public Contacts getContactsAccessControl() {
		AccessControlDto accessControlDto = getAccessControlDto();
		return accessControlDto.getContacts();
	}

	/**
	 * Gets access control data for jobs entity.
	 * @return Jobs access control data
	 */
	public Jobs getJobsAccessControl() {
		AccessControlDto accessControlDto = getAccessControlDto();
		return accessControlDto.getJobs();
	}

	/**
	 * Gets access control data for companies entity.
	 * @return Companies access control data
	 */
	public Companies getCompaniesAccessControl() {
		AccessControlDto accessControlDto = getAccessControlDto();
		return accessControlDto.getCompanies();
	}

	/**
	 * Gets access control data for deals entity.
	 * @return Deals access control data
	 */
	public Deals getDealsAccessControl() {
		AccessControlDto accessControlDto = getAccessControlDto();
		return accessControlDto.getDeals();
	}

	/**
	 * Gets access control data for notes entity.
	 * @return Notes access control data
	 */
	public Notes getNotesAccessControl() {
		AccessControlDto accessControlDto = getAccessControlDto();
		return accessControlDto.getNotes();
	}

	/**
	 * Gets access control data for invoices entity.
	 * @return Invoices access control data
	 */
	public Invoices getInvoicesAccessControl() {
		AccessControlDto accessControlDto = getAccessControlDto();
		return accessControlDto.getInvoices();
	}

	/**
	 * Gets access control data for placementbilling entity.
	 * @return PlacementBilling access control data
	 */
	public PlacementBilling getPlacementBillingAccessControl() {
		AccessControlDto accessControlDto = getAccessControlDto();
		return accessControlDto.getPlacementBilling();
	}

	/**
	 * Gets access control data for taskmeetings entity.
	 * @return TaskMeetings access control data
	 */
	public TaskMeetings getTaskMeetingsAccessControl() {
		AccessControlDto accessControlDto = getAccessControlDto();
		return accessControlDto.getTaskMeetings();
	}

	/**
	 * Gets access control data for calllog entity.
	 * @return CallLog access control data
	 */
	public CallLog getCallLogAccessControl() {
		AccessControlDto accessControlDto = getAccessControlDto();
		return accessControlDto.getCallLog();
	}

	/**
	 * Gets access control data for files entity.
	 * @return Files access control data
	 */
	public Files getFilesAccessControl() {
		AccessControlDto accessControlDto = getAccessControlDto();
		return accessControlDto.getFiles();
	}

	/**
	 * Gets access control data for emailtemplates entity.
	 * @return EmailTemplates access control data
	 */
	public EmailTemplates getEmailTemplatesAccessControl() {
		AccessControlDto accessControlDto = getAccessControlDto();
		return accessControlDto.getEmailTemplates();
	}

	/**
	 * Builds a JOOQ Condition for jobs based on access control settings. This method
	 * replicates the logic from PHP AccessControl::getAccessControlWhereCondition.
	 * @param ownerIdField The JOOQ field representing the owner ID (e.g., job.OWNERID)
	 * @param jobIdField The JOOQ field representing the job ID (e.g., job.ID)
	 * @return JOOQ Condition that filters jobs based on access control, or
	 * DSL.trueCondition() for "Everything"
	 */
	public Condition buildJobsAccessControlCondition(Field<Integer> ownerIdField, Field<Integer> jobIdField) {
		Jobs jobsAccessControl = getJobsAccessControl();
		if (jobsAccessControl == null) {
			return DSL.falseCondition();
		}
		LOGGER.info(
				"Jobs access control - canAdd: {}, canEdit: {}, canView: {}, canDelete: {}, ownerChange: {}, fileAccess: {}",
				jobsAccessControl.getCanAdd(), jobsAccessControl.getCanEdit(), jobsAccessControl.getCanView(),
				jobsAccessControl.getCanDelete(), jobsAccessControl.getOwnerChange(),
				jobsAccessControl.getFileAccess());

		String canView = jobsAccessControl.getCanView();
		if (canView == null) {
			return DSL.falseCondition();
		}

		PermissionLevel permissionLevel = parsePermissionLevel(canView);
		if (permissionLevel == null) {
			return DSL.falseCondition();
		}

		Integer currentUserId = this.authHolder.getAuthenticationPrincipalUniqueIdentifier();

		return switch (permissionLevel) {
			case TEAM_ONLY -> {
				List<Integer> teamUserIds = getTeamUserIds(currentUserId);
				if (!teamUserIds.isEmpty()) {
					// Include current user in the list if not already present
					if (!teamUserIds.contains(currentUserId)) {
						teamUserIds.add(currentUserId);
					}
					Condition ownerCondition = ownerIdField.in(teamUserIds);

					// Get team IDs for the current user
					List<Integer> teamIds = getTeamIds(currentUserId);
					if (!teamIds.isEmpty()) {
						// Create subquery for collaborator_jobs_t table
						// collaborator_type = 1 means TEAM type
						Table<?> collaboratorTable = DSL.table(DSL.name(COLLABORATOR_JOBS_TABLE));
						Field<Integer> collaboratorJobIdField = DSL
							.field(DSL.name(COLLABORATOR_JOBS_TABLE, JOB_ID_COLUMN), Integer.class);
						Field<Integer> collaboratorIdField = DSL
							.field(DSL.name(COLLABORATOR_JOBS_TABLE, COLLABORATOR_ID_COLUMN), Integer.class);
						Field<Byte> collaboratorTypeField = DSL
							.field(DSL.name(COLLABORATOR_JOBS_TABLE, COLLABORATOR_TYPE_COLUMN), Byte.class);

						// Create subquery: SELECT job_id FROM collaborator_jobs_t
						// WHERE collaborator_id IN (teamIds) AND collaborator_type = 1
						org.jooq.Select<org.jooq.Record1<Integer>> collaboratorSubquery = DSL
							.select(collaboratorJobIdField)
							.from(collaboratorTable)
							.where(collaboratorIdField.in(teamIds)
								.and(collaboratorTypeField.eq(COLLABORATOR_TYPE_TEAM)));

						// Combine: (ownerId IN teamUserIds) OR (jobId IN
						// collaboratorJobIds)
						yield ownerCondition.or(jobIdField.in(collaboratorSubquery));
					}

					yield ownerCondition;
				}
				else {
					// If no team members, only show owned jobs
					yield ownerIdField.eq(currentUserId);
				}
			}
			case OWNED_ONLY -> ownerIdField.eq(currentUserId);
			case EVERYTHING -> DSL.trueCondition();
			default -> DSL.falseCondition();
		};
	}

	/**
	 * Builds a JOOQ Condition for companies based on access control settings. This method
	 * replicates the logic from PHP AccessControl::getAccessControlWhereCondition.
	 * @param ownerIdField The JOOQ field representing the owner ID (e.g.,
	 * company.OWNERID)
	 * @return JOOQ Condition that filters companies based on access control, or
	 * DSL.trueCondition() for "Everything"
	 */
	public Condition buildCompaniesAccessControlCondition(Field<Integer> ownerIdField) {
		Companies companiesAccessControl = getCompaniesAccessControl();
		if (companiesAccessControl == null) {
			return DSL.falseCondition();
		}
		LOGGER.info(
				"Companies access control - canAdd: {}, canEdit: {}, canView: {}, canDelete: {}, ownerChange: {}, fileAccess: {}",
				companiesAccessControl.getCanAdd(), companiesAccessControl.getCanEdit(),
				companiesAccessControl.getCanView(), companiesAccessControl.getCanDelete(),
				companiesAccessControl.getOwnerChange(), companiesAccessControl.getFileAccess());

		String canView = companiesAccessControl.getCanView();
		if (canView == null) {
			return DSL.falseCondition();
		}

		PermissionLevel permissionLevel = parsePermissionLevel(canView);
		if (permissionLevel == null) {
			return DSL.falseCondition();
		}

		Integer currentUserId = this.authHolder.getAuthenticationPrincipalUniqueIdentifier();

		return switch (permissionLevel) {
			case TEAM_ONLY -> {
				List<Integer> teamUserIds = getTeamUserIds(currentUserId);
				if (!teamUserIds.isEmpty()) {
					// Include current user in the list if not already present
					if (!teamUserIds.contains(currentUserId)) {
						teamUserIds.add(currentUserId);
					}
					yield ownerIdField.in(teamUserIds);
				}
				else {
					// If no team members, only show owned companies
					yield ownerIdField.eq(currentUserId);
				}
			}
			case OWNED_ONLY -> ownerIdField.eq(currentUserId);
			case EVERYTHING -> DSL.trueCondition();
			default -> DSL.falseCondition();
		};
	}

	/**
	 * Builds a JOOQ Condition for deals based on access control settings. This method
	 * replicates the logic from PHP AccessControl::getAccessControlWhereCondition.
	 * @param ownerIdField The JOOQ field representing the owner ID (e.g., deals.OWNERID)
	 * @return JOOQ Condition that filters deals based on access control, or
	 * DSL.trueCondition() for "Everything"
	 */
	public Condition buildDealsAccessControlCondition(Field<Integer> ownerIdField) {
		Deals dealsAccessControl = getDealsAccessControl();
		if (dealsAccessControl == null) {
			return DSL.falseCondition();
		}
		LOGGER.info(
				"Deals access control - canAdd: {}, canEdit: {}, canView: {}, canDelete: {}, ownerChange: {}, fileAccess: {}",
				dealsAccessControl.getCanAdd(), dealsAccessControl.getCanEdit(), dealsAccessControl.getCanView(),
				dealsAccessControl.getCanDelete(), dealsAccessControl.getOwnerChange(),
				dealsAccessControl.getFileAccess());

		String canView = dealsAccessControl.getCanView();
		if (canView == null) {
			return DSL.falseCondition();
		}

		PermissionLevel permissionLevel = parsePermissionLevel(canView);
		if (permissionLevel == null) {
			return DSL.falseCondition();
		}

		Integer currentUserId = this.authHolder.getAuthenticationPrincipalUniqueIdentifier();

		return switch (permissionLevel) {
			case TEAM_ONLY -> {
				List<Integer> teamUserIds = getTeamUserIds(currentUserId);
				if (!teamUserIds.isEmpty()) {
					// Include current user in the list if not already present
					if (!teamUserIds.contains(currentUserId)) {
						teamUserIds.add(currentUserId);
					}
					yield ownerIdField.in(teamUserIds);
				}
				else {
					// If no team members, only show owned deals
					yield ownerIdField.eq(currentUserId);
				}
			}
			case OWNED_ONLY -> ownerIdField.eq(currentUserId);
			case EVERYTHING -> DSL.trueCondition();
			default -> DSL.falseCondition();
		};
	}

	/**
	 * Builds a JOOQ Condition for contacts based on access control settings. This method
	 * replicates the logic from PHP AccessControl::getAccessControlWhereCondition.
	 * @param ownerIdField The JOOQ field representing the owner ID (e.g.,
	 * contact.OWNERID)
	 * @return JOOQ Condition that filters contacts based on access control, or
	 * DSL.trueCondition() for "Everything"
	 */
	public Condition buildContactsAccessControlCondition(Field<Integer> ownerIdField) {
		Contacts contactsAccessControl = getContactsAccessControl();
		if (contactsAccessControl == null) {
			return DSL.falseCondition();
		}

		String canView = contactsAccessControl.getCanView();
		if (canView == null) {
			return DSL.falseCondition();
		}

		PermissionLevel permissionLevel = parsePermissionLevel(canView);
		if (permissionLevel == null) {
			return DSL.falseCondition();
		}

		Integer currentUserId = this.authHolder.getAuthenticationPrincipalUniqueIdentifier();

		return switch (permissionLevel) {
			case TEAM_ONLY -> {
				List<Integer> teamUserIds = getTeamUserIds(currentUserId);
				if (!teamUserIds.isEmpty()) {
					if (!teamUserIds.contains(currentUserId)) {
						teamUserIds.add(currentUserId);
					}
					yield ownerIdField.in(teamUserIds);
				}
				else {
					yield ownerIdField.eq(currentUserId);
				}
			}
			case OWNED_ONLY -> ownerIdField.eq(currentUserId);
			case EVERYTHING -> DSL.trueCondition();
			default -> DSL.falseCondition();
		};
	}

	/**
	 * Builds a JOOQ Condition for candidates based on access control settings. This
	 * method replicates the logic from PHP AccessControl::getAccessControlWhereCondition.
	 * @param ownerIdField The JOOQ field representing the owner ID (e.g.,
	 * candidate.OWNERID)
	 * @return JOOQ Condition that filters candidates based on access control, or
	 * DSL.trueCondition() for "Everything"
	 */
	public Condition buildCandidatesAccessControlCondition(Field<Integer> ownerIdField) {
		Candidates candidatesAccessControl = getCandidatesAccessControl();
		if (candidatesAccessControl == null) {
			return DSL.falseCondition();
		}
		LOGGER.info(
				"Candidates access control - canAdd: {}, canEdit: {}, canView: {}, canDelete: {}, ownerChange: {}, fileAccess: {}",
				candidatesAccessControl.getCanAdd(), candidatesAccessControl.getCanEdit(),
				candidatesAccessControl.getCanView(), candidatesAccessControl.getCanDelete(),
				candidatesAccessControl.getOwnerChange(), candidatesAccessControl.getFileAccess());

		String canView = candidatesAccessControl.getCanView();
		if (canView == null) {
			return DSL.falseCondition();
		}

		PermissionLevel permissionLevel = parsePermissionLevel(canView);
		if (permissionLevel == null) {
			return DSL.falseCondition();
		}

		Integer currentUserId = this.authHolder.getAuthenticationPrincipalUniqueIdentifier();

		return switch (permissionLevel) {
			case TEAM_ONLY -> {
				List<Integer> teamUserIds = getTeamUserIds(currentUserId);
				if (!teamUserIds.isEmpty()) {
					// Include current user in the list if not already present
					if (!teamUserIds.contains(currentUserId)) {
						teamUserIds.add(currentUserId);
					}
					yield ownerIdField.in(teamUserIds);
				}
				else {
					// If no team members, only show owned candidates
					yield ownerIdField.eq(currentUserId);
				}
			}
			case OWNED_ONLY -> ownerIdField.eq(currentUserId);
			case EVERYTHING -> DSL.trueCondition();
			default -> DSL.falseCondition();
		};
	}

	/**
	 * Gets all user IDs that are in the same team(s) as the given user.
	 * @param userId The user ID to get team members for
	 * @return List of user IDs including the user and all team members
	 */
	public List<Integer> getTeamUserIds(Integer userId) {
		return this.teamMemberJpaRepository.findAllUserIdsByTeamMembership(userId);
	}

	/**
	 * Gets all team IDs that the given user is a member of.
	 * @param userId The user ID to get team IDs for
	 * @return List of team IDs
	 */
	private List<Integer> getTeamIds(Integer userId) {
		return this.teamMemberJpaRepository.findAllTeamIdsByUserId(userId);
	}

	/**
	 * Parses a string value to PermissionLevel enum. Returns null if the string cannot be
	 * parsed to a valid PermissionLevel.
	 * @param value The string value to parse
	 * @return PermissionLevel enum value, or null if parsing fails
	 */
	private PermissionLevel parsePermissionLevel(String value) {
		try {
			return PermissionLevel.fromLabel(value);
		}
		catch (IllegalArgumentException ex) {
			return null;
		}
	}

	/**
	 * Evaluates whether the current user has the given permission level for a specific
	 * record, based on the record's owner ID.
	 * @param permissionLevelStr permission level string from the role access JSON (e.g.
	 * "Everything", "Owned Only", "Team Only")
	 * @param ownerId ownerid of the record being checked
	 * @param currentUserId authenticated user's ID
	 * @param teamUserIds pre-fetched team user IDs (including the current user); may be
	 * empty when the permission level is not Team Only
	 * @return true if access is granted
	 */
	public boolean resolvePermission(String permissionLevelStr, Integer ownerId, Integer currentUserId,
			List<Integer> teamUserIds) {
		if (permissionLevelStr == null) {
			return false;
		}
		PermissionLevel level = parsePermissionLevel(permissionLevelStr);
		if (level == null) {
			return false;
		}
		return switch (level) {
			case EVERYTHING -> true;
			case OWNED_ONLY -> currentUserId.equals(ownerId);
			case TEAM_ONLY -> teamUserIds != null && teamUserIds.contains(ownerId);
			default -> false;
		};
	}

	/**
	 * Returns whether the given permission level string requires a team user ID lookup.
	 * @param permissionLevelStr permission level string from the role access JSON
	 * @return true if the level is Team Only
	 */
	public boolean requiresTeamLookup(String permissionLevelStr) {
		PermissionLevel level = parsePermissionLevel(permissionLevelStr);
		return level == PermissionLevel.TEAM_ONLY;
	}

}
