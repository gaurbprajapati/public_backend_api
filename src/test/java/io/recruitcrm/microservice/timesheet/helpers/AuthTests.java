package io.recruitcrm.microservice.timesheet.helpers;

import io.recruitcrm.entity.model.Account;
import io.recruitcrm.entity.model.User;
import io.recruitcrm.microservice.timesheet.exceptions.ResourceNotFoundException;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;

class AuthTests {

	@Mock
	private User userMock;

	@Mock
	private Auth auth;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		this.auth = new Auth();
	}

	@Test
	@DisplayName("Set user when user is not null")
	void setUserWhenUserIsNotNull() {
		// given
		Account account = mock(Account.class);
		Mockito.when(this.userMock.getAccount()).thenReturn(account);
		Mockito.when(account.getId()).thenReturn(1);
		Mockito.when(this.userMock.getId()).thenReturn(2);
		this.auth.setUser(this.userMock);

		// when
		User resultUser = this.auth.getUser();
		Integer resultAccountId = this.auth.getAccountId();
		Integer resultUserId = this.auth.getUserId();

		// then
		assertThat(resultUser).isEqualTo(this.userMock);
		assertThat(resultAccountId).isEqualTo(1);
		assertThat(resultUserId).isEqualTo(2);
	}

	@Test
	@DisplayName("Set user when user is null")
	void setUserWhenUserIsNull() {
		// given & when
		this.auth.setUser(null);

		// then
		Throwable userException = catchThrowable(() -> this.auth.getUser());
		assertThat(userException).isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("User not found.");

		Throwable accountIdException = catchThrowable(() -> this.auth.getAccountId());
		assertThat(accountIdException).isInstanceOf(UnauthorizedAccessException.class);

		Throwable userIdException = catchThrowable(() -> this.auth.getUserId());
		assertThat(userIdException).isInstanceOf(ResourceNotFoundException.class)
			.hasMessageContaining("User not found.");
	}

	@Test
	@DisplayName("getUserOrThrow returns user when user is not null")
	void getUserOrThrowReturnsUserWhenUserIsNotNull() {
		// given
		Account account = mock(Account.class);
		Mockito.when(this.userMock.getAccount()).thenReturn(account);
		Mockito.when(account.getId()).thenReturn(1);
		this.auth.setUser(this.userMock);

		// when
		User resultUser = this.auth.getUserOrThrow();

		// then
		assertThat(resultUser).isEqualTo(this.userMock);
	}

	@Test
	@DisplayName("getUserOrThrow throws UnauthorizedAccessException when user is null")
	void getUserOrThrowThrowsUnauthorizedAccessExceptionWhenUserIsNull() {
		// given & when
		this.auth.setUser(null);

		// then
		Throwable exception = catchThrowable(() -> this.auth.getUserOrThrow());
		assertThat(exception).isInstanceOf(UnauthorizedAccessException.class);
	}

	@Test
	@DisplayName("Throw UnauthorizedAccessException when accountId is null")
	void throwUnauthorizedAccessExceptionWhenAccountIdIsNull() {
		// given
		Account account = mock(Account.class);
		Mockito.when(this.userMock.getAccount()).thenReturn(account);
		Mockito.when(account.getId()).thenReturn(null); // Simulate accountId being null
		this.auth.setUser(this.userMock);

		// when
		Throwable accountIdException = catchThrowable(() -> this.auth.getAccountId());

		// then
		assertThat(accountIdException).isInstanceOf(UnauthorizedAccessException.class);
	}

}
