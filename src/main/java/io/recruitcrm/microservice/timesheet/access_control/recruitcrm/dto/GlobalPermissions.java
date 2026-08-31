package io.recruitcrm.microservice.timesheet.access_control.recruitcrm.dto;

import io.recruitcrm.microservice.timesheet.access_control.recruitcrm.core.PermissionLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GlobalPermissions {

	private PermissionLevel reports;

	private PermissionLevel recruiterPerformanceReport;

	private PermissionLevel candidateLifecycleReport;

	private PermissionLevel clientPerformanceReport;

	private PermissionLevel jobStatisticReport;

	private PermissionLevel dealReport;

	private PermissionLevel exportToCsv;

	private PermissionLevel bulkUpdateField;

	private PermissionLevel bulkDelete;

	private PermissionLevel adminSettings;

	private PermissionLevel plansAndBilling;

	private PermissionLevel userManagement;

	private PermissionLevel rolesPermissions;

	private PermissionLevel teams;

	private PermissionLevel accountManagement;

	private PermissionLevel emailTriggers;

	private PermissionLevel salesPipeline;

	private PermissionLevel hiringPipeline;

	private PermissionLevel dealsPipeline;

	private PermissionLevel jobStatus;

	private PermissionLevel customizeInvoice;

	private PermissionLevel fieldsSharedWithClient;

	private PermissionLevel candidateFields;

	private PermissionLevel companyFields;

	private PermissionLevel contactFields;

	private PermissionLevel dealFields;

	private PermissionLevel jobFields;

	private PermissionLevel publicJobPageSettings;

	private PermissionLevel jobApplicationFormSettings;

	private PermissionLevel profileUpdateFormSettings;

	private PermissionLevel talentPoolPageSettings;

	private PermissionLevel apiIntegrations;

	private PermissionLevel jobBoardIntegrations;

	private PermissionLevel callingIntegrations;

	private PermissionLevel callTypeCustomization;

	private PermissionLevel noteTypeCustomization;

	private PermissionLevel meetingTypeCustomization;

	private PermissionLevel taskTypeCustomization;

	private PermissionLevel standardEmailTemplates;

	private PermissionLevel resumeFormatting;

	private PermissionLevel smsTemplates;

	private PermissionLevel activityTemplates;

	private PermissionLevel hideEmail;

	private PermissionLevel blacklistEmailId;

	private PermissionLevel ipRestriction;

	private PermissionLevel privateEmails;

	private PermissionLevel allConnectedEmail;

	private PermissionLevel customXmlSettings;

	private PermissionLevel advancedAnalytics;

	private PermissionLevel allowToChooseOwnFields;

	private PermissionLevel recordCallsByDefault;

	private PermissionLevel allowToChooseCallRecord;

	private PermissionLevel allowToDownloadRecords;

	private PermissionLevel dataEnrichment;

	private PermissionLevel targetReportCreateAccess;

	private PermissionLevel targetReport;

	private PermissionLevel executiveSearchReport;

	private PermissionLevel sentEmailKpiReport;

	private PermissionLevel externalJobBoardIntegration;

	private PermissionLevel emailSequence;

	private PermissionLevel workflowAutomation;

	private PermissionLevel hotlist;

	private PermissionLevel customizeOffLimitStatus;

	private PermissionLevel jobAdvertising;

	private PermissionLevel timeToHireReport;

	private PermissionLevel accountOverviewReport;

	private PermissionLevel dealsByTeam;

	private PermissionLevel pitchCandidatePipeline;

}