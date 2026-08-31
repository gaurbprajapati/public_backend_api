package io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto;

import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.PermissionLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public final class GlobalPermissionFieldHandler {

	private GlobalPermissionFieldHandler() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}

	private static final Set<String> PERMISSION_FIELDS = Set.of("reports", "recruiterperformancereport",
			"candidatelifecyclereport", "clientperformancereport", "jobstatisticreport", "dealreport", "exporttocsv",
			"bulkupdatefield", "bulkdelete", "adminsettings", "plansandbilling", "usermanagement", "rolespermissions",
			"teams", "accountmanagement", "emailtriggers", "salespipeline", "hiringpipeline", "dealspipeline",
			"jobstatus", "customizeinvoice", "fieldssharedwithclient", "candidatefields", "companyfields",
			"contactfields", "dealfields", "jobfields", "publicjobpagesettings", "jobapplicationformsettings",
			"profileupdateformsettings", "talentpoolpagesettings", "apiintegrations", "jobboardintegrations",
			"callingintegrations", "calltypecustomization", "notetypecustomization", "meetingtypecustomization",
			"tasktypecustomization", "standardemailtemplates", "resumeformatting", "smstemplates", "activitytemplates",
			"hideemail", "blacklistemailid", "iprestriction", "privateemails", "allconnectedemail", "customxmlsettings",
			"advancedanalytics", "allowtochooseownfields", "recordcallsbydefault", "allowtochoosecallrecord",
			"allowtodownloadrecords", "dataenrichment", "targetreportcreateaccess", "targetreport",
			"executivesearchreport", "sentemailkpireport", "externaljobboardintegration", "emailsequence",
			"workflowautomation", "hotlist", "customizeofflimitstatus", "jobadvertising", "timetohirereport",
			"accountoverviewreport", "dealsbyteam", "pitchcandidatepipeline");

	private static final Map<String, BiConsumer<GlobalPermissions, PermissionLevel>> PERMISSION_SETTERS = new HashMap<>();

	static {
		// Initialize permission setters using method references
		PERMISSION_SETTERS.put("reports", GlobalPermissions::setReports);
		PERMISSION_SETTERS.put("recruiterperformancereport", GlobalPermissions::setRecruiterPerformanceReport);
		PERMISSION_SETTERS.put("candidatelifecyclereport", GlobalPermissions::setCandidateLifecycleReport);
		PERMISSION_SETTERS.put("clientperformancereport", GlobalPermissions::setClientPerformanceReport);
		PERMISSION_SETTERS.put("jobstatisticreport", GlobalPermissions::setJobStatisticReport);
		PERMISSION_SETTERS.put("dealreport", GlobalPermissions::setDealReport);
		PERMISSION_SETTERS.put("exporttocsv", GlobalPermissions::setExportToCsv);
		PERMISSION_SETTERS.put("bulkupdatefield", GlobalPermissions::setBulkUpdateField);
		PERMISSION_SETTERS.put("bulkdelete", GlobalPermissions::setBulkDelete);
		PERMISSION_SETTERS.put("adminsettings", GlobalPermissions::setAdminSettings);
		PERMISSION_SETTERS.put("plansandbilling", GlobalPermissions::setPlansAndBilling);
		PERMISSION_SETTERS.put("usermanagement", GlobalPermissions::setUserManagement);
		PERMISSION_SETTERS.put("rolespermissions", GlobalPermissions::setRolesPermissions);
		PERMISSION_SETTERS.put("teams", GlobalPermissions::setTeams);
		PERMISSION_SETTERS.put("accountmanagement", GlobalPermissions::setAccountManagement);
		PERMISSION_SETTERS.put("emailtriggers", GlobalPermissions::setEmailTriggers);
		PERMISSION_SETTERS.put("salespipeline", GlobalPermissions::setSalesPipeline);
		PERMISSION_SETTERS.put("hiringpipeline", GlobalPermissions::setHiringPipeline);
		PERMISSION_SETTERS.put("dealspipeline", GlobalPermissions::setDealsPipeline);
		PERMISSION_SETTERS.put("jobstatus", GlobalPermissions::setJobStatus);
		PERMISSION_SETTERS.put("customizeinvoice", GlobalPermissions::setCustomizeInvoice);
		PERMISSION_SETTERS.put("fieldssharedwithclient", GlobalPermissions::setFieldsSharedWithClient);
		PERMISSION_SETTERS.put("candidatefields", GlobalPermissions::setCandidateFields);
		PERMISSION_SETTERS.put("companyfields", GlobalPermissions::setCompanyFields);
		PERMISSION_SETTERS.put("contactfields", GlobalPermissions::setContactFields);
		PERMISSION_SETTERS.put("dealfields", GlobalPermissions::setDealFields);
		PERMISSION_SETTERS.put("jobfields", GlobalPermissions::setJobFields);
		PERMISSION_SETTERS.put("publicjobpagesettings", GlobalPermissions::setPublicJobPageSettings);
		PERMISSION_SETTERS.put("jobapplicationformsettings", GlobalPermissions::setJobApplicationFormSettings);
		PERMISSION_SETTERS.put("profileupdateformsettings", GlobalPermissions::setProfileUpdateFormSettings);
		PERMISSION_SETTERS.put("talentpoolpagesettings", GlobalPermissions::setTalentPoolPageSettings);
		PERMISSION_SETTERS.put("apiintegrations", GlobalPermissions::setApiIntegrations);
		PERMISSION_SETTERS.put("jobboardintegrations", GlobalPermissions::setJobBoardIntegrations);
		PERMISSION_SETTERS.put("callingintegrations", GlobalPermissions::setCallingIntegrations);
		PERMISSION_SETTERS.put("calltypecustomization", GlobalPermissions::setCallTypeCustomization);
		PERMISSION_SETTERS.put("notetypecustomization", GlobalPermissions::setNoteTypeCustomization);
		PERMISSION_SETTERS.put("meetingtypecustomization", GlobalPermissions::setMeetingTypeCustomization);
		PERMISSION_SETTERS.put("tasktypecustomization", GlobalPermissions::setTaskTypeCustomization);
		PERMISSION_SETTERS.put("standardemailtemplates", GlobalPermissions::setStandardEmailTemplates);
		PERMISSION_SETTERS.put("resumeformatting", GlobalPermissions::setResumeFormatting);
		PERMISSION_SETTERS.put("smstemplates", GlobalPermissions::setSmsTemplates);
		PERMISSION_SETTERS.put("activitytemplates", GlobalPermissions::setActivityTemplates);
		PERMISSION_SETTERS.put("hideemail", GlobalPermissions::setHideEmail);
		PERMISSION_SETTERS.put("blacklistemailid", GlobalPermissions::setBlacklistEmailId);
		PERMISSION_SETTERS.put("iprestriction", GlobalPermissions::setIpRestriction);
		PERMISSION_SETTERS.put("privateemails", GlobalPermissions::setPrivateEmails);
		PERMISSION_SETTERS.put("allconnectedemail", GlobalPermissions::setAllConnectedEmail);
		PERMISSION_SETTERS.put("customxmlsettings", GlobalPermissions::setCustomXmlSettings);
		PERMISSION_SETTERS.put("advancedanalytics", GlobalPermissions::setAdvancedAnalytics);
		PERMISSION_SETTERS.put("allowtochooseownfields", GlobalPermissions::setAllowToChooseOwnFields);
		PERMISSION_SETTERS.put("recordcallsbydefault", GlobalPermissions::setRecordCallsByDefault);
		PERMISSION_SETTERS.put("allowtochoosecallrecord", GlobalPermissions::setAllowToChooseCallRecord);
		PERMISSION_SETTERS.put("allowtodownloadrecords", GlobalPermissions::setAllowToDownloadRecords);
		PERMISSION_SETTERS.put("dataenrichment", GlobalPermissions::setDataEnrichment);
		PERMISSION_SETTERS.put("targetreportcreateaccess", GlobalPermissions::setTargetReportCreateAccess);
		PERMISSION_SETTERS.put("targetreport", GlobalPermissions::setTargetReport);
		PERMISSION_SETTERS.put("executivesearchreport", GlobalPermissions::setExecutiveSearchReport);
		PERMISSION_SETTERS.put("sentemailkpireport", GlobalPermissions::setSentEmailKpiReport);
		PERMISSION_SETTERS.put("externaljobboardintegration", GlobalPermissions::setExternalJobBoardIntegration);
		PERMISSION_SETTERS.put("emailsequence", GlobalPermissions::setEmailSequence);
		PERMISSION_SETTERS.put("workflowautomation", GlobalPermissions::setWorkflowAutomation);
		PERMISSION_SETTERS.put("hotlist", GlobalPermissions::setHotlist);
		PERMISSION_SETTERS.put("customizeofflimitstatus", GlobalPermissions::setCustomizeOffLimitStatus);
		PERMISSION_SETTERS.put("jobadvertising", GlobalPermissions::setJobAdvertising);
		PERMISSION_SETTERS.put("timetohirereport", GlobalPermissions::setTimeToHireReport);
		PERMISSION_SETTERS.put("accountoverviewreport", GlobalPermissions::setAccountOverviewReport);
		PERMISSION_SETTERS.put("dealsbyteam", GlobalPermissions::setDealsByTeam);
		PERMISSION_SETTERS.put("pitchcandidatepipeline", GlobalPermissions::setPitchCandidatePipeline);
	}

	public static boolean isPermissionField(String name) {
		return PERMISSION_FIELDS.contains(name);
	}

	public static void setPermissionField(GlobalPermissions permissions, String name, Integer value) {
		BiConsumer<GlobalPermissions, PermissionLevel> setter = PERMISSION_SETTERS.get(name);
		if (setter != null) {
			PermissionLevel level = (value != null && value == 1) ? PermissionLevel.YES : PermissionLevel.NO;
			setter.accept(permissions, level);
		}
	}

}