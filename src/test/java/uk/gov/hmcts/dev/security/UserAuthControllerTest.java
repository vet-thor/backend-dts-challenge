package uk.gov.hmcts.dev.security;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.dev.config.properties.ApiProperties;
import uk.gov.hmcts.dev.dto.AuthRequest;
import uk.gov.hmcts.dev.dto.AuthResponse;
import uk.gov.hmcts.dev.util.helper.ErrorMessageHelper;
import uk.gov.hmcts.dev.util.helper.SuccessMessageHelper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.dev.test_data.constants.TestCredentialConstant.*;

@ActiveProfiles("test")
@WebMvcTest(UserAuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("UserAuthController: Given an authentication is initiated")
class UserAuthControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ApiProperties apiProperties;
    @MockitoBean
    private UserAuthService userAuthService;
    @MockitoBean
    private ErrorMessageHelper errorMessageHelper;
    @MockitoBean
    private SuccessMessageHelper successMessageHelper;
    @MockitoBean
    private JWTFilter jwtFilter;

    @Test
    @DisplayName("POST: Should authorise with status code 200 when a valid username and password is supplied")
    void shouldAuthorise() throws Exception {
        //Arrange
        var request = new AuthRequest(VALID_USERNAME, VALID_PASSWORD);
        var response = new AuthResponse(EXPECTED_TOKEN, JwtConstant.BEARER);

        //Given
        given(userAuthService.login(any())).willReturn(response);

        //When/Then
        mockMvc.perform(post(apiProperties.getAuthEndpoint())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value(EXPECTED_TOKEN));

        // Verify
        verify(userAuthService).login(any());
    }

    @Test
    @DisplayName("POST: Should fail authorisation with status code 401 when an invalid username and password is supplied")
    void shouldFailAuthorisation() throws Exception {
        // Arrange
        var request = new AuthRequest(INVALID_USERNAME, INVALID_PASSWORD);

        // Given
        given(userAuthService.login(any(AuthRequest.class))).willThrow(new BadCredentialsException("There was an error with you case"));
        given(errorMessageHelper.generalErrorMessage()).willReturn("There was an error with you case");
        given(errorMessageHelper.failedAuthenticationErrorMessage()).willReturn("Invalid username/ password");

        // When/Then
        mockMvc.perform(post(apiProperties.getAuthEndpoint())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("There was an error with you case"))
                .andExpect(jsonPath("$.data.error").value("Invalid username/ password"));

        // Verify
        verify(userAuthService).login(any(AuthRequest.class));
        then(errorMessageHelper).should().generalErrorMessage();
        then(errorMessageHelper).should().failedAuthenticationErrorMessage();
    }
}