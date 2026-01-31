package uk.gov.hmcts.dev.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.dev.config.extensions.PostgresTestContainerConfiguration;
import uk.gov.hmcts.dev.config.extensions.RedisTestContainerConfiguration;
import uk.gov.hmcts.dev.config.properties.ApplicationProperties;
import uk.gov.hmcts.dev.model.Task;
import uk.gov.hmcts.dev.repository.TaskRepository;
import uk.gov.hmcts.dev.util.helper.ErrorMessageHelper;
import uk.gov.hmcts.dev.util.helper.FieldHelper;

import java.time.Instant;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({RedisTestContainerConfiguration.class, PostgresTestContainerConfiguration.class})
class ExceptionHandlerConfigIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private FieldHelper fieldHelper;
    @Autowired
    private ApplicationProperties appProps;
    @Autowired
    private ErrorMessageHelper errorMessage;

    private static final String BASE_URL = "/api/v2/case/";

    @Test
    @WithMockUser(username = "testuser", roles = {"STAFF"})
    void handleArgumentNotValidExceptionHandler() throws Exception {
        mockMvc.perform(
                        post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new Task()))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.data.errors.title").value(fieldHelper.titleRequired()))
                .andExpect(jsonPath("$.data.errors.description").value(fieldHelper.descriptionRequired()))
                .andExpect(jsonPath("$.data.errors.due").value(fieldHelper.dueDateRequired()));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"STAFF"})
    void handleArgumentNotValidExceptionHandler_forPut() throws Exception{
        mockMvc.perform(
                        put(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new Task()))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.data.errors.id").value(fieldHelper.idRequired()));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"STAFF"})
    void handleUnexpectedException() throws Exception {
        mockMvc.perform(
                        post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.data.error").value(errorMessage.unexpectedErrorMessage()));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"STAFF"})
    void handleExpiredToken() throws Exception {
        var expiredToken = Jwts.builder()
                .subject("staff")
                .expiration(Date.from(Instant.now().minusSeconds(60))) // expired 1 minute ago
                .signWith(Keys.hmacShaKeyFor(appProps.getSecurityKey().getBytes()))
                .compact();

        mockMvc.perform(get(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + expiredToken)
                )
                .andExpect(status().isUnauthorized());
    }
}