package uk.gov.hmcts.dev.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.dev.dto.AuthRequest;
import uk.gov.hmcts.dev.model.LangType;
import uk.gov.hmcts.dev.model.User;
import uk.gov.hmcts.dev.repository.UserRepository;
import uk.gov.hmcts.dev.util.helper.ErrorMessageHelper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserAuthControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ErrorMessageHelper errorMessage;

    private static final String BASE_URL = "/api/v2/auth/";

    @BeforeEach
    void setupUser() throws JsonProcessingException {
        User user = new User();
        user.setUsername("teststaff");
        user.setPassword(passwordEncoder.encode("pass123"));
        user.setLang(LangType.ES);
        user.setRole("ROLE_USER");

        userRepository.save(user);
    }

    @Test
    void shouldFailAuthorisation() throws Exception {
        var request = new AuthRequest("incorrect_user", "incorrect_password");
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(errorMessage.generalErrorMessage()))
                .andExpect(jsonPath("$.data.error").value(errorMessage.failedAuthenticationErrorMessage()));

    }

    @Test
    void shouldAuthorise() throws Exception {
        var request = new AuthRequest("teststaff", "pass123");
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());

    }
}