package uk.gov.hmcts.dev.test_data;

import uk.gov.hmcts.dev.dto.TaskResponse;
import uk.gov.hmcts.dev.dto.CreateTaskRequest;
import uk.gov.hmcts.dev.model.TaskStatus;
import uk.gov.hmcts.dev.test_data.constants.ServiceTestConstants;

import java.util.List;

import static uk.gov.hmcts.dev.test_data.constants.ServiceTestConstants.*;

public class CaseTestData {

    public static CreateTaskRequest reviewEvidenceMockCreateRequestPayload(){
        return CreateTaskRequest.builder()
                .title(REVIEW_EVIDENCE_TITLE)
                .description(REVIEW_EVIDENCE_DESCRIPTION)
                .due(VALID_DUE_DATE)
                .status(TaskStatus.OPEN)
                .build();
    }

    public static CreateTaskRequest monthlyProgressReportMockCreateRequestPayload(){
        return CreateTaskRequest.builder()
                .title(MONTHLY_PROGRESS_REPORT_TITLE)
                .description(MONTHLY_PROGRESS_REPORT_DESCRIPTION)
                .due(VALID_DUE_DATE)
                .status(TaskStatus.OPEN)
                .build();
    }

    public static List<TaskResponse> listOfExpectedResponseMockData(){
        return List.of(
                new TaskResponse(
                        REVIEW_EVIDENCE_ID,
                        REVIEW_EVIDENCE_TITLE,
                        REVIEW_EVIDENCE_DESCRIPTION,
                        TaskStatus.IN_PROGRESS,
                        VALID_DUE_DATE
                ),
                new TaskResponse(
                        CLIENT_ASSESSMENT_ID,
                        CLIENT_ASSESSMENT_TITLE,
                        CLIENT_ASSESSMENT_DESCRIPTION,
                        TaskStatus.IN_PROGRESS,
                        VALID_DUE_DATE
                ),
                new TaskResponse(
                        ServiceTestConstants.MONTHLY_PROGRESS_REPORT_ID,
                        ServiceTestConstants.MONTHLY_PROGRESS_REPORT_TITLE,
                        ServiceTestConstants.MONTHLY_PROGRESS_REPORT_DESCRIPTION,
                        TaskStatus.IN_PROGRESS,
                        VALID_DUE_DATE
                )
        );
    }
}
