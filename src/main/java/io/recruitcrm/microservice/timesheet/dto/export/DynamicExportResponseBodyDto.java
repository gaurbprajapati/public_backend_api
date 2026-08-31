package io.recruitcrm.microservice.timesheet.dto.export;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dynamic export data container that can hold any number of fields without requiring
 * fixed DTOs. Uses Map&lt;String, Object&gt; for flexibility.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DynamicExportResponseBodyDto {

	private Map<String, Object> data;

	private List<String> columnOrder;

	/**
	 * Get value for a specific field
	 */
	public Object getValue(String fieldName) {
		return this.data.get(fieldName);
	}

	/**
	 * Get all values in the order specified by columnOrder
	 */
	public List<Object> getValuesInOrder() {
		return this.columnOrder.stream().map(this.data::get).toList();
	}

	/**
	 * Get all values in the order specified by columnOrder, excluding the internal
	 * 'timesheet' field
	 */
	public List<Object> getValuesInOrderExcludingTimesheet() {
		return this.columnOrder.stream()
			.filter((columnName) -> !"timesheet".equals(columnName))
			.map(this.data::get)
			.toList();
	}

	/**
	 * Get all data as a map
	 */
	public Map<String, Object> getAllData() {
		return new HashMap<>(this.data);
	}

	/**
	 * Add a field value
	 */
	public void addValue(String fieldName, Object value) {
		if (this.data == null) {
			this.data = new HashMap<>();
		}
		this.data.put(fieldName, value);
	}

	/**
	 * Check if field exists
	 */
	public boolean hasField(String fieldName) {
		return this.data != null && this.data.containsKey(fieldName);
	}

}