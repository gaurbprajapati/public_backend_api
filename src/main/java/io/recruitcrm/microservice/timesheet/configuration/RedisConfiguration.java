package io.recruitcrm.microservice.timesheet.configuration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import io.recruitcrm.microservice.TimesheetApplication;
import io.recruitcrm.microservice.timesheet.flagsmith.FlagsmithBaseFlag;
import io.recruitcrm.microservice.timesheet.flagsmith.FlagsmithFlags;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Configuration
public class RedisConfiguration {

	@Bean
	public RedisTemplate<?, ?> redisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<?, ?> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
		return template;
	}

	@Bean
	public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
			.allowIfBaseType(Map.class)
			.allowIfBaseType(List.class)
			.allowIfSubType(FlagsmithFlags.class)
			.allowIfSubType(FlagsmithBaseFlag.class)
			.build();
		objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);
		objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

		RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
			.prefixCacheNameWith(TimesheetApplication.class.getPackageName() + ".")
			.entryTtl(Duration.ofHours(24))
			.serializeKeysWith(RedisSerializationContext.SerializationPair
				.fromSerializer(new StringRedisSerializer(StandardCharsets.UTF_8)))
			.serializeValuesWith(RedisSerializationContext.SerializationPair
				.fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper)))
			.disableCachingNullValues();

		return RedisCacheManager.builder(connectionFactory).cacheDefaults(config).build();
	}

}
