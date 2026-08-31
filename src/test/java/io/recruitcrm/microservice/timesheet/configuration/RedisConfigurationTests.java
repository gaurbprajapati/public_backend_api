package io.recruitcrm.microservice.timesheet.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@ExtendWith(MockitoExtension.class)
class RedisConfigurationTests {

	@Mock
	private RedisConnectionFactory connectionFactory;

	private final RedisConfiguration redisConfiguration = new RedisConfiguration();

	@Test
	@DisplayName("Should create RedisTemplate bean with provided connection factory")
	void testRedisTemplateBeanCreatedWithConnectionFactory() {
		// When
		RedisTemplate<?, ?> redisTemplate = this.redisConfiguration.redisTemplate(this.connectionFactory);

		// Then
		assertThat(redisTemplate).isNotNull();
		assertThat(redisTemplate.getConnectionFactory()).isSameAs(this.connectionFactory);
	}

	@Test
	@DisplayName("Should create RedisCacheManager bean with expected configuration")
	void testCacheManagerBeanCreatedSuccessfully() {
		// When
		RedisCacheManager cacheManager = this.redisConfiguration.cacheManager(this.connectionFactory);

		// Then
		assertThat(cacheManager).isNotNull();
		assertThat(cacheManager.getCacheConfigurations()).isNotNull();
	}

}
