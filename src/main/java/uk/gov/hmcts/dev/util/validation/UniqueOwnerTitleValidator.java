package uk.gov.hmcts.dev.util.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.dev.dto.JwtUserDetails;
import uk.gov.hmcts.dev.repository.TaskRepository;
import uk.gov.hmcts.dev.util.SecurityUtils;

@Component
@RequiredArgsConstructor
public class UniqueOwnerTitleValidator implements ConstraintValidator<UniqueOwnerTitle, String> {

    private final TaskRepository taskRepository;

    @Override
    public boolean isValid(String  title, ConstraintValidatorContext context) {
        if (title == null) {
            return true;
        }

        var ownerId = SecurityUtils.getPrincipal()
                .map(JwtUserDetails::getId)
                .orElse(null);

        return !taskRepository.existsByTitleAndCreatedBy(title, ownerId);
    }
}
