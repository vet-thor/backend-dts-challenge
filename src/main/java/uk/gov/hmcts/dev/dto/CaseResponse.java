package uk.gov.hmcts.dev.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.dev.model.TaskStatus;
import java.util.UUID;

@Builder
public record CaseResponse(
        UUID id,
        String title,
        String description,
        TaskStatus status,
        LocalDateTime due
) {
}
