package io.recruitcrm.microservice.timesheet.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Centralised "before advice" that logs entry into controller endpoint methods. The log
 * line is emitted under the target controller's own logger name so existing log
 * filtering/attribution continues to work.
 */
@Aspect
@Component
public class ControllerLoggingAspect {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerLoggingAspect.class);

	@Before("@within(org.springframework.web.bind.annotation.RestController)")
	public void logEndpointEntry(JoinPoint joinPoint) {
		LOGGER.info("inside {}() method :: start", joinPoint.getSignature().getName());
	}

}
