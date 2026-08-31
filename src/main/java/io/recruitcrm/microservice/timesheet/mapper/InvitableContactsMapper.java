package io.recruitcrm.microservice.timesheet.mapper;

import io.recruitcrm.microservice.timesheet.dto.portal.client.InvitableContactQueryResultDto;
import io.recruitcrm.microservice.timesheet.dto.portal.client.InvitableContactResponseBodyDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InvitableContactsMapper {

	public List<InvitableContactResponseBodyDto> mapToResponseDtos(List<InvitableContactQueryResultDto> queryResults) {
		return queryResults.stream().map(this::mapToResponseDto).toList();
	}

	private InvitableContactResponseBodyDto mapToResponseDto(InvitableContactQueryResultDto queryResult) {
		InvitableContactResponseBodyDto dto = new InvitableContactResponseBodyDto();
		dto.setId(queryResult.getId());
		dto.setFirstName(queryResult.getFirstName());
		dto.setLastName(queryResult.getLastName());
		dto.setEmail(queryResult.getEmail());
		dto.setPortalStatusId(queryResult.getPortalStatusId());
		dto.setPhoto(queryResult.getPhoto());
		dto.setSrno(queryResult.getSrno());
		dto.setCompanyName(queryResult.getCompanyName());
		// canView / canEdit / canDelete are set by InvitableContactsService after mapping
		return dto;
	}

}
