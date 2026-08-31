package io.recruitcrm.microservice.timesheet.configuration;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderNameCase;
import org.jooq.conf.RenderQuotedNames;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.meta.jaxb.Database;
import org.jooq.meta.jaxb.Generator;
import org.jooq.meta.jaxb.Jdbc;
import org.jooq.meta.jaxb.Logging;
import org.jooq.meta.jaxb.Target;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import javax.sql.DataSource;
import java.util.List;

@Configuration
public class JooqORMConfiguration {

	@Value("${spring.datasource.reader.driver-class-name}")
	private String driver;

	@Value("${spring.datasource.reader.username}")
	private String username;

	@Value("${spring.datasource.reader.password}")
	private String password;

	@Value("${spring.datasource.reader.url}")
	private String jdbcUrl;

	@Value("${spring.datasource.reader.schema}")
	private String inputSchema;

	private final List<String> requiredTables = List.of("cst_timesheet_t", "cst_timesheet_setting_t",
			"cst_timesheet_invoice_t", "tblcurrency", "tbldealcandidates", "tblcandidate", "tbljob",
			"cst_timesheet_setting_association_t", "cst_rule_template_t", "tblcompany", "tbldealjobs",
			"cst_timesheet_approver_t", "tbldeals", "cst_time_log_t", "cst_timesheet_approval_t", "tbluser",
			"candidate_custom_fields_t", "candidate_last_activities_t", "candidate_custom_fields_t", "tblextrafields",
			"entity_off_limit_t", "off_limit_status_t", "tblgender", "cst_job_timesheet_access_t",
			"tblassignjobcandidate", "off_limit_status_colour_t", "tbldealpipelinestages", "tbljobstatus",
			"cst_time_log_break_interval_t", "job_secondary_contacts_t", "cst_time_log_interval_t",
			"contractor_portal_status_t", "cst_timesheet_reimbursement_t",
			"cst_timesheet_reimbursement_status_history_t", "client_portal_status_t", "tblcontact",
			"contact_company_t");

	@Bean
	public org.jooq.meta.jaxb.Configuration jooqConfig() {

		return new org.jooq.meta.jaxb.Configuration().withLogging(Logging.INFO)
			.withJdbc(new Jdbc().withDriver("com.mysql.cj.jdbc.Driver")
				.withUsername(this.username)
				.withPassword(this.password)
				.withUrl(this.jdbcUrl))
			.withGenerator(new Generator()
				.withDatabase(new Database().withName("org.jooq.meta.mysql.MySQLDatabase")
					.withIncludes(String.join("|", this.requiredTables))
					.withInputSchema(this.inputSchema)
					.withOutputSchemaToDefault(true)) // Add this line to omit schema
				.withTarget(new Target().withEncoding("UTF-8")
					.withPackageName("io.recruitcrm.microservice.search.models.jooq")
					.withDirectory("src/main/java")));
	}

	// RecruitCRM_Test
	@Bean
	public static Settings getJooqSettings() {
		return new Settings().withRenderNameCase(RenderNameCase.AS_IS)
			.withRenderQuotedNames(RenderQuotedNames.NEVER)
			.withRenderSchema(false);
	}

	/**
	 * Creates a transaction-aware DataSource proxy that ensures JOOQ operations
	 * participate in Spring-managed transactions. This is critical for proper transaction
	 * rollback behavior when mixing JPA and JOOQ operations.
	 * @param dataSource The original DataSource bean
	 * @return Transaction-aware DataSource proxy
	 */
	@Bean(name = "transactionAwareDataSource")
	public DataSource transactionAwareDataSource(DataSource dataSource) {
		return new TransactionAwareDataSourceProxy(dataSource);
	}

	/**
	 * Creates DSLContext configured to participate in Spring transactions. Uses
	 * TransactionAwareDataSourceProxy to ensure JOOQ operations are part of the same
	 * transaction boundary as JPA operations.
	 * @param transactionAwareDataSource The transaction-aware DataSource proxy
	 * @param jooqSettings JOOQ settings for rendering
	 * @return DSLContext configured for Spring transaction management
	 */
	@Bean(name = JooqDslContextTarget.RECRUITCRM_AURORA)
	public DSLContext auroraDbDSLContext(@Qualifier("transactionAwareDataSource") DataSource transactionAwareDataSource,
			Settings jooqSettings) {
		// Use the transaction-aware DataSource to ensure JOOQ operations
		// participate in Spring transactions and roll back correctly on failures
		return DSL.using(transactionAwareDataSource, SQLDialect.MYSQL, jooqSettings);
	}

}
