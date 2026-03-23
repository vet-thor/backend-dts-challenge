package uk.gov.hmcts.dev.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.dev.config.extensions.PostgresTestContainerConfiguration;
import uk.gov.hmcts.dev.config.extensions.RedisTestContainerConfiguration;
import uk.gov.hmcts.dev.config.properties.JwtProperties;
import uk.gov.hmcts.dev.model.Task;
import uk.gov.hmcts.dev.util.helper.ErrorMessageHelper;
import uk.gov.hmcts.dev.util.helper.FieldHelper;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Date;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uk.gov.hmcts.dev.test_data.constants.TestCredentialConstant.VALID_ROLE_STAFF;
import static uk.gov.hmcts.dev.test_data.constants.TestCredentialConstant.VALID_USERNAME;

@Disabled
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({RedisTestContainerConfiguration.class, PostgresTestContainerConfiguration.class})
class ExceptionHandlerConfigIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private FieldHelper fieldHelper;
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private ErrorMessageHelper errorMessage;

    private static final String BASE_URL = "/api/v1/tasks";

    @Test
    void handleArgumentNotValidExceptionHandler() throws Exception {
        mockMvc.perform(
                        post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new Task()))
                                .with(user(VALID_USERNAME).roles(VALID_ROLE_STAFF))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("400 BAD_REQUEST"))
                .andExpect(jsonPath("$.data.errors.title").value(fieldHelper.titleRequired()))
                .andExpect(jsonPath("$.data.errors.description").value(fieldHelper.descriptionRequired()))
                .andExpect(jsonPath("$.data.errors.due").value(fieldHelper.dueDateRequired()));
    }

    @Test
    void handleArgumentNotValidExceptionHandler_forPut() throws Exception{
        mockMvc.perform(
                        put(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new Task()))
                                .with(user(VALID_USERNAME).roles(VALID_ROLE_STAFF))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("400 BAD_REQUEST"))
                .andExpect(jsonPath("$.data.errors.id").value(fieldHelper.idRequired()));
    }

    @Test
    void handleUnexpectedException() throws Exception {
        mockMvc.perform(
                        post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(user(VALID_USERNAME).roles(VALID_ROLE_STAFF))
                )
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("500 INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.data.error").value(errorMessage.unexpectedErrorMessage()));
    }

    @Test
    void handleExpiredToken() throws Exception {
        var expiredToken = Jwts.builder()
                .subject(VALID_ROLE_STAFF)
                .expiration(Date.from(Instant.now().minusSeconds(60))) // expired 1 minute ago
                .signWith(Keys.hmacShaKeyFor(jwtProperties.secret().getBytes()))
                .compact();

        mockMvc.perform(get(BASE_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized());
    }
}