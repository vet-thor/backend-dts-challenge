package uk.gov.hmcts.dev.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.dev.config.properties.JwtProperties;
import uk.gov.hmcts.dev.dto.JwtUserDetails;
import uk.gov.hmcts.dev.util.StringUtil;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
class JWTUtil {
    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.secret().getBytes());
    }

    public String extractSubject(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    public UUID extractId(String token) {
        return StringUtil.stringToUUID(extractSubject(token));
    }

    public Date extractExpiration(String token) {
        var claims = extractAllClaims(token);

        return claims.getExpiration();
    }

    private Claims extractAllClaims(String token) {
        var signingKey = getSigningKey();

        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Boolean isTokenExpired(String token) {
        var expiration = extractExpiration(token);

        return expiration.before(new Date());
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
                .header()
                .type(Header.JWT_TYPE)
                .add(JwtConstant.TYP, JwtConstant.JWT)
                .and()
                .subject(subject)
                .claims(claims)
                .issuedAt(new Date())
                .expiration(jwtProperties.calculateExpiration())
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public Boolean validateToken(String token) {
        return !isTokenExpired(token);
    }
}
