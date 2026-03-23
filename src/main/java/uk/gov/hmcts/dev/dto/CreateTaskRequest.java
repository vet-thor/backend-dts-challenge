package uk.gov.hmcts.dev.dto;


import java.time.LocalDateTime;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import uk.gov.hmcts.dev.util.validation.UniqueOwnerTitle;
import uk.gov.hmcts.dev.model.TaskStatus;

@Builder
public record CreateTaskRequest(
        @UniqueOwnerTitle(message = "{error.duplicate.title}")
        @NotEmpty(message = "{title.required}")
        String title,
        @NotEmpty(message = "{description.required}")
        String description,
        TaskStatus status,
        @NotNull(message = "{due.date.required}")
        LocalDateTime due
) {
}
