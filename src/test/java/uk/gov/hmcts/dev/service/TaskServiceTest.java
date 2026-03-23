package uk.gov.hmcts.dev.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.dev.dto.TaskResponse;
import uk.gov.hmcts.dev.dto.SearchCriteria;
import jakarta.persistence.EntityNotFoundException;
import uk.gov.hmcts.dev.dto.UpdateTaskRequest;
import uk.gov.hmcts.dev.mapper.TaskMapper;
import uk.gov.hmcts.dev.model.Task;
import uk.gov.hmcts.dev.model.TaskStatus;
import uk.gov.hmcts.dev.repository.TaskRepository;
import uk.gov.hmcts.dev.test_data.constants.ServiceTestConstants;
import uk.gov.hmcts.dev.test_data.arhument_source.UpdateStatusArgumentSource;
import uk.gov.hmcts.dev.util.helper.ErrorMessageHelper;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.nonNull;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.dev.test_data.CaseTestData.*;

@ExtendWith(MockitoExtension.class)

class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private ErrorMessageHelper errorMessageHelper;

    @InjectMocks
    private TaskService taskService;

    private List<Task> savedtaskList;

    @BeforeEach
    void setup() {

        savedtaskList = List.of(
                setupTaskEntity(
                        ServiceTestConstants.REVIEW_EVIDENCE_ID,
                        ServiceTestConstants.REVIEW_EVIDENCE_TITLE,
                        ServiceTestConstants.REVIEW_EVIDENCE_DESCRIPTION,
                        TaskStatus.IN_PROGRESS),
                setupTaskEntity(
                        ServiceTestConstants.CLIENT_ASSESSMENT_ID,
                        ServiceTestConstants.CLIENT_ASSESSMENT_TITLE,
                        ServiceTestConstants.CLIENT_ASSESSMENT_DESCRIPTION,
                        TaskStatus.IN_PROGRESS),
                setupTaskEntity(
                        ServiceTestConstants.MONTHLY_PROGRESS_REPORT_ID,
                        ServiceTestConstants.MONTHLY_PROGRESS_REPORT_TITLE,
                        ServiceTestConstants.MONTHLY_PROGRESS_REPORT_DESCRIPTION,
                        TaskStatus.IN_PROGRESS)
        );
    }

    @Nested
    @DisplayName("Given one or more task(s) is queried")
    public class GetTaskTest {
        @Test
        @DisplayName("Should return a task response when a valid id is supplied")
        void getTaskById_shouldReturnTask() {
            //Arrange
            TaskResponse reviewEvidenceExpectedResponse = listOfExpectedResponseMockData().getFirst();
            Task reviewEvidenceSavedTask = savedtaskList.getFirst();

            // Given
            when(taskRepository.findById(ServiceTestConstants.REVIEW_EVIDENCE_ID)).thenReturn(Optional.of(reviewEvidenceSavedTask));
            when(taskMapper.toTaskResponse(reviewEvidenceSavedTask)).thenReturn(reviewEvidenceExpectedResponse);

            // When
            var result = taskService.getTask(ServiceTestConstants.REVIEW_EVIDENCE_ID);

            // Then
            assertTrue(nonNull(result.getTask()));
            assertEquals(ServiceTestConstants.REVIEW_EVIDENCE_ID, result.getTask().id());
            assertEquals(ServiceTestConstants.REVIEW_EVIDENCE_TITLE, result.getTask().title());

            // Verify
            verify(taskRepository).findById(ServiceTestConstants.REVIEW_EVIDENCE_ID);
            verify(taskMapper).toTaskResponse(reviewEvidenceSavedTask);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when a task is not found by id")
        void getTaskById_shouldThrowExceptionWhenNotFound() {
            // Given
            when(taskRepository.findById(ServiceTestConstants.REVIEW_EVIDENCE_ID)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(EntityNotFoundException.class, () -> {
                taskService.getTask(ServiceTestConstants.REVIEW_EVIDENCE_ID);
            });
        }

        @Test
        @DisplayName("Should return a list of task records when a search specification occurs")
        void getAllTasks_shouldReturnAllCases() {
            // Given
            given(taskRepository.findAll(ArgumentMatchers.<Specification<Task>>any(), any(Pageable.class))).willReturn(new PageImpl<>(savedtaskList));
            given(taskMapper.pageToTasksResponse(new PageImpl<>(savedtaskList))).willReturn(listOfExpectedResponseMockData());

            // When
            var result = taskService.getTask(
                    SearchCriteria.builder()
                            .page(0)
                            .limit(10)
                            .sortBy("createAt")
                            .sortOrder(Sort.Direction.DESC)
                            .build()
            );

            // Then
            assertEquals(3, result.getTasks().size());
            assertEquals(
                    List.of(
                            ServiceTestConstants.REVIEW_EVIDENCE_TITLE,
                            ServiceTestConstants.CLIENT_ASSESSMENT_TITLE,
                            ServiceTestConstants.MONTHLY_PROGRESS_REPORT_TITLE
                    ),
                    result.getTasks().stream().map(TaskResponse::title).toList()
            );

            // Verify
            verify(taskRepository).findAll(ArgumentMatchers.<Specification<Task>>any(), any(Pageable.class));
            verify(taskMapper).pageToTasksResponse(new PageImpl<>(savedtaskList));
        }
    }

    @Nested
    @DisplayName("Given a new task is created")
    public class CreateTaskTest {
        @Test
        @DisplayName("Should create new task when valid request is made")
        void createTask_shouldSaveTask() {
            //Arrange
            Task reviewEvidenceSavedTask = savedtaskList.getFirst();
            TaskResponse reviewEvidenceExpectedResponse = listOfExpectedResponseMockData().getFirst();
            var reviewEvidenceCreatePayload = reviewEvidenceMockCreateRequestPayload();

            // Given
            given(taskMapper.toTask(reviewEvidenceCreatePayload)).willReturn(reviewEvidenceSavedTask);
            given(taskRepository.save(reviewEvidenceSavedTask)).willReturn(reviewEvidenceSavedTask);
            given(taskMapper.toTaskResponse(reviewEvidenceSavedTask)).willReturn(reviewEvidenceExpectedResponse);

            // When
            var result = taskService.createTask(reviewEvidenceCreatePayload);

            // Then
            assertTrue(nonNull(result.getTask()));
            assertEquals(reviewEvidenceExpectedResponse.title(), result.getTask().title());
            assertEquals(reviewEvidenceExpectedResponse.description(), result.getTask().description());
            assertEquals(TaskStatus.IN_PROGRESS, result.getTask().status());

            // Verify
            verify(taskMapper).toTask(reviewEvidenceCreatePayload);
            verify(taskRepository, times(1)).save(reviewEvidenceSavedTask);
            verify(taskMapper).toTaskResponse(reviewEvidenceSavedTask);
        }
    }

    @Nested
    @DisplayName("Given an existing task is modified")
    public class UpdateTaskTest {
        @ParameterizedTest(name = "Scenario {index}: Updated values: {arguments}")
        @ArgumentsSource(UpdateStatusArgumentSource.class)
        @DisplayName("Should update when a task is modified")
        void updateTaskStatus_shouldUpdateStatus(
                UpdateTaskRequest reviewEvidenceUpdateRequest,
                String expectedTitle, String expectedDescription, TaskStatus expectedStatus, LocalDateTime expectedDueDate) {

            //Arrange
            Task reviewEvidenceSavedTask = savedtaskList.getFirst();
            Task reviewEvidenceModifiedTask = setupTaskEntity(
                    ServiceTestConstants.REVIEW_EVIDENCE_ID,
                    expectedTitle,
                    expectedDescription,
                    expectedStatus
            );
            TaskResponse reviewEvidenceExpectedResponse = new TaskResponse(
                    ServiceTestConstants.REVIEW_EVIDENCE_ID,
                    expectedTitle,
                    expectedDescription,
                    expectedStatus,
                    expectedDueDate
            );

            reviewEvidenceModifiedTask.setUpdatedAt(LocalDateTime.now());

            //Given
            given(taskRepository.findById(ServiceTestConstants.REVIEW_EVIDENCE_ID)).willReturn(Optional.of(reviewEvidenceSavedTask));
            given(taskRepository.save(reviewEvidenceSavedTask)).willReturn(reviewEvidenceModifiedTask);
            given(taskMapper.toTaskResponse(reviewEvidenceModifiedTask)).willReturn(reviewEvidenceExpectedResponse);

            // When
            var result = taskService.updateTask(reviewEvidenceUpdateRequest);

            // Then
            assertNotNull(result.getTask());
            assertEquals(expectedTitle, result.getTask().title());
            assertEquals(expectedDescription, result.getTask().description());
            assertEquals(expectedStatus, result.getTask().status());
            assertEquals(expectedDueDate, result.getTask().due());

            //Verify
            verify(taskRepository).findById(ServiceTestConstants.REVIEW_EVIDENCE_ID);
            verify(taskMapper).applyChangesToTask(reviewEvidenceUpdateRequest, reviewEvidenceSavedTask);
            verify(taskRepository).save(reviewEvidenceSavedTask);
            verify(taskMapper).toTaskResponse(reviewEvidenceModifiedTask);
        }

        @Test
        @DisplayName("Should not update task when an invalid task id is supplied")
        void updateTaskStatus_shouldThrowExceptionWhenNotFound() {
            //Arrange
            var request = new UpdateTaskRequest(
                    ServiceTestConstants.INVALID_TASK_ID,
                    ServiceTestConstants.REVIEW_EVIDENCE_TITLE,
                    ServiceTestConstants.REVIEW_EVIDENCE_DESCRIPTION,
                    TaskStatus.COMPLETED,
                    LocalDateTime.now().plusDays(10)
            );

            //Given
            given(taskRepository.findById(ServiceTestConstants.INVALID_TASK_ID)).willReturn(Optional.empty());

            //When/Then
            assertThrows(EntityNotFoundException.class, () -> {
                taskService.updateTask(request);
            });

            //Verify
            verify(taskRepository).findById(ServiceTestConstants.INVALID_TASK_ID);
        }
    }

    @Nested
    @DisplayName("Given an existing task is deleted")
    public class DeleteTaskTest {
        @Test
        @DisplayName("Should perform a soft delete of the record when a record is deleted with valid ID")
        void deleteTask_shouldDeleteTask() {
            //Arrange
            Task reviewEvidenceSavedTask = savedtaskList.getFirst();

            // Given
            given(taskRepository.findById(ServiceTestConstants.REVIEW_EVIDENCE_ID)).willReturn(Optional.ofNullable(reviewEvidenceSavedTask));

            // When /Then
            taskService.deleteTask(ServiceTestConstants.REVIEW_EVIDENCE_ID);

            // Verify
            verify(taskRepository, times(1)).findById(ServiceTestConstants.REVIEW_EVIDENCE_ID);
//            verify(taskRepository).save(reviewEvidenceSavedTask);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when a record is not found")
        void deleteTask_shouldThrowExceptionWhenNotFound() {
            //Arrange
            Task reviewEvidenceSavedTask = savedtaskList.getFirst();

            //Given
            given(taskRepository.findById(reviewEvidenceSavedTask.getId())).willReturn(Optional.empty());

            //When/Then
            assertThrows(EntityNotFoundException.class, () -> {
                taskService.deleteTask(ServiceTestConstants.REVIEW_EVIDENCE_ID);
            });

            // Verify
            verify(taskRepository, times(1)).findById(ServiceTestConstants.REVIEW_EVIDENCE_ID);
            verify(taskRepository, never()).save(reviewEvidenceSavedTask);
        }
    }

    private static Task setupTaskEntity(UUID taskId, String title, String description, TaskStatus status){

        return Task.builder()
                .id(taskId)
                .title(title)
                .description(description)
                .status(status)
                .due(ServiceTestConstants.VALID_DUE_DATE)
                .createdAt(LocalDateTime.now()).build();
    }
}