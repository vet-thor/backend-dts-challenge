package uk.gov.hmcts.dev.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import uk.gov.hmcts.dev.model.LangType;
import uk.gov.hmcts.dev.model.User;
import uk.gov.hmcts.dev.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.dev.test_data.constants.TestCredentialConstant.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Given a user initiates a signing request")
class UserInfoConfigManagerTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserInfoConfigManager userInfoConfigManager;

    private static final UUID VALID_USER_ID = UUID.randomUUID();
    private static final String ROLE_TYPE_USER = "ROLE_USER";

    @Test
    @DisplayName("Should successfully load username when a valid username and password is supplied")
    void shouldLoadUserByUsernameSuccessfully() {
        // Arrange
        var user = User.builder()
                .id(UUID.randomUUID())
                .username(VALID_USERNAME)
                .password(VALID_PASSWORD)
                .lang(LangType.GB)
                .role(ROLE_TYPE_USER)
                .build();

        //Given
        given(userRepository.findByUsername(VALID_USERNAME)).willReturn(Optional.of(user));

        //When
        var response = userInfoConfigManager.loadUserByUsername(VALID_USERNAME);

        //Then
        assertEquals(VALID_USERNAME, response.getUsername());
        assertEquals(VALID_PASSWORD, response.getPassword());
        assertTrue(response.getAuthorities().contains(new SimpleGrantedAuthority(ROLE_TYPE_USER)));

        //Verify
        verify(userRepository, times(1)).findByUsername(VALID_USERNAME);
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when username is not found")
    void shouldThrowExceptionWhenUsernameNotFound() {
        // Given
        given(userRepository.findByUsername(INVALID_USERNAME)).willReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            userInfoConfigManager.loadUserByUsername(INVALID_USERNAME);
        });

        //Verify
        verify(userRepository, times(1)).findByUsername(INVALID_USERNAME);
    }

    @Test
    @DisplayName("Should load user when a valid userId is supplied")
    void shouldLoadUserByIdSuccessfully() {
        // Arrange
        var user = User.builder()
                .id(UUID.randomUUID())
                .username(VALID_USERNAME)
                .password(VALID_PASSWORD)
                .lang(LangType.GB)
                .role(ROLE_TYPE_USER)
                .build();

        //Given
        given(userRepository.findById(VALID_USER_ID)).willReturn(Optional.of(user));

        //When
        var response = userInfoConfigManager.loadUserById(VALID_USER_ID);

        //Then
        assertEquals(VALID_USERNAME, response.getUsername());
        assertEquals(VALID_PASSWORD, response.getPassword());
        assertTrue(response.getAuthorities().contains(new SimpleGrantedAuthority(ROLE_TYPE_USER)));

        //Verify
        verify(userRepository, times(1)).findById(VALID_USER_ID);
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when user id is not found")
    void shouldThrowExceptionWhenUserIdNotFound() {
        //Given
        given(userRepository.findById(VALID_USER_ID)).willReturn(Optional.empty());

        //When/Then
        assertThrows(UsernameNotFoundException.class, () -> {
            userInfoConfigManager.loadUserById(VALID_USER_ID);
        });

        //Verify
        verify(userRepository, times(1)).findById(VALID_USER_ID);
    }
}