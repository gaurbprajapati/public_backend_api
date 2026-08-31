package io.recruitcrm.microservice.timesheet.helpers.auth;

import io.recruitcrm.entity.model.Candidate;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.AccessControlChecker;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Entity;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.Permission;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.PermissionLevel;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.AccessControlCheckMetadataContext;
import io.recruitcrm.microservice.timesheet.access_control.contract_staffing.dto.PermissionCheckContext;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.AuthPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.ContractorPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.PrincipalType;
import io.recruitcrm.microservice.timesheet.dao.timesheet.TimesheetJpaRepository;
import io.recruitcrm.microservice.timesheet.exceptions.ForbiddenAccessException;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import io.recruitcrm.microservice.timesheet.helpers.constants.EntityNameConstants;
import io.recruitcrm.microservice.timesheet.helpers.constants.ExceptionMessageConstants;
import io.recruitcrm.microservice.timesheet.helpers.constants.ReimbursementConstants;
import io.recruitcrm.microservice.timesheet.repositories.timesheet.ITimesheetRepository;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class ReimbursementAccessValidator {

	private final AuthHolder auth;

	private final TimesheetJpaRepository timesheetJpaRepository;

	private final AccessControlChecker contractStaffingAccessControlChecker;

	private final ITimesheetRepository timesheetRepository;

	public ReimbursementAccessValidator(AuthHolder auth, TimesheetJpaRepository timesheetJpaRepository,
			AccessControlChecker contractStaffingAccessControlChecker, ITimesheetRepository timesheetRepository) {
		this.auth = auth;
		this.timesheetJpaRepository = timesheetJpaRepository;
		this.contractStaffingAccessControlChecker = contractStaffingAccessControlChecker;
		this.timesheetRepository = timesheetRepository;
	}

	public void validateTimesheetViewAccess(Integer timesheetId, Integer accountId) {
		findTimesheetOrThrow(timesheetId, accountId);
		checkPermissionByPrincipal(timesheetId, Permission.VIEW_TIMESHEET);
	}

	public void validateTimesheetEditAccess(Integer timesheetId, Integer accountId) {
		findTimesheetOrThrow(timesheetId, accountId);
		checkPermissionByPrincipal(timesheetId, Permission.EDIT_TIMESHEET);
	}

	public void validateTimesheetApproveAccess(Integer timesheetId, Integer accountId) {
		findTimesheetOrThrow(timesheetId, accountId);
		checkPermissionByPrincipal(timesheetId, Permission.APPROVE_TIMESHEET);
	}

	public void validateTimesheetCreateAccess(Integer timesheetId) {
		AuthPrincipal principal = this.auth.getUnifiedPrincipal();

		if (Objects.requireNonNull(principal.getPrincipalType()) == PrincipalType.USER) {
			PermissionCheckContext ctx = new PermissionCheckContext();
			ctx.setPermission(Permission.CREATE_TIMESHEET);
			ctx.setPermissionLevel(PermissionLevel.YES);

			AccessControlCheckMetadataContext meta = new AccessControlCheckMetadataContext();
			meta.setTimesheetId(timesheetId);

			this.contractStaffingAccessControlChecker.allows(Entity.TIMESHEET, ctx, meta);
		}
	}

	private void checkPermissionByPrincipal(Integer timesheetId, Permission permission) {
		AuthPrincipal principal = this.auth.getUnifiedPrincipal();

		if (Objects.requireNonNull(principal.getPrincipalType()) == PrincipalType.USER) {
			PermissionCheckContext ctx = new PermissionCheckContext();
			ctx.setPermission(permission);
			ctx.setPermissionLevel(PermissionLevel.YES);

			AccessControlCheckMetadataContext meta = new AccessControlCheckMetadataContext();
			meta.setTimesheetId(timesheetId);

			this.contractStaffingAccessControlChecker.allows(Entity.TIMESHEET, ctx, meta);
		}
	}

	private void findTimesheetOrThrow(Integer timesheetId, Integer accountId) {
		this.timesheetJpaRepository.findByIdAndAccountId(timesheetId, accountId)
			.orElseThrow(() -> new ResourceNotFoundException(EntityNameConstants.TIMESHEET, timesheetId));
	}

	public void validateAccessControlForCreateReimbursement(Integer timesheetId, Integer accountId) {
		findTimesheetOrThrow(timesheetId, accountId);

		AuthPrincipal principal = this.auth.getUnifiedPrincipal();

		switch (principal.getPrincipalType()) {
			case USER -> {
				PermissionCheckContext permissionCheckContext = new PermissionCheckContext();
				permissionCheckContext.setPermission(Permission.CREATE_TIMESHEET);
				permissionCheckContext.setPermissionLevel(PermissionLevel.YES);

				AccessControlCheckMetadataContext metadataContext = new AccessControlCheckMetadataContext();
				metadataContext.setTimesheetId(timesheetId);

				this.contractStaffingAccessControlChecker.allows(Entity.TIMESHEET, permissionCheckContext,
						metadataContext);
			}
			case CONTRACTOR -> {
				ContractorPrincipal contractorPrincipal = (ContractorPrincipal) principal;
				Integer contractorId = contractorPrincipal.getCandidateId();

				Candidate candidate = this.timesheetRepository.getCandidateLinkedToTimesheet(timesheetId, accountId);
				if (candidate == null || !Objects.equals(candidate.getId(), contractorId)) {
					throw new UnauthorizedAccessException(
							ExceptionMessageConstants.REIMBURSEMENT_CONTRACTOR_CREATE_OWN_ONLY);
				}
			}
			default -> throw new ForbiddenAccessException(ExceptionMessageConstants.REIMBURSEMENT_CREATE_FORBIDDEN);
		}
	}

	public void validateAccessControlForUpdateReimbursement(Integer timesheetId, Integer accountId,
			Integer reimbursementStatus) {
		findTimesheetOrThrow(timesheetId, accountId);

		AuthPrincipal principal = this.auth.getUnifiedPrincipal();

		switch (principal.getPrincipalType()) {
			case USER -> {
				PermissionCheckContext permissionCheckContext = new PermissionCheckContext();
				permissionCheckContext.setPermission(Permission.EDIT_TIMESHEET);
				permissionCheckContext.setPermissionLevel(PermissionLevel.YES);

				AccessControlCheckMetadataContext metadataContext = new AccessControlCheckMetadataContext();
				metadataContext.setTimesheetId(timesheetId);

				this.contractStaffingAccessControlChecker.allows(Entity.TIMESHEET, permissionCheckContext,
						metadataContext);

				if (Integer.valueOf(ReimbursementConstants.STATUS_APPROVED).equals(reimbursementStatus)) {
					throw new ValidationErrorException(ExceptionMessageConstants.REIMBURSEMENT_NOT_EDITABLE);
				}

			}
			case CONTRACTOR -> {
				ContractorPrincipal contractorPrincipal = (ContractorPrincipal) principal;
				Integer contractorId = contractorPrincipal.getCandidateId();

				Candidate candidate = this.timesheetRepository.getCandidateLinkedToTimesheet(timesheetId, accountId);
				if (candidate == null || !Objects.equals(candidate.getId(), contractorId)) {
					throw new UnauthorizedAccessException(
							ExceptionMessageConstants.REIMBURSEMENT_CONTRACTOR_UPDATE_OWN_ONLY);
				}

				if (Integer.valueOf(ReimbursementConstants.STATUS_APPROVED).equals(reimbursementStatus)) {
					throw new ValidationErrorException(ExceptionMessageConstants.REIMBURSEMENT_NOT_EDITABLE);
				}
			}

			default -> throw new ForbiddenAccessException(ExceptionMessageConstants.REIMBURSEMENT_UPDATE_FORBIDDEN);
		}
	}

	public void validateAccessControlForDeleteReimbursement(Integer timesheetId, Integer accountId) {
		findTimesheetOrThrow(timesheetId, accountId);

		AuthPrincipal principal = this.auth.getUnifiedPrincipal();

		switch (principal.getPrincipalType()) {
			case USER -> {
				PermissionCheckContext permissionCheckContext = new PermissionCheckContext();
				permissionCheckContext.setPermission(Permission.DELETE_TIMESHEET);
				permissionCheckContext.setPermissionLevel(PermissionLevel.YES);

				AccessControlCheckMetadataContext metadataContext = new AccessControlCheckMetadataContext();
				metadataContext.setTimesheetId(timesheetId);

				this.contractStaffingAccessControlChecker.allows(Entity.TIMESHEET, permissionCheckContext,
						metadataContext);
			}
			case CONTRACTOR -> {
				ContractorPrincipal contractorPrincipal = (ContractorPrincipal) principal;
				Integer contractorId = contractorPrincipal.getCandidateId();

				Candidate candidate = this.timesheetRepository.getCandidateLinkedToTimesheet(timesheetId, accountId);
				if (candidate == null || !Objects.equals(candidate.getId(), contractorId)) {
					throw new UnauthorizedAccessException(
							ExceptionMessageConstants.REIMBURSEMENT_CONTRACTOR_DELETE_OWN_ONLY);
				}
			}

			default -> throw new ForbiddenAccessException(ExceptionMessageConstants.REIMBURSEMENT_CREATE_FORBIDDEN);
		}
	}

	public void validateAccessControlForGetStatusHistory(Integer timesheetId, Integer accountId) {
		findTimesheetOrThrow(timesheetId, accountId);

		AuthPrincipal principal = this.auth.getUnifiedPrincipal();

		if (principal.getPrincipalType() == PrincipalType.USER) {
			PermissionCheckContext permissionCheckContext = new PermissionCheckContext();
			permissionCheckContext.setPermission(Permission.VIEW_TIMESHEET);
			permissionCheckContext.setPermissionLevel(PermissionLevel.YES);

			AccessControlCheckMetadataContext metadataContext = new AccessControlCheckMetadataContext();
			metadataContext.setTimesheetId(timesheetId);

			this.contractStaffingAccessControlChecker.allows(Entity.TIMESHEET, permissionCheckContext, metadataContext);
		}
	}

	public void validateAccessControlForUpdateStatus(Integer timesheetId, Integer accountId) {
		findTimesheetOrThrow(timesheetId, accountId);

		AuthPrincipal principal = this.auth.getUnifiedPrincipal();

		if (principal.getPrincipalType() == PrincipalType.USER) {

			PermissionCheckContext permissionCheckContext = new PermissionCheckContext();
			permissionCheckContext.setPermission(Permission.APPROVE_TIMESHEET);
			permissionCheckContext.setPermissionLevel(PermissionLevel.YES);

			AccessControlCheckMetadataContext metadataContext = new AccessControlCheckMetadataContext();
			metadataContext.setTimesheetId(timesheetId);

			this.contractStaffingAccessControlChecker.allows(Entity.TIMESHEET, permissionCheckContext, metadataContext);

		}
		else if (principal.getPrincipalType() == PrincipalType.CONTRACTOR) {
			throw new UnauthorizedAccessException(
					ExceptionMessageConstants.REIMBURSEMENT_CONTRACTOR_CANNOT_UPDATE_STATUS);
		}
	}

}
