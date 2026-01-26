package uk.gov.hmcts.dev.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import uk.gov.hmcts.dev.dto.CaseRequest;
import uk.gov.hmcts.dev.dto.CaseResponse;
import uk.gov.hmcts.dev.dto.SearchCriteria;
import jakarta.persistence.EntityNotFoundException;
import uk.gov.hmcts.dev.mapper.TaskMapper;
import uk.gov.hmcts.dev.model.Task;
import uk.gov.hmcts.dev.model.TaskStatus;
import uk.gov.hmcts.dev.repository.TaskRepository;
import uk.gov.hmcts.dev.util.helper.ErrorMessageHelper;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.nonNull;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private ErrorMessageHelper errorMessageHelper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TaskService taskService;

    private Task task1, task2, task3;

    @BeforeEach
    void setup(){
        task1 = CaseTestFactory.createTask(UUID.randomUUID(), "Task 1", "A new description 1", TaskStatus.OPEN);
        task2 = CaseTestFactory.createTask(UUID.randomUUID(), "Task 2", "A new description 2", TaskStatus.OPEN);
        task3 = CaseTestFactory.createTask(UUID.randomUUID(), "Task 3", "A new description 3", TaskStatus.OPEN);
    }

    @Test
    void createTask_shouldSaveTask() {
        var dto = new CaseRequest(null, task1.getTitle(), task1.getDescription(), task1.getStatus(), task1.getDue());
        var outputCase = new CaseResponse(task1.getId(), task1.getTitle(), task1.getDescription(), task1.getStatus(), task1.getDue());

//      // Given
        when(taskRepository.save(any())).thenReturn(any());
        when(taskMapper.toTaskResponse(task1)).thenReturn(outputCase);

        // When
        var result = taskService.createTask(dto);

        // Then
        assertTrue(nonNull(result.getTask()));
        assertEquals("Task 1", result.getTask().title());
        assertEquals("A new description 1", result.getTask().description());
        assertEquals(TaskStatus.OPEN, result.getTask().status());
    }

    @Test
    void getTaskById_shouldReturnTask() {
        var outputCase = new CaseResponse(task1.getId(), task1.getTitle(), task1.getDescription(), task1.getStatus(), task1.getDue());
        // Given
        when(taskRepository.findById(task1.getId())).thenReturn(Optional.of(task1));
        when(taskMapper.toTaskResponse(task1)).thenReturn(outputCase);

        // When
        var result = taskService.getTask(task1.getId());

        // Then
        assertTrue(nonNull(result.getTask()));
        assertEquals(task1.getId(), result.getTask().id());
        assertEquals("Task 1", result.getTask().title());
    }

    @Test
    void getTaskById_shouldThrowExceptionWhenNotFound() {
        // Given
        when(taskRepository.findById(task1.getId())).thenReturn(Optional.empty());

        // When/Then
        assertThrows(EntityNotFoundException.class, () -> {
            taskService.getTask(task1.getId());
        });
    }

    @Test
    void getAllTasks_shouldReturnAllCases() {
        // Given
        Page<Task> mockPage = new PageImpl<>(List.of(task1, task2, task3));
        var outputCase = new CaseResponse(task1.getId(), task1.getTitle(), task1.getDescription(), task1.getStatus(), task1.getDue());
        var outputCase2 = new CaseResponse(task2.getId(), task2.getTitle(), task2.getDescription(), task2.getStatus(), task2.getDue());
        var outputCase3 = new CaseResponse(task3.getId(), task3.getTitle(), task3.getDescription(), task3.getStatus(), task3.getDue());

        when(taskRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(mockPage);
        when(taskMapper.pageToTasksResponse(mockPage)).thenReturn(List.of(outputCase, outputCase2, outputCase3));

        // When
        var result = taskService.getTask(SearchCriteria.builder().page(0).limit(10).sortBy("createdAt").sortOrder(Sort.Direction.DESC).build());

        // Then
        assertEquals(3, result.getTasks().size());
        assertEquals("Task 1", result.getTasks().get(0).title());
        assertEquals("Task 2", result.getTasks().get(1).title());
        assertEquals("Task 3", result.getTasks().get(2).title());
    }

    @Test
    void updateTaskStatus_shouldUpdateStatus() {
        // Given
        var request = new CaseRequest(task1.getId(), task1.getTitle(), null, TaskStatus.COMPLETED, task1.getDue());
        var outputCase = new CaseResponse(task1.getId(), task1.getTitle(), task1.getDescription(), request.status(), task1.getDue());
        var updatedCase = CaseTestFactory.createTask(task1.getId(), task1.getTitle(), task1.getDescription(), TaskStatus.COMPLETED);
        updatedCase.setUpdatedAt(LocalDateTime.now());

        when(taskRepository.findById(task1.getId())).thenReturn(Optional.of(task1));
        when(taskRepository.save(any())).thenReturn(any());
        when(taskMapper.toTaskResponse(updatedCase)).thenReturn(outputCase);

        // When
        var result = taskService.updateTask(request);

        // Then
        assertTrue(nonNull(result.getTask()));
        assertEquals(TaskStatus.COMPLETED, result.getTask().status());
        verify(taskRepository).save(task1);
    }

    @Test
    void updateTaskStatus_shouldThrowExceptionWhenNotFound(){
        //Given
        var request = new CaseRequest(task1.getId(), task1.getTitle(), null, TaskStatus.COMPLETED, task1.getDue());

        when(taskRepository.findById(task1.getId())).thenReturn(Optional.empty());

        //When/Then
        assertThrows(EntityNotFoundException.class, () -> {
            taskService.updateTask(request);
        });
    }

    @Test
    void deleteTask_shouldDeleteTask() {
        // Given

        when(taskRepository.findById(task1.getId())).thenReturn(Optional.ofNullable(task1));

        // When
        taskService.deleteTask(task1.getId());

        // Then
        verify(taskRepository).save(task1);
    }

    @Test
    void deleteTask_shouldThrowExceptionWhenNotFound(){
        //Given
        when(taskRepository.findById(task1.getId())).thenReturn(Optional.empty());

        //When/Then
        assertThrows(EntityNotFoundException.class, () -> {
            taskService.deleteTask(task1.getId());
        });
    }
}

class CaseTestFactory{
    public static Task createTask(UUID taskId, String title, String description, TaskStatus status){
        var task = new Task(title, description, status, LocalDateTime.of(2025, 12, 6, 6, 6));
        task.setId(taskId);
        task.setCreatedAt(LocalDateTime.now());

        return task;
    }
}