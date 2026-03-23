package uk.gov.hmcts.dev.mapper;

import lombok.NonNull;
import org.springframework.data.domain.Page;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.dev.dto.CreateTaskRequest;
import uk.gov.hmcts.dev.dto.TaskResponse;
import uk.gov.hmcts.dev.dto.UpdateTaskRequest;
import uk.gov.hmcts.dev.model.Task;

import java.util.List;

import static java.util.Objects.nonNull;

@Component
public class TaskMapper {

    public TaskResponse toTaskResponse(@NonNull Task request){

        return TaskResponse.builder()
                .id(request.getId())
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus())
                .due(request.getDue())
                .build();
    }

    public Task toTask(CreateTaskRequest request){

        return Task.builder()
                .title(request.title())
                .description(request.description())
                .status(request.status())
                .due(request.due())
                .build();
    }

    public List<TaskResponse> pageToTasksResponse(Page<Task> page) {
        return page.getContent().stream()
                .map(c -> new TaskResponse(
                        c.getId(),
                        c.getTitle(),
                        c.getDescription(),
                        c.getStatus(),
                        c.getDue())
                ).toList();
    }

    public void applyChangesToTask(UpdateTaskRequest request, Task task) {

        if(nonNull(request.title())){
            task.setTitle(request.title());
        }

        if(nonNull(request.description())){
            task.setDescription(request.description());
        }

        if(nonNull(request.status())){
            task.setStatus(request.status());
        }

        if(nonNull(request.due())){
            task.setDue(request.due());
        }
    }
}
