package io.recruitcrm.microservice.timesheet.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
public class APINormalResponse<T> {

	protected APIResponseKeyMeta meta;

	protected T data;

	public APINormalResponse() {
		this(new APIResponseKeyMeta(), null);
	}

	public APINormalResponse(T data) {
		this(new APIResponseKeyMeta(), data);
	}

	public APINormalResponse(APIResponseKeyMeta meta) {
		this(meta, null);
	}

	public APINormalResponse(T data, APIResponseKeyMeta meta) {
		this.meta = meta;
		this.data = data;
	}

	public APINormalResponse(T data, String message) {
		this(new APIResponseKeyMeta(message), data);
	}

}
