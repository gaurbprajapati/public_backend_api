/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.service;

import io.jsonwebtoken.Claims;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

public interface IJwtService {

	String extractUsername(String token);

	<T> T extractClaim(String token, Function<Claims, T> claimsResolver);

	String generateToken(Map<String, Object> extraClaims, Integer userId);

	long getExpirationTime();

	String buildToken(Map<String, Object> extraClaims, Integer userID, long expiration);

	boolean isTokenValid(String token, Integer userId);

	boolean isTokenExpired(String token);

	Date extractExpiration(String token);

	Claims extractAllClaims(String token);

	SecretKey getSignInKey();

}
