package io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import io.recruitcrm.entity.model.Account;
import io.recruitcrm.entity.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationPrincipal Tests")
class AuthenticationPrincipalTests {

	@Mock
	private User user;

	@Mock
	private Account account;

	@Test
	@DisplayName("Getters should return values from wrapped user")
	void testGettersReturnValuesFromUser() {
		Integer userId = 7;
		Integer roleId = 9;
		Integer organizationId = 11;
		String username = "sample-user";

		AuthenticationPrincipal principal = new AuthenticationPrincipal();
		principal.setUser(this.user);

		given(this.user.getId()).willReturn(userId);
		given(this.user.getUsername()).willReturn(username);
		given(this.user.getRoleId()).willReturn(roleId);
		given(this.user.getAccount()).willReturn(this.account);
		given(this.account.getId()).willReturn(organizationId);

		assertThat(principal.getUniqueIdentifier()).isEqualTo(userId);
		assertThat(principal.getUserName()).isEqualTo(username);
		assertThat(principal.getRoleIdentifier()).isEqualTo(roleId);
		assertThat(principal.getOrganizationIdentifier()).isEqualTo(organizationId);
		assertThat(principal.getUser()).isEqualTo(this.user);
	}

	@Test
	@DisplayName("Get role identifier label should throw unsupported operation")
	void testGetRoleIdentifierLabelThrowsUnsupportedOperation() {
		AuthenticationPrincipal principal = new AuthenticationPrincipal();

		assertThatThrownBy(principal::getRoleIdentifierLabel).isInstanceOf(UnsupportedOperationException.class)
			.hasMessage("Not supported yet.");
	}

}
