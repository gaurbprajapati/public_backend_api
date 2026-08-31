package io.recruitcrm.microservice.timesheet.dao.contact;

import io.recruitcrm.entity.model.Contact;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactJpaRepository extends JpaRepository<Contact, Integer> {

	Contact getByIdAndAccountId(Integer id, Integer accountId);

	@EntityGraph(attributePaths = { "account", "owner", "createdBy", "updatedBy" })
	Optional<Contact> findByEmailAndAccountId(String email, Integer accountId);

	List<Contact> findAllByEmailAndAccountIdAndDeleted(String email, Integer accountId, Boolean deleted);

	List<Contact> findAllByEmailAndAccountId(String email, Integer accountId);

	@EntityGraph(attributePaths = { "account", "owner", "createdBy", "updatedBy" })
	Optional<Contact> findByIdAndAccountId(Integer id, Integer accountId);

	@EntityGraph(attributePaths = { "account", "owner", "createdBy", "updatedBy" })
	@Override
	Optional<Contact> findById(Integer id);

}
