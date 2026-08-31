package io.recruitcrm.microservice.timesheet.dto.contractor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractorQueryResultDto {

	private Integer id;

	private String name;

	private String photo;

	private String slug;

	private String position;

	private String email;

	private String phone;

	private String country;

	private Integer ownerId;

	private String ownerName;

	private Integer addedOn;

	private Integer addedById;

	private String addedByName;

	private Integer updatedOn;

	private Integer updatedById;

	private String updatedByName;

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

	private Integer contractorOffLimitStatusId;

	private String contractorOffLimitReason;

	private Integer contractorOffLimitEndDate;

	private Integer contractorOffLimitStartDate;

	private String contractorStatusLabel;

	private String contractorBackgroundColorHex;

	private String contractorTextColorHex;

	private String contractorMarkedByName;

}
