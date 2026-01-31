package uk.gov.hmcts.dev.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
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
import uk.gov.hmcts.dev.dto.CaseRequest;
import uk.gov.hmcts.dev.dto.JwtUserDetails;
import uk.gov.hmcts.dev.model.Task;
import uk.gov.hmcts.dev.model.TaskStatus;
import uk.gov.hmcts.dev.repository.TaskRepository;
import uk.gov.hmcts.dev.util.SecurityUtils;
import uk.gov.hmcts.dev.util.helper.ErrorMessageHelper;
import uk.gov.hmcts.dev.util.helper.FieldHelper;
import uk.gov.hmcts.dev.util.helper.SuccessMessageHelper;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({RedisTestContainerConfiguration.class, PostgresTestContainerConfiguration.class})
@Transactional
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SuccessMessageHelper successMessage;

    @Autowired
    private ErrorMessageHelper errorMessage;

    @Autowired
    private FieldHelper fieldHelper;

    @Autowired
    private TaskRepository taskRepository;

    private List<Task> savedTasks;
    private final UUID createdByForTask1 = UUID.randomUUID();
    private final UUID createdByForTask2 = UUID.randomUUID();
    private static final String BASE_URL = "/api/v2/case/";

    @BeforeEach
    void setUp() {
        var task1 = Task.builder()
                .title("Test title")
                .description("Test description")
                .status(TaskStatus.OPEN)
                .due(LocalDateTime.now().plusDays(180))
                .createdBy(createdByForTask1)
                .build();
        var task2 = Task.builder()
                .title("Test title 2")
                .description("Test description 2")
                .status(TaskStatus.OPEN)
                .due(LocalDateTime.now().plusDays(180))
                .createdBy(createdByForTask2)
                .build();

        taskRepository.deleteAll();
        savedTasks = taskRepository.saveAllAndFlush(List.of(task1, task2));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"STAFF"})
    void shouldCreateTask() throws Exception {
        var request = new CaseRequest(
                null,
                "Test title",// Will create because the owner (createdBy == principal.id) doesn't match existing record
                "Test description",
                null,
                LocalDateTime.now().plusDays(180)
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.task.title").value(request.title()))
                .andExpect(jsonPath("$.data.task.description").value(request.description()))
                .andExpect(jsonPath("$.data.task.status").value(TaskStatus.OPEN.toString()));

    }

    @Test
    @WithMockUser(username = "testuser", roles = {"STAFF"})
    void shouldNotCreateTaskWhenTitleExistForSameOwner() throws Exception {
        var request = new CaseRequest(
                null,
                "Test title",// Will not create because the owner (createdBy == principal.id) matches existing record
                "Test description",
                null,
                LocalDateTime.now().plusDays(180)
        );

        try(MockedStatic<SecurityUtils> mockedSecurityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            //Setting up the principal to match task 1 createdBy UUID
            mockedSecurityUtils.when(SecurityUtils::getPrincipal).thenReturn(Optional.of(JwtUserDetails.builder().id(createdByForTask1).build()));

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.errors.title").value(errorMessage.duplicateTitleErrorMessage()));
        }
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"STAFF"})
    void shouldNotCreateTaskWithInvalidRequest() throws Exception {

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CaseRequest.builder().build()))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.errors.title").value(fieldHelper.titleRequired()))
                .andExpect(jsonPath("$.data.errors.description").value(fieldHelper.descriptionRequired()))
                .andExpect(jsonPath("$.data.errors.due").value(fieldHelper.dueDateRequired()));

    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void shouldNotCreateTaskWithWrongPermission_denyAccess() throws Exception {
        var request = new CaseRequest(
                null,
                "Test title",
                "Test description",
                null,
                LocalDateTime.now().plusDays(180)
        );
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.data.error").value(errorMessage.unauthorizedErrorMessage()));

    }

    @Test
    @WithMockUser(username = "testuser", roles = {"STAFF"})
    public void shouldReturnAllCases() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tasks", hasSize(2)));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"STAFF"})
    public void shouldReturnCaseWhenSearchByTitle() throws Exception {
        mockMvc.perform(get(BASE_URL).param("title", "title 2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tasks", hasSize(1)))
                .andExpect(jsonPath("$.data.tasks[0].title").value("Test title 2"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"STAFF"})
    public void shouldReturnCaseWhenSearchByCreatedBy() throws Exception {
        mockMvc.perform(get(BASE_URL).param("createdBy", createdByForTask1.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tasks", hasSize(1)))
                .andExpect(jsonPath("$.data.tasks[0].title").value("Test title"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"STAFF"})
    public void shouldReturnOneById() throws Exception {
        try(MockedStatic<SecurityUtils> mockedSecurityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            //Setting up the principal to match task 1 createdBy UUID
            mockedSecurityUtils.when(SecurityUtils::getPrincipal).thenReturn(Optional.of(JwtUserDetails.builder().id(savedTasks.get(0).getCreatedBy()).build()));

            mockMvc.perform(get(BASE_URL + "{id}", savedTasks.get(0).getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.task.id").value(savedTasks.get(0).getId().toString()))
                    .andExpect(jsonPath("$.data.task.title").value(savedTasks.get(0).getTitle()))
                    .andExpect(jsonPath("$.data.task.status").value(savedTasks.get(0).getStatus().toString()));
        }
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"STAFF"})
    void shouldNotAllowAccessToRecordByIdIfUserIsNotOwner_denyAccess() throws Exception {

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getPrincipal).thenReturn(Optional.of(JwtUserDetails.builder().id(UUID.randomUUID()).build()));
            mockMvc.perform(get(BASE_URL + "{id}", savedTasks.get(0).getId()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.data.error").value(errorMessage.unauthorizedErrorMessage()));
        }
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"STAFF"})
    public void shouldUpdateStatus() throws Exception {

        var request = CaseRequest.builder()
                .id(savedTasks.get(0).getId())
                .status(TaskStatus.COMPLETED)
                .build();

        try(MockedStatic<SecurityUtils> mockedSecurityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            //Setting up the principal to match task 1 createdBy UUID
            mockedSecurityUtils.when(SecurityUtils::getPrincipal).thenReturn(Optional.of(JwtUserDetails.builder().id(savedTasks.get(0).getCreatedBy()).build()));

            mockMvc.perform(
                            put(BASE_URL)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.task.id").value(savedTasks.get(0).getId().toString()))
                    .andExpect(jsonPath("$.data.task.title").value(savedTasks.get(0).getTitle()))
                    .andExpect(jsonPath("$.data.task.status").value(savedTasks.get(0).getStatus().toString()));
        }
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"STAFF"})
    void shouldNotUpdateTaskWithInvalidRequestId() throws Exception {
        // Updating a task without passing the id should be rejected.
        // The endpoint will return a bad request with a message
        try(MockedStatic<SecurityUtils> mockedSecurityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            //Setting up the principal to match task 1 createdBy UUID
            mockedSecurityUtils.when(SecurityUtils::getPrincipal).thenReturn(Optional.of(JwtUserDetails.builder().id(savedTasks.get(0).getCreatedBy()).build()));

            mockMvc.perform(put(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(CaseRequest.builder().status(TaskStatus.COMPLETED).build()))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.errors.id").value(fieldHelper.idRequired()));
        }
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"STAFF"})
    void shouldNotUpdateTaskWhenUserIsNotOwner_denyAccess() throws Exception {
        var request = CaseRequest.builder()
                .id(savedTasks.get(0).getId())
                .status(TaskStatus.COMPLETED)
                .build();

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getPrincipal).thenReturn(Optional.of(JwtUserDetails.builder().id(UUID.randomUUID()).build()));
            mockMvc.perform(put(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.data.error").value(errorMessage.unauthorizedErrorMessage()));
        }
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void shouldNotUpdateTaskWhenUnauthorisedPermission_denyAccess() throws Exception {
        var request = CaseRequest.builder()
                .id(savedTasks.get(0).getId())
                .status(TaskStatus.COMPLETED)
                .build();

        try(MockedStatic<SecurityUtils> mockedSecurityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            //Setting up the principal to match task 1 createdBy UUID
            mockedSecurityUtils.when(SecurityUtils::getPrincipal).thenReturn(Optional.of(JwtUserDetails.builder().id(savedTasks.get(0).getCreatedBy()).build()));

            mockMvc.perform(put(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.data.error").value(errorMessage.unauthorizedErrorMessage()));
        }
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"STAFF"})
    public void shouldDeleteCase() throws Exception {
        try(MockedStatic<SecurityUtils> mockedSecurityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            //Setting up the principal to match task 1 createdBy UUID
            mockedSecurityUtils.when(SecurityUtils::getPrincipal).thenReturn(Optional.of(JwtUserDetails.builder().id(savedTasks.get(0).getCreatedBy()).build()));

            // Execute & Verify
            mockMvc.perform(delete(BASE_URL + "{id}", savedTasks.get(0).getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(successMessage.deleteTaskSuccessMessage()));

            // Verify task is actually deleted
            assertFalse(taskRepository.existsById(savedTasks.get(0).getId()));
        }
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"STAFF"})
    public void shouldNotDeleteWhenUserIsNotOwner_denyAccess() throws Exception {
        try(MockedStatic<SecurityUtils> mockedSecurityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            //Setting up the principal to not match any tasks createdBy UUID
            mockedSecurityUtils.when(SecurityUtils::getPrincipal).thenReturn(Optional.of(JwtUserDetails.builder().id(UUID.randomUUID()).build()));

            // Execute & Verify
            mockMvc.perform(delete(BASE_URL + "{id}", savedTasks.get(0).getId()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.data.error").value(errorMessage.unauthorizedErrorMessage()));

            // Verify task is not deleted deleted
            assertTrue(taskRepository.existsById(savedTasks.get(0).getId()));
        }
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void shouldNotDeleteTaskWhenUnauthorisedPermission_denyAccess() throws Exception {
        // Prepare
        var taskId = savedTasks.get(0).getId();

        try(MockedStatic<SecurityUtils> mockedSecurityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            //Setting up the principal to match task 1 createdBy UUID
            mockedSecurityUtils.when(SecurityUtils::getPrincipal).thenReturn(Optional.of(JwtUserDetails.builder().id(savedTasks.get(0).getCreatedBy()).build()));

            // Execute & Verify
            mockMvc.perform(delete(BASE_URL + "{id}", taskId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.data.error").value(errorMessage.unauthorizedErrorMessage()));
        }
    }
}