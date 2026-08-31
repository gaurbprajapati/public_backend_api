/*
 * Copyright (c) 2025. RecruitCRM
 * All rights reserved.
 */

package io.recruitcrm.microservice.timesheet.configuration.auth.recruitcrm.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService implements IJwtService {

	@Value("${security.recruitcrm.jwt.secret-key}")
	private String secretKey;

	@Value("${security.recruitcrm.jwt.expiration-time}")
	private long jwtExpiration;

	@Override
	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	@Override
	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	@Override
	public String generateToken(Map<String, Object> extraClaims, Integer userId) {
		return buildToken(extraClaims, userId, this.jwtExpiration);
	}

	@Override
	public long getExpirationTime() {
		return this.jwtExpiration;
	}

	@Override
	public String buildToken(Map<String, Object> extraClaims, Integer userID, long expiration) {
		return Jwts.builder()
			.claims(extraClaims)
			.subject(userID.toString())
			.issuedAt(new Date(System.currentTimeMillis()))
			.expiration(new Date(System.currentTimeMillis() + expiration))
			.signWith(getSignInKey(), Jwts.SIG.HS256)
			.compact();
	}

	@Override
	public boolean isTokenValid(String token, Integer userId) {
		final String username = extractUsername(token);
		return (Integer.parseInt(username) == userId && !isTokenExpired(token));
	}

	@Override
	public boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	@Override
	public Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	@Override
	public Claims extractAllClaims(String token) {
		return Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(token).getPayload();
	}

	@Override
	public SecretKey getSignInKey() {
		return Keys.hmacShaKeyFor(this.secretKey.getBytes());
	}

}