package io.recruitcrm.microservice.timesheet.configuration;

import io.recruitcrm.microservice.timesheet.helpers.GenericHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

	final CorsProperties corsProperties;

	@Value("#{T(org.springframework.util.StringUtils).hasText('${swagger.username}') ? '${swagger.username}' : 'swagger_user'}")
	private String swaggerUsername;

	@Value("#{T(org.springframework.util.StringUtils).hasText('${swagger.password}') ? '${swagger.password}' : 'cinnamon-radiance-grub1'}")
	private String swaggerPassword;

	public SecurityConfiguration(CorsProperties corsProperties) {
		this.corsProperties = corsProperties;
	}

	protected static final List<String> DEFAULT_ALLOWED_ORIGINS;

	/*
	 * It is worth noting that since version 2.4.0, Spring Boot introduced
	 * allowedOriginPatterns in addition to just allowedOrigins. This new element gives
	 * more flexibility when defining patterns. Furthermore, when allowCredentials is
	 * true, allowedOrigins cannot contain the special value ‘*’ since that cannot be set
	 * on the Access-Control-Allow-Origin response header. To solve this issue and allow
	 * the credentials to a set of origins, we can either list them explicitly or consider
	 * using allowedOriginPatterns instead. https://www.baeldung.com/spring-cors
	 */
	protected static final List<String> DEFAULT_ALLOWED_ORIGIN_PATTERNS;

	static {
		DEFAULT_ALLOWED_ORIGINS = new ArrayList<>();
		DEFAULT_ALLOWED_ORIGIN_PATTERNS = new ArrayList<>();

		DEFAULT_ALLOWED_ORIGIN_PATTERNS.add("*.facebook.com");
		DEFAULT_ALLOWED_ORIGIN_PATTERNS.add("*.google.com");
		DEFAULT_ALLOWED_ORIGIN_PATTERNS.add("*.indeed.com");
		DEFAULT_ALLOWED_ORIGIN_PATTERNS.add("*.linkedin.com");
		DEFAULT_ALLOWED_ORIGIN_PATTERNS.add("*.live.com");
		DEFAULT_ALLOWED_ORIGIN_PATTERNS.add("*.microsoft.com");
		DEFAULT_ALLOWED_ORIGIN_PATTERNS.add("*.microsoft365.com");
		DEFAULT_ALLOWED_ORIGIN_PATTERNS.add("*.naukri.com");
		DEFAULT_ALLOWED_ORIGIN_PATTERNS.add("*.office.com");
		DEFAULT_ALLOWED_ORIGIN_PATTERNS.add("*.office365.com");
		DEFAULT_ALLOWED_ORIGIN_PATTERNS.add("*.recruitcrm.io");
		DEFAULT_ALLOWED_ORIGIN_PATTERNS.add("*.recruitcrm.net");
		DEFAULT_ALLOWED_ORIGIN_PATTERNS.add("*.recruitcrm.com");
		DEFAULT_ALLOWED_ORIGIN_PATTERNS.add("*.xing.com");
		DEFAULT_ALLOWED_ORIGIN_PATTERNS.add("*.zoominfo.com");

		DEFAULT_ALLOWED_ORIGINS.add("https://discord.com");
		DEFAULT_ALLOWED_ORIGINS.add("https://github.com");
		DEFAULT_ALLOWED_ORIGINS.add("https://hireline.io");
		DEFAULT_ALLOWED_ORIGINS.add("https://mx.indeed.com");
		DEFAULT_ALLOWED_ORIGINS.add("https://outlook.office.com");
		DEFAULT_ALLOWED_ORIGINS.add("https://recruitcrm.io");
		DEFAULT_ALLOWED_ORIGINS.add("https://recruitcrm.net");
		DEFAULT_ALLOWED_ORIGINS.add("https://recruitcrm.com");
		DEFAULT_ALLOWED_ORIGINS.add("https://sourcewhale.com");
		DEFAULT_ALLOWED_ORIGINS.add("https://stackoverflow.com");
		DEFAULT_ALLOWED_ORIGINS.add("https://twitter.com");
		DEFAULT_ALLOWED_ORIGINS.add("https://web.telegram.org");
		DEFAULT_ALLOWED_ORIGINS.add("https://www.carehome.co.uk");
		DEFAULT_ALLOWED_ORIGINS.add("https://www.computrabajo.com.mx");
		DEFAULT_ALLOWED_ORIGINS.add("https://www.monster.com");
		DEFAULT_ALLOWED_ORIGINS.add("https://www.monsterindia.com");
		DEFAULT_ALLOWED_ORIGINS.add("https://www.occ.com.mx");
		DEFAULT_ALLOWED_ORIGINS.add("https://www.office.com");
		DEFAULT_ALLOWED_ORIGINS.add("https://www.ziprecruiter.com");
	}

	@Bean
	@SuppressWarnings("java:S4502")
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.cors((httpSecurityCorsConfigurer) -> httpSecurityCorsConfigurer
			.configurationSource(corsConfigurationSource()))
			.sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.csrf(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests(
					(authz) -> authz.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
						.authenticated()
						.anyRequest()
						.permitAll())
			.httpBasic(Customizer.withDefaults());

		return http.build();
	}

	@Bean
	public InMemoryUserDetailsManager userDetailsService() {
		PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
		UserDetails swaggerUser = User.builder()
			.username(this.swaggerUsername)
			.password(passwordEncoder.encode(this.swaggerPassword))
			.roles("SWAGGER")
			.build();

		return new InMemoryUserDetailsManager(swaggerUser);
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();

		// Allow all methods
		configuration.setAllowedMethods(List.of("*"));

		// Allowed origins
		if (!this.corsProperties.getAllowedOrigins().isEmpty()) {
			List<String> combinedAllowedOrigins = GenericHelper.combineAndRemoveDuplicates(
					this.corsProperties.getAllowedOrigins(), SecurityConfiguration.DEFAULT_ALLOWED_ORIGINS);
			configuration.setAllowedOrigins(combinedAllowedOrigins);
		}
		else {
			configuration.setAllowedOrigins(SecurityConfiguration.DEFAULT_ALLOWED_ORIGINS);
		}

		// Allowed origin patterns
		if (!this.corsProperties.getAllowedOriginPatterns().isEmpty()) {
			List<String> combinedAllowedOriginPatterns = GenericHelper.combineAndRemoveDuplicates(
					this.corsProperties.getAllowedOriginPatterns(),
					SecurityConfiguration.DEFAULT_ALLOWED_ORIGIN_PATTERNS);
			configuration.setAllowedOriginPatterns(combinedAllowedOriginPatterns);
		}
		else {
			configuration.setAllowedOriginPatterns(SecurityConfiguration.DEFAULT_ALLOWED_ORIGIN_PATTERNS);
		}

		// Allow all headers
		configuration.setAllowedHeaders(List.of("*"));

		// Allow credentials
		configuration.setAllowCredentials(true);

		// Exposed headers - Allow frontend to access Content-Disposition header for file
		// downloads
		configuration.setExposedHeaders(List.of("Content-Disposition"));

		// Max age
		configuration.setMaxAge(0L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);

		return source;
	}

}
