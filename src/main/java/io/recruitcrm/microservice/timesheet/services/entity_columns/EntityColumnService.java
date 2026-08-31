package io.recruitcrm.microservice.timesheet.services.entity_columns;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.recruitcrm.entity.model.DataTableSetting;
import io.recruitcrm.entity.model.User;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.configuration.auth.IAuthenticationPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.AuthPrincipal;
import io.recruitcrm.microservice.timesheet.configuration.auth.principal.PrincipalType;
import io.recruitcrm.microservice.timesheet.dao.datatable_setting.DataTableSettingJpaRepository;
import io.recruitcrm.microservice.timesheet.dto.entity_columns.AccountViewColumnResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.entity_columns.ColumnAccountViewResponseBodyDto;
import io.recruitcrm.microservice.timesheet.dto.entity_columns.locale.LocaleResponseBodyDto;
import io.recruitcrm.microservice.timesheet.exceptions.JsonReadException;
import io.recruitcrm.microservice.timesheet.exceptions.UnauthorizedAccessException;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import io.recruitcrm.microservice.timesheet.helpers.entity.EntityColumnConstants;
import io.recruitcrm.microservice.timesheet.services.locale.LocaleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class EntityColumnService implements IEntityColumnService {

	private static final Logger LOGGER = LoggerFactory.getLogger(EntityColumnService.class);

	private final ObjectMapper objectMapper;

	private final LocaleService localeService;

	final AuthHolder auth;

	private final DataTableSettingJpaRepository dataTableSettingJpaRepository;

	private static final int ACCOUNT_OWNER_ROLE_ID = 4;

	private static final String FILE_EXTENSION = ".json";

	private static final String DEFAULT_LOCALE = "en";

	private static final Set<String> OTHERS_VIEW_ALLOWED_ENTITIES = Set.of(EntityColumnConstants.ALL_CONTRACTOR_PAGE,
			EntityColumnConstants.ALL_TIMESHEET_PAGE, EntityColumnConstants.TIMESHEET_CONTRACTOR);

	private final Map<String, String> entityToFileMap;

	public EntityColumnService(AuthHolder auth, ObjectMapper objectMapper, LocaleService localeService,
			DataTableSettingJpaRepository dataTableSettingJpaRepository) {
		this.objectMapper = objectMapper;
		this.auth = auth;
		this.localeService = localeService;
		this.dataTableSettingJpaRepository = dataTableSettingJpaRepository;
		// Map entity types to their corresponding JSON files
		// contractor_portal -> entity_columns_contractor_portal.json
		// client_portal -> entity_columns_client_portal.json
		// all_timesheet_page -> entity_columns_all_timesheet_page.json
		// all_contractor_page -> entity_columns_all_contractor_page.json
		this.entityToFileMap = Map.of(EntityColumnConstants.TIMESHEET_CONTRACTOR,
				EntityColumnConstants.ENTITY_COLUMNS_CONTRACTOR_JSON, EntityColumnConstants.TIMESHEET_DEAL,
				EntityColumnConstants.ENTITY_COLUMNS_DEAL_JSON, EntityColumnConstants.CONTRACTOR,
				EntityColumnConstants.ENTITY_COLUMNS_CONTRACTOR_PORTAL_JSON, EntityColumnConstants.CLIENT,
				EntityColumnConstants.ENTITY_COLUMNS_CLIENT_PORTAL_JSON, EntityColumnConstants.ALL_TIMESHEET_PAGE,
				EntityColumnConstants.ENTITY_COLUMNS_ALL_TIMESHEET_PAGE, EntityColumnConstants.ALL_CONTRACTOR_PAGE,
				EntityColumnConstants.ENTITY_COLUMNS_ALL_CONTRACTOR_PAGE);
	}

	@Override
	public AccountViewColumnResponseBodyDto getEntityColumns(String entity) {
		// Validate persona access for portal entities
		validatePersonaAccess(entity);

		String fileName = this.entityToFileMap.get(entity);

		if (fileName == null) {
			throw new ValidationErrorException("Invalid entity: " + entity);
		}

		String locale = getLocaleForEntity(entity);

		AccountViewColumnResponseBodyDto baseColumns = prepareEntityColumnsUsingLocale(locale, fileName);

		// Apply saved DataTableSetting for USER persona only (portal users have no saved
		// column state)
		if (this.auth.getPrincipalType() == PrincipalType.USER) {
			applyUserPersonaColumnSettings(baseColumns, entity);
		}

		return baseColumns;
	}

	private void applyUserPersonaColumnSettings(AccountViewColumnResponseBodyDto baseColumns, String entity) {
		Integer userId = this.auth.getAuthenticationPrincipalUniqueIdentifier();
		User user = this.auth.getAuthenticationPrincipal().getUser();
		Integer ownerId = user.getAccount().getAccountOwner();
		boolean isNonOwner = !Objects.equals(userId, ownerId);

		List<DataTableSetting> savedSettings = this.dataTableSettingJpaRepository
			.findByUserIdAndDatatableKeyOrderByIdAsc(userId, entity);

		boolean isOthersViewFallback = false;
		if ((savedSettings == null || savedSettings.isEmpty()) && isNonOwner) {
			savedSettings = this.dataTableSettingJpaRepository.findByUserIdAndDatatableKeyOrderByIdAsc(ownerId,
					entity + EntityColumnConstants.OTHERS_VIEW_SUFFIX);
			isOthersViewFallback = true;
		}

		boolean neutralizeDefaultPins = isNonOwner && !isOthersViewFallback;
		if (neutralizeDefaultPins) {
			stripPinnedStateFromColumns(baseColumns);
		}

		if (savedSettings != null && !savedSettings.isEmpty()) {
			DataTableSetting latestSetting = savedSettings.get(savedSettings.size() - 1);
			mergeColumnStateIntoBaseColumns(baseColumns, latestSetting.getColumnState());
			if (isOthersViewFallback) {
				stripPinnedStateFromColumns(baseColumns);
			}
		}
	}

	/**
	 * Validate persona access only for portal entities Portal entities require specific
	 * personas, non-portal entities work as before without validation
	 */
	private void validatePersonaAccess(String entity) {
		// Only validate portal entities
		if (EntityColumnConstants.CONTRACTOR.equals(entity)) {
			AuthPrincipal principal = this.auth.getUnifiedPrincipal();
			if (principal == null || principal.getPrincipalType() != PrincipalType.CONTRACTOR) {
				throw new UnauthorizedAccessException("Only contractors can access contractor portal entity columns");
			}
		}
		else if (EntityColumnConstants.CLIENT.equals(entity)) {
			AuthPrincipal principal = this.auth.getUnifiedPrincipal();
			if (principal == null || principal.getPrincipalType() != PrincipalType.CONTACT) {
				throw new UnauthorizedAccessException("Only contacts can access client portal entity columns");
			}
		}
		// For non-portal entities (timesheet_contractor, timesheet_deal), no validation
		// needed - work as before
	}

	/**
	 * Get locale based on entity type For portal entities (contractor_portal,
	 * client_portal), use portal-specific logic For non-portal entities, use User locale
	 * as before
	 */
	private String getLocaleForEntity(String entity) {
		// Check if it's a portal entity
		boolean isPortalEntity = EntityColumnConstants.CONTRACTOR.equals(entity)
				|| EntityColumnConstants.CLIENT.equals(entity);

		if (isPortalEntity) {
			// For portal entities, check principal type
			AuthPrincipal principal = this.auth.getUnifiedPrincipal();
			if (principal != null && (principal.getPrincipalType() == PrincipalType.CONTRACTOR
					|| principal.getPrincipalType() == PrincipalType.CONTACT)) {
				// Portal users use default locale
				return DEFAULT_LOCALE;
			}
		}

		// For non-portal entities or USER persona, get locale from User entity (works as
		// before)
		IAuthenticationPrincipal<Integer, User> authenticationPrincipal = this.auth.getAuthenticationPrincipal();
		User authUser = authenticationPrincipal.getUser();
		return authUser.getLocale();
	}

	@Override
	public AccountViewColumnResponseBodyDto getAccountViewColumnsByEntity(String entity) {
		if (this.auth.getPrincipalType() != PrincipalType.USER) {
			throw new UnauthorizedAccessException("Only RCRM users can access account view columns");
		}

		Integer roleId = this.auth.getAuthenticationPrincipalRoleIdentifier();
		if (!Objects.equals(roleId, ACCOUNT_OWNER_ROLE_ID)) {
			throw new UnauthorizedAccessException("Only account owner can access account view columns");
		}

		if (!OTHERS_VIEW_ALLOWED_ENTITIES.contains(entity)) {
			throw new ValidationErrorException("Others view not supported for entity: " + entity);
		}

		String fileName = this.entityToFileMap.get(entity);
		String locale = getLocaleForEntity(entity);
		AccountViewColumnResponseBodyDto baseColumns = prepareEntityColumnsUsingLocale(locale, fileName);
		stripLayoutStateFromColumns(baseColumns);

		Integer userId = this.auth.getAuthenticationPrincipalUniqueIdentifier();
		String othersViewKey = entity + EntityColumnConstants.OTHERS_VIEW_SUFFIX;
		List<DataTableSetting> savedSettings = this.dataTableSettingJpaRepository
			.findByUserIdAndDatatableKeyOrderByIdAsc(userId, othersViewKey);

		if (savedSettings != null && !savedSettings.isEmpty()) {
			DataTableSetting latestSetting = savedSettings.get(savedSettings.size() - 1);
			mergeColumnStateIntoBaseColumns(baseColumns, latestSetting.getColumnState());
		}

		stripPinnedStateFromColumns(baseColumns);

		return baseColumns;
	}

	private void stripLayoutStateFromColumns(AccountViewColumnResponseBodyDto columns) {
		for (ColumnAccountViewResponseBodyDto column : columns.values()) {
			column.setWidth(null);
			column.setPinned(null);
			column.setSort(null);
			column.setSortIndex(null);
			column.setColId(null);
		}
	}

	private void stripPinnedStateFromColumns(AccountViewColumnResponseBodyDto columns) {
		for (ColumnAccountViewResponseBodyDto column : columns.values()) {
			column.setPinned(null);
		}
	}

	private void mergeColumnStateIntoBaseColumns(AccountViewColumnResponseBodyDto baseColumns, String columnStateJson) {
		if (columnStateJson == null || columnStateJson.isBlank()) {
			return;
		}
		try {
			Map<String, Map<String, Object>> columnStateMap = this.objectMapper.readValue(columnStateJson,
					new TypeReference<Map<String, Map<String, Object>>>() {
					});

			for (Map.Entry<String, ColumnAccountViewResponseBodyDto> entry : baseColumns.entrySet()) {
				Map<String, Object> colState = columnStateMap.get(entry.getKey());
				if (colState == null) {
					continue;
				}
				applyColumnStateToColumn(entry.getValue(), colState);
			}
		}
		catch (Exception ex) {
			LOGGER.warn("Failed to parse saved column state, returning base columns unchanged: {}", ex.getMessage());
		}
	}

	private void applyColumnStateToColumn(ColumnAccountViewResponseBodyDto column, Map<String, Object> colState) {
		if (Boolean.FALSE.equals(column.getVisibleLocked())) {
			Object visible = colState.get("visible");
			if (visible instanceof Boolean visibleBool) {
				column.setVisible(visibleBool);
			}
		}

		Object listPageOrder = colState.get("listPageOrder");
		if (listPageOrder instanceof Number listPageOrderNum) {
			column.setListPageOrder(listPageOrderNum.intValue());
		}

		Object width = colState.get("width");
		if (width instanceof Number widthNum) {
			column.setWidth(widthNum.doubleValue());
		}

		// Apply pinned whenever the saved state carries the key, including an explicit
		// null / "nopin" sentinel. This lets a user unpin a column that is pinned by
		// default in the base JSON; otherwise the
		// base default would always re-pin it on reload.
		if (colState.containsKey("pinned")) {
			Object pinned = colState.get("pinned");
			if (pinned instanceof String pinnedStr) {
				column.setPinned("nopin".equals(pinnedStr) ? null : pinnedStr);
			}
			else {
				column.setPinned(null);
			}
		}

		Object colId = colState.get("colId");
		if (colId instanceof String colIdStr) {
			column.setColId(colIdStr);
		}

		Object sort = colState.get("sort");
		if (sort instanceof String sortStr) {
			column.setSort(sortStr);
		}

		Object sortIndex = colState.get("sortIndex");
		if (sortIndex instanceof Number sortIndexNum) {
			column.setSortIndex(sortIndexNum.intValue());
		}
	}

	private AccountViewColumnResponseBodyDto prepareEntityColumnsUsingLocale(String locale,
			String entityColumnFileName) {

		try {
			String localeFileName = locale + FILE_EXTENSION;

			ClassPathResource localeResource = new ClassPathResource(localeFileName);
			String jsonStrings = new String(Files.readAllBytes(Paths.get(localeResource.getURI())));
			LocaleResponseBodyDto localeJson = this.objectMapper.readValue(jsonStrings, LocaleResponseBodyDto.class);

			ClassPathResource entityColumnResource = new ClassPathResource(entityColumnFileName);
			String jsonString = new String(Files.readAllBytes(Paths.get(entityColumnResource.getURI())));
			AccountViewColumnResponseBodyDto entityColumnJson = this.objectMapper.readValue(jsonString,
					AccountViewColumnResponseBodyDto.class);

			this.localeService.mergeLocaleLabelsIntoEntityColumns(entityColumnJson, localeJson);

			return entityColumnJson;
		}
		catch (IOException exception) {
			throw new JsonReadException("Failed to read or parse entity columns file", exception);
		}
	}

}