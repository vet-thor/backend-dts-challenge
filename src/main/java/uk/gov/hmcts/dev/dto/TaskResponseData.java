package uk.gov.hmcts.dev.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Setter
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskResponseData {
    private CaseResponse task;
    private List<CaseResponse> tasks;
    private long totalElement;
    private int totalPage;
}
