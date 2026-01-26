package uk.gov.hmcts.dev.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.gov.hmcts.dev.dto.JwtUserDetails;

import java.util.Optional;

public class SecurityUtils {
    private SecurityUtils() {
    }

    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public static Optional<JwtUserDetails> getPrincipal() {
        var authentication = getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        var principal = authentication.getPrincipal();

        if (principal instanceof JwtUserDetails jwtUserDetails) {
            return Optional.of(jwtUserDetails);
        }

        return Optional.empty();
    }
}
