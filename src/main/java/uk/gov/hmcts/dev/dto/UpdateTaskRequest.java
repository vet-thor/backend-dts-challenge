package uk.gov.hmcts.dev.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import uk.gov.hmcts.dev.model.TaskStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record UpdateTaskRequest(
        @NotNull(message = "{id.required}")
        UUID id,
        String title,
        String description,
        TaskStatus status,
        LocalDateTime due
) {
}
