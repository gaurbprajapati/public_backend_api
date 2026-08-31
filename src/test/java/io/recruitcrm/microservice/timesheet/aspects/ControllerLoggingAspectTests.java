package io.recruitcrm.microservice.timesheet.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@DisplayName("ControllerLoggingAspect Tests")
class ControllerLoggingAspectTests {

	@Test
	@DisplayName("logEndpointEntry resolves the target logger and logs the method entry")
	void testLogEndpointEntryLogsMethodEntry() {
		ControllerLoggingAspect aspect = new ControllerLoggingAspect();
		JoinPoint joinPoint = mock(JoinPoint.class);
		Signature signature = mock(Signature.class);
		given(joinPoint.getTarget()).willReturn(new SampleController());
		given(joinPoint.getSignature()).willReturn(signature);
		given(signature.getName()).willReturn("sampleEndpoint");

		assertThatCode(() -> aspect.logEndpointEntry(joinPoint)).doesNotThrowAnyException();

		then(joinPoint).should().getTarget();
		then(signature).should().getName();
	}

	private static final class SampleController {

	}

}
