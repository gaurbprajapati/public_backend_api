/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Generated;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "candidates", "contacts", "jobs", "companies", "deals", "placementbilling", "taskmeetings",
		"notes", "calllog", "files", "emailtemplates", "invoices", "globalPermissions", "reports",
		"recruiterperformancereportaccess", "recruiterperformancereport", "candidatelifecyclereport",
		"clientperformancereport", "jobstatisticreport", "dealreport", "exporttocsv", "bulkupdatefield", "bulkdelete",
		"adminsettings", "plansandbilling", "usermanagement", "rolespermissions", "teams", "accountmanagement",
		"emailtriggers", "salespipeline", "hiringpipeline", "dealspipeline", "jobstatus", "customizeinvoice",
		"fieldssharedwithclient", "candidatefields", "companyfields", "contactfields", "dealfields", "jobfields",
		"publicjobpagesettings", "jobapplicationformsettings", "profileupdateformsettings", "talentpoolpagesettings",
		"apiintegrations", "jobboardintegrations", "callingintegrations", "calltypecustomization",
		"notetypecustomization", "pitchcandidatepipeline", "standardemailtemplates", "resumeformatting", "hideemail",
		"blacklistemailid", "smstemplates", "iprestriction", "timetohirereport", "accountoverviewreport", "dealsbyteam",
		"executivesearchreport", "externaljobboardintegration", "emailsequence", "meetingtypecustomization",
		"customxmlsettings", "activitytemplates", "workflow_automation", "sentemailkpireport", "advancedAnalytics",
		"allowToChooseOwnFields", "recordCallsByDefault", "allowToChooseCallRecord", "allowToDownloadRecords",
		"tasktypecustomization", "targetreportaccess", "targetreport", "privateemails", "allconnectedemail",
		"dataEnrichmentReportAccess", "dataEnrichment", "targetreportcreateaccess", "auditlog", "hotlist",
		"customizeOffLimitStatus", "jobAdvertising" })
@Generated("jsonschema2pojo")
public class AccessControlDto {

	@JsonProperty("candidates")
	@Valid
	private Candidates candidates;

	@JsonProperty("contacts")
	@Valid
	private Contacts contacts;

	@JsonProperty("jobs")
	@Valid
	private Jobs jobs;

	@JsonProperty("companies")
	@Valid
	private Companies companies;

	@JsonProperty("deals")
	@Valid
	private Deals deals;

	@JsonProperty("placementbilling")
	@Valid
	private PlacementBilling placementBilling;

	@JsonProperty("taskmeetings")
	@Valid
	private TaskMeetings taskMeetings;

	@JsonProperty("notes")
	@Valid
	private Notes notes;

	@JsonProperty("calllog")
	@Valid
	private CallLog callLog;

	@JsonProperty("files")
	@Valid
	private Files files;

	@JsonProperty("emailtemplates")
	@Valid
	private EmailTemplates emailTemplates;

	@JsonProperty("invoices")
	@Valid
	private Invoices invoices;

	@JsonProperty("globalPermissions")
	@Valid
	private GlobalPermissions globalPermissions = new GlobalPermissions();

	@JsonProperty("reports")
	private Integer reports;

	@JsonProperty("recruiterperformancereportaccess")
	private String recruiterPerformanceReportAccess;

	@JsonProperty("recruiterperformancereport")
	private Integer recruiterPerformanceReport;

	@JsonProperty("candidatelifecyclereport")
	private Integer candidateLifecycleReport;

	@JsonProperty("clientperformancereport")
	private Integer clientPerformanceReport;

	@JsonProperty("jobstatisticreport")
	private Integer jobStatisticReport;

	@JsonProperty("dealreport")
	private Integer dealReport;

	@JsonProperty("exporttocsv")
	private Integer exportToCsv;

	@JsonProperty("bulkupdatefield")
	private Integer bulkUpdateField;

	@JsonProperty("bulkdelete")
	private Integer bulkDelete;

	@JsonProperty("adminsettings")
	private Integer adminSettings;

	@JsonProperty("plansandbilling")
	private Integer plansAndBilling;

	@JsonProperty("usermanagement")
	private Integer userManagement;

	@JsonProperty("rolespermissions")
	private Integer rolesPermissions;

	@JsonProperty("teams")
	private Integer teams;

	@JsonProperty("accountmanagement")
	private Integer accountManagement;

	@JsonProperty("emailtriggers")
	private Integer emailTriggers;

	@JsonProperty("salespipeline")
	private Integer salesPipeline;

	@JsonProperty("hiringpipeline")
	private Integer hiringPipeline;

	@JsonProperty("dealspipeline")
	private Integer dealsPipeline;

	@JsonProperty("jobstatus")
	private Integer jobStatus;

	@JsonProperty("customizeinvoice")
	private Integer customizeInvoice;

	@JsonProperty("fieldssharedwithclient")
	private Integer fieldsSharedWithClient;

	@JsonProperty("candidatefields")
	private Integer candidateFields;

	@JsonProperty("companyfields")
	private Integer companyFields;

	@JsonProperty("contactfields")
	private Integer contactFields;

	@JsonProperty("dealfields")
	private Integer dealFields;

	@JsonProperty("jobfields")
	private Integer jobFields;

	@JsonProperty("publicjobpagesettings")
	private Integer publicJobPageSettings;

	@JsonProperty("jobapplicationformsettings")
	private Integer jobApplicationFormSettings;

	@JsonProperty("profileupdateformsettings")
	private Integer profileUpdateFormSettings;

	@JsonProperty("talentpoolpagesettings")
	private Integer talentPoolPageSettings;

	@JsonProperty("apiintegrations")
	private Integer apiIntegrations;

	@JsonProperty("jobboardintegrations")
	private Integer jobBoardIntegrations;

	@JsonProperty("callingintegrations")
	private Integer callingIntegrations;

	@JsonProperty("calltypecustomization")
	private Integer callTypeCustomization;

	@JsonProperty("notetypecustomization")
	private Integer noteTypeCustomization;

	@JsonProperty("pitchcandidatepipeline")
	private Integer pitchCandidatePipeline;

	@JsonProperty("standardemailtemplates")
	private Integer standardEmailTemplates;

	@JsonProperty("resumeformatting")
	private Integer resumeFormatting;

	@JsonProperty("hideemail")
	private Integer hideEmail;

	@JsonProperty("blacklistemailid")
	private Integer blacklistEmailId;

	@JsonProperty("smstemplates")
	private Integer smsTemplates;

	@JsonProperty("iprestriction")
	private Integer ipRestriction;

	@JsonProperty("timetohirereport")
	private Integer timeToHireReport;

	@JsonProperty("accountoverviewreport")
	private Integer accountOverviewReport;

	@JsonProperty("dealsbyteam")
	private Integer dealsByTeam;

	@JsonProperty("executivesearchreport")
	private Integer executiveSearchReport;

	@JsonProperty("externaljobboardintegration")
	private Integer externalJobBoardIntegration;

	@JsonProperty("emailsequence")
	private Integer emailSequence;

	@JsonProperty("meetingtypecustomization")
	private Integer meetingTypeCustomization;

	@JsonProperty("customxmlsettings")
	private Integer customXmlSettings;

	@JsonProperty("activitytemplates")
	private Integer activityTemplates;

	@JsonProperty("workflow_automation")
	private Integer workflowAutomation;

	@JsonProperty("sentemailkpireport")
	private Integer sentEmailKpiReport;

	@JsonProperty("advancedAnalytics")
	private Integer advancedAnalytics;

	@JsonProperty("allowToChooseOwnFields")
	private Integer allowToChooseOwnFields;

	@JsonProperty("recordCallsByDefault")
	private Integer recordCallsByDefault;

	@JsonProperty("allowToChooseCallRecord")
	private Integer allowToChooseCallRecord;

	@JsonProperty("allowToDownloadRecords")
	private Integer allowToDownloadRecords;

	@JsonProperty("tasktypecustomization")
	private Integer taskTypeCustomization;

	@JsonProperty("targetreportaccess")
	private String targetReportAccess;

	@JsonProperty("targetreport")
	private Integer targetReport;

	@JsonProperty("privateemails")
	private Integer privateEmails;

	@JsonProperty("allconnectedemail")
	private Integer allConnectedEmail;

	@JsonProperty("dataEnrichmentReportAccess")
	private String dataEnrichmentReportAccess;

	@JsonProperty("dataEnrichment")
	private Integer dataEnrichment;

	@JsonProperty("targetreportcreateaccess")
	private Integer targetReportCreateAccess;

	@JsonProperty("auditlog")
	private Integer auditLog;

	@JsonProperty("hotlist")
	private Integer hotList;

	@JsonProperty("customizeOffLimitStatus")
	private Integer customizeOffLimitStatus;

	@JsonProperty("jobAdvertising")
	private Integer jobAdvertising;

	@JsonIgnore
	@Valid
	private final Map<String, Object> additionalProperties = new LinkedHashMap<>();

	@PostConstruct
	public void initializeGlobalPermissions() {
		if (this.globalPermissions == null) {
			this.globalPermissions = new GlobalPermissions();
		}

		// Process all permission fields
		processIntegerField("exporttocsv", this.exportToCsv);
		processIntegerField("bulkupdatefield", this.bulkUpdateField);
		processIntegerField("bulkdelete", this.bulkDelete);
		processIntegerField("adminsettings", this.adminSettings);
		processIntegerField("plansandbilling", this.plansAndBilling);
		processIntegerField("usermanagement", this.userManagement);
		processIntegerField("rolespermissions", this.rolesPermissions);
		processIntegerField("teams", this.teams);
		processIntegerField("accountmanagement", this.accountManagement);
		processIntegerField("emailtriggers", this.emailTriggers);
		processIntegerField("salespipeline", this.salesPipeline);
		processIntegerField("hiringpipeline", this.hiringPipeline);
		processIntegerField("dealspipeline", this.dealsPipeline);
		processIntegerField("jobstatus", this.jobStatus);
		processIntegerField("customizeinvoice", this.customizeInvoice);
		processIntegerField("fieldssharedwithclient", this.fieldsSharedWithClient);
		processIntegerField("candidatefields", this.candidateFields);
		processIntegerField("companyfields", this.companyFields);
		processIntegerField("contactfields", this.contactFields);
		processIntegerField("dealfields", this.dealFields);
		processIntegerField("jobfields", this.jobFields);
		processIntegerField("publicjobpagesettings", this.publicJobPageSettings);
		processIntegerField("jobapplicationformsettings", this.jobApplicationFormSettings);
		processIntegerField("profileupdateformsettings", this.profileUpdateFormSettings);
		processIntegerField("talentpoolpagesettings", this.talentPoolPageSettings);
		processIntegerField("apiintegrations", this.apiIntegrations);
		processIntegerField("jobboardintegrations", this.jobBoardIntegrations);
		processIntegerField("callingintegrations", this.callingIntegrations);
		processIntegerField("calltypecustomization", this.callTypeCustomization);
		processIntegerField("notetypecustomization", this.noteTypeCustomization);
		processIntegerField("meetingtypecustomization", this.meetingTypeCustomization);
		processIntegerField("tasktypecustomization", this.taskTypeCustomization);
		processIntegerField("standardemailtemplates", this.standardEmailTemplates);
		processIntegerField("resumeformatting", this.resumeFormatting);
		processIntegerField("smstemplates", this.smsTemplates);
		processIntegerField("activitytemplates", this.activityTemplates);
		processIntegerField("hideemail", this.hideEmail);
		processIntegerField("blacklistemailid", this.blacklistEmailId);
		processIntegerField("iprestriction", this.ipRestriction);
		processIntegerField("privateemails", this.privateEmails);
		processIntegerField("allconnectedemail", this.allConnectedEmail);
		processIntegerField("customxmlsettings", this.customXmlSettings);
		processIntegerField("advancedanalytics", this.advancedAnalytics);
		processIntegerField("allowtochooseownfields", this.allowToChooseOwnFields);
		processIntegerField("recordcallsbydefault", this.recordCallsByDefault);
		processIntegerField("allowtochoosecallrecord", this.allowToChooseCallRecord);
		processIntegerField("allowtodownloadrecords", this.allowToDownloadRecords);
		processIntegerField("dataenrichment", this.dataEnrichment);
		processIntegerField("targetreportcreateaccess", this.targetReportCreateAccess);
		processIntegerField("targetreport", this.targetReport);
		processIntegerField("executivesearchreport", this.executiveSearchReport);
		processIntegerField("sentemailkpireport", this.sentEmailKpiReport);
		processIntegerField("externaljobboardintegration", this.externalJobBoardIntegration);
		processIntegerField("workflowautomation", this.workflowAutomation);
		processIntegerField("hotlist", this.hotList);
		processIntegerField("customizeofflimitstatus", this.customizeOffLimitStatus);
		processIntegerField("jobadvertising", this.jobAdvertising);
		processIntegerField("timetohirereport", this.timeToHireReport);
		processIntegerField("accountoverviewreport", this.accountOverviewReport);
		processIntegerField("dealsbyteam", this.dealsByTeam);
		processIntegerField("pitchcandidatepipeline", this.pitchCandidatePipeline);
		processIntegerField("emailsequence", this.emailSequence);

		// Handle String fields
		processStringField("recruiterperformancereportaccess", this.recruiterPerformanceReportAccess);
		processStringField("targetreportaccess", this.targetReportAccess);
		processStringField("dataEnrichmentReportAccess", this.dataEnrichmentReportAccess);
	}

	private void processIntegerField(String name, Integer value) {
		if (value != null) {
			GlobalPermissionFieldHandler.setPermissionField(this.globalPermissions, name, value);
		}
	}

	private void processStringField(String name, String value) {
		if (value != null && SpecialPermissionFieldHandler.isSpecialPermissionField(name)) {
			try {
				int intValue = SpecialPermissionFieldHandler.processSpecialPermissionValue(value);
				GlobalPermissionFieldHandler.setPermissionField(this.globalPermissions, name, intValue);
			}
			catch (IllegalArgumentException ex) {
				// Log the error but don't throw - this allows the application to continue
				// even if there's an invalid permission value
				System.err.println("Error processing special permission field " + name + ": " + ex.getMessage());
			}
		}
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		// Handle special permission fields
		if (SpecialPermissionFieldHandler.isSpecialPermissionField(name)) {
			if (this.globalPermissions == null) {
				this.globalPermissions = new GlobalPermissions();
			}
			try {
				int intValue = SpecialPermissionFieldHandler.processSpecialPermissionValue(value);
				GlobalPermissionFieldHandler.setPermissionField(this.globalPermissions, name, intValue);
			}
			catch (IllegalArgumentException ex) {
				System.err.println("Error processing special permission field " + name + ": " + ex.getMessage());
			}
			return;
		}

		// Handle regular permission fields
		if (value instanceof Integer intValue && GlobalPermissionFieldHandler.isPermissionField(name)) {
			if (this.globalPermissions == null) {
				this.globalPermissions = new GlobalPermissions();
			}
			GlobalPermissionFieldHandler.setPermissionField(this.globalPermissions, name, intValue);
			return;
		}

		// Store any other fields in additionalProperties
		this.additionalProperties.put(name, value);
	}

}
