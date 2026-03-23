package uk.gov.hmcts.dev.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.dev.config.ExceptionHandlerConfig;
import uk.gov.hmcts.dev.config.LocaleConfiguration;
import uk.gov.hmcts.dev.dto.*;
import uk.gov.hmcts.dev.model.TaskStatus;
import uk.gov.hmcts.dev.repository.TaskRepository;
import uk.gov.hmcts.dev.security.JWTFilter;
import uk.gov.hmcts.dev.security.PermissionChecker;
import uk.gov.hmcts.dev.security.SecurityConfig;
import uk.gov.hmcts.dev.security.UserInfoConfigManager;
import uk.gov.hmcts.dev.service.TaskService;
import uk.gov.hmcts.dev.util.SecurityUtils;
import uk.gov.hmcts.dev.util.helper.ErrorMessageHelper;
import uk.gov.hmcts.dev.util.helper.FieldHelper;
import uk.gov.hmcts.dev.util.helper.SuccessMessageHelper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.dev.test_data.CaseTestData.*;
import static uk.gov.hmcts.dev.test_data.constants.SuccessTestMessageConstant.RESOURCE_DELETED_SUCCESSFULLY;
import static uk.gov.hmcts.dev.test_data.constants.TestCredentialConstant.*;
import static uk.gov.hmcts.dev.test_data.constants.ErrorTestMessageConstant.UNAUTHORISED_ERROR_MESSAGE;
import static uk.gov.hmcts.dev.test_data.constants.ServiceTestConstants.*;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({LocaleConfiguration.class, SecurityConfig.class, ExceptionHandlerConfig.class})
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private FieldHelper fieldHelper;
    @MockitoBean
    private ErrorMessageHelper errorMessageHelper;
    @MockitoBean
    private SuccessMessageHelper successMessageHelper;
    @MockitoBean
    private TaskRepository taskRepository;
    @MockitoBean
    private JWTFilter jwtFilter;
    @MockitoBean
    private UserInfoConfigManager userInfoConfigManager;
    @MockitoBean
    private TaskService taskService;
    @MockitoBean(name = "permissionChecker")
    private PermissionChecker permissionChecker;
    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/tasks";

    @Nested
    @DisplayName("Given a user who is authenticated creates a task")
    public class CreateTaskTest {
        @Test
        @WithMockUser(roles = VALID_ROLE_STAFF)
        @DisplayName("Should create a new task when a record with the same title does not exists by the creator")
        void shouldCreateTask() throws Exception {
            var reviewEvidenceRequestPayload = reviewEvidenceMockCreateRequestPayload();
            var reviewEvidenceResponsePayload = listOfExpectedResponseMockData().getFirst();
            var taskResponseData = TaskResponseData
                                .builder()
                                .task(reviewEvidenceResponsePayload).build();

            given(taskService.createTask(any(CreateTaskRequest.class))).willReturn(taskResponseData);
            given(successMessageHelper.createTaskSuccessMessage()).willReturn("Task Created Successfully");

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reviewEvidenceRequestPayload))
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.message").value("Task Created Successfully"))
                    .andExpect(jsonPath("$.data.task.title").value(REVIEW_EVIDENCE_TITLE))
                    .andExpect(jsonPath("$.data.task.description").value(REVIEW_EVIDENCE_DESCRIPTION))
                    .andExpect(jsonPath("$.data.task.status").value(TaskStatus.IN_PROGRESS.toString()));

        }

        @Test
        @WithMockUser(roles = VALID_ROLE_STAFF)
        @DisplayName("Should not create a task when a user has already created task with same title and creator")
        void shouldNotCreateTaskWhenTitleExistForSameOwner() throws Exception {
            var reviewEvidenceRequestPayload = reviewEvidenceMockCreateRequestPayload();

            try (MockedStatic<SecurityUtils> mockedSecurityUtils = Mockito.mockStatic(SecurityUtils.class)) {
                //Setting up the principal to match task 1 createdBy UUID
                mockedSecurityUtils.when(SecurityUtils::getPrincipal).thenReturn(Optional.of(JwtUserDetails.builder().id(CREATED_BY_USER_ID).build()));
                given(taskRepository.existsByTitleIgnoreCaseAndCreatedBy(REVIEW_EVIDENCE_TITLE, CREATED_BY_USER_ID)).willReturn(true);

                mockMvc.perform(post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reviewEvidenceRequestPayload))
                        )
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.data.errors.title").value("Title already exists"));
            }
        }

        @Test
        @WithMockUser(roles = VALID_ROLE_STAFF)
        @DisplayName("Should not create task when a task is created without inputting required fields")
        void shouldNotCreateTaskWithInvalidRequest() throws Exception {

            var invalidRequest = CreateTaskRequest.builder().build();

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest))
                            .with(user(VALID_USERNAME).roles(VALID_ROLE_STAFF))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.errors.title").value("Title is required"))
                    .andExpect(jsonPath("$.data.errors.description").value("Description is required"))
                    .andExpect(jsonPath("$.data.errors.due").value("Due date is required"));
        }

        @Test
        @WithMockUser(roles = VALID_ROLE_USER)
        @DisplayName("Should not create task when a user doesn't have the proper create permission")
        public void shouldNotCreateTaskWithWrongPermission_denyAccess() throws Exception {
            var reviewEvidenceRequestPayload = reviewEvidenceMockCreateRequestPayload();

            given(errorMessageHelper.unauthorizedErrorMessage()).willReturn(UNAUTHORISED_ERROR_MESSAGE);

            mockMvc.perform(
                        post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reviewEvidenceRequestPayload))
                    )
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.data.error").value(UNAUTHORISED_ERROR_MESSAGE));

        }
    }

    @Nested
    @DisplayName("Given a user who is authenticated fetches a task(s)")
    public class GetTaskTest {
        @Test
        @WithMockUser(roles = VALID_ROLE_STAFF)
        @DisplayName("Should return all owners task when fetched with a valid ID")
        public void shouldReturnAllTasks() throws Exception {
            var listOfTaskResponse = listOfExpectedResponseMockData();
            var expectedResponse = TaskResponseData.builder()
                    .tasks(listOfTaskResponse)
                    .build();

            given(taskService.getTask(any(SearchCriteria.class))).willReturn(expectedResponse);

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.tasks", hasSize(3)))
                    .andExpect(jsonPath("$.data.tasks[0].title").value(REVIEW_EVIDENCE_TITLE))
                    .andExpect(jsonPath("$.data.tasks[1].title").value(CLIENT_ASSESSMENT_TITLE))
                    .andExpect(jsonPath("$.data.tasks[2].title").value(MONTHLY_PROGRESS_REPORT_TITLE));
        }

        @Test
        @WithMockUser(roles = VALID_ROLE_STAFF)
        @DisplayName("Should return an owners task when searched by title of task")
        public void shouldReturnTaskWhenSearchByTitle() throws Exception {
            var listOfTaskResponse = listOfExpectedResponseMockData().get(1);
            var expectedResponse = TaskResponseData.builder()
                    .tasks(List.of(listOfTaskResponse))
                    .build();
            given(taskService.getTask(any(SearchCriteria.class))).willReturn(expectedResponse);

            mockMvc.perform(
                        get(BASE_URL)
                            .param("title", CLIENT_ASSESSMENT_TITLE)
                            .with(user(VALID_USERNAME).roles(VALID_ROLE_STAFF))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.tasks", hasSize(1)))
                    .andExpect(jsonPath("$.data.tasks[0].title").value(CLIENT_ASSESSMENT_TITLE));
        }

        @Test
        @WithMockUser(roles = VALID_ROLE_STAFF)
        @DisplayName("Should return a single task when searched by valid ID")
        public void shouldReturnOneById() throws Exception {

            var reviewEvidenceResponsePayload = listOfExpectedResponseMockData().getFirst();

            given(permissionChecker.isOwnersCase(REVIEW_EVIDENCE_ID)).willReturn(true);
            given(taskService.getTask(REVIEW_EVIDENCE_ID)).willReturn(TaskResponseData.builder()
                    .task(reviewEvidenceResponsePayload).build());

            mockMvc.perform(
                            get(BASE_URL + "/{id}", REVIEW_EVIDENCE_ID)
                                    .with(user(VALID_USERNAME).roles(VALID_ROLE_STAFF))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.task.id").value(REVIEW_EVIDENCE_ID.toString()))
                    .andExpect(jsonPath("$.data.task.title").value(REVIEW_EVIDENCE_TITLE))
                    .andExpect(jsonPath("$.data.task.status").value(TaskStatus.IN_PROGRESS.toString()));
        }

        @Test
        @WithMockUser(roles = VALID_ROLE_USER)
        @DisplayName("Should not return a task when user doesn't own the task")
        void shouldNotAllowAccessToRecordByIdIfUserIsNotOwner_denyAccess() throws Exception {
            var taskId = UUID.randomUUID();

            given(permissionChecker.isOwnersCase(taskId)).willReturn(false);
            given(errorMessageHelper.unauthorizedErrorMessage()).willReturn("You do not have permission to access this resource");

            mockMvc.perform(
                            get(BASE_URL + "/{id}", taskId)
                    )
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value("403 FORBIDDEN"))
                    .andExpect(jsonPath("$.data.error").value("You do not have permission to access this resource"));
        }
    }

    @Nested
    @DisplayName("Given a user who is authenticated updates a task. Only owner can update task")
    public class UpdateTaskTest {
        @Test
        @WithMockUser(roles = "STAFF")
        @DisplayName("Should update task status when owner updates by valid ID")
        public void shouldUpdateStatus() throws Exception {

            var modifiedRecord = TaskResponse.builder()
                    .id(REVIEW_EVIDENCE_ID)
                    .title(REVIEW_EVIDENCE_TITLE)
                    .description(REVIEW_EVIDENCE_DESCRIPTION)
                    .due(VALID_DUE_DATE)
                    .status(TaskStatus.COMPLETED)
                    .build();

            var updateTaskRequest = UpdateTaskRequest.builder()
                    .id(REVIEW_EVIDENCE_ID)
                    .status(TaskStatus.COMPLETED)
                    .build();


            var responseData = TaskResponseData
                    .builder()
                    .task(modifiedRecord)
                    .build();

            given(permissionChecker.isOwnersCase(REVIEW_EVIDENCE_ID)).willReturn(true);
            given(taskService.updateTask(any(UpdateTaskRequest.class))).willReturn(responseData);
            given(successMessageHelper.updateTaskSuccessMessage()).willReturn("Task updated successfully");

            mockMvc.perform(
                            put(BASE_URL)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateTaskRequest))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Task updated successfully"))
                    .andExpect(jsonPath("$.data.task.id").value(REVIEW_EVIDENCE_ID.toString()))
                    .andExpect(jsonPath("$.data.task.title").value(REVIEW_EVIDENCE_TITLE))
                    .andExpect(jsonPath("$.data.task.status").value(TaskStatus.COMPLETED.toString()));
        }

        @Test
        @DisplayName("Should not update task when an owner updates with an invalid task ID")
        void shouldNotUpdateTaskWithInvalidRequestId() throws Exception {
            // Updating a task without passing the id should be rejected.
            // The endpoint will return a bad request with a message

            var updateTaskRequest = CreateTaskRequest.builder().build();

            mockMvc.perform(
                        put(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateTaskRequest))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.errors.id").value("ID is a required Field"));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should not update task when a user doesn't have the required permission to update the task")
        public void shouldNotUpdateTaskWhenUnauthorisedPermission_denyAccess() throws Exception {

            var request = UpdateTaskRequest.builder()
                    .id(REVIEW_EVIDENCE_ID)
                    .status(TaskStatus.COMPLETED)
                    .build();

            given(permissionChecker.isOwnersCase(REVIEW_EVIDENCE_ID)).willReturn(true);
            given(errorMessageHelper.unauthorizedErrorMessage()).willReturn("You are not authorised to access this resource");

            mockMvc.perform(put(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.data.error").value("You are not authorised to access this resource"));
        }
    }

    @Nested
    @DisplayName("Given a user who is authenticated deletes a task. Only owner can delete task")
    public class DeleteTaskTest {
        @Test
        @WithMockUser(roles = VALID_ROLE_STAFF)
        @DisplayName("Should delete a task when a task is deleted by owner")
        public void shouldDeleteTask() throws Exception {

            given(permissionChecker.isOwnersCase(REVIEW_EVIDENCE_ID)).willReturn(true);
            given(successMessageHelper.deleteTaskSuccessMessage()).willReturn(RESOURCE_DELETED_SUCCESSFULLY);

            // Execute & Verify
            mockMvc.perform(
                            delete(BASE_URL + "/{id}", REVIEW_EVIDENCE_ID)
                                    .with(user(VALID_USERNAME).roles(VALID_ROLE_STAFF))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(RESOURCE_DELETED_SUCCESSFULLY));

        }

        @Test
        @WithMockUser(roles = VALID_ROLE_STAFF)
        @DisplayName("Should not delete a task when not created by user")
        public void shouldNotDeleteWhenUserIsNotOwner_denyAccess() throws Exception {

            given(permissionChecker.isOwnersCase(REVIEW_EVIDENCE_ID)).willReturn(false);
            given(errorMessageHelper.unauthorizedErrorMessage()).willReturn(UNAUTHORISED_ERROR_MESSAGE);

            // Execute & Verify
            mockMvc.perform(
                            delete(BASE_URL + "/{id}", REVIEW_EVIDENCE_ID)
                    )
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.data.error").value(UNAUTHORISED_ERROR_MESSAGE));

        }

        @Test
        @WithMockUser(roles = VALID_ROLE_USER)
        @DisplayName("Should not delete task when user does not have the required permissions")
        public void shouldNotDeleteTaskWhenUnauthorisedPermission_denyAccess() throws Exception {

            given(errorMessageHelper.unauthorizedErrorMessage()).willReturn(UNAUTHORISED_ERROR_MESSAGE);

            // Execute & Verify
            mockMvc.perform(
                            delete(BASE_URL + "/{id}", REVIEW_EVIDENCE_ID)
                                    .with(user(VALID_USERNAME).roles(VALID_ROLE_USER))
                    )
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.data.error").value(UNAUTHORISED_ERROR_MESSAGE));
        }
    }
}