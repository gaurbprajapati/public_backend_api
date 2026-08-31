package io.recruitcrm.microservice.timesheet.repositories.invoice;

import io.recruitcrm.aws.aurora.annotation.WriterRoute;
import io.recruitcrm.contract_staffing.entity.model.TimesheetInvoice;
import io.recruitcrm.microservice.timesheet.dao.invoice.InvoiceJpaRepository;
import io.recruitcrm.microservice.timesheet.dto.invoice.InvoiceValidationQueryResultDto;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
public class TimesheetInvoiceRepository implements ITimesheetInvoiceRepository {

	private final InvoiceJpaRepository invoiceJpaRepository;

	private final EntityManager entityManager;

	public TimesheetInvoiceRepository(InvoiceJpaRepository invoiceJpaRepository, EntityManager entityManager) {
		this.invoiceJpaRepository = invoiceJpaRepository;
		this.entityManager = entityManager;
	}

	@Override
	public TimesheetInvoice findByTimesheetId(Integer timesheetId, Integer accountId) {
		return this.invoiceJpaRepository.findByTimesheetIdAndAccountId(timesheetId, accountId);
	}

	@Override
	public List<TimesheetInvoice> findByTimesheetIdIn(List<Integer> timesheetIds, Integer accountId) {
		return this.invoiceJpaRepository.findByTimesheetIdIn(timesheetIds, accountId);
	}

	@Override
	@WriterRoute
	@Transactional(propagation = Propagation.MANDATORY)
	public TimesheetInvoice saveInvoice(TimesheetInvoice invoice) {
		return this.invoiceJpaRepository.save(invoice);
	}

	@Override
	public TimesheetInvoice findInvoiceWithStatusHistoryByTimesheetId(Integer timesheetId, Integer accountId) {
		return this.invoiceJpaRepository.findByTimesheetIdAndAccountId(timesheetId, accountId);
	}

	@Override
	public TimesheetInvoice findBillDetailsByTimesheetId(Integer timesheetId, Integer accountId) {
		return this.invoiceJpaRepository.findByTimesheetIdAndAccountId(timesheetId, accountId);
	}

	@Override
	public List<InvoiceValidationQueryResultDto> validateTimesheetsForInvoice(List<Integer> timesheetIds,
			Integer accountId) {
		String jpql = "SELECT new io.recruitcrm.microservice.timesheet.dto.invoice.InvoiceValidationQueryResultDto("
				+ "t.id, " + "(SELECT ta1.timesheetApprovalStatusTypeId FROM TimesheetApproval ta1 "
				+ "WHERE ta1.id = (SELECT MAX(ta2.id) FROM TimesheetApproval ta2 WHERE ta2.timesheetId = t.id)), "
				+ "c.companyName, " + "t.periodStart, " + "t.periodEnd, " + "ts.billCurrencyId, "
				+ "CAST(t.totalBillData AS double), " + "billCurrency.symbol, " + "billCurrency.code, "
				+ "CONCAT(candidate.firstName, ' ', candidate.lastName), " + "candidate.profilePic, "
				+ "candidate.serialNo, " + "c.id, " + "j.id, " + "j.slug, " + "j.contactId, " + "candidate.id, "
				+ "candidate.ownerId, " + "candidate.slug, " + "d.id, " + "acj.id, " + "payCurrency.code, "
				+ "payCurrency.symbol, " + "CAST(ts.isReimbursementEnabled AS integer)) " + "FROM Timesheet t "
				+ "LEFT JOIN TimesheetSetting ts ON t.timesheetSettingId = ts.id "
				+ "LEFT JOIN TimesheetSettingAssociation tsa ON ts.association.id = tsa.id "
				+ "LEFT JOIN Job j ON j.id = tsa.jobId " + "LEFT JOIN Company c ON c.id = j.company.id "
				+ "LEFT JOIN Candidate candidate ON candidate.id = tsa.contractorId "
				+ "LEFT JOIN Currency billCurrency ON billCurrency.id = ts.billCurrencyId "
				+ "LEFT JOIN Currency payCurrency ON payCurrency.id = ts.payCurrencyId "
				+ "LEFT JOIN DealJob dj ON dj.jobId = j.id " + "LEFT JOIN Deal d ON d.id = dj.dealId "
				+ "LEFT JOIN AssignCandidateJob acj ON acj.candidateId = tsa.contractorId AND acj.jobId = tsa.jobId "
				+ "WHERE t.id IN :timesheetIds AND ts.accountId = :accountId " + "ORDER BY t.id";

		return this.entityManager.createQuery(jpql, InvoiceValidationQueryResultDto.class)
			.setParameter("timesheetIds", timesheetIds)
			.setParameter("accountId", accountId)
			.getResultList();
	}

	public Set<Integer> getTimesheetIds(List<Integer> timesheetIds, Integer accountId) {
		String jpql = "SELECT ti.timesheetId FROM TimesheetInvoice ti "
				+ "WHERE ti.timesheetId IN :timesheetIds AND ti.accountId = :accountId AND ti.invoiceId IS NOT NULL";

		List<Integer> resultList = this.entityManager.createQuery(jpql, Integer.class)
			.setParameter("timesheetIds", timesheetIds)
			.setParameter("accountId", accountId)
			.getResultList();

		return new HashSet<>(resultList);
	}

}