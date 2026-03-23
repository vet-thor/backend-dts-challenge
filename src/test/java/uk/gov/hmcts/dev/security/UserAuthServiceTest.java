package uk.gov.hmcts.dev.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.gov.hmcts.dev.dto.AuthRequest;
import uk.gov.hmcts.dev.dto.JwtUserDetails;
import uk.gov.hmcts.dev.model.LangType;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.dev.test_data.constants.TestCredentialConstant.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Given user authorisation")
class UserAuthServiceTest {
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JWTUtil jwtUtil;
    @Mock
    private Authentication authentication;
    @InjectMocks
    private UserAuthService authService;

    @Test
    @DisplayName("Should set security context and return token when a successful login is established")
    void shouldSetSecurityContextAndReturnToken() {
        // Arrange
        var authRequest = new AuthRequest(VALID_USERNAME, VALID_PASSWORD);
        var jwtUserDetails = JwtUserDetails.builder()
                .id(UUID.randomUUID())
                .username(VALID_USERNAME)
                .password(VALID_PASSWORD)
                .lang(LangType.GB)
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        //Given
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).willReturn(authentication);
        given(authentication.getPrincipal()).willReturn(jwtUserDetails);
        given(jwtUtil.generateToken(jwtUserDetails)).willReturn(EXPECTED_TOKEN);

        // When
        var response = authService.login(authRequest);

        // Then
        assertNotNull(response);
        assertEquals(EXPECTED_TOKEN, response.accessToken());
        assertEquals(JwtConstant.BEARER, response.tokenType());

        //Verify
        verify(jwtUtil).generateToken(jwtUserDetails);
        verify(authenticationManager).authenticate(argThat(auth ->
                Objects.equals(auth.getPrincipal(), VALID_USERNAME) &&
                        Objects.equals(auth.getCredentials(), VALID_PASSWORD)
        ));
    }

    @AfterEach
    void teardown(){
        SecurityContextHolder.clearContext();
    }
}