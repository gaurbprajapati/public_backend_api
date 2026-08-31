package io.recruitcrm.microservice.timesheet.configuration;

import io.recruitcrm.microservice.timesheet.exceptions.ConflictException;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import io.sentry.SentryOptions;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Configuration
public class SentryConfiguration {

	@Bean
	public SentryOptions.BeforeSendCallback beforeSendCallback() {
		return (event, hint) -> {
			Throwable throwable = event.getThrowable();
			if (throwable instanceof ResourceNotFoundException || throwable instanceof ValidationErrorException
					|| throwable instanceof UnauthorizedAccessException || throwable instanceof ConflictException
					|| throwable instanceof HttpRequestMethodNotSupportedException
					|| throwable instanceof HttpMessageNotReadableException
					|| throwable instanceof MethodArgumentNotValidException
					|| throwable instanceof NoResourceFoundException
					|| throwable instanceof MethodArgumentTypeMismatchException
					|| throwable instanceof AsyncRequestNotUsableException
					|| throwable instanceof ClientAbortException) {
				return null;
			}
			return event;
		};
	}

}
