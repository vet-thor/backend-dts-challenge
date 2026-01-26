package uk.gov.hmcts.dev.util.helper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public final class ErrorMessageHelper {
    private final MessageUtil messageUtil;

    public String duplicateTitleErrorMessage(){
        return messageUtil.message("error.duplicate.title");
    }

    public String duplicateEntityErrorMessage(){
        return messageUtil.message("error.duplicate.entity");
    }

    public String caseNotFoundErrorMessage() {
        return messageUtil.message("error.case.not.found");
    }

    public String fieldValidationFailedErrorMessage(){
        return messageUtil.message("error.field.validation.failed");
    }

    public String failedAuthenticationErrorMessage() {
        return messageUtil.message("error.failed.authentication");
    }

    public String unauthorizedErrorMessage(){
        return messageUtil.message("error.authorization.denied");
    }

    public String generalErrorMessage(){
        return messageUtil.message("error.general.issue");
    }

    public String unexpectedErrorMessage(){
        return messageUtil.message("error.unexpected");
    }

}
