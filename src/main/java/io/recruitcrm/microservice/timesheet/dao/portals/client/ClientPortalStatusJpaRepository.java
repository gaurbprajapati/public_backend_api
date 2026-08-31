/*
 * Copyright (c) 2026. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.dao.portals.client;

import io.recruitcrm.contract_staffing.entity.model.ClientPortalStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link ClientPortalStatus}.
 */
@Repository
public interface ClientPortalStatusJpaRepository extends JpaRepository<ClientPortalStatus, Integer> {

	Optional<ClientPortalStatus> findByVmsUserEmailAndAccountId(String vmsUserEmail, Integer accountId);

	boolean existsByVmsUserEmailAndAccountIdNot(String vmsUserEmail, Integer accountId);

	List<ClientPortalStatus> findByVmsUserEmailInAndAccountId(Collection<String> vmsUserEmails, Integer accountId);

	@Query("""
			SELECT c.vmsUserEmail
			FROM ClientPortalStatus c
			WHERE c.vmsUserEmail IN :emails
			  AND c.portalStatusId = :portalStatusId
			  AND c.accountId <> :accountId
			""")
	List<String> findVmsUserEmailsPortalEnabledUnderDifferentAccount(@Param("emails") Collection<String> emails,
			@Param("portalStatusId") Integer portalStatusId, @Param("accountId") Integer accountId);

}
