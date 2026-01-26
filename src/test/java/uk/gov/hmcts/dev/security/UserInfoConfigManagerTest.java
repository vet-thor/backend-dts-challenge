package uk.gov.hmcts.dev.security;

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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserInfoConfigManagerTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserInfoConfigManager userInfoConfigManager;

    @Test
    void shouldLoadUserByUsernameSuccessfully() {
        // Arrange
        var username = "testuser";
        var user = User.builder()
                .id(UUID.randomUUID())
                .username(username)
                .password("securepass")
                .lang(LangType.GB)
                .role("ROLE_USER")
                .build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        var response = userInfoConfigManager.loadUserByUsername(username);

        assertEquals(username, response.getUsername());
        assertEquals("securepass", response.getPassword());
        assertTrue(response.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void shouldThrowExceptionWhenUsernameNotFound() {
        // Arrange
        var username = "missinguser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            userInfoConfigManager.loadUserByUsername(username);
        });
    }

    @Test
    void shouldLoadUserByIdSuccessfully() {
        // Arrange
        UUID userId = UUID.randomUUID();
        var user = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .password("securepass")
                .lang(LangType.GB)
                .role("ROLE_USER")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        var response = userInfoConfigManager.loadUserById(userId);

        assertEquals("testuser", response.getUsername());
        assertEquals("securepass", response.getPassword());
        assertTrue(response.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void shouldThrowExceptionWhenUserIdNotFound() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            userInfoConfigManager.loadUserById(userId);
        });
    }
}