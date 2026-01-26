package uk.gov.hmcts.dev.util.helper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class FieldHelper {
    private final MessageUtil messageUtil;

    public String idRequired(){
        return messageUtil.message("id.required");
    }

    public String titleRequired() {
        return messageUtil.message("title.required");
    }

    public String descriptionRequired() {
        return messageUtil.message("description.required");
    }

    public String dueDateRequired() {
        return messageUtil.message("due.date.required");
    }
}
