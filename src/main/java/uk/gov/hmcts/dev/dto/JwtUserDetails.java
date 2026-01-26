package uk.gov.hmcts.dev.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import uk.gov.hmcts.dev.model.LangType;

import java.util.Collection;
import java.util.UUID;

@Setter
@Getter
@Builder
public class JwtUserDetails implements UserDetails {
    private UUID id;
    private String password;
    private String username;
    private LangType lang;
    private Collection<? extends GrantedAuthority> authorities;
}