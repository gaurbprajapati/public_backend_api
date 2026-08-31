/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import io.recruitcrm.microservice.timesheet.testdata.JooqORMConfigurationTestDataFactory;
import javax.sql.DataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderNameCase;
import org.jooq.conf.RenderQuotedNames;
import org.jooq.meta.jaxb.Logging;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit tests for {@link JooqORMConfiguration}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JooqORMConfiguration Tests")
class JooqORMConfigurationTests {

	@Mock
	private DataSource dataSource;

	private JooqORMConfiguration configuration;

	@BeforeEach
	void setUp() {
		this.configuration = new JooqORMConfiguration();
		ReflectionTestUtils.setField(this.configuration, "username", JooqORMConfigurationTestDataFactory.TEST_USERNAME);
		ReflectionTestUtils.setField(this.configuration, "password",
				JooqORMConfigurationTestDataFactory.TEST_JDBC_CREDENTIAL);
		ReflectionTestUtils.setField(this.configuration, "jdbcUrl", JooqORMConfigurationTestDataFactory.TEST_JDBC_URL);
		ReflectionTestUtils.setField(this.configuration, "inputSchema",
				JooqORMConfigurationTestDataFactory.TEST_INPUT_SCHEMA);
	}

	@Test
	@DisplayName("getJooqSettings should configure AS_IS names, no quoting, and omit schema rendering")
	void testGetJooqSettingsReturnsExpectedRenderingFlags() {
		// When
		org.jooq.conf.Settings settings = JooqORMConfiguration.getJooqSettings();

		// Then
		assertThat(settings.getRenderNameCase()).isEqualTo(RenderNameCase.AS_IS);
		assertThat(settings.getRenderQuotedNames()).isEqualTo(RenderQuotedNames.NEVER);
		assertThat(settings.isRenderSchema()).isFalse();
	}

	@Test
	@DisplayName("transactionAwareDataSource should wrap the delegate in TransactionAwareDataSourceProxy")
	void testTransactionAwareDataSourceWrapsDelegate() {
		// When
		DataSource wrapped = this.configuration.transactionAwareDataSource(this.dataSource);

		// Then
		assertThat(wrapped).isInstanceOf(TransactionAwareDataSourceProxy.class);
		assertThat(((TransactionAwareDataSourceProxy) wrapped).getTargetDataSource()).isSameAs(this.dataSource);
	}

	@Test
	@DisplayName("auroraDbDSLContext should create MYSQL DSLContext using the transaction-aware DataSource")
	void testAuroraDbDslContextUsesMysqlDialectAndSettings() {
		// Given
		DataSource txAware = this.configuration.transactionAwareDataSource(this.dataSource);
		org.jooq.conf.Settings settings = JooqORMConfiguration.getJooqSettings();

		// When
		DSLContext dslContext = this.configuration.auroraDbDSLContext(txAware, settings);

		// Then
		assertThat(dslContext).isNotNull();
		assertThat(dslContext.configuration().dialect()).isEqualTo(SQLDialect.MYSQL);
		org.jooq.conf.Settings effective = dslContext.configuration().settings();
		assertThat(effective.getRenderNameCase()).isEqualTo(settings.getRenderNameCase());
		assertThat(effective.getRenderQuotedNames()).isEqualTo(settings.getRenderQuotedNames());
		assertThat(effective.isRenderSchema()).isEqualTo(settings.isRenderSchema());
	}

	@Test
	@DisplayName("jooqConfig should build codegen Configuration with JDBC, generator, and INFO logging")
	void testJooqConfigBuildsExpectedCodegenConfiguration() {
		// When
		org.jooq.meta.jaxb.Configuration codegenConfig = this.configuration.jooqConfig();

		// Then
		assertThat(codegenConfig.getLogging()).isEqualTo(Logging.INFO);
		assertThat(codegenConfig.getJdbc().getDriver())
			.isEqualTo(JooqORMConfigurationTestDataFactory.EXPECTED_CODEGEN_JDBC_DRIVER);
		assertThat(codegenConfig.getJdbc().getUsername()).isEqualTo(JooqORMConfigurationTestDataFactory.TEST_USERNAME);
		assertThat(codegenConfig.getJdbc().getPassword())
			.isEqualTo(JooqORMConfigurationTestDataFactory.TEST_JDBC_CREDENTIAL);
		assertThat(codegenConfig.getJdbc().getUrl()).isEqualTo(JooqORMConfigurationTestDataFactory.TEST_JDBC_URL);

		assertThat(codegenConfig.getGenerator().getDatabase().getName())
			.isEqualTo(JooqORMConfigurationTestDataFactory.EXPECTED_JOOQ_META_DATABASE_CLASS_NAME);
		assertThat(codegenConfig.getGenerator().getDatabase().getIncludes())
			.contains(JooqORMConfigurationTestDataFactory.SAMPLE_INCLUDED_TABLE_NAME)
			.contains("|");
		assertThat(codegenConfig.getGenerator().getDatabase().getIncludes().split("\\|"))
			.hasSize(JooqORMConfigurationTestDataFactory.REQUIRED_TABLE_NAME_COUNT);
		assertThat(codegenConfig.getGenerator().getDatabase().getInputSchema())
			.isEqualTo(JooqORMConfigurationTestDataFactory.TEST_INPUT_SCHEMA);
		assertThat(codegenConfig.getGenerator().getDatabase().isOutputSchemaToDefault()).isTrue();

		assertThat(codegenConfig.getGenerator().getTarget().getEncoding())
			.isEqualTo(JooqORMConfigurationTestDataFactory.EXPECTED_GENERATOR_ENCODING);
		assertThat(codegenConfig.getGenerator().getTarget().getPackageName())
			.isEqualTo(JooqORMConfigurationTestDataFactory.EXPECTED_GENERATOR_PACKAGE_NAME);
		assertThat(codegenConfig.getGenerator().getTarget().getDirectory())
			.isEqualTo(JooqORMConfigurationTestDataFactory.EXPECTED_GENERATOR_DIRECTORY);
	}

}
