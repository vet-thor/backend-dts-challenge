package uk.gov.hmcts.dev.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.dev.dto.JwtUserDetails;
import uk.gov.hmcts.dev.repository.TaskRepository;
import uk.gov.hmcts.dev.util.SecurityUtils;

import java.util.UUID;

@Component("permissionChecker")
@RequiredArgsConstructor
public class PermissionChecker {
    private final TaskRepository taskRepository;

    public boolean isOwnersCase(UUID caseId){
        var ownerId = SecurityUtils.getPrincipal()
                .map(JwtUserDetails::getId)
                .orElse(null);

        return taskRepository.existsByIdAndCreatedBy(caseId, ownerId);
    }
}
