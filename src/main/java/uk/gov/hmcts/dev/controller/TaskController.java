package uk.gov.hmcts.dev.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
    @Operation(
            summary = "Search and filter tasks",
            description = "Provides paginated task retrieval with advanced filtering options."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Task retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TaskResponseData.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - You do not have the permission to this resource",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    public ResponseEntity<ResponseData<TaskResponseData>> getTask(
            @Parameter(description = "Filter by title", example = "Complete project")
            @RequestParam(name = "title", required = false) String title,

            @Parameter(description = "Filter by description", example = "Finish the Spring Boot project")
            @RequestParam(name = "description", required = false) String description,

            @Parameter(description = "Filter by specific task status")
            @RequestParam(name = "status", required = false) TaskStatus status,

            @Parameter(description = "Start of due date range", schema = @Schema(type = "string", format = "date-time", example = "2023-12-30T00:00:00"))
            @RequestParam(name = "dueFrom", required = false) LocalDateTime dueFrom,

            @Parameter(description = "End of due date range", schema = @Schema(type = "string", format = "date-time", example = "2023-12-31T23:59:59"))
            @RequestParam(name = "dueTo", required = false) LocalDateTime dueTo,

            @Parameter(description = "Page number for pagination")
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,

            @Parameter(description = "Number of records per page")
            @RequestParam(name = "limit", defaultValue = "10", required = false) int limit,

            @Parameter(description = "Field name to sort results by", example = "createdAt")
            @RequestParam(name = "sortBy", defaultValue = "createdAt", required = false) String sortBy,

            @Parameter(description = "Direction of sorting (ASC or DESC)")
            @RequestParam(name = "sortOrder", defaultValue = "DESC", required = false) Sort.Direction sortOrder,

            @Parameter(description = "UUID of the task creator")
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
    @Operation(
            summary = "Get task by ID",
            description = "Retrieves a specific task. Access is restricted to STAFF members who are the owners of the case.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Task retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TaskResponseData.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - User is not the owner or lacks STAFF role",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
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
    @Operation(
            summary = "Create a new task",
            description = "Allows a STAFF member to create a task. The system validates uniqueness of the title for the current owner.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Task created successfully",
                    content = @Content(schema = @Schema(implementation = TaskResponseData.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation Error - Invalid input data or duplicate title",
                    content = @Content(schema = @Schema(example = "{\"message\": \"Title is required\", \"status\": 400}"))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Valid JWT token required",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - You do not have the permission to this resource",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
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
    @Operation(
            summary = "Update an existing task",
            description = "Updates task details. Restricted to the STAFF member who owns the specific task. Performs an update based on the request body.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Task updated successfully",
                    content = @Content(schema = @Schema(implementation = TaskResponseData.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input - check field constraints or ID format",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - You do not have the permission to this resource",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    public ResponseEntity<ResponseData<Object>> updateTask(@RequestBody @Valid UpdateTaskRequest request){

        return ResponseHandler.generateResponse(
                successMessage.updateTaskSuccessMessage(),
                HttpStatus.OK,
                taskService.updateTask(request)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionChecker.isOwnersCase(#id) && hasRole('STAFF')")
    @Operation(
            summary = "Delete a task by ID",
            description = "Permanently removes a task from the system. Restricted to the STAFF member who owns the specific task.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Task deleted successfully",
                    content = @Content(schema = @Schema(implementation = ResponseData.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - You do not have the permission to this resource",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
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
