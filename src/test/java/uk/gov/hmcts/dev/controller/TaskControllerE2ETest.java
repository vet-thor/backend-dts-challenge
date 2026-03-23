package uk.gov.hmcts.dev.controller;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.util.MultiValueMap;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.dev.config.extensions.PostgresTestContainerConfiguration;
import uk.gov.hmcts.dev.config.extensions.RedisTestContainerConfiguration;
import uk.gov.hmcts.dev.dto.CreateTaskRequest;
import uk.gov.hmcts.dev.dto.JwtUserDetails;
import uk.gov.hmcts.dev.dto.UpdateTaskRequest;
import uk.gov.hmcts.dev.model.Task;
import uk.gov.hmcts.dev.model.TaskStatus;
import uk.gov.hmcts.dev.repository.TaskRepository;
import uk.gov.hmcts.dev.test_data.arhument_source.GetTaskSearchByArgumentSource;
import uk.gov.hmcts.dev.util.SecurityUtils;
import uk.gov.hmcts.dev.util.helper.ErrorMessageHelper;
import uk.gov.hmcts.dev.util.helper.FieldHelper;
import uk.gov.hmcts.dev.util.helper.SuccessMessageHelper;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uk.gov.hmcts.dev.test_data.CaseTestData.monthlyProgressReportMockCreateRequestPayload;
import static uk.gov.hmcts.dev.test_data.CaseTestData.reviewEvidenceMockCreateRequestPayload;
import static uk.gov.hmcts.dev.test_data.constants.TestCredentialConstant.*;
import static uk.gov.hmcts.dev.test_data.constants.ServiceTestConstants.*;


@Disabled
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import({RedisTestContainerConfiguration.class, PostgresTestContainerConfiguration.class})
@Transactional
class TaskControllerE2ETest {

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
    private List<Task> savedTaskList;

    private static final String BASE_URL = "/api/v1/tasks";

    @BeforeEach
    void setUp() {
        savedTaskList = taskRepository.saveAllAndFlush(
                List.of(

                        // Review evidence data
                        Task.builder()
                                .title(REVIEW_EVIDENCE_TITLE)
                                .description(REVIEW_EVIDENCE_DESCRIPTION)
                                .status(TaskStatus.IN_PROGRESS)
                                .due(VALID_DUE_DATE)
                                .createdBy(CREATED_BY_USER_ID)
                                .build(),

                        // Client assessment data
                        Task.builder()
                                .title(CLIENT_ASSESSMENT_TITLE)
                                .description(CLIENT_ASSESSMENT_DESCRIPTION)
                                .status(TaskStatus.IN_PROGRESS)
                                .due(VALID_DUE_DATE)
                                .createdBy(CREATED_BY_USER_ID)
                                .build()
                )
        );
    }

    @Nested
    @DisplayName("Given a user who is authenticated creates a task")
    public class CreateTaskTest {
        @Test
        @DisplayName("Should create a new task when a record with the same title does not exists by the creator")
        void shouldCreateTask() throws Exception {
            var reviewEvidenceRequestPayload = monthlyProgressReportMockCreateRequestPayload();

            try (MockedStatic<SecurityUtils> mockedSecurityUtils = Mockito.mockStatic(SecurityUtils.class)) {
                //Setting up the principal to match task 1 createdBy UUID
                mockedSecurityUtils.when(SecurityUtils::getPrincipal).thenReturn(Optional.of(JwtUserDetails.builder().id(CREATED_BY_USER_ID).build()));

                mockMvc.perform(post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reviewEvidenceRequestPayload))
                                .with(user(VALID_USERNAME).roles(VALID_ROLE_STAFF))
                        )
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.data.task.title").value(reviewEvidenceRequestPayload.title()))
                        .andExpect(jsonPath("$.data.task.description").value(reviewEvidenceRequestPayload.description()))
                        .andExpect(jsonPath("$.data.task.status").value(TaskStatus.OPEN.toString()));
            }

        }

        @Test
        @DisplayName("Should not create a task when a user has already created task with same title and creator")
        void shouldNotCreateTaskWhenTitleExistForSameOwner() throws Exception {
            var reviewEvidenceRequestPayload = reviewEvidenceMockCreateRequestPayload();

            try (MockedStatic<SecurityUtils> mockedSecurityUtils = Mockito.mockStatic(SecurityUtils.class)) {
                //Setting up the principal to match task 1 createdBy UUID
                mockedSecurityUtils.when(SecurityUtils::getPrincipal).thenReturn(Optional.of(JwtUserDetails.builder().id(CREATED_BY_USER_ID).build()));

                mockMvc.perform(post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reviewEvidenceRequestPayload))
                                .with(user(VALID_USERNAME).roles(VALID_ROLE_STAFF))
                        )
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.data.errors.title").value(errorMessage.duplicateTitleErrorMessage()));
            }
        }

        @Test
        @DisplayName("Should not create task when a task is created without inputting required fields")
        void shouldNotCreateTaskWithInvalidRequest() throws Exception {

            try (MockedStatic<SecurityUtils> mockedSecurityUtils = Mockito.mockStatic(SecurityUtils.class)) {
                //Setting up the principal to match task 1 createdBy UUID
                mockedSecurityUtils.when(SecurityUtils::getPrincipal).thenReturn(Optional.of(JwtUserDetails.builder().id(CREATED_BY_USER_ID).build()));

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(CreateTaskRequest.builder().build()))
                            .with(user(VALID_USERNAME).roles(VALID_ROLE_STAFF))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.errors.title").value(fieldHelper.titleRequired()))
                    .andExpect(jsonPath("$.data.errors.description").value(fieldHelper.descriptionRequired()))
                    .andExpect(jsonPath("$.data.errors.due").value(fieldHelper.dueDateRequired()));
}
        }

        @Test
        @DisplayName("Should not create task when a user doesn't have the proper create permission")
        public void shouldNotCreateTaskWithWrongPermission_denyAccess() throws Exception {
            var reviewEvidenceRequestPayload = reviewEvidenceMockCreateRequestPayload();

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reviewEvidenceRequestPayload))
                            .with(user(VALID_USERNAME).roles(VALID_ROLE_USER))
                    )
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.data.error").value(errorMessage.unauthorizedErrorMessage()));

        }
    }

    @Nested
    @DisplayName("Given a user who is authenticated fetches a task(s)")
    public class GetTaskTest {
        @Test
        @DisplayName("Should return all owners task when fetched with a valid ID")
        public void shouldReturnAllTasks() throws Exception {

            try (MockedStatic<SecurityUtils> mockedSecurityUtils = Mockito.mockStatic(SecurityUtils.class)) {
                //Setting up the principal to match task 1 createdBy UUID
                mockedSecurityUtils.when(SecurityUtils::getPrincipal).thenReturn(Optional.of(JwtUserDetails.builder().id(CREATED_BY_USER_ID).build()));

                mockMvc.perform(
                            get(BASE_URL)
                                .with(user(VALID_USERNAME).roles(VALID_ROLE_STAFF))
                        )
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.tasks", hasSize(2)));
            }
        }

        @ParameterizedTest(name = "searched by column {0}")
        @ArgumentsSource(GetTaskSearchByArgumentSource.class)
        @DisplayName("Should return an owners task when")
        public void shouldReturnTaskWhenSearchBy(Map<String, String> params, int expectedSize, String expectedValue) throws Exception {

            try (MockedStatic<SecurityUtils> mockedSecurityUtils = Mockito.mockStatic(SecurityUtils.class)) {
                //Setting up the principal to match task 1 createdBy UUID
                mockedSecurityUtils.when(SecurityUtils::getPrincipal).thenReturn(Optional.of(JwtUserDetails.builder().id(CREATED_BY_USER_ID).build()));

                mockMvc.perform(
                            get(BASE_URL)
                                .params(MultiValueMap.fromSingleValue(params))
                                .with(user(VALID_USERNAME).roles(VALID_ROLE_STAFF))
                        )
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.tasks", hasSize(expectedSize)))
                        .andExpect(jsonPath("$.data.tasks[0].title").value(expectedValue));
            }
        }

        @Test
        @DisplayName("Should return a single task when searched by valid ID")
        public void shouldReturnOneById() throws Exception {
            var taskId = savedTaskList.getFirst().getId();

            try (MockedStatic<SecurityUtils> mockedSecurityUtils = Mockito.mockStatic(SecurityUtils.class)) {
                //Setting up the principal to match task 1 createdBy UUID
                mockedSecurityUtils.when(SecurityUtils::getPrincipal).thenReturn(Optional.of(JwtUserDetails.builder().id(CREATED_BY_USER_ID).build()));

                mockMvc.perform(
                                get(BASE_URL + "/{id}", taskId)
                                        .with(user(VALID_USERNAME).roles(VALID_ROLE_STAFF))
                        )
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.task.id").value(taskId.toString()))
                        .andExpect(jsonPath("$.data.task.title").value(REVIEW_EVIDENCE_TITLE))
                        .andExpect(jsonPath("$.data.task.status").value(TaskStatus.IN_PROGRESS.toString()));
            }
        }

        @Test
        @DisplayName("Should not return a task when user doesn't own the task")
        void shouldNotAllowAccessToRecordByIdIfUserIsNotOwner_denyAccess() throws Exception {
            var taskId = savedTaskList.getFirst().getId();

            try (MockedStatic<SecurityUtils> mockedSecurityUtils = Mockito.mockStatic(SecurityUtils.class)) {
                mockedSecurityUtils.when(SecurityUtils::getPrincipal).thenReturn(Optional.of(JwtUserDetails.builder().id(SECOND_CREATED_BY_USER_ID).build()));
                mockMvc.perform(
                                get(BASE_URL + "/{id}", taskId)
                                        .with(user(VALID_USERNAME).roles(VALID_ROLE_STAFF))
                        )
                        .andExpect(status().isForbidden())
                        .andExpect(jsonPath("$.status").value("403 FORBIDDEN"))
                        .andExpect(jsonPath("$.data.error").value(errorMessage.unauthorizedErrorMessage()));
            }
        }
    }

    @Nested
    @DisplayName("Given a user who is authenticated updates a task. Only owner can update task")
    public class UpdateTaskTest {
        @Test
        @DisplayName("Should update task status when owner updates by valid ID")
        public void shouldUpdateStatus() throws Exception {

            var taskId = savedTaskList.getFirst().getId();
            var updateTaskRequest = UpdateTaskRequest.builder()
                    .id(taskId)
                    .status(TaskStatus.COMPLETED)
                    .build();

            try (MockedStatic<SecurityUtils> mockedSecurityUtils = Mockito.mockStatic(SecurityUtils.class)) {
                //Setting up the principal to match task 1 createdBy UUID
                mockedSecurityUtils.when(SecurityUtils::getPrincipal).thenReturn(Optional.of(JwtUserDetails.builder().id(savedTaskList.getFirst().getCreatedBy()).build()));

                mockMvc.perform(
                                put(BASE_URL)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(updateTaskRequest))
                                        .with(user(VALID_USERNAME).roles(VALID_ROLE_STAFF))
                        )
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.task.id").value(taskId.toString()))
                        .andExpect(jsonPath("$.data.task.title").value(REVIEW_EVIDENCE_TITLE))
                        .andExpect(jsonPath("$.data.task.status").value(TaskStatus.COMPLETED.toString()));
            }
        }

        @Test
        @DisplayName("Should not update task when an owner updates with an invalid task ID")
        void shouldNotUpdateTaskWithInvalidRequestId() throws Exception {
            // Updating a task without passing the id should be rejected.
            // The endpoint will return a bad request with a message
            try (MockedStatic<SecurityUtils> mockedSecurityUtils = Mockito.mockStatic(SecurityUtils.class)) {
                //Setting up the principal to match task 1 createdBy UUID
                mockedSecurityUtils.when(SecurityUtils::getPrincipal).thenReturn(Optional.of(JwtUserDetails.builder().id(CREATED_BY_USER_ID).build()));

                mockMvc.perform(put(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(CreateTaskRequest.builder().status(TaskStatus.COMPLETED).build()))
                                .with(user(VALID_USERNAME).roles(VALID_ROLE_STAFF))
                        )
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.data.errors.id").value(fieldHelper.idRequired()));
            }
        }

        @Test
        @DisplayName("Should not update task when a user is not owner of task")
        void shouldNotUpdateTaskWhenUserIsNotOwner_denyAccess() throws Exception {

            var taskId = savedTaskList.getFirst().getId();
            var request = UpdateTaskRequest.builder()
                    .id(taskId)
                    .status(TaskStatus.COMPLETED)
                    .build();

            try (MockedStatic<SecurityUtils> mockedSecurityUtils = Mockito.mockStatic(SecurityUtils.class)) {
                mockedSecurityUtils.when(SecurityUtils::getPrincipal).thenReturn(Optional.of(JwtUserDetails.builder().id(UUID.randomUUID()).build()));

                mockMvc.perform(put(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(user(VALID_USERNAME).roles(VALID_ROLE_STAFF))
                        )
                        .andExpect(status().isForbidden())
                        .andExpect(jsonPath("$.status").value("403 FORBIDDEN"))
                        .andExpect(jsonPath("$.data.error").value(errorMessage.unauthorizedErrorMessage()));
            }
        }

        @Test
        @DisplayName("Should not update task when a user doesn't have the required permission to update the task")
        public void shouldNotUpdateTaskWhenUnauthorisedPermission_denyAccess() throws Exception {

            var taskId = savedTaskList.getFirst().getId();
            var request = UpdateTaskRequest.builder()
                    .id(taskId)
                    .status(TaskStatus.COMPLETED)
                    .build();

            try (MockedStatic<SecurityUtils> mockedSecurityUtils = Mockito.mockStatic(SecurityUtils.class)) {
                //Setting up the principal to match task 1 createdBy UUID
                mockedSecurityUtils.when(SecurityUtils::getPrincipal).thenReturn(Optional.of(JwtUserDetails.builder().id(savedTaskList.getFirst().getCreatedBy()).build()));

                mockMvc.perform(put(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(user(VALID_USERNAME).roles(VALID_ROLE_USER))
                        )
                        .andExpect(status().isForbidden())
                        .andExpect(jsonPath("$.data.error").value(errorMessage.unauthorizedErrorMessage()));
            }
        }
    }

    @Nested
    @DisplayName("Given a user who is authenticated deletes a task. Only owner can delete task")
    public class DeleteTaskTest {
        @Test
        @DisplayName("Should delete a task when a task is deleted by owner")
        public void shouldDeleteTask() throws Exception {
            var taskId = savedTaskList.getFirst().getId();

            try (MockedStatic<SecurityUtils> mockedSecurityUtils = Mockito.mockStatic(SecurityUtils.class)) {
                //Setting up the principal to match task 1 createdBy UUID
                mockedSecurityUtils.when(SecurityUtils::getPrincipal).thenReturn(Optional.of(JwtUserDetails.builder().id(savedTaskList.getFirst().getCreatedBy()).build()));

                // Execute & Verify
                mockMvc.perform(
                                delete(BASE_URL + "/{id}", taskId)
                                        .with(user(VALID_USERNAME).roles(VALID_ROLE_STAFF))
                        )
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.message").value(successMessage.deleteTaskSuccessMessage()));

                // Verify task is actually deleted
                assertFalse(taskRepository.existsById(taskId));
            }
        }

        @Test
        @DisplayName("Should not delete a task when not created by user")
        public void shouldNotDeleteWhenUserIsNotOwner_denyAccess() throws Exception {
            var taskId = savedTaskList.getFirst().getId();

            try (MockedStatic<SecurityUtils> mockedSecurityUtils = Mockito.mockStatic(SecurityUtils.class)) {
                //Setting up the principal to not match any tasks createdBy UUID
                mockedSecurityUtils.when(SecurityUtils::getPrincipal).thenReturn(Optional.of(JwtUserDetails.builder().id(UUID.randomUUID()).build()));

                // Execute & Verify
                mockMvc.perform(
                                delete(BASE_URL + "/{id}", taskId)
                                        .with(user(VALID_USERNAME).roles(VALID_ROLE_STAFF))
                        )
                        .andExpect(status().isForbidden())
                        .andExpect(jsonPath("$.data.error").value(errorMessage.unauthorizedErrorMessage()));

                // Verify task is not deleted deleted
                assertTrue(taskRepository.existsById(taskId));
            }
        }

        @Test
        @DisplayName("Should not delete task when user does not have the required permissions")
        public void shouldNotDeleteTaskWhenUnauthorisedPermission_denyAccess() throws Exception {
            // Prepare
            var taskId = savedTaskList.getFirst().getId();

            try (MockedStatic<SecurityUtils> mockedSecurityUtils = Mockito.mockStatic(SecurityUtils.class)) {
                //Setting up the principal to match task 1 createdBy UUID
                mockedSecurityUtils.when(SecurityUtils::getPrincipal).thenReturn(Optional.of(JwtUserDetails.builder().id(savedTaskList.getFirst().getCreatedBy()).build()));

                // Execute & Verify
                mockMvc.perform(
                                delete(BASE_URL + "/{id}", taskId)
                                        .with(user(VALID_USERNAME).roles(VALID_ROLE_USER))
                        )
                        .andExpect(status().isForbidden())
                        .andExpect(jsonPath("$.data.error").value(errorMessage.unauthorizedErrorMessage()));
            }

            // Verify task is not deleted deleted
            assertTrue(taskRepository.existsById(taskId));
        }
    }
}