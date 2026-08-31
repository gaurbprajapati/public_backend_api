package io.recruitcrm.microservice.timesheet.dao.user_role;

import io.recruitcrm.entity.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRoleJpaRepository extends JpaRepository<UserRole, Integer> {

	/**
	 * Find UserRole by accountId and createdBy (userId).
	 * @param accountId The account ID
	 * @param createdBy The user ID who created the record
	 * @return Optional UserRole if found
	 */
	@Query("SELECT ur FROM UserRole ur WHERE ur.accountId = :accountId AND ur.createdBy.id = :createdBy")
	Optional<UserRole> findByAccountIdAndCreatedBy(@Param("accountId") Integer accountId,
			@Param("createdBy") Integer createdBy);

}
