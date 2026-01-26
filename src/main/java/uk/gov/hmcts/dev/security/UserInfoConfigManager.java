package uk.gov.hmcts.dev.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.dev.dto.JwtUserDetails;
import uk.gov.hmcts.dev.repository.UserRepository;
import uk.gov.hmcts.dev.service.AuthUserDetailsService;

import java.util.UUID;
import java.util.List;

@Service
@RequiredArgsConstructor
class UserInfoConfigManager implements UserDetailsService, AuthUserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        var user = userRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException(
                        "User not found"));

        return JwtUserDetails.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .lang(user.getLang())
                .authorities(List.of(new SimpleGrantedAuthority(user.getRole())))
                .build();
    }

    @Override
    public UserDetails loadUserById(UUID id) throws UsernameNotFoundException {
        var user = userRepository.findById(id).orElseThrow(
                () -> new UsernameNotFoundException(
                        "User not found"));

        return JwtUserDetails.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .lang(user.getLang())
                .authorities(List.of(new SimpleGrantedAuthority(user.getRole())))
                .build();
    }
}
