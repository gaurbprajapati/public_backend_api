package io.recruitcrm.microservice.timesheet.repositories.contact;

import io.recruitcrm.microservice.timesheet.dto.contact.ContactNamePhotoQueryResultDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class ContactRepository {

	private final EntityManager entityManager;

	public ContactRepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public Map<Integer, ContactNamePhotoQueryResultDto> getContactNamePhotoMap(Set<Integer> ids) {
		String jpql = "SELECT c.id, new io.recruitcrm.microservice.timesheet.dto.contact.ContactNamePhotoQueryResultDto("
				+ "CASE WHEN c.lastName IS NULL OR c.lastName = '' THEN c.firstName ELSE CONCAT(c.firstName, ' ', c.lastName) END, c.photo, c.email) "
				+ "FROM Contact c WHERE c.id IN :ids";
		TypedQuery<Object[]> query = this.entityManager.createQuery(jpql, Object[].class);
		query.setParameter("ids", ids);

		// Transform the result into a Map<Integer, ContactNamePhotoDto>
		return query.getResultList()
			.stream()
			.collect(Collectors.toMap((result) -> (Integer) result[0],
					(result) -> (ContactNamePhotoQueryResultDto) result[1]));
	}

}
