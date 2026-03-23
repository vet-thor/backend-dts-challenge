package uk.gov.hmcts.dev.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import uk.gov.hmcts.dev.model.TaskStatus;
import java.util.UUID;

@Builder
public record TaskResponse(
        UUID id,
        String title,
        String description,
        TaskStatus status,
        LocalDateTime due
) {
}
