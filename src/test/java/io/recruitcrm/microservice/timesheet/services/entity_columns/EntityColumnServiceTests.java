package io.recruitcrm.microservice.timesheet.services.entity_columns;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.recruitcrm.entity.model.Account;
import io.recruitcrm.entity.model.DataTableSetting;
import io.recruitcrm.entity.model.User;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.configuration.auth.IAuthenticationPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.AuthPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.PrincipalType;
import io.recruitcrm.microservice.timesheet.dao.datatable_setting.DataTableSettingJpaRepository;
import io.recruitcrm.microservice.timesheet.dto.entity_columns.AccountViewColumnResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.entity_columns.ColumnAccountViewResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.entity_columns.locale.LabelResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.entity_columns.locale.LocaleResponseBodyDto;
import io.recruitcrm.microservice.timesheet.exceptions.JsonReadException;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import io.recruitcrm.microservice.timesheet.helpers.entity.EntityColumnConstants;
import io.recruitcrm.microservice.timesheet.services.locale.LocaleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EntityColumnServiceTests {

	@Mock
	private ObjectMapper objectMapper;

	@Mock
	private LocaleService localeService;

	@Mock
	private AuthHolder auth;

	@Mock
	private IAuthenticationPrincipal<Integer, User> authenticationPrincipal;

	@Mock
	private User user;

	@Mock
	private Account account;

	@Mock
	private AuthPrincipal unifiedPrincipal;

	@Mock
	private DataTableSettingJpaRepository dataTableSettingJpaRepository;

	@InjectMocks
	private EntityColumnService entityColumnService;

	private static final String VALID_JSON = "{\"columns\":[{\"id\":1,\"name\":\"test\"}]}";

	private static final String INVALID_JSON = "{invalid json}";

	private static final Path TEST_PATH = Paths.get("test.json");

	private static final URI TEST_URI = TEST_PATH.toUri();

	@BeforeEach
	void setUp() {
		// Setup ReflectionTestUtils for objectMapper
		ReflectionTestUtils.setField(this.entityColumnService, "objectMapper", this.objectMapper);
	}

	@Test
	@DisplayName("Get entity columns for contractor successfully")
	void testGetEntityColumnsForContractor() throws IOException {
		// given
		AccountViewColumnResponseBodyDto expectedDto = new AccountViewColumnResponseBodyDto();
		willDoNothing().given(this.localeService).mergeLocaleLabelsIntoEntityColumns(any(), any());

		// Setup auth mocks
		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getAuthenticationPrincipal()).willReturn(this.authenticationPrincipal);
		given(this.authenticationPrincipal.getUser()).willReturn(this.user);
		given(this.user.getLocale()).willReturn("en");
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(1);
		given(this.user.getAccount()).willReturn(this.account);
		given(this.account.getAccountOwner()).willReturn(1);
		given(this.dataTableSettingJpaRepository.findByUserIdAndDatatableKeyOrderByIdAsc(1,
				EntityColumnConstants.TIMESHEET_CONTRACTOR))
			.willReturn(java.util.Collections.emptyList());

		try (MockedStatic<Files> filesMock = mockStatic(Files.class);
				MockedStatic<Paths> pathsMock = mockStatic(Paths.class);
				MockedConstruction<ClassPathResource> resourceMock = mockConstruction(ClassPathResource.class,
						(mock, context) -> given(mock.getURI()).willReturn(TEST_URI))) {

			given(Paths.get(TEST_URI)).willReturn(TEST_PATH);
			given(Files.readAllBytes(TEST_PATH)).willReturn(VALID_JSON.getBytes());
			given(this.objectMapper.readValue(VALID_JSON, AccountViewColumnResponseBodyDto.class))
				.willReturn(expectedDto);
			given(this.objectMapper.readValue(VALID_JSON,
					io.recruitcrm.microservice.timesheet.dto.entity_columns.locale.LocaleResponseBodyDto.class))
				.willReturn(new io.recruitcrm.microservice.timesheet.dto.entity_columns.locale.LocaleResponseBodyDto());

			// when
			AccountViewColumnResponseBodyDto result = this.entityColumnService
				.getEntityColumns(EntityColumnConstants.TIMESHEET_CONTRACTOR);

			// then
			assertThat(result).isEqualTo(expectedDto);
		}
	}

	@Test
	@DisplayName("Get entity columns for deal successfully")
	void testGetEntityColumnsForDeal() throws IOException {
		// given
		AccountViewColumnResponseBodyDto expectedDto = new AccountViewColumnResponseBodyDto();
		willDoNothing().given(this.localeService).mergeLocaleLabelsIntoEntityColumns(any(), any());

		// Setup auth mocks
		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getAuthenticationPrincipal()).willReturn(this.authenticationPrincipal);
		given(this.authenticationPrincipal.getUser()).willReturn(this.user);
		given(this.user.getLocale()).willReturn("en");
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(1);
		given(this.user.getAccount()).willReturn(this.account);
		given(this.account.getAccountOwner()).willReturn(1);
		given(this.dataTableSettingJpaRepository.findByUserIdAndDatatableKeyOrderByIdAsc(1,
				EntityColumnConstants.TIMESHEET_DEAL))
			.willReturn(java.util.Collections.emptyList());

		try (MockedStatic<Files> filesMock = mockStatic(Files.class);
				MockedStatic<Paths> pathsMock = mockStatic(Paths.class);
				MockedConstruction<ClassPathResource> resourceMock = mockConstruction(ClassPathResource.class,
						(mock, context) -> given(mock.getURI()).willReturn(TEST_URI))) {

			given(Paths.get(TEST_URI)).willReturn(TEST_PATH);
			given(Files.readAllBytes(TEST_PATH)).willReturn(VALID_JSON.getBytes());
			given(this.objectMapper.readValue(VALID_JSON, AccountViewColumnResponseBodyDto.class))
				.willReturn(expectedDto);
			given(this.objectMapper.readValue(VALID_JSON,
					io.recruitcrm.microservice.timesheet.dto.entity_columns.locale.LocaleResponseBodyDto.class))
				.willReturn(new io.recruitcrm.microservice.timesheet.dto.entity_columns.locale.LocaleResponseBodyDto());

			// when
			AccountViewColumnResponseBodyDto result = this.entityColumnService
				.getEntityColumns(EntityColumnConstants.TIMESHEET_DEAL);

			// then
			assertThat(result).isEqualTo(expectedDto);
		}
	}

	@Test
	@DisplayName("Get entity columns - Invalid entity")
	void testGetEntityColumnsInvalidEntity() {
		// when & then
		assertThatThrownBy(() -> this.entityColumnService.getEntityColumns("invalid_entity"))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Invalid entity");
	}

	@Test
	@DisplayName("Get entity columns - File read error")
	void testGetEntityColumnsFileReadError() throws IOException {
		// given
		// Setup auth mocks
		given(this.auth.getAuthenticationPrincipal()).willReturn(this.authenticationPrincipal);
		given(this.authenticationPrincipal.getUser()).willReturn(this.user);
		given(this.user.getLocale()).willReturn("en");

		try (MockedStatic<Files> filesMock = mockStatic(Files.class);
				MockedStatic<Paths> pathsMock = mockStatic(Paths.class);
				MockedConstruction<ClassPathResource> resourceMock = mockConstruction(ClassPathResource.class,
						(mock, context) -> given(mock.getURI()).willReturn(TEST_URI))) {

			given(Paths.get(TEST_URI)).willReturn(TEST_PATH);
			given(Files.readAllBytes(TEST_PATH)).willThrow(new IOException("File not found"));

			// when & then
			assertThatThrownBy(
					() -> this.entityColumnService.getEntityColumns(EntityColumnConstants.TIMESHEET_CONTRACTOR))
				.isInstanceOf(JsonReadException.class)
				.hasMessageContaining("Failed to read or parse entity columns file");
		}
	}

	@Test
	@DisplayName("Get entity columns - JSON parse error")
	void testGetEntityColumnsJsonParseError() throws IOException {
		// given
		ObjectMapper realObjectMapper = new ObjectMapper();
		ReflectionTestUtils.setField(this.entityColumnService, "objectMapper", realObjectMapper);

		// Setup auth mocks
		given(this.auth.getAuthenticationPrincipal()).willReturn(this.authenticationPrincipal);
		given(this.authenticationPrincipal.getUser()).willReturn(this.user);
		given(this.user.getLocale()).willReturn("en");

		try (MockedStatic<Files> filesMock = mockStatic(Files.class);
				MockedStatic<Paths> pathsMock = mockStatic(Paths.class);
				MockedConstruction<ClassPathResource> resourceMock = mockConstruction(ClassPathResource.class,
						(mock, context) -> given(mock.getURI()).willReturn(TEST_URI))) {

			given(Paths.get(TEST_URI)).willReturn(TEST_PATH);
			given(Files.readAllBytes(TEST_PATH)).willReturn(INVALID_JSON.getBytes());

			// when & then
			assertThatThrownBy(
					() -> this.entityColumnService.getEntityColumns(EntityColumnConstants.TIMESHEET_CONTRACTOR))
				.isInstanceOf(JsonReadException.class)
				.hasMessageContaining("Failed to read or parse entity columns file");
		}
		finally {
			// Restore the mocked ObjectMapper
			ReflectionTestUtils.setField(this.entityColumnService, "objectMapper", this.objectMapper);
		}
	}

	@Test
	@DisplayName("Get entity columns for contractor portal with contractor principal successfully")
	void testGetEntityColumnsContractorPortalContractorPrincipalReturnsColumns() throws IOException {
		// given
		AccountViewColumnResponseBodyDto expectedDto = new AccountViewColumnResponseBodyDto();
		willDoNothing().given(this.localeService).mergeLocaleLabelsIntoEntityColumns(any(), any());

		// Setup portal entity mocks
		given(this.auth.getUnifiedPrincipal()).willReturn(this.unifiedPrincipal);
		given(this.unifiedPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);

		try (MockedStatic<Files> filesMock = mockStatic(Files.class);
				MockedStatic<Paths> pathsMock = mockStatic(Paths.class);
				MockedConstruction<ClassPathResource> resourceMock = mockConstruction(ClassPathResource.class,
						(mock, context) -> given(mock.getURI()).willReturn(TEST_URI))) {

			given(Paths.get(TEST_URI)).willReturn(TEST_PATH);
			given(Files.readAllBytes(TEST_PATH)).willReturn(VALID_JSON.getBytes());
			given(this.objectMapper.readValue(VALID_JSON, AccountViewColumnResponseBodyDto.class))
				.willReturn(expectedDto);
			given(this.objectMapper.readValue(VALID_JSON,
					io.recruitcrm.microservice.timesheet.dto.entity_columns.locale.LocaleResponseBodyDto.class))
				.willReturn(new io.recruitcrm.microservice.timesheet.dto.entity_columns.locale.LocaleResponseBodyDto());

			// when
			AccountViewColumnResponseBodyDto result = this.entityColumnService
				.getEntityColumns(EntityColumnConstants.CONTRACTOR);

			// then
			assertThat(result).isEqualTo(expectedDto);
		}
	}

	@Test
	@DisplayName("Get entity columns for contractor portal with non-contractor principal throws UnauthorizedAccessException")
	void testGetEntityColumnsContractorPortalNonContractorPrincipalThrowsUnauthorizedAccessException() {
		// given
		given(this.auth.getUnifiedPrincipal()).willReturn(this.unifiedPrincipal);
		given(this.unifiedPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);

		// when & then
		assertThatThrownBy(() -> this.entityColumnService.getEntityColumns(EntityColumnConstants.CONTRACTOR))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("Only contractors can access contractor portal entity columns");
	}

	@Test
	@DisplayName("Get entity columns for contractor portal with null principal throws UnauthorizedAccessException")
	void testGetEntityColumnsContractorPortalNullPrincipalThrowsUnauthorizedAccessException() {
		// given
		given(this.auth.getUnifiedPrincipal()).willReturn(null);

		// when & then
		assertThatThrownBy(() -> this.entityColumnService.getEntityColumns(EntityColumnConstants.CONTRACTOR))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("Only contractors can access contractor portal entity columns");
	}

	@Test
	@DisplayName("Get entity columns for client portal with contact principal successfully")
	void testGetEntityColumnsClientPortalContactPrincipalReturnsColumns() throws IOException {
		// given
		AccountViewColumnResponseBodyDto expectedDto = new AccountViewColumnResponseBodyDto();
		willDoNothing().given(this.localeService).mergeLocaleLabelsIntoEntityColumns(any(), any());

		// Setup portal entity mocks
		given(this.auth.getUnifiedPrincipal()).willReturn(this.unifiedPrincipal);
		given(this.unifiedPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTACT);

		try (MockedStatic<Files> filesMock = mockStatic(Files.class);
				MockedStatic<Paths> pathsMock = mockStatic(Paths.class);
				MockedConstruction<ClassPathResource> resourceMock = mockConstruction(ClassPathResource.class,
						(mock, context) -> given(mock.getURI()).willReturn(TEST_URI))) {

			given(Paths.get(TEST_URI)).willReturn(TEST_PATH);
			given(Files.readAllBytes(TEST_PATH)).willReturn(VALID_JSON.getBytes());
			given(this.objectMapper.readValue(VALID_JSON, AccountViewColumnResponseBodyDto.class))
				.willReturn(expectedDto);
			given(this.objectMapper.readValue(VALID_JSON,
					io.recruitcrm.microservice.timesheet.dto.entity_columns.locale.LocaleResponseBodyDto.class))
				.willReturn(new io.recruitcrm.microservice.timesheet.dto.entity_columns.locale.LocaleResponseBodyDto());

			// when
			AccountViewColumnResponseBodyDto result = this.entityColumnService
				.getEntityColumns(EntityColumnConstants.CLIENT);

			// then
			assertThat(result).isEqualTo(expectedDto);
		}
	}

	@Test
	@DisplayName("Get entity columns for client portal with non-contact principal throws UnauthorizedAccessException")
	void testGetEntityColumnsClientPortalNonContactPrincipalThrowsUnauthorizedAccessException() {
		// given
		given(this.auth.getUnifiedPrincipal()).willReturn(this.unifiedPrincipal);
		given(this.unifiedPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);

		// when & then
		assertThatThrownBy(() -> this.entityColumnService.getEntityColumns(EntityColumnConstants.CLIENT))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("Only contacts can access client portal entity columns");
	}

	@Test
	@DisplayName("Get entity columns for client portal with null principal throws UnauthorizedAccessException")
	void testGetEntityColumnsClientPortalNullPrincipalThrowsUnauthorizedAccessException() {
		// given
		given(this.auth.getUnifiedPrincipal()).willReturn(null);

		// when & then
		assertThatThrownBy(() -> this.entityColumnService.getEntityColumns(EntityColumnConstants.CLIENT))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("Only contacts can access client portal entity columns");
	}

	@Test
	@DisplayName("Get entity columns for portal entity with USER principal uses user locale")
	void testGetEntityColumnsPortalEntityUserPrincipalUsesUserLocale() throws IOException {
		// given
		AccountViewColumnResponseBodyDto expectedDto = new AccountViewColumnResponseBodyDto();
		willDoNothing().given(this.localeService).mergeLocaleLabelsIntoEntityColumns(any(), any());

		// Setup portal entity mocks — non-portal path uses auth principal only for locale
		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getAuthenticationPrincipal()).willReturn(this.authenticationPrincipal);
		given(this.authenticationPrincipal.getUser()).willReturn(this.user);
		given(this.user.getLocale()).willReturn("fr");
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(1);
		given(this.user.getAccount()).willReturn(this.account);
		given(this.account.getAccountOwner()).willReturn(1);
		given(this.dataTableSettingJpaRepository.findByUserIdAndDatatableKeyOrderByIdAsc(1,
				EntityColumnConstants.TIMESHEET_CONTRACTOR))
			.willReturn(java.util.Collections.emptyList());

		try (MockedStatic<Files> filesMock = mockStatic(Files.class);
				MockedStatic<Paths> pathsMock = mockStatic(Paths.class);
				MockedConstruction<ClassPathResource> resourceMock = mockConstruction(ClassPathResource.class,
						(mock, context) -> given(mock.getURI()).willReturn(TEST_URI))) {

			given(Paths.get(TEST_URI)).willReturn(TEST_PATH);
			given(Files.readAllBytes(TEST_PATH)).willReturn(VALID_JSON.getBytes());
			given(this.objectMapper.readValue(VALID_JSON, AccountViewColumnResponseBodyDto.class))
				.willReturn(expectedDto);
			given(this.objectMapper.readValue(VALID_JSON,
					io.recruitcrm.microservice.timesheet.dto.entity_columns.locale.LocaleResponseBodyDto.class))
				.willReturn(new io.recruitcrm.microservice.timesheet.dto.entity_columns.locale.LocaleResponseBodyDto());

			// when - non-portal entity, USER principal, non-default locale
			AccountViewColumnResponseBodyDto result = this.entityColumnService
				.getEntityColumns(EntityColumnConstants.TIMESHEET_CONTRACTOR);

			// then
			assertThat(result).isEqualTo(expectedDto);
		}
	}

	@Test
	@DisplayName("Get entity columns - USER principal with saved settings merges column state")
	void testGetEntityColumnsUserPrincipalWithSavedSettingsMergesColumnState() throws IOException {
		// given — real ObjectMapper so both JSON parsing steps work end-to-end
		ObjectMapper realObjectMapper = new ObjectMapper();
		ReflectionTestUtils.setField(this.entityColumnService, "objectMapper", realObjectMapper);

		willDoNothing().given(this.localeService).mergeLocaleLabelsIntoEntityColumns(any(), any());
		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getAuthenticationPrincipal()).willReturn(this.authenticationPrincipal);
		given(this.authenticationPrincipal.getUser()).willReturn(this.user);
		given(this.user.getLocale()).willReturn("en");
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(42);
		given(this.user.getAccount()).willReturn(this.account);
		given(this.account.getAccountOwner()).willReturn(42);

		String columnStateJson = "{\"name\":{\"visible\":false,\"listPageOrder\":7}}";
		DataTableSetting savedSetting = new DataTableSetting();
		savedSetting.setColumnState(columnStateJson);
		given(this.dataTableSettingJpaRepository.findByUserIdAndDatatableKeyOrderByIdAsc(42,
				EntityColumnConstants.TIMESHEET_CONTRACTOR))
			.willReturn(java.util.List.of(savedSetting));

		String localeJson = "{}";
		String columnJson = "{\"name\":{\"field\":\"name\",\"visible\":true,\"listPageOrder\":1,\"visible_locked\":false}}";

		try (MockedStatic<Files> filesMock = mockStatic(Files.class);
				MockedStatic<Paths> pathsMock = mockStatic(Paths.class);
				MockedConstruction<ClassPathResource> resourceMock = mockConstruction(ClassPathResource.class,
						(mock, context) -> given(mock.getURI()).willReturn(TEST_URI))) {

			given(Paths.get(TEST_URI)).willReturn(TEST_PATH);
			given(Files.readAllBytes(TEST_PATH)).willReturn(localeJson.getBytes()).willReturn(columnJson.getBytes());

			// when
			AccountViewColumnResponseBodyDto result = this.entityColumnService
				.getEntityColumns(EntityColumnConstants.TIMESHEET_CONTRACTOR);

			// then — saved state overrides JSON defaults
			ColumnAccountViewResponseBodyDto nameColumn = result.get("name");
			assertThat(nameColumn).isNotNull();
			assertThat(nameColumn.getVisible()).isFalse();
			assertThat(nameColumn.getListPageOrder()).isEqualTo(7);
		}
		finally {
			ReflectionTestUtils.setField(this.entityColumnService, "objectMapper", this.objectMapper);
		}
	}

	@Test
	@DisplayName("Get entity columns - USER principal with null column state returns base columns unchanged")
	void testGetEntityColumnsUserPrincipalWithNullColumnStateReturnsBaseColumnsUnchanged() throws IOException {
		// given
		ObjectMapper realObjectMapper = new ObjectMapper();
		ReflectionTestUtils.setField(this.entityColumnService, "objectMapper", realObjectMapper);

		willDoNothing().given(this.localeService).mergeLocaleLabelsIntoEntityColumns(any(), any());
		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getAuthenticationPrincipal()).willReturn(this.authenticationPrincipal);
		given(this.authenticationPrincipal.getUser()).willReturn(this.user);
		given(this.user.getLocale()).willReturn("en");
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(1);
		given(this.user.getAccount()).willReturn(this.account);
		given(this.account.getAccountOwner()).willReturn(1);

		DataTableSetting savedSetting = new DataTableSetting();
		savedSetting.setColumnState(null);
		given(this.dataTableSettingJpaRepository.findByUserIdAndDatatableKeyOrderByIdAsc(1,
				EntityColumnConstants.TIMESHEET_CONTRACTOR))
			.willReturn(java.util.List.of(savedSetting));

		String localeJson = "{}";
		String columnJson = "{\"name\":{\"field\":\"name\",\"visible\":true,\"listPageOrder\":3}}";

		try (MockedStatic<Files> filesMock = mockStatic(Files.class);
				MockedStatic<Paths> pathsMock = mockStatic(Paths.class);
				MockedConstruction<ClassPathResource> resourceMock = mockConstruction(ClassPathResource.class,
						(mock, context) -> given(mock.getURI()).willReturn(TEST_URI))) {

			given(Paths.get(TEST_URI)).willReturn(TEST_PATH);
			given(Files.readAllBytes(TEST_PATH)).willReturn(localeJson.getBytes()).willReturn(columnJson.getBytes());

			// when
			AccountViewColumnResponseBodyDto result = this.entityColumnService
				.getEntityColumns(EntityColumnConstants.TIMESHEET_CONTRACTOR);

			// then — null columnState is a no-op; JSON defaults are preserved
			ColumnAccountViewResponseBodyDto nameColumn = result.get("name");
			assertThat(nameColumn).isNotNull();
			assertThat(nameColumn.getVisible()).isTrue();
			assertThat(nameColumn.getListPageOrder()).isEqualTo(3);
		}
		finally {
			ReflectionTestUtils.setField(this.entityColumnService, "objectMapper", this.objectMapper);
		}
	}

	@Test
	@DisplayName("Get entity columns - portal persona skips DataTableSetting lookup")
	void testGetEntityColumnsPortalPersonaSkipsDataTableSettingLookup() throws IOException {
		// given
		AccountViewColumnResponseBodyDto expectedDto = new AccountViewColumnResponseBodyDto();
		willDoNothing().given(this.localeService).mergeLocaleLabelsIntoEntityColumns(any(), any());

		given(this.auth.getUnifiedPrincipal()).willReturn(this.unifiedPrincipal);
		given(this.unifiedPrincipal.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);

		try (MockedStatic<Files> filesMock = mockStatic(Files.class);
				MockedStatic<Paths> pathsMock = mockStatic(Paths.class);
				MockedConstruction<ClassPathResource> resourceMock = mockConstruction(ClassPathResource.class,
						(mock, context) -> given(mock.getURI()).willReturn(TEST_URI))) {

			given(Paths.get(TEST_URI)).willReturn(TEST_PATH);
			given(Files.readAllBytes(TEST_PATH)).willReturn(VALID_JSON.getBytes());
			given(this.objectMapper.readValue(VALID_JSON, AccountViewColumnResponseBodyDto.class))
				.willReturn(expectedDto);
			given(this.objectMapper.readValue(VALID_JSON,
					io.recruitcrm.microservice.timesheet.dto.entity_columns.locale.LocaleResponseBodyDto.class))
				.willReturn(new io.recruitcrm.microservice.timesheet.dto.entity_columns.locale.LocaleResponseBodyDto());

			// when
			this.entityColumnService.getEntityColumns(EntityColumnConstants.CONTRACTOR);

			// then — CONTRACTOR principal should never trigger a DB lookup
			verify(this.dataTableSettingJpaRepository, never()).findByUserIdAndDatatableKeyOrderByIdAsc(any(), any());
		}
	}

	// ===== getAccountViewColumnsByEntity tests =====

	@Test
	@DisplayName("Get account view columns - Contractor principal throws UnauthorizedAccessException")
	void testGetAccountViewColumnsByEntityContractorPrincipalThrowsUnauthorizedAccessException() {
		// given
		given(this.auth.getPrincipalType()).willReturn(PrincipalType.CONTRACTOR);

		// when & then
		assertThatThrownBy(
				() -> this.entityColumnService.getAccountViewColumnsByEntity(EntityColumnConstants.ALL_CONTRACTOR_PAGE))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("Only RCRM users can access account view columns");
	}

	@Test
	@DisplayName("Get account view columns - Contact principal throws UnauthorizedAccessException")
	void testGetAccountViewColumnsByEntityContactPrincipalThrowsUnauthorizedAccessException() {
		// given
		given(this.auth.getPrincipalType()).willReturn(PrincipalType.CONTACT);

		// when & then
		assertThatThrownBy(
				() -> this.entityColumnService.getAccountViewColumnsByEntity(EntityColumnConstants.ALL_TIMESHEET_PAGE))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("Only RCRM users can access account view columns");
	}

	@Test
	@DisplayName("Get account view columns - non-owner USER throws UnauthorizedAccessException")
	void testGetAccountViewColumnsByEntityNonOwnerThrowsUnauthorizedAccessException() {
		// given
		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getAuthenticationPrincipalRoleIdentifier()).willReturn(2); // admin,
																					// not
																					// owner

		// when & then
		assertThatThrownBy(
				() -> this.entityColumnService.getAccountViewColumnsByEntity(EntityColumnConstants.ALL_CONTRACTOR_PAGE))
			.isInstanceOf(UnauthorizedAccessException.class)
			.hasMessageContaining("Only account owner can access account view columns");
	}

	@Test
	@DisplayName("Get account view columns - Invalid entity throws ValidationErrorException")
	void testGetAccountViewColumnsByEntityInvalidEntityThrowsValidationErrorException() {
		// given
		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getAuthenticationPrincipalRoleIdentifier()).willReturn(4);

		// when & then
		assertThatThrownBy(() -> this.entityColumnService.getAccountViewColumnsByEntity("invalid_entity"))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Others view not supported for entity");
	}

	@Test
	@DisplayName("Get account view columns - Portal entity throws ValidationErrorException")
	void testGetAccountViewColumnsByEntityPortalEntityThrowsValidationErrorException() {
		// given
		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getAuthenticationPrincipalRoleIdentifier()).willReturn(4);

		// when & then
		assertThatThrownBy(
				() -> this.entityColumnService.getAccountViewColumnsByEntity(EntityColumnConstants.CONTRACTOR))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Others view not supported for entity");
	}

	@Test
	@DisplayName("Get account view columns - timesheet_deal entity throws ValidationErrorException")
	void testGetAccountViewColumnsByEntityTimesheetDealThrowsValidationErrorException() {
		// given
		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getAuthenticationPrincipalRoleIdentifier()).willReturn(4);

		// when & then
		assertThatThrownBy(
				() -> this.entityColumnService.getAccountViewColumnsByEntity(EntityColumnConstants.TIMESHEET_DEAL))
			.isInstanceOf(ValidationErrorException.class)
			.hasMessageContaining("Others view not supported for entity");
	}

	@Test
	@DisplayName("Get account view columns for contractors with no saved settings returns base columns")
	void testGetAccountViewColumnsByEntityContractorsNoSavedSettingsReturnsBaseColumns() throws IOException {
		// given
		AccountViewColumnResponseBodyDto expectedDto = new AccountViewColumnResponseBodyDto();
		willDoNothing().given(this.localeService).mergeLocaleLabelsIntoEntityColumns(any(), any());

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getAuthenticationPrincipalRoleIdentifier()).willReturn(4);
		given(this.auth.getAuthenticationPrincipal()).willReturn(this.authenticationPrincipal);
		given(this.authenticationPrincipal.getUser()).willReturn(this.user);
		given(this.user.getLocale()).willReturn("en");
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(1);
		given(this.dataTableSettingJpaRepository.findByUserIdAndDatatableKeyOrderByIdAsc(1,
				EntityColumnConstants.ALL_CONTRACTOR_PAGE + EntityColumnConstants.OTHERS_VIEW_SUFFIX))
			.willReturn(java.util.Collections.emptyList());

		try (MockedStatic<Files> filesMock = mockStatic(Files.class);
				MockedStatic<Paths> pathsMock = mockStatic(Paths.class);
				MockedConstruction<ClassPathResource> resourceMock = mockConstruction(ClassPathResource.class,
						(mock, context) -> given(mock.getURI()).willReturn(TEST_URI))) {

			given(Paths.get(TEST_URI)).willReturn(TEST_PATH);
			given(Files.readAllBytes(TEST_PATH)).willReturn(VALID_JSON.getBytes());
			given(this.objectMapper.readValue(VALID_JSON, AccountViewColumnResponseBodyDto.class))
				.willReturn(expectedDto);
			given(this.objectMapper.readValue(VALID_JSON,
					io.recruitcrm.microservice.timesheet.dto.entity_columns.locale.LocaleResponseBodyDto.class))
				.willReturn(new io.recruitcrm.microservice.timesheet.dto.entity_columns.locale.LocaleResponseBodyDto());

			// when
			AccountViewColumnResponseBodyDto result = this.entityColumnService
				.getAccountViewColumnsByEntity(EntityColumnConstants.ALL_CONTRACTOR_PAGE);

			// then
			assertThat(result).isEqualTo(expectedDto);
		}
	}

	@Test
	@DisplayName("Get account view columns for timesheets with no saved settings returns base columns")
	void testGetAccountViewColumnsByEntityTimesheetsNoSavedSettingsReturnsBaseColumns() throws IOException {
		// given
		AccountViewColumnResponseBodyDto expectedDto = new AccountViewColumnResponseBodyDto();
		willDoNothing().given(this.localeService).mergeLocaleLabelsIntoEntityColumns(any(), any());

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getAuthenticationPrincipalRoleIdentifier()).willReturn(4);
		given(this.auth.getAuthenticationPrincipal()).willReturn(this.authenticationPrincipal);
		given(this.authenticationPrincipal.getUser()).willReturn(this.user);
		given(this.user.getLocale()).willReturn("en");
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(1);
		given(this.dataTableSettingJpaRepository.findByUserIdAndDatatableKeyOrderByIdAsc(1,
				EntityColumnConstants.ALL_TIMESHEET_PAGE + EntityColumnConstants.OTHERS_VIEW_SUFFIX))
			.willReturn(java.util.Collections.emptyList());

		try (MockedStatic<Files> filesMock = mockStatic(Files.class);
				MockedStatic<Paths> pathsMock = mockStatic(Paths.class);
				MockedConstruction<ClassPathResource> resourceMock = mockConstruction(ClassPathResource.class,
						(mock, context) -> given(mock.getURI()).willReturn(TEST_URI))) {

			given(Paths.get(TEST_URI)).willReturn(TEST_PATH);
			given(Files.readAllBytes(TEST_PATH)).willReturn(VALID_JSON.getBytes());
			given(this.objectMapper.readValue(VALID_JSON, AccountViewColumnResponseBodyDto.class))
				.willReturn(expectedDto);
			given(this.objectMapper.readValue(VALID_JSON,
					io.recruitcrm.microservice.timesheet.dto.entity_columns.locale.LocaleResponseBodyDto.class))
				.willReturn(new io.recruitcrm.microservice.timesheet.dto.entity_columns.locale.LocaleResponseBodyDto());

			// when
			AccountViewColumnResponseBodyDto result = this.entityColumnService
				.getAccountViewColumnsByEntity(EntityColumnConstants.ALL_TIMESHEET_PAGE);

			// then
			assertThat(result).isEqualTo(expectedDto);
		}
	}

	@Test
	@DisplayName("Get account view columns for timesheet_contractor with no saved settings returns base columns")
	void testGetAccountViewColumnsByEntityTimesheetContractorNoSavedSettingsReturnsBaseColumns() throws IOException {
		// given
		AccountViewColumnResponseBodyDto expectedDto = new AccountViewColumnResponseBodyDto();
		willDoNothing().given(this.localeService).mergeLocaleLabelsIntoEntityColumns(any(), any());

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getAuthenticationPrincipalRoleIdentifier()).willReturn(4);
		given(this.auth.getAuthenticationPrincipal()).willReturn(this.authenticationPrincipal);
		given(this.authenticationPrincipal.getUser()).willReturn(this.user);
		given(this.user.getLocale()).willReturn("en");
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(1);
		given(this.dataTableSettingJpaRepository.findByUserIdAndDatatableKeyOrderByIdAsc(1,
				EntityColumnConstants.TIMESHEET_CONTRACTOR + EntityColumnConstants.OTHERS_VIEW_SUFFIX))
			.willReturn(java.util.Collections.emptyList());

		try (MockedStatic<Files> filesMock = mockStatic(Files.class);
				MockedStatic<Paths> pathsMock = mockStatic(Paths.class);
				MockedConstruction<ClassPathResource> resourceMock = mockConstruction(ClassPathResource.class,
						(mock, context) -> given(mock.getURI()).willReturn(TEST_URI))) {

			given(Paths.get(TEST_URI)).willReturn(TEST_PATH);
			given(Files.readAllBytes(TEST_PATH)).willReturn(VALID_JSON.getBytes());
			given(this.objectMapper.readValue(VALID_JSON, AccountViewColumnResponseBodyDto.class))
				.willReturn(expectedDto);
			given(this.objectMapper.readValue(VALID_JSON,
					io.recruitcrm.microservice.timesheet.dto.entity_columns.locale.LocaleResponseBodyDto.class))
				.willReturn(new io.recruitcrm.microservice.timesheet.dto.entity_columns.locale.LocaleResponseBodyDto());

			// when
			AccountViewColumnResponseBodyDto result = this.entityColumnService
				.getAccountViewColumnsByEntity(EntityColumnConstants.TIMESHEET_CONTRACTOR);

			// then
			assertThat(result).isEqualTo(expectedDto);
		}
	}

	@Test
	@DisplayName("Get account view columns with saved settings merges column state")
	void testGetAccountViewColumnsByEntityWithSavedSettingsMergesColumnState() throws IOException {
		// given — use real ObjectMapper so both JSON parsing steps work end-to-end
		ObjectMapper realObjectMapper = new ObjectMapper();
		ReflectionTestUtils.setField(this.entityColumnService, "objectMapper", realObjectMapper);

		willDoNothing().given(this.localeService).mergeLocaleLabelsIntoEntityColumns(any(), any());
		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getAuthenticationPrincipalRoleIdentifier()).willReturn(4);
		given(this.auth.getAuthenticationPrincipal()).willReturn(this.authenticationPrincipal);
		given(this.authenticationPrincipal.getUser()).willReturn(this.user);
		given(this.user.getLocale()).willReturn("en");
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(1);

		// saved state only contains "name" — "status" column is absent, triggering
		// continue
		String columnStateJson = "{\"name\":{\"visible\":false,\"listPageOrder\":5}}";
		DataTableSetting savedSetting = new DataTableSetting();
		savedSetting.setColumnState(columnStateJson);

		given(this.dataTableSettingJpaRepository.findByUserIdAndDatatableKeyOrderByIdAsc(1,
				EntityColumnConstants.ALL_CONTRACTOR_PAGE + EntityColumnConstants.OTHERS_VIEW_SUFFIX))
			.willReturn(java.util.List.of(savedSetting));

		// two columns: "name" (in saved state) and "status" (absent → continue branch)
		String localeJson = "{}";
		String columnJson = "{\"name\":{\"field\":\"name\",\"visible\":true,\"listPageOrder\":1,\"visible_locked\":false},"
				+ "\"status\":{\"field\":\"status\",\"visible\":true,\"listPageOrder\":2,\"visible_locked\":false}}";

		try (MockedStatic<Files> filesMock = mockStatic(Files.class);
				MockedStatic<Paths> pathsMock = mockStatic(Paths.class);
				MockedConstruction<ClassPathResource> resourceMock = mockConstruction(ClassPathResource.class,
						(mock, context) -> given(mock.getURI()).willReturn(TEST_URI))) {

			given(Paths.get(TEST_URI)).willReturn(TEST_PATH);
			given(Files.readAllBytes(TEST_PATH)).willReturn(localeJson.getBytes()).willReturn(columnJson.getBytes());

			// when
			AccountViewColumnResponseBodyDto result = this.entityColumnService
				.getAccountViewColumnsByEntity(EntityColumnConstants.ALL_CONTRACTOR_PAGE);

			// then — "name" overlaid from saved state; "status" absent from saved state →
			// continue, defaults kept
			ColumnAccountViewResponseBodyDto nameColumn = result.get("name");
			assertThat(nameColumn).isNotNull();
			assertThat(nameColumn.getVisible()).isFalse();
			assertThat(nameColumn.getListPageOrder()).isEqualTo(5);
			ColumnAccountViewResponseBodyDto statusColumn = result.get("status");
			assertThat(statusColumn).isNotNull();
			assertThat(statusColumn.getVisible()).isTrue(); // unchanged JSON default
		}
		finally {
			ReflectionTestUtils.setField(this.entityColumnService, "objectMapper", this.objectMapper);
		}
	}

	@Test
	@DisplayName("Get account view columns strips width, pinned, sort, sortIndex, colId from base JSON columns")
	void testGetAccountViewColumnsByEntityStripsLayoutStateFromBaseColumns() throws IOException {
		ObjectMapper realObjectMapper = new ObjectMapper();
		ReflectionTestUtils.setField(this.entityColumnService, "objectMapper", realObjectMapper);

		willDoNothing().given(this.localeService).mergeLocaleLabelsIntoEntityColumns(any(), any());
		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getAuthenticationPrincipalRoleIdentifier()).willReturn(4);
		given(this.auth.getAuthenticationPrincipal()).willReturn(this.authenticationPrincipal);
		given(this.authenticationPrincipal.getUser()).willReturn(this.user);
		given(this.user.getLocale()).willReturn("en");
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(1);
		given(this.dataTableSettingJpaRepository.findByUserIdAndDatatableKeyOrderByIdAsc(1,
				EntityColumnConstants.ALL_CONTRACTOR_PAGE + EntityColumnConstants.OTHERS_VIEW_SUFFIX))
			.willReturn(java.util.Collections.emptyList());

		String localeJson = "{}";
		String columnJson = "{\"name\":{\"field\":\"name\",\"visible\":true,\"listPageOrder\":1,"
				+ "\"visible_locked\":false,\"width\":200.0,\"pinned\":\"left\","
				+ "\"sort\":\"asc\",\"sortIndex\":0,\"colId\":\"custom_id\"}}";

		try (MockedStatic<Files> filesMock = mockStatic(Files.class);
				MockedStatic<Paths> pathsMock = mockStatic(Paths.class);
				MockedConstruction<ClassPathResource> resourceMock = mockConstruction(ClassPathResource.class,
						(mock, context) -> given(mock.getURI()).willReturn(TEST_URI))) {

			given(Paths.get(TEST_URI)).willReturn(TEST_PATH);
			given(Files.readAllBytes(TEST_PATH)).willReturn(localeJson.getBytes()).willReturn(columnJson.getBytes());

			AccountViewColumnResponseBodyDto result = this.entityColumnService
				.getAccountViewColumnsByEntity(EntityColumnConstants.ALL_CONTRACTOR_PAGE);

			ColumnAccountViewResponseBodyDto nameColumn = result.get("name");
			assertThat(nameColumn).isNotNull();
			assertThat(nameColumn.getWidth()).isNull();
			assertThat(nameColumn.getPinned()).isNull();
			assertThat(nameColumn.getSort()).isNull();
			assertThat(nameColumn.getSortIndex()).isNull();
			assertThat(nameColumn.getColId()).isNull();
			assertThat(nameColumn.getVisible()).isTrue();
			assertThat(nameColumn.getListPageOrder()).isEqualTo(1);
		}
		finally {
			ReflectionTestUtils.setField(this.entityColumnService, "objectMapper", this.objectMapper);
		}
	}

	@Test
	@DisplayName("Get account view columns - Others View neutralizes pinned even when saved state carries it")
	void testGetAccountViewColumnsByEntityStripsPinnedFromSavedState() throws IOException {
		// given — real ObjectMapper so the saved column state merges end-to-end
		ObjectMapper realObjectMapper = new ObjectMapper();
		ReflectionTestUtils.setField(this.entityColumnService, "objectMapper", realObjectMapper);

		willDoNothing().given(this.localeService).mergeLocaleLabelsIntoEntityColumns(any(), any());
		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getAuthenticationPrincipalRoleIdentifier()).willReturn(4);
		given(this.auth.getAuthenticationPrincipal()).willReturn(this.authenticationPrincipal);
		given(this.authenticationPrincipal.getUser()).willReturn(this.user);
		given(this.user.getLocale()).willReturn("en");
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(1);

		// owner's saved Others View, as serialized by AG-Grid getColumnState(): it
		// carries a
		// "pinned" key for the column the owner added, including the default-pinned one.
		String columnStateJson = "{\"name\":{\"visible\":true,\"listPageOrder\":2,\"pinned\":\"left\"}}";
		DataTableSetting ownerSetting = new DataTableSetting();
		ownerSetting.setColumnState(columnStateJson);
		given(this.dataTableSettingJpaRepository.findByUserIdAndDatatableKeyOrderByIdAsc(1,
				EntityColumnConstants.ALL_CONTRACTOR_PAGE + EntityColumnConstants.OTHERS_VIEW_SUFFIX))
			.willReturn(java.util.List.of(ownerSetting));

		String localeJson = "{}";
		// base JSON pins the column by default
		String columnJson = "{\"name\":{\"field\":\"name\",\"visible\":true,\"listPageOrder\":1,"
				+ "\"visible_locked\":false,\"pinned\":\"left\"}}";

		try (MockedStatic<Files> filesMock = mockStatic(Files.class);
				MockedStatic<Paths> pathsMock = mockStatic(Paths.class);
				MockedConstruction<ClassPathResource> resourceMock = mockConstruction(ClassPathResource.class,
						(mock, context) -> given(mock.getURI()).willReturn(TEST_URI))) {

			given(Paths.get(TEST_URI)).willReturn(TEST_PATH);
			given(Files.readAllBytes(TEST_PATH)).willReturn(localeJson.getBytes()).willReturn(columnJson.getBytes());

			// when
			AccountViewColumnResponseBodyDto result = this.entityColumnService
				.getAccountViewColumnsByEntity(EntityColumnConstants.ALL_CONTRACTOR_PAGE);

			// then — visibility/order from the saved state applies, but pin state is
			// neutralized
			ColumnAccountViewResponseBodyDto nameColumn = result.get("name");
			assertThat(nameColumn).isNotNull();
			assertThat(nameColumn.getListPageOrder()).isEqualTo(2);
			assertThat(nameColumn.getPinned()).isNull();
		}
		finally {
			ReflectionTestUtils.setField(this.entityColumnService, "objectMapper", this.objectMapper);
		}
	}

	@Test
	@DisplayName("Get account view columns with null column state returns base columns unchanged")
	void testGetAccountViewColumnsByEntityWithNullColumnStateReturnsBaseColumnsUnchanged() throws IOException {
		// given
		AccountViewColumnResponseBodyDto expectedDto = new AccountViewColumnResponseBodyDto();
		willDoNothing().given(this.localeService).mergeLocaleLabelsIntoEntityColumns(any(), any());

		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getAuthenticationPrincipalRoleIdentifier()).willReturn(4);
		given(this.auth.getAuthenticationPrincipal()).willReturn(this.authenticationPrincipal);
		given(this.authenticationPrincipal.getUser()).willReturn(this.user);
		given(this.user.getLocale()).willReturn("en");
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(1);

		DataTableSetting savedSetting = new DataTableSetting();
		savedSetting.setColumnState(null);

		given(this.dataTableSettingJpaRepository.findByUserIdAndDatatableKeyOrderByIdAsc(1,
				EntityColumnConstants.ALL_CONTRACTOR_PAGE + EntityColumnConstants.OTHERS_VIEW_SUFFIX))
			.willReturn(java.util.List.of(savedSetting));

		try (MockedStatic<Files> filesMock = mockStatic(Files.class);
				MockedStatic<Paths> pathsMock = mockStatic(Paths.class);
				MockedConstruction<ClassPathResource> resourceMock = mockConstruction(ClassPathResource.class,
						(mock, context) -> given(mock.getURI()).willReturn(TEST_URI))) {

			given(Paths.get(TEST_URI)).willReturn(TEST_PATH);
			given(Files.readAllBytes(TEST_PATH)).willReturn(VALID_JSON.getBytes());
			given(this.objectMapper.readValue(VALID_JSON, AccountViewColumnResponseBodyDto.class))
				.willReturn(expectedDto);
			given(this.objectMapper.readValue(VALID_JSON,
					io.recruitcrm.microservice.timesheet.dto.entity_columns.locale.LocaleResponseBodyDto.class))
				.willReturn(new io.recruitcrm.microservice.timesheet.dto.entity_columns.locale.LocaleResponseBodyDto());

			// when
			AccountViewColumnResponseBodyDto result = this.entityColumnService
				.getAccountViewColumnsByEntity(EntityColumnConstants.ALL_CONTRACTOR_PAGE);

			// then — null columnState is a no-op; base columns returned as-is
			assertThat(result).isEqualTo(expectedDto);
		}
	}

	@Test
	@DisplayName("Get entity columns - no user settings falls back to owner locked view")
	void testGetEntityColumnsNoUserSettingsFallsBackToOwnerLockedView() throws IOException {
		// given — real ObjectMapper for end-to-end JSON parsing
		ObjectMapper realObjectMapper = new ObjectMapper();
		ReflectionTestUtils.setField(this.entityColumnService, "objectMapper", realObjectMapper);

		willDoNothing().given(this.localeService).mergeLocaleLabelsIntoEntityColumns(any(), any());
		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getAuthenticationPrincipal()).willReturn(this.authenticationPrincipal);
		given(this.authenticationPrincipal.getUser()).willReturn(this.user);
		given(this.user.getLocale()).willReturn("en");
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(5);

		given(this.user.getAccount()).willReturn(this.account);
		given(this.account.getAccountOwner()).willReturn(1);

		// user has no own settings
		given(this.dataTableSettingJpaRepository.findByUserIdAndDatatableKeyOrderByIdAsc(5,
				EntityColumnConstants.TIMESHEET_CONTRACTOR))
			.willReturn(java.util.Collections.emptyList());

		// owner has a locked view saved under entity + "_others_view"
		String columnStateJson = "{\"name\":{\"visible\":false,\"listPageOrder\":2}}";
		DataTableSetting ownerSetting = new DataTableSetting();
		ownerSetting.setColumnState(columnStateJson);
		given(this.dataTableSettingJpaRepository.findByUserIdAndDatatableKeyOrderByIdAsc(1,
				EntityColumnConstants.TIMESHEET_CONTRACTOR + EntityColumnConstants.OTHERS_VIEW_SUFFIX))
			.willReturn(java.util.List.of(ownerSetting));

		String localeJson = "{}";
		String columnJson = "{\"name\":{\"field\":\"name\",\"visible\":true,\"listPageOrder\":1,\"visible_locked\":false}}";

		try (MockedStatic<Files> filesMock = mockStatic(Files.class);
				MockedStatic<Paths> pathsMock = mockStatic(Paths.class);
				MockedConstruction<ClassPathResource> resourceMock = mockConstruction(ClassPathResource.class,
						(mock, context) -> given(mock.getURI()).willReturn(TEST_URI))) {

			given(Paths.get(TEST_URI)).willReturn(TEST_PATH);
			given(Files.readAllBytes(TEST_PATH)).willReturn(localeJson.getBytes()).willReturn(columnJson.getBytes());

			// when
			AccountViewColumnResponseBodyDto result = this.entityColumnService
				.getEntityColumns(EntityColumnConstants.TIMESHEET_CONTRACTOR);

			// then — owner's locked view overrides JSON defaults
			ColumnAccountViewResponseBodyDto nameColumn = result.get("name");
			assertThat(nameColumn).isNotNull();
			assertThat(nameColumn.getVisible()).isFalse();
			assertThat(nameColumn.getListPageOrder()).isEqualTo(2);
		}
		finally {
			ReflectionTestUtils.setField(this.entityColumnService, "objectMapper", this.objectMapper);
		}
	}

	@Test
	@DisplayName("Get entity columns - others view fallback never pins columns even when owner saved state carries pinned")
	void testGetEntityColumnsOthersViewFallbackStripsPinnedFromSavedState() throws IOException {
		// given
		ObjectMapper realObjectMapper = new ObjectMapper();
		ReflectionTestUtils.setField(this.entityColumnService, "objectMapper", realObjectMapper);

		willDoNothing().given(this.localeService).mergeLocaleLabelsIntoEntityColumns(any(), any());
		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getAuthenticationPrincipal()).willReturn(this.authenticationPrincipal);
		given(this.authenticationPrincipal.getUser()).willReturn(this.user);
		given(this.user.getLocale()).willReturn("en");
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(5);

		given(this.user.getAccount()).willReturn(this.account);
		given(this.account.getAccountOwner()).willReturn(1);

		// user has no own settings -> others view fallback to owner's saved state
		given(this.dataTableSettingJpaRepository.findByUserIdAndDatatableKeyOrderByIdAsc(5,
				EntityColumnConstants.TIMESHEET_CONTRACTOR))
			.willReturn(java.util.Collections.emptyList());

		// owner's others view saved state, as serialized by AG-Grid getColumnState():
		// it carries a "pinned" key for every column, including the default-pinned one.
		String columnStateJson = "{\"name\":{\"visible\":true,\"listPageOrder\":2,\"pinned\":\"left\"}}";
		DataTableSetting ownerSetting = new DataTableSetting();
		ownerSetting.setColumnState(columnStateJson);
		given(this.dataTableSettingJpaRepository.findByUserIdAndDatatableKeyOrderByIdAsc(1,
				EntityColumnConstants.TIMESHEET_CONTRACTOR + EntityColumnConstants.OTHERS_VIEW_SUFFIX))
			.willReturn(java.util.List.of(ownerSetting));

		String localeJson = "{}";
		// base JSON pins the column by default
		String columnJson = "{\"name\":{\"field\":\"name\",\"visible\":true,\"listPageOrder\":1,"
				+ "\"visible_locked\":false,\"pinned\":\"left\"}}";

		try (MockedStatic<Files> filesMock = mockStatic(Files.class);
				MockedStatic<Paths> pathsMock = mockStatic(Paths.class);
				MockedConstruction<ClassPathResource> resourceMock = mockConstruction(ClassPathResource.class,
						(mock, context) -> given(mock.getURI()).willReturn(TEST_URI))) {

			given(Paths.get(TEST_URI)).willReturn(TEST_PATH);
			given(Files.readAllBytes(TEST_PATH)).willReturn(localeJson.getBytes()).willReturn(columnJson.getBytes());

			// when
			AccountViewColumnResponseBodyDto result = this.entityColumnService
				.getEntityColumns(EntityColumnConstants.TIMESHEET_CONTRACTOR);

			// then — layout/order from others view applies, but pin state is neutralized
			ColumnAccountViewResponseBodyDto nameColumn = result.get("name");
			assertThat(nameColumn).isNotNull();
			assertThat(nameColumn.getListPageOrder()).isEqualTo(2);
			assertThat(nameColumn.getPinned()).isNull();
		}
		finally {
			ReflectionTestUtils.setField(this.entityColumnService, "objectMapper", this.objectMapper);
		}
	}

	@Test
	@DisplayName("Get entity columns - non-owner with synced Others View in own row drops base default pin")
	void testGetEntityColumnsNonOwnerSyncedOwnRowDropsDefaultPin() throws IOException {
		// given — reproduces the real Others View flow: when the owner saves, Albatross
		// (setOthersViewToAllUsers) copies the Others View state into every non-owner's
		// OWN row under the entity key. That state only carries visibility/order, no
		// "pinned" key, so the base-JSON default pin must not leak through.
		ObjectMapper realObjectMapper = new ObjectMapper();
		ReflectionTestUtils.setField(this.entityColumnService, "objectMapper", realObjectMapper);

		willDoNothing().given(this.localeService).mergeLocaleLabelsIntoEntityColumns(any(), any());
		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getAuthenticationPrincipal()).willReturn(this.authenticationPrincipal);
		given(this.authenticationPrincipal.getUser()).willReturn(this.user);
		given(this.user.getLocale()).willReturn("en");
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(5);

		given(this.user.getAccount()).willReturn(this.account);
		given(this.account.getAccountOwner()).willReturn(1);

		// non-owner HAS an own row (synced from the Others View): visibility/order only,
		// no pin
		String syncedColumnState = "{\"timesheetPeriod\":{\"visible\":true,\"listPageOrder\":2}}";
		DataTableSetting userSetting = new DataTableSetting();
		userSetting.setColumnState(syncedColumnState);
		given(this.dataTableSettingJpaRepository.findByUserIdAndDatatableKeyOrderByIdAsc(5,
				EntityColumnConstants.ALL_TIMESHEET_PAGE))
			.willReturn(java.util.List.of(userSetting));

		String localeJson = "{}";
		// base JSON pins timesheetPeriod by default
		String columnJson = "{\"timesheetPeriod\":{\"field\":\"timesheetPeriod\",\"visible\":true,\"listPageOrder\":1,"
				+ "\"visible_locked\":true,\"pinned\":\"left\"}}";

		try (MockedStatic<Files> filesMock = mockStatic(Files.class);
				MockedStatic<Paths> pathsMock = mockStatic(Paths.class);
				MockedConstruction<ClassPathResource> resourceMock = mockConstruction(ClassPathResource.class,
						(mock, context) -> given(mock.getURI()).willReturn(TEST_URI))) {

			given(Paths.get(TEST_URI)).willReturn(TEST_PATH);
			given(Files.readAllBytes(TEST_PATH)).willReturn(localeJson.getBytes()).willReturn(columnJson.getBytes());

			// when
			AccountViewColumnResponseBodyDto result = this.entityColumnService
				.getEntityColumns(EntityColumnConstants.ALL_TIMESHEET_PAGE);

			// then — order from the synced state applies, default pin is dropped
			ColumnAccountViewResponseBodyDto periodColumn = result.get("timesheetPeriod");
			assertThat(periodColumn).isNotNull();
			assertThat(periodColumn.getListPageOrder()).isEqualTo(2);
			assertThat(periodColumn.getPinned()).isNull();
		}
		finally {
			ReflectionTestUtils.setField(this.entityColumnService, "objectMapper", this.objectMapper);
		}
	}

	@Test
	@DisplayName("Get entity columns - non-owner keeps explicit pin saved in own row")
	void testGetEntityColumnsNonOwnerHonorsExplicitSavedPin() throws IOException {
		// given — a non-owner with override permission pins a column themselves; their
		// own row then carries an explicit "pinned" key, which must be honored (not
		// dropped).
		ObjectMapper realObjectMapper = new ObjectMapper();
		ReflectionTestUtils.setField(this.entityColumnService, "objectMapper", realObjectMapper);

		willDoNothing().given(this.localeService).mergeLocaleLabelsIntoEntityColumns(any(), any());
		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getAuthenticationPrincipal()).willReturn(this.authenticationPrincipal);
		given(this.authenticationPrincipal.getUser()).willReturn(this.user);
		given(this.user.getLocale()).willReturn("en");
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(5);

		given(this.user.getAccount()).willReturn(this.account);
		given(this.account.getAccountOwner()).willReturn(1);

		// non-owner's own row carries an explicit pin on a column the base JSON does not
		// pin
		String userColumnState = "{\"contractorName\":{\"visible\":true,\"listPageOrder\":3,\"pinned\":\"right\"}}";
		DataTableSetting userSetting = new DataTableSetting();
		userSetting.setColumnState(userColumnState);
		given(this.dataTableSettingJpaRepository.findByUserIdAndDatatableKeyOrderByIdAsc(5,
				EntityColumnConstants.ALL_TIMESHEET_PAGE))
			.willReturn(java.util.List.of(userSetting));

		String localeJson = "{}";
		String columnJson = "{\"contractorName\":{\"field\":\"contractorName\",\"visible\":true,\"listPageOrder\":1,"
				+ "\"visible_locked\":false,\"pinned\":null}}";

		try (MockedStatic<Files> filesMock = mockStatic(Files.class);
				MockedStatic<Paths> pathsMock = mockStatic(Paths.class);
				MockedConstruction<ClassPathResource> resourceMock = mockConstruction(ClassPathResource.class,
						(mock, context) -> given(mock.getURI()).willReturn(TEST_URI))) {

			given(Paths.get(TEST_URI)).willReturn(TEST_PATH);
			given(Files.readAllBytes(TEST_PATH)).willReturn(localeJson.getBytes()).willReturn(columnJson.getBytes());

			// when
			AccountViewColumnResponseBodyDto result = this.entityColumnService
				.getEntityColumns(EntityColumnConstants.ALL_TIMESHEET_PAGE);

			// then — the user's explicit pin survives
			ColumnAccountViewResponseBodyDto nameColumn = result.get("contractorName");
			assertThat(nameColumn).isNotNull();
			assertThat(nameColumn.getListPageOrder()).isEqualTo(3);
			assertThat(nameColumn.getPinned()).isEqualTo("right");
		}
		finally {
			ReflectionTestUtils.setField(this.entityColumnService, "objectMapper", this.objectMapper);
		}
	}

	@Test
	@DisplayName("Get entity columns - user own settings uses user settings not owner")
	void testGetEntityColumnsWithUserSettingsUsesUserSettings() throws IOException {
		// given
		ObjectMapper realObjectMapper = new ObjectMapper();
		ReflectionTestUtils.setField(this.entityColumnService, "objectMapper", realObjectMapper);

		willDoNothing().given(this.localeService).mergeLocaleLabelsIntoEntityColumns(any(), any());
		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getAuthenticationPrincipal()).willReturn(this.authenticationPrincipal);
		given(this.authenticationPrincipal.getUser()).willReturn(this.user);
		given(this.user.getLocale()).willReturn("en");
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(5);

		given(this.user.getAccount()).willReturn(this.account);
		given(this.account.getAccountOwner()).willReturn(1);

		// user has their own saved settings
		String userColumnStateJson = "{\"name\":{\"visible\":true,\"listPageOrder\":3}}";
		DataTableSetting userSetting = new DataTableSetting();
		userSetting.setColumnState(userColumnStateJson);
		given(this.dataTableSettingJpaRepository.findByUserIdAndDatatableKeyOrderByIdAsc(5,
				EntityColumnConstants.TIMESHEET_CONTRACTOR))
			.willReturn(java.util.List.of(userSetting));

		String localeJson = "{}";
		String columnJson = "{\"name\":{\"field\":\"name\",\"visible\":false,\"listPageOrder\":9,\"visible_locked\":false}}";

		try (MockedStatic<Files> filesMock = mockStatic(Files.class);
				MockedStatic<Paths> pathsMock = mockStatic(Paths.class);
				MockedConstruction<ClassPathResource> resourceMock = mockConstruction(ClassPathResource.class,
						(mock, context) -> given(mock.getURI()).willReturn(TEST_URI))) {

			given(Paths.get(TEST_URI)).willReturn(TEST_PATH);
			given(Files.readAllBytes(TEST_PATH)).willReturn(localeJson.getBytes()).willReturn(columnJson.getBytes());

			// when
			AccountViewColumnResponseBodyDto result = this.entityColumnService
				.getEntityColumns(EntityColumnConstants.TIMESHEET_CONTRACTOR);

			// then — user's own settings win; owner's locked view is never reached
			ColumnAccountViewResponseBodyDto nameColumn = result.get("name");
			assertThat(nameColumn).isNotNull();
			assertThat(nameColumn.getVisible()).isTrue();
			assertThat(nameColumn.getListPageOrder()).isEqualTo(3);
			verify(this.dataTableSettingJpaRepository, never()).findByUserIdAndDatatableKeyOrderByIdAsc(1,
					EntityColumnConstants.TIMESHEET_CONTRACTOR + EntityColumnConstants.OTHERS_VIEW_SUFFIX);
		}
		finally {
			ReflectionTestUtils.setField(this.entityColumnService, "objectMapper", this.objectMapper);
		}
	}

	@Test
	@DisplayName("Get account view columns - invalid column state JSON logs warning and returns base columns unchanged")
	void testGetAccountViewColumnsByEntityInvalidColumnStateJsonReturnsBaseColumnsUnchanged() throws IOException {
		// given — real ObjectMapper so the invalid JSON triggers the catch block
		ObjectMapper realObjectMapper = new ObjectMapper();
		ReflectionTestUtils.setField(this.entityColumnService, "objectMapper", realObjectMapper);

		willDoNothing().given(this.localeService).mergeLocaleLabelsIntoEntityColumns(any(), any());
		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getAuthenticationPrincipalRoleIdentifier()).willReturn(4);
		given(this.auth.getAuthenticationPrincipal()).willReturn(this.authenticationPrincipal);
		given(this.authenticationPrincipal.getUser()).willReturn(this.user);
		given(this.user.getLocale()).willReturn("en");
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(1);

		DataTableSetting savedSetting = new DataTableSetting();
		savedSetting.setColumnState("{invalid json}");
		given(this.dataTableSettingJpaRepository.findByUserIdAndDatatableKeyOrderByIdAsc(1,
				EntityColumnConstants.ALL_CONTRACTOR_PAGE + EntityColumnConstants.OTHERS_VIEW_SUFFIX))
			.willReturn(java.util.List.of(savedSetting));

		String localeJson = "{}";
		String columnJson = "{\"name\":{\"field\":\"name\",\"visible\":true,\"listPageOrder\":1}}";

		try (MockedStatic<Files> filesMock = mockStatic(Files.class);
				MockedStatic<Paths> pathsMock = mockStatic(Paths.class);
				MockedConstruction<ClassPathResource> resourceMock = mockConstruction(ClassPathResource.class,
						(mock, context) -> given(mock.getURI()).willReturn(TEST_URI))) {

			given(Paths.get(TEST_URI)).willReturn(TEST_PATH);
			given(Files.readAllBytes(TEST_PATH)).willReturn(localeJson.getBytes()).willReturn(columnJson.getBytes());

			// when
			AccountViewColumnResponseBodyDto result = this.entityColumnService
				.getAccountViewColumnsByEntity(EntityColumnConstants.ALL_CONTRACTOR_PAGE);

			// then — parse failure is swallowed; JSON defaults preserved
			ColumnAccountViewResponseBodyDto nameColumn = result.get("name");
			assertThat(nameColumn).isNotNull();
			assertThat(nameColumn.getVisible()).isTrue();
			assertThat(nameColumn.getListPageOrder()).isEqualTo(1);
		}
		finally {
			ReflectionTestUtils.setField(this.entityColumnService, "objectMapper", this.objectMapper);
		}
	}

	@Test
	@DisplayName("Get account view columns - file read error throws JsonReadException")
	void testGetAccountViewColumnsByEntityFileReadErrorThrowsJsonReadException() throws IOException {
		// given
		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getAuthenticationPrincipalRoleIdentifier()).willReturn(4);
		given(this.auth.getAuthenticationPrincipal()).willReturn(this.authenticationPrincipal);
		given(this.authenticationPrincipal.getUser()).willReturn(this.user);
		given(this.user.getLocale()).willReturn("en");

		try (MockedStatic<Files> filesMock = mockStatic(Files.class);
				MockedStatic<Paths> pathsMock = mockStatic(Paths.class);
				MockedConstruction<ClassPathResource> resourceMock = mockConstruction(ClassPathResource.class,
						(mock, context) -> given(mock.getURI()).willReturn(TEST_URI))) {

			given(Paths.get(TEST_URI)).willReturn(TEST_PATH);
			given(Files.readAllBytes(TEST_PATH)).willThrow(new IOException("File not found"));

			// when & then
			assertThatThrownBy(() -> this.entityColumnService
				.getAccountViewColumnsByEntity(EntityColumnConstants.ALL_CONTRACTOR_PAGE))
				.isInstanceOf(JsonReadException.class)
				.hasMessageContaining("Failed to read or parse entity columns file");
		}
	}

	// ===== LocaleService direct tests =====

	@Test
	@DisplayName("LocaleService - null entityColumns returns without throwing")
	void testLocaleServiceNullEntityColumnsReturnsEarly() {
		LocaleService sut = new LocaleService();
		LocaleResponseBodyDto locale = new LocaleResponseBodyDto();
		org.assertj.core.api.Assertions.assertThatCode(() -> sut.mergeLocaleLabelsIntoEntityColumns(null, locale))
			.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("LocaleService - null localeLabels leaves columns unchanged")
	void testLocaleServiceNullLocaleLabelsReturnsEarly() {
		LocaleService sut = new LocaleService();
		AccountViewColumnResponseBodyDto columns = new AccountViewColumnResponseBodyDto();
		ColumnAccountViewResponseBodyDto col = new ColumnAccountViewResponseBodyDto();
		col.setLabel("Original");
		columns.put("name", col);

		sut.mergeLocaleLabelsIntoEntityColumns(columns, null);

		assertThat(columns.get("name").getLabel()).isEqualTo("Original");
	}

	@Test
	@DisplayName("LocaleService - null localeValues (timesheet map) returns without modifying columns")
	void testLocaleServiceNullLocaleValuesReturnsEarly() {
		LocaleService sut = new LocaleService();
		AccountViewColumnResponseBodyDto columns = new AccountViewColumnResponseBodyDto();
		ColumnAccountViewResponseBodyDto col = new ColumnAccountViewResponseBodyDto();
		col.setLabel("Original");
		columns.put("name", col);

		sut.mergeLocaleLabelsIntoEntityColumns(columns, new LocaleResponseBodyDto());

		assertThat(columns.get("name").getLabel()).isEqualTo("Original");
	}

	@Test
	@DisplayName("LocaleService - matching locale entry updates label and longlabel")
	void testLocaleServiceMatchingEntryUpdatesColumn() {
		LocaleService sut = new LocaleService();
		AccountViewColumnResponseBodyDto columns = new AccountViewColumnResponseBodyDto();
		ColumnAccountViewResponseBodyDto col = new ColumnAccountViewResponseBodyDto();
		col.setLabel("Old");
		col.setLonglabel("Old Long");
		columns.put("name", col);

		LocaleResponseBodyDto locale = new LocaleResponseBodyDto(
				java.util.Map.of("name", new LabelResponseBodyDto("New", "New Long")));
		sut.mergeLocaleLabelsIntoEntityColumns(columns, locale);

		assertThat(col.getLabel()).isEqualTo("New");
		assertThat(col.getLonglabel()).isEqualTo("New Long");
	}

	@Test
	@DisplayName("LocaleService - no matching locale key leaves column unchanged")
	void testLocaleServiceNoMatchingKeyLeavesColumnUnchanged() {
		LocaleService sut = new LocaleService();
		AccountViewColumnResponseBodyDto columns = new AccountViewColumnResponseBodyDto();
		ColumnAccountViewResponseBodyDto col = new ColumnAccountViewResponseBodyDto();
		col.setLabel("Original");
		columns.put("name", col);

		LocaleResponseBodyDto locale = new LocaleResponseBodyDto(
				java.util.Map.of("other", new LabelResponseBodyDto("Other", "Other Long")));
		sut.mergeLocaleLabelsIntoEntityColumns(columns, locale);

		assertThat(col.getLabel()).isEqualTo("Original");
	}

	@Test
	@DisplayName("LocaleService - null label value skips setLabel")
	void testLocaleServiceNullLabelValueSkipsSetLabel() {
		LocaleService sut = new LocaleService();
		AccountViewColumnResponseBodyDto columns = new AccountViewColumnResponseBodyDto();
		ColumnAccountViewResponseBodyDto col = new ColumnAccountViewResponseBodyDto();
		col.setLabel("Original");
		col.setLonglabel("Original Long");
		columns.put("name", col);

		LocaleResponseBodyDto locale = new LocaleResponseBodyDto(
				java.util.Map.of("name", new LabelResponseBodyDto(null, "Updated Long")));
		sut.mergeLocaleLabelsIntoEntityColumns(columns, locale);

		assertThat(col.getLabel()).isEqualTo("Original");
		assertThat(col.getLonglabel()).isEqualTo("Updated Long");
	}

	@Test
	@DisplayName("LocaleService - null longlabel value skips setLonglabel")
	void testLocaleServiceNullLonglabelValueSkipsSetLonglabel() {
		LocaleService sut = new LocaleService();
		AccountViewColumnResponseBodyDto columns = new AccountViewColumnResponseBodyDto();
		ColumnAccountViewResponseBodyDto col = new ColumnAccountViewResponseBodyDto();
		col.setLabel("Original");
		col.setLonglabel("Original Long");
		columns.put("name", col);

		LocaleResponseBodyDto locale = new LocaleResponseBodyDto(
				java.util.Map.of("name", new LabelResponseBodyDto("Updated", null)));
		sut.mergeLocaleLabelsIntoEntityColumns(columns, locale);

		assertThat(col.getLabel()).isEqualTo("Updated");
		assertThat(col.getLonglabel()).isEqualTo("Original Long");
	}

	// ===== mergeColumnStateIntoBaseColumns — new field coverage =====

	@Test
	@DisplayName("Merge column state applies width, pinned, colId, sort, sortIndex from saved state")
	void testMergeColumnStateAppliesWidthPinnedColIdSortSortIndex() throws IOException {
		ObjectMapper realObjectMapper = new ObjectMapper();
		ReflectionTestUtils.setField(this.entityColumnService, "objectMapper", realObjectMapper);

		willDoNothing().given(this.localeService).mergeLocaleLabelsIntoEntityColumns(any(), any());
		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getAuthenticationPrincipal()).willReturn(this.authenticationPrincipal);
		given(this.authenticationPrincipal.getUser()).willReturn(this.user);
		given(this.user.getLocale()).willReturn("en");
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(1);
		given(this.user.getAccount()).willReturn(this.account);
		given(this.account.getAccountOwner()).willReturn(1);

		String columnStateJson = "{\"name\":{\"width\":150.5,\"pinned\":\"left\",\"colId\":\"custom_id\","
				+ "\"sort\":\"asc\",\"sortIndex\":0,\"listPageOrder\":3}}";
		DataTableSetting savedSetting = new DataTableSetting();
		savedSetting.setColumnState(columnStateJson);
		given(this.dataTableSettingJpaRepository.findByUserIdAndDatatableKeyOrderByIdAsc(1,
				EntityColumnConstants.TIMESHEET_CONTRACTOR))
			.willReturn(java.util.List.of(savedSetting));

		String localeJson = "{}";
		String columnJson = "{\"name\":{\"field\":\"name\",\"visible\":true,\"listPageOrder\":1,\"visible_locked\":false}}";

		try (MockedStatic<Files> filesMock = mockStatic(Files.class);
				MockedStatic<Paths> pathsMock = mockStatic(Paths.class);
				MockedConstruction<ClassPathResource> resourceMock = mockConstruction(ClassPathResource.class,
						(mock, context) -> given(mock.getURI()).willReturn(TEST_URI))) {

			given(Paths.get(TEST_URI)).willReturn(TEST_PATH);
			given(Files.readAllBytes(TEST_PATH)).willReturn(localeJson.getBytes()).willReturn(columnJson.getBytes());

			AccountViewColumnResponseBodyDto result = this.entityColumnService
				.getEntityColumns(EntityColumnConstants.TIMESHEET_CONTRACTOR);

			ColumnAccountViewResponseBodyDto nameColumn = result.get("name");
			assertThat(nameColumn).isNotNull();
			assertThat(nameColumn.getWidth()).isEqualTo(150.5);
			assertThat(nameColumn.getPinned()).isEqualTo("left");
			assertThat(nameColumn.getColId()).isEqualTo("custom_id");
			assertThat(nameColumn.getSort()).isEqualTo("asc");
			assertThat(nameColumn.getSortIndex()).isZero();
			assertThat(nameColumn.getListPageOrder()).isEqualTo(3);
		}
		finally {
			ReflectionTestUtils.setField(this.entityColumnService, "objectMapper", this.objectMapper);
		}
	}

	@Test
	@DisplayName("Merge column state unpins a default-pinned column when saved pinned is null")
	void testMergeColumnStateUnpinsWhenSavedPinnedIsNull() throws IOException {
		ObjectMapper realObjectMapper = new ObjectMapper();
		ReflectionTestUtils.setField(this.entityColumnService, "objectMapper", realObjectMapper);

		willDoNothing().given(this.localeService).mergeLocaleLabelsIntoEntityColumns(any(), any());
		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getAuthenticationPrincipal()).willReturn(this.authenticationPrincipal);
		given(this.authenticationPrincipal.getUser()).willReturn(this.user);
		given(this.user.getLocale()).willReturn("en");
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(1);
		given(this.user.getAccount()).willReturn(this.account);
		given(this.account.getAccountOwner()).willReturn(1);

		// saved state explicitly clears the pin (user unpinned the column)
		String columnStateJson = "{\"name\":{\"pinned\":null}}";
		DataTableSetting savedSetting = new DataTableSetting();
		savedSetting.setColumnState(columnStateJson);
		given(this.dataTableSettingJpaRepository.findByUserIdAndDatatableKeyOrderByIdAsc(1,
				EntityColumnConstants.TIMESHEET_CONTRACTOR))
			.willReturn(java.util.List.of(savedSetting));

		String localeJson = "{}";
		// base column is pinned-left by default
		String columnJson = "{\"name\":{\"field\":\"name\",\"visible\":true,\"listPageOrder\":1,"
				+ "\"visible_locked\":true,\"pinned\":\"left\"}}";

		try (MockedStatic<Files> filesMock = mockStatic(Files.class);
				MockedStatic<Paths> pathsMock = mockStatic(Paths.class);
				MockedConstruction<ClassPathResource> resourceMock = mockConstruction(ClassPathResource.class,
						(mock, context) -> given(mock.getURI()).willReturn(TEST_URI))) {

			given(Paths.get(TEST_URI)).willReturn(TEST_PATH);
			given(Files.readAllBytes(TEST_PATH)).willReturn(localeJson.getBytes()).willReturn(columnJson.getBytes());

			AccountViewColumnResponseBodyDto result = this.entityColumnService
				.getEntityColumns(EntityColumnConstants.TIMESHEET_CONTRACTOR);

			ColumnAccountViewResponseBodyDto nameColumn = result.get("name");
			assertThat(nameColumn).isNotNull();
			assertThat(nameColumn.getPinned()).isNull();
		}
		finally {
			ReflectionTestUtils.setField(this.entityColumnService, "objectMapper", this.objectMapper);
		}
	}

	@Test
	@DisplayName("Merge column state unpins a default-pinned column when saved pinned is the 'nopin' sentinel")
	void testMergeColumnStateUnpinsWhenSavedPinnedIsNopin() throws IOException {
		ObjectMapper realObjectMapper = new ObjectMapper();
		ReflectionTestUtils.setField(this.entityColumnService, "objectMapper", realObjectMapper);

		willDoNothing().given(this.localeService).mergeLocaleLabelsIntoEntityColumns(any(), any());
		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getAuthenticationPrincipal()).willReturn(this.authenticationPrincipal);
		given(this.authenticationPrincipal.getUser()).willReturn(this.user);
		given(this.user.getLocale()).willReturn("en");
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(1);
		given(this.user.getAccount()).willReturn(this.account);
		given(this.account.getAccountOwner()).willReturn(1);

		String columnStateJson = "{\"name\":{\"pinned\":\"nopin\"}}";
		DataTableSetting savedSetting = new DataTableSetting();
		savedSetting.setColumnState(columnStateJson);
		given(this.dataTableSettingJpaRepository.findByUserIdAndDatatableKeyOrderByIdAsc(1,
				EntityColumnConstants.TIMESHEET_CONTRACTOR))
			.willReturn(java.util.List.of(savedSetting));

		String localeJson = "{}";
		String columnJson = "{\"name\":{\"field\":\"name\",\"visible\":true,\"listPageOrder\":1,"
				+ "\"visible_locked\":true,\"pinned\":\"left\"}}";

		try (MockedStatic<Files> filesMock = mockStatic(Files.class);
				MockedStatic<Paths> pathsMock = mockStatic(Paths.class);
				MockedConstruction<ClassPathResource> resourceMock = mockConstruction(ClassPathResource.class,
						(mock, context) -> given(mock.getURI()).willReturn(TEST_URI))) {

			given(Paths.get(TEST_URI)).willReturn(TEST_PATH);
			given(Files.readAllBytes(TEST_PATH)).willReturn(localeJson.getBytes()).willReturn(columnJson.getBytes());

			AccountViewColumnResponseBodyDto result = this.entityColumnService
				.getEntityColumns(EntityColumnConstants.TIMESHEET_CONTRACTOR);

			ColumnAccountViewResponseBodyDto nameColumn = result.get("name");
			assertThat(nameColumn).isNotNull();
			assertThat(nameColumn.getPinned()).isNull();
		}
		finally {
			ReflectionTestUtils.setField(this.entityColumnService, "objectMapper", this.objectMapper);
		}
	}

	@Test
	@DisplayName("Merge column state does not apply visible when visibleLocked is true")
	void testMergeColumnStateDoesNotApplyVisibleWhenVisibleLocked() throws IOException {
		ObjectMapper realObjectMapper = new ObjectMapper();
		ReflectionTestUtils.setField(this.entityColumnService, "objectMapper", realObjectMapper);

		willDoNothing().given(this.localeService).mergeLocaleLabelsIntoEntityColumns(any(), any());
		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getAuthenticationPrincipal()).willReturn(this.authenticationPrincipal);
		given(this.authenticationPrincipal.getUser()).willReturn(this.user);
		given(this.user.getLocale()).willReturn("en");
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(1);
		given(this.user.getAccount()).willReturn(this.account);
		given(this.account.getAccountOwner()).willReturn(1);

		// saved state tries to hide the column
		String columnStateJson = "{\"name\":{\"visible\":false,\"listPageOrder\":9}}";
		DataTableSetting savedSetting = new DataTableSetting();
		savedSetting.setColumnState(columnStateJson);
		given(this.dataTableSettingJpaRepository.findByUserIdAndDatatableKeyOrderByIdAsc(1,
				EntityColumnConstants.TIMESHEET_CONTRACTOR))
			.willReturn(java.util.List.of(savedSetting));

		String localeJson = "{}";
		// column is locked — visible must stay true regardless of saved state
		String columnJson = "{\"name\":{\"field\":\"name\",\"visible\":true,\"listPageOrder\":1,\"visible_locked\":true}}";

		try (MockedStatic<Files> filesMock = mockStatic(Files.class);
				MockedStatic<Paths> pathsMock = mockStatic(Paths.class);
				MockedConstruction<ClassPathResource> resourceMock = mockConstruction(ClassPathResource.class,
						(mock, context) -> given(mock.getURI()).willReturn(TEST_URI))) {

			given(Paths.get(TEST_URI)).willReturn(TEST_PATH);
			given(Files.readAllBytes(TEST_PATH)).willReturn(localeJson.getBytes()).willReturn(columnJson.getBytes());

			AccountViewColumnResponseBodyDto result = this.entityColumnService
				.getEntityColumns(EntityColumnConstants.TIMESHEET_CONTRACTOR);

			ColumnAccountViewResponseBodyDto nameColumn = result.get("name");
			assertThat(nameColumn).isNotNull();
			assertThat(nameColumn.getVisible()).isTrue();
			assertThat(nameColumn.getListPageOrder()).isEqualTo(9);
		}
		finally {
			ReflectionTestUtils.setField(this.entityColumnService, "objectMapper", this.objectMapper);
		}
	}

	@Test
	@DisplayName("Get entity columns - USER principal with blank column state returns base columns unchanged")
	void testGetEntityColumnsUserPrincipalWithBlankColumnStateReturnsBaseColumnsUnchanged() throws IOException {
		ObjectMapper realObjectMapper = new ObjectMapper();
		ReflectionTestUtils.setField(this.entityColumnService, "objectMapper", realObjectMapper);

		willDoNothing().given(this.localeService).mergeLocaleLabelsIntoEntityColumns(any(), any());
		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getAuthenticationPrincipal()).willReturn(this.authenticationPrincipal);
		given(this.authenticationPrincipal.getUser()).willReturn(this.user);
		given(this.user.getLocale()).willReturn("en");
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(1);
		given(this.user.getAccount()).willReturn(this.account);
		given(this.account.getAccountOwner()).willReturn(1);

		DataTableSetting savedSetting = new DataTableSetting();
		savedSetting.setColumnState("   ");
		given(this.dataTableSettingJpaRepository.findByUserIdAndDatatableKeyOrderByIdAsc(1,
				EntityColumnConstants.TIMESHEET_CONTRACTOR))
			.willReturn(java.util.List.of(savedSetting));

		String localeJson = "{}";
		String columnJson = "{\"name\":{\"field\":\"name\",\"visible\":true,\"listPageOrder\":3}}";

		try (MockedStatic<Files> filesMock = mockStatic(Files.class);
				MockedStatic<Paths> pathsMock = mockStatic(Paths.class);
				MockedConstruction<ClassPathResource> resourceMock = mockConstruction(ClassPathResource.class,
						(mock, context) -> given(mock.getURI()).willReturn(TEST_URI))) {

			given(Paths.get(TEST_URI)).willReturn(TEST_PATH);
			given(Files.readAllBytes(TEST_PATH)).willReturn(localeJson.getBytes()).willReturn(columnJson.getBytes());

			AccountViewColumnResponseBodyDto result = this.entityColumnService
				.getEntityColumns(EntityColumnConstants.TIMESHEET_CONTRACTOR);

			ColumnAccountViewResponseBodyDto nameColumn = result.get("name");
			assertThat(nameColumn).isNotNull();
			assertThat(nameColumn.getVisible()).isTrue();
			assertThat(nameColumn.getListPageOrder()).isEqualTo(3);
		}
		finally {
			ReflectionTestUtils.setField(this.entityColumnService, "objectMapper", this.objectMapper);
		}
	}

	@Test
	@DisplayName("Get account view columns - blank column state returns base columns unchanged")
	void testGetAccountViewColumnsByEntityWithBlankColumnStateReturnsBaseColumnsUnchanged() throws IOException {
		ObjectMapper realObjectMapper = new ObjectMapper();
		ReflectionTestUtils.setField(this.entityColumnService, "objectMapper", realObjectMapper);

		willDoNothing().given(this.localeService).mergeLocaleLabelsIntoEntityColumns(any(), any());
		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getAuthenticationPrincipalRoleIdentifier()).willReturn(4);
		given(this.auth.getAuthenticationPrincipal()).willReturn(this.authenticationPrincipal);
		given(this.authenticationPrincipal.getUser()).willReturn(this.user);
		given(this.user.getLocale()).willReturn("en");
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(1);

		DataTableSetting savedSetting = new DataTableSetting();
		savedSetting.setColumnState("   ");
		given(this.dataTableSettingJpaRepository.findByUserIdAndDatatableKeyOrderByIdAsc(1,
				EntityColumnConstants.ALL_CONTRACTOR_PAGE + EntityColumnConstants.OTHERS_VIEW_SUFFIX))
			.willReturn(java.util.List.of(savedSetting));

		String localeJson = "{}";
		String columnJson = "{\"name\":{\"field\":\"name\",\"visible\":true,\"listPageOrder\":3}}";

		try (MockedStatic<Files> filesMock = mockStatic(Files.class);
				MockedStatic<Paths> pathsMock = mockStatic(Paths.class);
				MockedConstruction<ClassPathResource> resourceMock = mockConstruction(ClassPathResource.class,
						(mock, context) -> given(mock.getURI()).willReturn(TEST_URI))) {

			given(Paths.get(TEST_URI)).willReturn(TEST_PATH);
			given(Files.readAllBytes(TEST_PATH)).willReturn(localeJson.getBytes()).willReturn(columnJson.getBytes());

			AccountViewColumnResponseBodyDto result = this.entityColumnService
				.getAccountViewColumnsByEntity(EntityColumnConstants.ALL_CONTRACTOR_PAGE);

			ColumnAccountViewResponseBodyDto nameColumn = result.get("name");
			assertThat(nameColumn).isNotNull();
			assertThat(nameColumn.getVisible()).isTrue();
			assertThat(nameColumn.getListPageOrder()).isEqualTo(3);
		}
		finally {
			ReflectionTestUtils.setField(this.entityColumnService, "objectMapper", this.objectMapper);
		}
	}

	@Test
	@DisplayName("EntityColumnConstants - private constructor throws UnsupportedOperationException")
	void testEntityColumnConstantsPrivateConstructorThrowsUnsupportedOperationException() throws Exception {
		Constructor<EntityColumnConstants> constructor = EntityColumnConstants.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		assertThatThrownBy(constructor::newInstance).isInstanceOf(InvocationTargetException.class)
			.hasCauseInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	@DisplayName("getLocaleForEntity - portal entity with null unified principal falls back to user locale")
	void testGetLocaleForEntityPortalEntityNullPrincipalFallsBackToUserLocale() throws Exception {
		given(this.auth.getUnifiedPrincipal()).willReturn(null);
		given(this.auth.getAuthenticationPrincipal()).willReturn(this.authenticationPrincipal);
		given(this.authenticationPrincipal.getUser()).willReturn(this.user);
		given(this.user.getLocale()).willReturn("fr");

		Method method = EntityColumnService.class.getDeclaredMethod("getLocaleForEntity", String.class);
		method.setAccessible(true);
		String result = (String) method.invoke(this.entityColumnService, EntityColumnConstants.CONTRACTOR);

		assertThat(result).isEqualTo("fr");
	}

	@Test
	@DisplayName("getLocaleForEntity - portal entity with non-portal principal type falls back to user locale")
	void testGetLocaleForEntityPortalEntityNonPortalPrincipalFallsBackToUserLocale() throws Exception {
		given(this.auth.getUnifiedPrincipal()).willReturn(this.unifiedPrincipal);
		given(this.unifiedPrincipal.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getAuthenticationPrincipal()).willReturn(this.authenticationPrincipal);
		given(this.authenticationPrincipal.getUser()).willReturn(this.user);
		given(this.user.getLocale()).willReturn("de");

		Method method = EntityColumnService.class.getDeclaredMethod("getLocaleForEntity", String.class);
		method.setAccessible(true);
		String result = (String) method.invoke(this.entityColumnService, EntityColumnConstants.CONTRACTOR);

		assertThat(result).isEqualTo("de");
	}

	@Test
	@DisplayName("Get entity columns - null saved settings skips merge and returns base columns unchanged")
	void testGetEntityColumnsNullSavedSettingsSkipsMergeReturnsBaseColumnsUnchanged() throws IOException {
		ObjectMapper realObjectMapper = new ObjectMapper();
		ReflectionTestUtils.setField(this.entityColumnService, "objectMapper", realObjectMapper);

		willDoNothing().given(this.localeService).mergeLocaleLabelsIntoEntityColumns(any(), any());
		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getAuthenticationPrincipal()).willReturn(this.authenticationPrincipal);
		given(this.authenticationPrincipal.getUser()).willReturn(this.user);
		given(this.user.getLocale()).willReturn("en");
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(1);
		given(this.user.getAccount()).willReturn(this.account);
		given(this.account.getAccountOwner()).willReturn(1);
		given(this.dataTableSettingJpaRepository.findByUserIdAndDatatableKeyOrderByIdAsc(1,
				EntityColumnConstants.TIMESHEET_CONTRACTOR))
			.willReturn(null);

		String localeJson = "{}";
		String columnJson = "{\"name\":{\"field\":\"name\",\"visible\":true,\"listPageOrder\":1}}";

		try (MockedStatic<Files> filesMock = mockStatic(Files.class);
				MockedStatic<Paths> pathsMock = mockStatic(Paths.class);
				MockedConstruction<ClassPathResource> resourceMock = mockConstruction(ClassPathResource.class,
						(mock, context) -> given(mock.getURI()).willReturn(TEST_URI))) {

			given(Paths.get(TEST_URI)).willReturn(TEST_PATH);
			given(Files.readAllBytes(TEST_PATH)).willReturn(localeJson.getBytes()).willReturn(columnJson.getBytes());

			AccountViewColumnResponseBodyDto result = this.entityColumnService
				.getEntityColumns(EntityColumnConstants.TIMESHEET_CONTRACTOR);

			ColumnAccountViewResponseBodyDto nameColumn = result.get("name");
			assertThat(nameColumn).isNotNull();
			assertThat(nameColumn.getVisible()).isTrue();
			assertThat(nameColumn.getListPageOrder()).isEqualTo(1);
		}
		finally {
			ReflectionTestUtils.setField(this.entityColumnService, "objectMapper", this.objectMapper);
		}
	}

	@Test
	@DisplayName("Get account view columns - null saved settings skips merge and returns base columns unchanged")
	void testGetAccountViewColumnsByEntityNullSavedSettingsSkipsMergeReturnsBaseColumnsUnchanged() throws IOException {
		ObjectMapper realObjectMapper = new ObjectMapper();
		ReflectionTestUtils.setField(this.entityColumnService, "objectMapper", realObjectMapper);

		willDoNothing().given(this.localeService).mergeLocaleLabelsIntoEntityColumns(any(), any());
		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getAuthenticationPrincipalRoleIdentifier()).willReturn(4);
		given(this.auth.getAuthenticationPrincipal()).willReturn(this.authenticationPrincipal);
		given(this.authenticationPrincipal.getUser()).willReturn(this.user);
		given(this.user.getLocale()).willReturn("en");
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(1);
		given(this.dataTableSettingJpaRepository.findByUserIdAndDatatableKeyOrderByIdAsc(1,
				EntityColumnConstants.ALL_CONTRACTOR_PAGE + EntityColumnConstants.OTHERS_VIEW_SUFFIX))
			.willReturn(null);

		String localeJson = "{}";
		String columnJson = "{\"name\":{\"field\":\"name\",\"visible\":true,\"listPageOrder\":1}}";

		try (MockedStatic<Files> filesMock = mockStatic(Files.class);
				MockedStatic<Paths> pathsMock = mockStatic(Paths.class);
				MockedConstruction<ClassPathResource> resourceMock = mockConstruction(ClassPathResource.class,
						(mock, context) -> given(mock.getURI()).willReturn(TEST_URI))) {

			given(Paths.get(TEST_URI)).willReturn(TEST_PATH);
			given(Files.readAllBytes(TEST_PATH)).willReturn(localeJson.getBytes()).willReturn(columnJson.getBytes());

			AccountViewColumnResponseBodyDto result = this.entityColumnService
				.getAccountViewColumnsByEntity(EntityColumnConstants.ALL_CONTRACTOR_PAGE);

			ColumnAccountViewResponseBodyDto nameColumn = result.get("name");
			assertThat(nameColumn).isNotNull();
			assertThat(nameColumn.getVisible()).isTrue();
			assertThat(nameColumn.getListPageOrder()).isEqualTo(1);
		}
		finally {
			ReflectionTestUtils.setField(this.entityColumnService, "objectMapper", this.objectMapper);
		}
	}

	@Test
	@DisplayName("Merge column state - absent listPageOrder in saved state leaves listPageOrder unchanged")
	void testMergeColumnStateAbsentListPageOrderLeavesListPageOrderUnchanged() throws IOException {
		ObjectMapper realObjectMapper = new ObjectMapper();
		ReflectionTestUtils.setField(this.entityColumnService, "objectMapper", realObjectMapper);

		willDoNothing().given(this.localeService).mergeLocaleLabelsIntoEntityColumns(any(), any());
		given(this.auth.getPrincipalType()).willReturn(PrincipalType.USER);
		given(this.auth.getAuthenticationPrincipal()).willReturn(this.authenticationPrincipal);
		given(this.authenticationPrincipal.getUser()).willReturn(this.user);
		given(this.user.getLocale()).willReturn("en");
		given(this.auth.getAuthenticationPrincipalUniqueIdentifier()).willReturn(1);
		given(this.user.getAccount()).willReturn(this.account);
		given(this.account.getAccountOwner()).willReturn(1);

		// colState has visible but NO listPageOrder — drives the false branch of
		// listPageOrder instanceof Number
		String columnStateJson = "{\"name\":{\"visible\":false}}";
		DataTableSetting savedSetting = new DataTableSetting();
		savedSetting.setColumnState(columnStateJson);
		given(this.dataTableSettingJpaRepository.findByUserIdAndDatatableKeyOrderByIdAsc(1,
				EntityColumnConstants.TIMESHEET_CONTRACTOR))
			.willReturn(java.util.List.of(savedSetting));

		String localeJson = "{}";
		String columnJson = "{\"name\":{\"field\":\"name\",\"visible\":true,\"listPageOrder\":5,\"visible_locked\":false}}";

		try (MockedStatic<Files> filesMock = mockStatic(Files.class);
				MockedStatic<Paths> pathsMock = mockStatic(Paths.class);
				MockedConstruction<ClassPathResource> resourceMock = mockConstruction(ClassPathResource.class,
						(mock, context) -> given(mock.getURI()).willReturn(TEST_URI))) {

			given(Paths.get(TEST_URI)).willReturn(TEST_PATH);
			given(Files.readAllBytes(TEST_PATH)).willReturn(localeJson.getBytes()).willReturn(columnJson.getBytes());

			AccountViewColumnResponseBodyDto result = this.entityColumnService
				.getEntityColumns(EntityColumnConstants.TIMESHEET_CONTRACTOR);

			ColumnAccountViewResponseBodyDto nameColumn = result.get("name");
			assertThat(nameColumn).isNotNull();
			// visible updated from saved state (lock is false)
			assertThat(nameColumn.getVisible()).isFalse();
			// listPageOrder absent from saved state — JSON default preserved
			assertThat(nameColumn.getListPageOrder()).isEqualTo(5);
		}
		finally {
			ReflectionTestUtils.setField(this.entityColumnService, "objectMapper", this.objectMapper);
		}
	}

}