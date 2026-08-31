package io.recruitcrm.microservice.timesheet.repositories.user;

import io.recruitcrm.microservice.timesheet.dto.user.UserDetailsQueryResultDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class UserRepository {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserRepository.class);

	private final EntityManager entityManager;

	public UserRepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public Map<Integer, UserDetailsQueryResultDto> getUserDetailsMap(Set<Integer> ids) {
		String jpql = "SELECT u.id, new io.recruitcrm.microservice.timesheet.dto.user.UserDetailsQueryResultDto("
				+ "CASE WHEN u.lastname IS NULL OR u.lastname = '' THEN u.firstname ELSE CONCAT(u.firstname, ' ', u.lastname) END, u.photo) "
				+ "FROM User u WHERE u.id IN :ids";
		TypedQuery<Object[]> query = this.entityManager.createQuery(jpql, Object[].class);
		query.setParameter("ids", ids);

		// Transform the result into a Map<Integer, UserDetailsDto>
		return query.getResultList()
			.stream()
			.collect(Collectors.toMap((result) -> (Integer) result[0],
					(result) -> (UserDetailsQueryResultDto) result[1]));
	}

	public UserDetailsQueryResultDto getUserDetails(Integer id) {
		String jpql = "SELECT new io.recruitcrm.microservice.timesheet.dto.user.UserDetailsQueryResultDto("
				+ "CASE WHEN u.lastname IS NULL OR u.lastname = '' THEN u.firstname ELSE CONCAT(u.firstname, ' ', u.lastname) END, u.photo) "
				+ "FROM User u WHERE u.id = :id";
		TypedQuery<UserDetailsQueryResultDto> query = this.entityManager.createQuery(jpql,
				UserDetailsQueryResultDto.class);
		query.setParameter("id", id);

		try {
			return query.getSingleResult();
		}
		catch (NoResultException ex) {
			return null;
		}
	}

	/**
	 * Retrieves the GMT difference (timezone offset) for a user by their ID. Queries the
	 * UserDetails table and joins with Timezone table to get the timezone string.
	 * @param userId The ID of the user
	 * @return The GMT difference string (e.g., "+05:30", "-08:00") or null if not found
	 */
	public String getGMTDifferenceByUserId(Integer userId) {
		String sql = "SELECT tz.timezone FROM tbluserdetails ud " + "LEFT JOIN tbltimezone tz ON tz.id = ud.timezone "
				+ "WHERE ud.userid = :userId";
		Query query = this.entityManager.createNativeQuery(sql);
		query.setParameter("userId", userId);

		try {
			Object result = query.getSingleResult();
			return (result != null) ? result.toString() : null;
		}
		catch (NoResultException ex) {
			return null;
		}
	}

	/**
	 * Retrieves the time format type for a user by their ID. Queries tbluserdetails.
	 * @param userId The ID of the user
	 * @return 0 = 12-hour (AM/PM), 1 = 24-hour, null if not found or column missing
	 */
	public Integer getTimeFormatTypeByUserId(Integer userId) {
		if (userId == null) {
			return null;
		}
		String sql = "SELECT ud.time_format_type FROM tbluserdetails ud WHERE ud.userid = :userId";
		Query query = this.entityManager.createNativeQuery(sql);
		query.setParameter("userId", userId);

		try {
			Object result = query.getSingleResult();
			if (result instanceof Number num) {
				return num.intValue();
			}
			return (result != null) ? Integer.valueOf(result.toString()) : null;
		}
		catch (Exception ex) {
			StringWriter sw = new StringWriter();
			ex.printStackTrace(new PrintWriter(sw));
			LOGGER.error("exception occurred inside getTimeFormatTypeByUserId() method :: {}", sw);
			return null;
		}
	}

}
