package uk.gov.hmcts.dev.dto;


import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import uk.gov.hmcts.dev.util.validation.UniqueOwnerTitle;
import uk.gov.hmcts.dev.model.TaskStatus;

@Builder
@Schema(description = "Request object for creating a new task")
public record CreateTaskRequest(

        @Schema(
                description = "Unique title for the task (must not exist for this owner)",
                example = "Review Case #12345",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @UniqueOwnerTitle(message = "{error.duplicate.title}")
        @NotEmpty(message = "{title.required}")
        String title,

        @Schema(
                description = "Detailed description of the task requirements",
                example = "Verify all documents are signed and uploaded to the portal.",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotEmpty(message = "{description.required}")
        String description,

        @Schema(
                description = "Current status of the task",
                example = "OPEN",
                defaultValue = "OPEN"
        )
        TaskStatus status,

        @Schema(
                description = "Deadline for task completion",
                example = "2028-12-31T17:00:00",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "{due.date.required}")
        LocalDateTime due
) {
}
