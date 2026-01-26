package uk.gov.hmcts.dev.dto;


import java.time.LocalDateTime;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import uk.gov.hmcts.dev.util.validation.UniqueOwnerTitle;
import uk.gov.hmcts.dev.util.validation.group.ValidateCreateGroup;
import uk.gov.hmcts.dev.util.validation.group.ValidateUpdateGroup;
import uk.gov.hmcts.dev.model.TaskStatus;
import java.util.UUID;

@Builder
public record CaseRequest(
        @NotNull(groups = {ValidateUpdateGroup.class}, message = "{id.required}")
        UUID id,
        @UniqueOwnerTitle(message = "{error.duplicate.title}", groups = {ValidateCreateGroup.class})
        @NotEmpty(message = "{title.required}", groups = {ValidateCreateGroup.class})
        String title,
        @NotEmpty(message = "{description.required}", groups = {ValidateCreateGroup.class})
        String description,
        TaskStatus status,
        @NotNull(message = "{due.date.required}", groups = {ValidateCreateGroup.class})
        LocalDateTime due
) {
}
