package io.recruitcrm.microservice.timesheet.dto.entity_columns;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ColumnAccountViewResponseBodyDto {

	private String field;

	private String label;

	private String longlabel;

	private String entity;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String colId;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Double width;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String pinned;

	private Boolean sortable;

	private Boolean visible;

	@JsonProperty("visible_locked")
	private Boolean visibleLocked;

	private Integer listPageOrder;

	@JsonProperty("allow_on_export")
	private Boolean allowOnExport;

	@JsonProperty("disable_on_export")
	private Boolean disableOnExport;

	private String type;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String sort;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Integer sortIndex;

}
