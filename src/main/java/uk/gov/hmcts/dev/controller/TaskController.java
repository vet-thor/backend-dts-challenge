package uk.gov.hmcts.dev.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import uk.gov.hmcts.dev.dto.*;
import uk.gov.hmcts.dev.model.TaskStatus;
import uk.gov.hmcts.dev.service.TaskService;
import uk.gov.hmcts.dev.util.helper.SuccessMessageHelper;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;
    private final SuccessMessageHelper successMessage;

    @GetMapping
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ResponseData<TaskResponseData>> getTask(
            @RequestParam(name = "title", required = false) String title,
            @RequestParam(name = "description", required = false) String description,
            @RequestParam(name = "status", required = false) TaskStatus status,
            @RequestParam(name = "dueFrom", required = false) LocalDateTime dueFrom,
            @RequestParam(name = "dueTo", required = false) LocalDateTime dueTo,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "limit", defaultValue = "10", required = false) int limit,
            @RequestParam(name = "sortBy", defaultValue = "createdAt", required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = "DESC", required = false) Sort.Direction sortOrder,
            @RequestParam(name = "createdBy", required = false) UUID createdBy
            ){
        var response = taskService.getTask(
                SearchCriteria.builder()
                        .title(title)
                        .description(description)
                        .status(status)
                        .dueFrom(dueFrom)
                        .dueTo(dueTo)
                        .createdBy(createdBy)
                        .page(page)
                        .limit(limit)
                        .sortBy(sortBy)
                        .sortOrder(sortOrder)
                        .build()
        );

        return ResponseHandler.generateResponse(
                successMessage.getTaskSuccessMessage(),
                HttpStatus.OK,
                response
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("@permissionChecker.isOwnersCase(#id) && hasRole('STAFF')")
    public ResponseEntity<ResponseData<TaskResponseData>> getTaskById(
            @PathVariable UUID id){
        var response = taskService.getTask(id);

        return ResponseHandler.generateResponse(
                successMessage.getTaskSuccessMessage(),
                HttpStatus.OK,
                response
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ResponseData<TaskResponseData>> createTask(@RequestBody @Valid CreateTaskRequest request){
        var response = taskService.createTask(request);

        return ResponseHandler.generateResponse(
                successMessage.createTaskSuccessMessage(),
                HttpStatus.CREATED,
                response
        );
    }

    @PutMapping
    @PreAuthorize("@permissionChecker.isOwnersCase(#request.id) && hasRole('STAFF')")
    public ResponseEntity<ResponseData<Object>> updateTask(@RequestBody @Valid UpdateTaskRequest request){

        return ResponseHandler.generateResponse(
                successMessage.updateTaskSuccessMessage(),
                HttpStatus.OK,
                taskService.updateTask(request)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionChecker.isOwnersCase(#id) && hasRole('STAFF')")
    public ResponseEntity<ResponseData<Object>> deleteTask(
            @PathVariable UUID id){
        taskService.deleteTask(id);

        return ResponseHandler.generateResponse(
                successMessage.deleteTaskSuccessMessage(),
                HttpStatus.OK,
                null
        );
    }
}
