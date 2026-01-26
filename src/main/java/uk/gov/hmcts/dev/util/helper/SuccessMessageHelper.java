package uk.gov.hmcts.dev.util.helper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public final class SuccessMessageHelper {
    private final MessageUtil messageUtil;

    public String loginSuccessMessage(){
        return messageUtil.message("success.user.login");
    }

    public String getTaskSuccessMessage(){
        return messageUtil.message("success.task.retrieved");
    }

    public String createTaskSuccessMessage() {
        return messageUtil.message("success.task.created");
    }

    public String updateTaskSuccessMessage(){
        return messageUtil.message("success.task.updated");
    }

    public String deleteTaskSuccessMessage(){
        return messageUtil.message("success.task.deleted");
    }

}
