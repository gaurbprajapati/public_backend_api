package io.recruitcrm.microservice.timesheet.testdata;

import io.recruitcrm.microservice.timesheet.configuration.auth.principal.AuthPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.PrincipalType;
import io.recruitcrm.microservice.timesheet.dto.entity_columns.AccountViewColumnResponseBodyDto;
import io.recruitcrm.microservice.timesheet.helpers.entity.EntityColumnConstants;
import io.recruitcrm.microservice.timesheet.responses.APINormalResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Test data factory for EntityColumn-related test objects.
 */
public final class EntityColumnTestDataFactory {

	private EntityColumnTestDataFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	// ===== Response DTOs =====

	public static AccountViewColumnResponseBodyDto createAccountViewColumnResponse() {
		return new AccountViewColumnResponseBodyDto();
	}

	public static Map<String, AccountViewColumnResponseBodyDto> createColumnsMap() {
		AccountViewColumnResponseBodyDto columns = createAccountViewColumnResponse();
		return Map.of("columns", columns);
	}

	public static Map<String, AccountViewColumnResponseBodyDto> createAccountViewColumnsMap() {
		AccountViewColumnResponseBodyDto columns = createAccountViewColumnResponse();
		return Map.of("accountViewColumns", columns);
	}

	public static List<Map<String, AccountViewColumnResponseBodyDto>> createColumnsList() {
		return Arrays.asList(createColumnsMap());
	}

	public static List<Map<String, AccountViewColumnResponseBodyDto>> createAccountViewColumnsList() {
		return Arrays.asList(createAccountViewColumnsMap());
	}

	// ===== API Response Entities =====

	public static ResponseEntity<APINormalResponse<List<Map<String, AccountViewColumnResponseBodyDto>>>> createEntityColumnsSuccessResponse(
			List<Map<String, AccountViewColumnResponseBodyDto>> data) {
		APINormalResponse<List<Map<String, AccountViewColumnResponseBodyDto>>> response = new APINormalResponse<>(data);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	// ===== Test Constants =====

	public static String getDefaultEntity() {
		return "timesheet";
	}

	public static String getTimesheetContractorEntity() {
		return EntityColumnConstants.TIMESHEET_CONTRACTOR;
	}

	public static String getTimesheetDealEntity() {
		return EntityColumnConstants.TIMESHEET_DEAL;
	}

	public static String getContractorPortalEntity() {
		return EntityColumnConstants.CONTRACTOR;
	}

	public static String getClientPortalEntity() {
		return EntityColumnConstants.CLIENT;
	}

	public static String getAllTimesheetPageEntity() {
		return EntityColumnConstants.ALL_TIMESHEET_PAGE;
	}

	public static String getAllContractorPageEntity() {
		return EntityColumnConstants.ALL_CONTRACTOR_PAGE;
	}

	public static String getInvalidEntity() {
		return "invalid_entity";
	}

	public static String getDefaultLocale() {
		return "en";
	}

	// ===== Auth Principal Test Data =====

	public static AuthPrincipal createContractorPrincipal() {
		return new AuthPrincipal() {
			@Override
			public PrincipalType getPrincipalType() {
				return PrincipalType.CONTRACTOR;
			}

			@Override
			public Integer getUniqueIdentifier() {
				return 1;
			}

			@Override
			public Integer getOrganizationIdentifier() {
				return 1;
			}

			@Override
			public String getEmail() {
				return "contractor@test.com";
			}

			@Override
			public String getDisplayName() {
				return "Test Contractor";
			}

			@Override
			public String getFullName() {
				return "Test Contractor";
			}

			@Override
			public Integer getRoleIdentifier() {
				return 0;
			}
		};
	}

	public static AuthPrincipal createContactPrincipal() {
		return new AuthPrincipal() {
			@Override
			public PrincipalType getPrincipalType() {
				return PrincipalType.CONTACT;
			}

			@Override
			public Integer getUniqueIdentifier() {
				return 1;
			}

			@Override
			public Integer getOrganizationIdentifier() {
				return 1;
			}

			@Override
			public String getEmail() {
				return "contact@test.com";
			}

			@Override
			public String getDisplayName() {
				return "Test Contact";
			}

			@Override
			public String getFullName() {
				return "Test Contact";
			}

			@Override
			public Integer getRoleIdentifier() {
				return 0;
			}
		};
	}

	public static AuthPrincipal createUserPrincipal() {
		return new AuthPrincipal() {
			@Override
			public PrincipalType getPrincipalType() {
				return PrincipalType.USER;
			}

			@Override
			public Integer getUniqueIdentifier() {
				return 1;
			}

			@Override
			public Integer getOrganizationIdentifier() {
				return 1;
			}

			@Override
			public String getEmail() {
				return "user@test.com";
			}

			@Override
			public String getDisplayName() {
				return "Test User";
			}

			@Override
			public String getFullName() {
				return "Test User";
			}

			@Override
			public Integer getRoleIdentifier() {
				return 1;
			}
		};
	}

	public static final class Messages {

		private Messages() {
			throw new UnsupportedOperationException("Utility class");
		}

		public static final String ENTITY_COLUMNS_FETCHED_SUCCESSFULLY = "Entity columns fetched successfully";

		public static final String ACCOUNT_VIEW_COLUMNS_FETCHED_SUCCESSFULLY = "Account view columns fetched successfully";

		public static final String INVALID_ENTITY_PREFIX = "Invalid entity: ";

		public static final String OTHERS_VIEW_NOT_SUPPORTED_PREFIX = "Others view not supported for entity";

		public static final String ONLY_RCRM_USERS_ERROR = "Only RCRM users can access account view columns";

		public static final String CONTRACTOR_ACCESS_ERROR = "Only contractors can access contractor portal entity columns";

		public static final String CLIENT_ACCESS_ERROR = "Only contacts can access client portal entity columns";

		public static final String JSON_READ_ERROR = "Failed to read or parse entity columns file";

	}

}