package uk.gov.hmcts.dev.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.dev.config.properties.ApplicationProperties;
import uk.gov.hmcts.dev.dto.JwtUserDetails;
import uk.gov.hmcts.dev.util.StringUtil;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
class JWTUtil {
    private ApplicationProperties appProps;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(appProps.getSecurityKey().getBytes());
    }

    public String extractSubject(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    public UUID extractId(String token) {
        return StringUtil.stringToUUID(extractSubject(token));
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(JwtUserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        claims.put(JwtConstant.ROLE, userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        return generateToken(userDetails.getId().toString(), claims);
    }

    public String generateToken(String subject) {
        Map<String, Object> claims = new HashMap<>();
        return generateToken(subject, claims);
    }

    public String generateToken(String subject, Map<String, Object> claims){
        return createToken(claims, subject);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .header().empty().add(JwtConstant.TYP,JwtConstant.JWT)
                .and()
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + appProps.getJwtTimeout()))
                .signWith(getSigningKey())
                .compact();
    }

    public Boolean validateToken(String token) {
        return !isTokenExpired(token);
    }
}
