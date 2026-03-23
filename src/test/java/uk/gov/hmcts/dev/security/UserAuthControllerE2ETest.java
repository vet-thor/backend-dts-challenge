package uk.gov.hmcts.dev.security;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.dev.config.extensions.PostgresTestContainerConfiguration;
import uk.gov.hmcts.dev.config.extensions.RedisTestContainerConfiguration;
import uk.gov.hmcts.dev.dto.AuthRequest;
import uk.gov.hmcts.dev.dto.AuthResponse;
import uk.gov.hmcts.dev.model.User;
import uk.gov.hmcts.dev.repository.UserRepository;
import uk.gov.hmcts.dev.util.helper.ErrorMessageHelper;
import uk.gov.hmcts.dev.util.helper.SuccessMessageHelper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.dev.test_data.constants.TestCredentialConstant.*;

@Disabled
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import({RedisTestContainerConfiguration.class, PostgresTestContainerConfiguration.class})
@DisplayName("/api/v1/auth: Given an authentication is initiated")
@Transactional
class UserAuthControllerE2ETest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ErrorMessageHelper errorMessageHelper;
    @Autowired
    private SuccessMessageHelper successMessageHelper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;

    private static final String BASE_URL = "/api/v1/auth";

    @BeforeEach
    void setup(){
        var user = User.builder()
                .username(VALID_USERNAME)
                .password(passwordEncoder.encode(VALID_PASSWORD))
                .role("ROLE_STAFF")
                .build();

        userRepository.save(user);
    }

    @Test
    @DisplayName("Should authorise with status code 200 when a valid username and password is supplied")
    void shouldAuthorise() throws Exception {
        //Arrange
        var request = new AuthRequest(VALID_USERNAME, VALID_PASSWORD);

        //When/Then
        mockMvc.perform(
                    post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.message").value(successMessageHelper.loginSuccessMessage()));

    }

    @Test
    @DisplayName("Should fail authorisation with status code 401 when an invalid username and password is supplied")
    void shouldFailAuthorisation() throws Exception {
        // Arrange
        var request = new AuthRequest(INVALID_USERNAME, INVALID_PASSWORD);

        // When/Then
        mockMvc.perform(
                    post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(errorMessageHelper.generalErrorMessage()))
                .andExpect(jsonPath("$.data.error").value(errorMessageHelper.failedAuthenticationErrorMessage()));

    }
}