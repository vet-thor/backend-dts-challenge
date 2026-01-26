package uk.gov.hmcts.dev.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import uk.gov.hmcts.dev.dto.AuthRequest;
import uk.gov.hmcts.dev.dto.JwtUserDetails;
import uk.gov.hmcts.dev.model.LangType;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAuthServiceTest {

    @InjectMocks
    private UserAuthService authService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JWTUtil jwtUtil;

    @Mock
    private Authentication authentication;

    @Test
    void shouldReturnAuthResponseWhenLoginIsSuccessful() {
        // Arrange
        var request = new AuthRequest("testuser", "pass123");
        var expectedToken = "mocked-jwt-token";

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        var jwtUserDetails = JwtUserDetails.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .password("pass123")
                .lang(LangType.GB)
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        when(authentication.getPrincipal())
                .thenReturn(jwtUserDetails);

        when(jwtUtil.generateToken(jwtUserDetails))
                .thenReturn(expectedToken);

        // When
        var response = authService.login(request);

        // Then
        assertNotNull(response);
        assertEquals(expectedToken, response.accessToken());
        assertEquals(JwtConstant.BEARER, response.tokenType());
    }

    @Test
    void shouldThrowExceptionWhenLoginFails() {
        // Arrange
        AuthRequest request = new AuthRequest("wronguser", "badpassword");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When/Then
        assertThrows(BadCredentialsException.class, () -> {
            authService.login(request);
        });
    }

}