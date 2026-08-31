/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.flagsmith;

import com.flagsmith.FlagsmithClient;
import com.flagsmith.exceptions.FlagsmithClientError;
import com.flagsmith.models.Flags;
import io.recruitcrm.entity.model.User;
import io.recruitcrm.logging.config.LoggerConfiguration;
import io.recruitcrm.logging.logger.Logger;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Map;
import java.util.Objects;

@Component
@EnableCaching
public class Flagsmith implements IFlagsmith {

	public static final String CACHE_NAME = "contract-staffing";

	private final Logger logger;

	@Value("${flagsmith.api.key}")
	String flagsmithApiKey;

	FlagsmithClient flagsmithClient;

	final CacheManager cacheManager;

	final Cache cache;

	public Flagsmith(CacheManager cacheManager, @Qualifier(LoggerConfiguration.SYNC_CONTEXT_LOGGER) Logger logger) {
		this.cacheManager = cacheManager;
		this.cache = this.cacheManager.getCache(CACHE_NAME);
		this.logger = logger;
	}

	@PostConstruct
	public void postConstruct() {
		this.flagsmithClient = FlagsmithClient.newBuilder().setApiKey(this.flagsmithApiKey).build();
	}

	@Override
	public FlagsmithFlags getIdentityFlags(Integer userId, Integer accountId) {
		Map<String, Object> traits = Map.of("Account Id", Objects.requireNonNull(accountId));
		final String cacheKey = "%d-%d".formatted(userId, accountId);
		try {
			FlagsmithFlags identityFlags = this.cache.get(cacheKey, FlagsmithFlags.class);
			if (identityFlags == null) {
				Flags flags = this.flagsmithClient.getIdentityFlags(Objects.requireNonNull(userId).toString(), traits);
				identityFlags = FlagsmithMapper.INSTANCE.toFlagsmithFlags(flags);
				this.cache.put(cacheKey, identityFlags);
			}
			return identityFlags;
		}
		catch (FlagsmithClientError flagsmithClientError) {
			this.logger
				.logError(MessageFormat.format("Error while fetching flags from flagsmith: {0}", flagsmithClientError));
			return null;
		}
	}

	@Override
	public FlagsmithFlags getIdentityFlags(User user) {
		Integer userId = user.getId();
		Integer accountId = user.getAccount().getId();
		return this.getIdentityFlags(userId, accountId);
	}

	@Override
	public Boolean isFeatureEnabled(String featureName, User user) {
		try {
			return this.getIdentityFlags(user).isFeatureEnabled(featureName);
		}
		catch (FlagsmithClientError flagsmithClientError) {
			this.logger.logError(MessageFormat.format("Error while checking feature flag: {0}", flagsmithClientError));
			return false;
		}
	}

	@Override
	public Object getFeatureValue(String featureName, User user) {
		try {
			return this.getIdentityFlags(user).getFeatureValue(featureName);
		}
		catch (FlagsmithClientError flagsmithClientError) {
			this.logger.logError(MessageFormat.format("Error while getting feature value: {0}", flagsmithClientError));
			return null;
		}
	}

}
