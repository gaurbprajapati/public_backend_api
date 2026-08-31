package io.recruitcrm.microservice.timesheet.dao.team_member;

import io.recruitcrm.entity.model.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamMemberJpaRepository extends JpaRepository<TeamMember, Integer> {

	/**
	 * Gets all user IDs that are in the same team(s) as the given user.
	 * @param userId The user ID to get team members for
	 * @return List of user IDs including the user and all team members
	 */
	@Query("SELECT DISTINCT tm.userId FROM TeamMember tm WHERE tm.teamId IN ("
			+ "SELECT t.teamId FROM TeamMember t WHERE t.userId = :userId)")
	List<Integer> findAllUserIdsByTeamMembership(@Param("userId") Integer userId);

	/**
	 * Gets all team IDs that the given user is a member of.
	 * @param userId The user ID to get team IDs for
	 * @return List of team IDs
	 */
	@Query("SELECT DISTINCT tm.teamId FROM TeamMember tm WHERE tm.userId = :userId")
	List<Integer> findAllTeamIdsByUserId(@Param("userId") Integer userId);

}
