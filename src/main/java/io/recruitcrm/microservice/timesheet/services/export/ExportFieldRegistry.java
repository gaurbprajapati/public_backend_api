package io.recruitcrm.microservice.timesheet.services.export;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.recruitcrm.microservice.timesheet.configuration.auth.AuthHolder;
import io.recruitcrm.microservice.timesheet.dto.export.ExportFieldConfig;
import io.recruitcrm.microservice.timesheet.dto.export.ExportFieldDefinition;
import io.recruitcrm.microservice.timesheet.dto.export.FieldDefinitionJson;
import io.recruitcrm.microservice.timesheet.dto.extra_fields.ExtraFieldDefinitionDto;
import io.recruitcrm.microservice.timesheet.exceptions.ValidationErrorException;
import io.recruitcrm.microservice.timesheet.helpers.enums.EntityType;
import io.recruitcrm.microservice.timesheet.repositories.extra_fields.IExtraFieldsRepository;

import org.jooq.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Registry that manages export fields with fully lazy loading optimization. Nothing is
 * loaded at startup - JSON config and field definitions are loaded only when first
 * requested and cached for future use. This approach maximizes startup performance.
 */
@Component
public class ExportFieldRegistry {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExportFieldRegistry.class);

	// Cache for already loaded field definitions (thread-safe)
	private final Map<String, ExportFieldDefinition> fieldDefinitionsCache = new ConcurrentHashMap<>();

	// Cache for JSON configuration (loaded on first access)
	private final Object jsonConfigLock = new Object();

	private ExportFieldConfig jsonConfig;

	private final ObjectMapper objectMapper;

	private final JooqFieldExpressionParser expressionParser;

	private final AdvancedJooqExpressionParser advancedParser;

	private final IExtraFieldsRepository extraFieldsRepository;

	private final AuthHolder authHolder;

	public ExportFieldRegistry(JooqFieldExpressionParser expressionParser, AdvancedJooqExpressionParser advancedParser,
			IExtraFieldsRepository extraFieldsRepository, AuthHolder authHolder) {
		this.objectMapper = new ObjectMapper();
		this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		this.expressionParser = expressionParser;
		this.advancedParser = advancedParser;
		this.extraFieldsRepository = extraFieldsRepository;
		this.authHolder = authHolder;
	}

	/**
	 * Get field definitions for the specified frontend field names (lazy loading)
	 */
	public List<ExportFieldDefinition> getFieldDefinitions(List<String> frontendFieldNames) {
		return frontendFieldNames.stream().map(this::getFieldDefinitionLazily).filter(Objects::nonNull).toList();
	}

	/**
	 * Get a specific field definition by frontend name (lazy loading)
	 */
	public ExportFieldDefinition getFieldDefinition(String frontendName) {
		return this.getFieldDefinitionLazily(frontendName);
	}

	/**
	 * Lazy loading implementation - loads and caches field definitions on-demand
	 */
	private ExportFieldDefinition getFieldDefinitionLazily(String frontendName) {
		// Check cache first
		if (this.fieldDefinitionsCache.containsKey(frontendName)) {
			return this.fieldDefinitionsCache.get(frontendName);
		}

		// Find field in JSON config (loads JSON on first access)
		FieldDefinitionJson jsonField = this.findJsonFieldByName(frontendName);
		if (jsonField != null && jsonField.isEnabled()) {
			// Parse JOOQ expression only when needed (expensive operation)
			ExportFieldDefinition definition = this.convertJsonToFieldDefinition(jsonField);

			// Cache for future use
			this.fieldDefinitionsCache.put(frontendName, definition);

			return definition;
		}

		// Handle dynamic custom column fields (custcolumn1, custcolumn2, etc.)
		if (frontendName.startsWith("custcolumn")) {
			ExportFieldDefinition customFieldDefinition = this.createCustomColumnFieldDefinition(frontendName);
			if (customFieldDefinition != null) {
				// Cache for future use
				this.fieldDefinitionsCache.put(frontendName, customFieldDefinition);
				return customFieldDefinition;
			}
		}

		// Field not found
		return null;
	}

	/**
	 * Load JSON configuration on-demand (thread-safe lazy initialization)
	 */
	private ExportFieldConfig getJsonConfig() {
		synchronized (this.jsonConfigLock) {
			if (this.jsonConfig == null) {
				try {
					ClassPathResource resource = new ClassPathResource("export-field-definitions.json");
					this.jsonConfig = this.objectMapper.readValue(resource.getInputStream(), ExportFieldConfig.class);
				}
				catch (IOException exception) {
					StringWriter sw = new StringWriter();
					exception.printStackTrace(new PrintWriter(sw));
					LOGGER.error("exception occurred inside getJsonConfig() method :: {}", sw);
					this.jsonConfig = new ExportFieldConfig(); // Empty config as
																// fallback
				}
			}
			return this.jsonConfig;
		}
	}

	/**
	 * Find a field definition in the JSON config by frontend name
	 */
	private FieldDefinitionJson findJsonFieldByName(String frontendName) {
		ExportFieldConfig config = this.getJsonConfig(); // Lazy load JSON config

		return config.getAllFields()
			.stream()
			.filter((field) -> frontendName.equals(field.getFrontendName()))
			.findFirst()
			.orElse(null);
	}

	/**
	 * Convert JSON field definition to ExportFieldDefinition object
	 */
	private ExportFieldDefinition convertJsonToFieldDefinition(FieldDefinitionJson jsonField) {

		// Parse the JOOQ expression string into an actual Field object
		Field<?> jooqField = this.expressionParser.parseExpression(jsonField.getJooqExpression(),
				jsonField.getFrontendName());

		// Convert string java type to Class
		Class<?> javaType = this.parseJavaType(jsonField.getJavaType());

		return ExportFieldDefinition.builder()
			.frontendName(jsonField.getFrontendName())
			.jooqField(jooqField)
			.displayName(jsonField.getDisplayName())
			.javaType(javaType)
			.requiredEntities(new HashSet<>(jsonField.getRequiredEntities()))
			.nullable(jsonField.isNullable())
			.enabled(jsonField.isEnabled())
			.build();
	}

	/**
	 * Create a field definition for custom column fields (custcolumn1, custcolumn2, etc.)
	 * using data from Tblextrafields table.
	 */
	private ExportFieldDefinition createCustomColumnFieldDefinition(String frontendName) {
		try {
			// Extract column number from field name (e.g., "custcolumn10" -> 10)
			String columnNumber = frontendName.substring("custcolumn".length());
			int columnIndex = Integer.parseInt(columnNumber);

			// Validate column index is within valid range (1-150)
			if (columnIndex >= 1 && columnIndex <= 150) {
				// Parse the JOOQ expression using the advanced parser (this handles date
				// formatting internally)
				Field<?> jooqField = this.advancedParser.parseExpression(frontendName, frontendName);

				// Try to get extra field definition from Tblextrafields for display name
				Map<Integer, ExtraFieldDefinitionDto> extraFields = this.extraFieldsRepository.getExtraFieldDefinitions(
						List.of(columnIndex), EntityType.CANDIDATE,
						this.authHolder.getAuthenticationPrincipalOrganizationIdentifier());

				ExtraFieldDefinitionDto extraField = extraFields.get(columnIndex);

				// Use actual field name if available, otherwise use fallback
				String displayName = (extraField != null) ? extraField.extrafieldname()
						: "Custom Column " + columnIndex;

				return ExportFieldDefinition.builder()
					.frontendName(frontendName)
					.jooqField(jooqField)
					.displayName(displayName)
					.javaType(String.class)
					.requiredEntities(Set.of("c", "ccf")) // candidate and custom fields
															// tables
					.nullable(true)
					.enabled(true)
					.build();
			}
		}
		catch (NumberFormatException ex) {
			// Invalid column number
		}

		return null;
	}

	/**
	 * Parse string java type to actual Class object
	 */
	private Class<?> parseJavaType(String javaType) {
		return switch (javaType) {
			case "String" -> String.class;
			case "Integer" -> Integer.class;
			case "BigDecimal" -> BigDecimal.class;
			case "Boolean" -> Boolean.class;
			case "Date" -> java.util.Date.class;
			default -> Object.class;
		};
	}

	/**
	 * Validate that all requested fields are available (optimized for lazy loading). For
	 * custom columns, only validates the field name format (custcolumn1-150), not whether
	 * they exist in Tblextrafields.
	 */
	public void validateFields(List<String> frontendFieldNames) {
		List<String> invalidFields = frontendFieldNames.stream().filter((fieldName) -> {
			// Check if field exists in JSON config
			if (this.findJsonFieldByName(fieldName) != null) {
				return false;
			}

			// Check if it's a valid custom column field (basic validation only)
			if (fieldName.startsWith("custcolumn")) {
				return !this.isValidCustomColumnField(fieldName);
			}

			// Field not found
			return true;
		}).toList();

		if (!invalidFields.isEmpty()) {
			throw new ValidationErrorException("Invalid export fields: " + String.join(", ", invalidFields));
		}
	}

	/**
	 * Basic validation for custom column fields without database calls
	 */
	private boolean isValidCustomColumnField(String frontendName) {
		try {
			// Extract column number from field name (e.g., "custcolumn10" -> 10)
			String columnNumber = frontendName.substring("custcolumn".length());
			int columnIndex = Integer.parseInt(columnNumber);

			// Validate column index is within valid range (1-150)
			return columnIndex >= 1 && columnIndex <= 150;
		}
		catch (NumberFormatException ex) {
			// Invalid column number
			return false;
		}
	}

	/**
	 * Get all entity aliases required for the specified fields
	 */
	public Set<String> getRequiredEntities(List<String> frontendFieldNames) {
		return this.getFieldDefinitions(frontendFieldNames)
			.stream()
			.flatMap((def) -> def.getRequiredEntities().stream())
			.collect(Collectors.toSet());
	}

	/**
	 * Get display name for a field (lazy loading)
	 */
	public String getDisplayName(String frontendName) {
		ExportFieldDefinition definition = this.getFieldDefinitionLazily(frontendName);
		return (definition != null) ? definition.getDisplayName() : frontendName;
	}

}
