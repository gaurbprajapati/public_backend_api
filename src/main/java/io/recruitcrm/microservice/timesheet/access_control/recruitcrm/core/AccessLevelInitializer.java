/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core;

import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.AccessControlDto;
import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto.GlobalPermissions;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Handles explicit initialization of access level getters for both entity and global
 * permissions. This class provides a clear, maintainable way to map entities and
 * permissions to their corresponding getter methods.
 */
@Component(AccessLevelInitializer.BEAN_NAME)
public class AccessLevelInitializer {

	public static final String BEAN_NAME = "recruitcrmAccessLevelInitializer";

	/**
	 * Initializes the entity access level getters with explicit mappings.
	 * @return A map of Entity to its corresponding getter function
	 */
	public Map<Entity, Function<AccessControlDto, ?>> initializeEntityAccessLevelGetters() {
		Map<Entity, Function<AccessControlDto, ?>> getters = new EnumMap<>(Entity.class);

		// Extended access level entities
		getters.put(Entity.CANDIDATES, AccessControlDto::getCandidates);
		getters.put(Entity.CONTACTS, AccessControlDto::getContacts);
		getters.put(Entity.JOBS, AccessControlDto::getJobs);
		getters.put(Entity.COMPANIES, AccessControlDto::getCompanies);
		getters.put(Entity.DEALS, AccessControlDto::getDeals);

		// Basic access level entities
		getters.put(Entity.CALL_LOG, AccessControlDto::getCallLog);
		getters.put(Entity.PLACEMENT_BILLING, AccessControlDto::getPlacementBilling);
		getters.put(Entity.TASK_MEETINGS, AccessControlDto::getTaskMeetings);
		getters.put(Entity.NOTES, AccessControlDto::getNotes);
		getters.put(Entity.FILES, AccessControlDto::getFiles);

		// Global permissions
		getters.put(Entity.GLOBAL, AccessControlDto::getGlobalPermissions);

		return getters;
	}

	/**
	 * Initializes the global permission getters with explicit mappings.
	 * @return A map of GlobalPermission to its corresponding getter function
	 */
	public Map<GlobalPermission, Function<GlobalPermissions, PermissionLevel>> initializeGlobalPermissionGetters() {
		Map<GlobalPermission, Function<GlobalPermissions, PermissionLevel>> getters = new EnumMap<>(
				GlobalPermission.class);

		// Report permissions
		getters.put(GlobalPermission.REPORTS, GlobalPermissions::getReports);
		getters.put(GlobalPermission.RECRUITER_PERFORMANCE_REPORT, GlobalPermissions::getRecruiterPerformanceReport);
		getters.put(GlobalPermission.CANDIDATE_LIFECYCLE_REPORT, GlobalPermissions::getCandidateLifecycleReport);
		getters.put(GlobalPermission.CLIENT_PERFORMANCE_REPORT, GlobalPermissions::getClientPerformanceReport);
		getters.put(GlobalPermission.JOB_STATISTIC_REPORT, GlobalPermissions::getJobStatisticReport);
		getters.put(GlobalPermission.DEAL_REPORT, GlobalPermissions::getDealReport);
		getters.put(GlobalPermission.TIME_TO_HIRE_REPORT, GlobalPermissions::getTimeToHireReport);
		getters.put(GlobalPermission.ACCOUNT_OVERVIEW_REPORT, GlobalPermissions::getAccountOverviewReport);
		getters.put(GlobalPermission.DEALS_BY_TEAM, GlobalPermissions::getDealsByTeam);
		getters.put(GlobalPermission.EXECUTIVE_SEARCH_REPORT, GlobalPermissions::getExecutiveSearchReport);
		getters.put(GlobalPermission.SENT_EMAIL_KPI_REPORT, GlobalPermissions::getSentEmailKpiReport);
		getters.put(GlobalPermission.TARGET_REPORT, GlobalPermissions::getTargetReport);

		// Export and bulk operations
		getters.put(GlobalPermission.EXPORT_TO_CSV, GlobalPermissions::getExportToCsv);
		getters.put(GlobalPermission.BULK_UPDATE_FIELD, GlobalPermissions::getBulkUpdateField);
		getters.put(GlobalPermission.BULK_DELETE, GlobalPermissions::getBulkDelete);

		// Administrative settings
		getters.put(GlobalPermission.ADMIN_SETTINGS, GlobalPermissions::getAdminSettings);
		getters.put(GlobalPermission.PLANS_AND_BILLING, GlobalPermissions::getPlansAndBilling);
		getters.put(GlobalPermission.USER_MANAGEMENT, GlobalPermissions::getUserManagement);
		getters.put(GlobalPermission.ROLES_PERMISSIONS, GlobalPermissions::getRolesPermissions);
		getters.put(GlobalPermission.TEAMS, GlobalPermissions::getTeams);
		getters.put(GlobalPermission.ACCOUNT_MANAGEMENT, GlobalPermissions::getAccountManagement);

		// Pipeline and workflow settings
		getters.put(GlobalPermission.EMAIL_TRIGGERS, GlobalPermissions::getEmailTriggers);
		getters.put(GlobalPermission.SALES_PIPELINE, GlobalPermissions::getSalesPipeline);
		getters.put(GlobalPermission.HIRING_PIPELINE, GlobalPermissions::getHiringPipeline);
		getters.put(GlobalPermission.DEALS_PIPELINE, GlobalPermissions::getDealsPipeline);
		getters.put(GlobalPermission.JOB_STATUS, GlobalPermissions::getJobStatus);
		getters.put(GlobalPermission.PITCH_CANDIDATE_PIPELINE, GlobalPermissions::getPitchCandidatePipeline);
		getters.put(GlobalPermission.WORKFLOW_AUTOMATION, GlobalPermissions::getWorkflowAutomation);

		// Customization settings
		getters.put(GlobalPermission.CUSTOMIZE_INVOICE, GlobalPermissions::getCustomizeInvoice);
		getters.put(GlobalPermission.FIELDS_SHARED_WITH_CLIENT, GlobalPermissions::getFieldsSharedWithClient);
		getters.put(GlobalPermission.CANDIDATE_FIELDS, GlobalPermissions::getCandidateFields);
		getters.put(GlobalPermission.COMPANY_FIELDS, GlobalPermissions::getCompanyFields);
		getters.put(GlobalPermission.CONTACT_FIELDS, GlobalPermissions::getContactFields);
		getters.put(GlobalPermission.DEAL_FIELDS, GlobalPermissions::getDealFields);
		getters.put(GlobalPermission.JOB_FIELDS, GlobalPermissions::getJobFields);
		getters.put(GlobalPermission.PUBLIC_JOB_PAGE_SETTINGS, GlobalPermissions::getPublicJobPageSettings);
		getters.put(GlobalPermission.JOB_APPLICATION_FORM_SETTINGS, GlobalPermissions::getJobApplicationFormSettings);
		getters.put(GlobalPermission.PROFILE_UPDATE_FORM_SETTINGS, GlobalPermissions::getProfileUpdateFormSettings);
		getters.put(GlobalPermission.TALENT_POOL_PAGE_SETTINGS, GlobalPermissions::getTalentPoolPageSettings);

		// Integration settings
		getters.put(GlobalPermission.API_INTEGRATIONS, GlobalPermissions::getApiIntegrations);
		getters.put(GlobalPermission.JOB_BOARD_INTEGRATIONS, GlobalPermissions::getJobBoardIntegrations);
		getters.put(GlobalPermission.CALLING_INTEGRATIONS, GlobalPermissions::getCallingIntegrations);
		getters.put(GlobalPermission.EXTERNAL_JOB_BOARD_INTEGRATION, GlobalPermissions::getExternalJobBoardIntegration);

		// Type customization settings
		getters.put(GlobalPermission.CALL_TYPE_CUSTOMIZATION, GlobalPermissions::getCallTypeCustomization);
		getters.put(GlobalPermission.NOTE_TYPE_CUSTOMIZATION, GlobalPermissions::getNoteTypeCustomization);
		getters.put(GlobalPermission.MEETING_TYPE_CUSTOMIZATION, GlobalPermissions::getMeetingTypeCustomization);
		getters.put(GlobalPermission.TASK_TYPE_CUSTOMIZATION, GlobalPermissions::getTaskTypeCustomization);

		// Template and formatting settings
		getters.put(GlobalPermission.STANDARD_EMAIL_TEMPLATES, GlobalPermissions::getStandardEmailTemplates);
		getters.put(GlobalPermission.RESUME_FORMATTING, GlobalPermissions::getResumeFormatting);
		getters.put(GlobalPermission.SMS_TEMPLATES, GlobalPermissions::getSmsTemplates);
		getters.put(GlobalPermission.ACTIVITY_TEMPLATES, GlobalPermissions::getActivityTemplates);

		// Security and privacy settings
		getters.put(GlobalPermission.HIDE_EMAIL, GlobalPermissions::getHideEmail);
		getters.put(GlobalPermission.BLACKLIST_EMAIL_ID, GlobalPermissions::getBlacklistEmailId);
		getters.put(GlobalPermission.IP_RESTRICTION, GlobalPermissions::getIpRestriction);
		getters.put(GlobalPermission.PRIVATE_EMAILS, GlobalPermissions::getPrivateEmails);
		getters.put(GlobalPermission.ALL_CONNECTED_EMAIL, GlobalPermissions::getAllConnectedEmail);

		// Advanced features
		getters.put(GlobalPermission.CUSTOM_XML_SETTINGS, GlobalPermissions::getCustomXmlSettings);
		getters.put(GlobalPermission.ADVANCED_ANALYTICS, GlobalPermissions::getAdvancedAnalytics);
		getters.put(GlobalPermission.ALLOW_TO_CHOOSE_OWN_FIELDS, GlobalPermissions::getAllowToChooseOwnFields);
		getters.put(GlobalPermission.RECORD_CALLS_BY_DEFAULT, GlobalPermissions::getRecordCallsByDefault);
		getters.put(GlobalPermission.ALLOW_TO_CHOOSE_CALL_RECORD, GlobalPermissions::getAllowToChooseCallRecord);
		getters.put(GlobalPermission.ALLOW_TO_DOWNLOAD_RECORDS, GlobalPermissions::getAllowToDownloadRecords);
		getters.put(GlobalPermission.DATA_ENRICHMENT, GlobalPermissions::getDataEnrichment);
		getters.put(GlobalPermission.TARGET_REPORT_CREATE_ACCESS, GlobalPermissions::getTargetReportCreateAccess);

		return getters;
	}

}