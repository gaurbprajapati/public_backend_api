package io.recruitcrm.microservice.timesheet.dto;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestParam;

public class PaginationRequestBodyDto {

	private static final Integer DEFAULT_PAGE_NO = 0;

	private static final Integer DEFAULT_PAGE_SIZE = 100;

	private Integer page;

	private Integer size;

	public PaginationRequestBodyDto(@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "size", required = false) Integer size) {
		this.page = page;
		this.size = size;
	}

	public Pageable toPageable() {
		if (this.page == null || this.page <= DEFAULT_PAGE_NO) {
			this.page = 0;
		}
		else {
			this.page = this.page - 1;
		}
		if (this.size == null || this.size > DEFAULT_PAGE_SIZE || this.size <= 0) {
			this.size = DEFAULT_PAGE_SIZE;
		}
		return PageRequest.of(this.page, this.size);
	}

}
