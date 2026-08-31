package io.recruitcrm.microservice.timesheet.dto.contractor;

import io.recruitcrm.microservice.timesheet.dto.timesheet.ContractorQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.DealResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.timesheet.JobResultBodyDto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractorListResponseBodyDto {

	private Integer id;

	private String name;

	private ContractorQueryResultDto contractor;

	private List<JobResultBodyDto> assignedJobs;

	private List<DealResponseBodyDto> deals;

	private String email;

	private String phone;

	private String country;

	private ContractorOwnerResponseBodyDto owner;

	private Integer addedOn;

	private ContractorAddedByResponseBodyDto addedBy;

	private Integer updatedOn;

	private ContractorUpdatedByResponseBodyDto updatedBy;

	private Integer status;

	private String title;

	private String city;

	private String state;

	private String postalCode;

	private String locality;

	private String fullAddress;

	private Integer birthDate;

	private String currentOrganization;

	private String skills;

	private String profilefacebook;

	private String profilegithub;

	private String profilelinkedin;

	private String profiletwitter;

	private String profilexing;

	private String source;

	private String gender;

}
