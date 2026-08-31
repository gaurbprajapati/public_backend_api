package io.recruitcrm.microservice.timesheet.repositories.extra_fields;

import io.recruitcrm.microservice.search.models.jooq.tables.Tblextrafields;
import io.recruitcrm.microservice.timesheet.dto.extra_fields.ExtraFieldDefinitionDto;
import io.recruitcrm.microservice.timesheet.helpers.enums.EntityType;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Repository implementation for managing extra fields (custom columns) from
 * Tblextrafields table.
 */
@Repository
public class ExtraFieldsRepository implements IExtraFieldsRepository {

	private static final Tblextrafields TEF = Tblextrafields.TBLEXTRAFIELDS;

	private final DSLContext dslContext;

	public ExtraFieldsRepository(DSLContext dslContext) {
		this.dslContext = dslContext;
	}

	@Override
	public Map<Integer, ExtraFieldDefinitionDto> getExtraFieldDefinitions(List<Integer> columnIds,
			EntityType entityType, Integer accountId) {
		var records = this.dslContext
			.select(TEF.COLUMNID, TEF.EXTRAFIELDNAME, TEF.EXTRAFIELDTYPE, TEF.ENTITYTYPEID, TEF.ACCOUNTID)
			.from(TEF)
			.where(TEF.COLUMNID.in(columnIds))
			.and(TEF.ENTITYTYPEID.eq(entityType.getId()))
			.and(TEF.ACCOUNTID.eq(accountId))
			.fetch();

		return records.stream()
			.collect(Collectors.toMap((result) -> result.getValue(TEF.COLUMNID),
					(result) -> new ExtraFieldDefinitionDto(result.getValue(TEF.COLUMNID),
							result.getValue(TEF.EXTRAFIELDNAME), result.getValue(TEF.EXTRAFIELDTYPE),
							result.getValue(TEF.ENTITYTYPEID), result.getValue(TEF.ACCOUNTID))));
	}

	@Override
	public Map<Integer, Boolean> checkExtraFieldsExist(List<Integer> columnIds, EntityType entityType,
			Integer accountId) {
		var existingColumnIds = this.dslContext.select(TEF.COLUMNID)
			.from(TEF)
			.where(TEF.COLUMNID.in(columnIds))
			.and(TEF.ENTITYTYPEID.eq(entityType.getId()))
			.and(TEF.ACCOUNTID.eq(accountId))
			.fetch(TEF.COLUMNID);

		return columnIds.stream().collect(Collectors.toMap((columnId) -> columnId, existingColumnIds::contains));
	}

}
