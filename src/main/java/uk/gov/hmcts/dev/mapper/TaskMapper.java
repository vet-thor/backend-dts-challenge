package uk.gov.hmcts.dev.mapper;

import lombok.NonNull;
import org.springframework.data.domain.Page;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.dev.dto.CaseRequest;
import uk.gov.hmcts.dev.dto.CaseResponse;
import uk.gov.hmcts.dev.model.Task;

import java.util.List;

@Component
public class TaskMapper {

    public CaseResponse toTaskResponse(@NonNull Task request){

        return CaseResponse.builder()
                .id(request.getId())
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus())
                .due(request.getDue())
                .build();
    }

    public Task toTask(CaseRequest request){

        return Task.builder()
                .title(request.title())
                .description(request.description())
                .status(request.status())
                .due(request.due())
                .build();
    }

    public List<CaseResponse> pageToTasksResponse(Page<Task> page) {
        return page.getContent().stream()
                .map(c -> new CaseResponse(
                        c.getId(),
                        c.getTitle(),
                        c.getDescription(),
                        c.getStatus(),
                        c.getDue())
                ).toList();
    }
}
