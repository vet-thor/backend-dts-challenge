package uk.gov.hmcts.dev.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import uk.gov.hmcts.dev.dto.AuthRequest;
import uk.gov.hmcts.dev.dto.AuthResponse;

import java.util.UUID;

public interface AuthUserDetailsService {
    UserDetails loadUserById(UUID id) throws UsernameNotFoundException;
}