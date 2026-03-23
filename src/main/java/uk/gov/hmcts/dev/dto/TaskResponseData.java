package uk.gov.hmcts.dev.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskResponseData {
    private TaskResponse task;
    private List<TaskResponse> tasks;
    private long totalElement;
    private int totalPage;
}
