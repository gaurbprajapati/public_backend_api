package io.recruitcrm.microservice.timesheet.search.filters;

import org.jooq.Condition;
import org.jooq.Table;

import io.recruitcrm.microservice.timesheet.search.constants.TableJoinType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TableJoinSpecification {

	private TableJoinType joinType;

	private Table<?> joinTable;

	private Condition joinCondition;

}
