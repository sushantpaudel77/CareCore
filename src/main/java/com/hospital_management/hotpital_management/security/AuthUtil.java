package com.hospital_management.hotpital_management.security;

import com.hospital_management.hotpital_management.model.User;
import com.hospital_management.hotpital_management.model.enums.AuthProviderType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;

@Slf4j
@Component
public class AuthUtil {

    @Value("${jwt.secret-key}")
    private String jwtSecretKey;

    private SecretKey getSecretkey() {
        return Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", user.getId().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 10))
                .signWith(getSecretkey())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSecretkey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    public AuthProviderType getProviderTypeFromRegistrationId(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> AuthProviderType.GOOGLE;
            case "github" -> AuthProviderType.GITHUB;
            case "facebook" -> AuthProviderType.FACEBOOK;
            case "twitter" -> AuthProviderType.TWITTER;
            default -> throw new IllegalArgumentException("Unsupported OAuth2 provider: " + registrationId);
        };
    }

    public String determineProviderIdFromOAuth2User(OAuth2User oAuth2User, String registrationId) {
        String providerId = switch (registrationId.toLowerCase()) {
            case "google" -> oAuth2User.getAttribute("sub");       // Google: "sub"
            case "github" -> Objects.requireNonNull(oAuth2User.getAttribute("id")).toString(); // GitHub: "id"
            case "facebook" -> Objects.requireNonNull(oAuth2User.getAttribute("id")).toString(); // Facebook: "id"
            case "twitter" -> Objects.requireNonNull(oAuth2User.getAttribute("id_str")).toString(); // Twitter: "id_str"
            default -> {
                log.error("Unsupported OAuth2 provider: {}", registrationId);
                throw new IllegalArgumentException("Unsupported OAuth2 provider: " + registrationId);
            }
        };

        if (providerId == null || providerId.isBlank()) {
            log.error("Unable to determine providerId for provider: {}", registrationId);
            throw new IllegalArgumentException("Unable to determine providerId for OAuth2 login");
        }

        return providerId;
    }
    public String determineUsernameFromOAuth2User(OAuth2User oAuth2User, String registrationId, String provider) {
        //  First priority email (if available)
        String email = oAuth2User.getAttribute("email");
        if (email != null && !email.isBlank()) {
            return email;
        }
        // Provider-specific fallback
        return switch (registrationId.toLowerCase()) {
            case "google" -> oAuth2User.getAttribute("name"); // Google usually provides 'name' if email is missing
            case "github" -> oAuth2User.getAttribute("login"); // GitHub login (username)
            case "facebook" -> oAuth2User.getAttribute("name"); // Facebook profile name
            case "twitter" -> oAuth2User.getAttribute("screen_name"); // Twitter handle
            default -> {
                // If all else fails → fallback to provider + providerId
                String providerId = String.valueOf(Objects.requireNonNull(oAuth2User.getAttribute("id")));
                yield provider + "_" + providerId;
            }
        };
    }
}
