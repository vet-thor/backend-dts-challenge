package uk.gov.hmcts.dev.test_data.arhument_source;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.ParameterDeclarations;
import uk.gov.hmcts.dev.dto.UpdateTaskRequest;
import uk.gov.hmcts.dev.model.TaskStatus;
import uk.gov.hmcts.dev.test_data.constants.ServiceTestConstants;

import java.time.LocalDateTime;
import java.util.stream.Stream;

public class UpdateStatusArgumentSource implements ArgumentsProvider {
    @Override
    public @NonNull Stream<? extends Arguments> provideArguments(@NonNull ParameterDeclarations declarations, @NonNull ExtensionContext context){

        return Stream.of(
                // Scenario for update title
                Arguments.of(
                        UpdateTaskRequest.builder()
                                .id(ServiceTestConstants.REVIEW_EVIDENCE_ID)
                                .title(ServiceTestConstants.REVIEW_EVIDENCE_UPDATED_TITLE)
                                .build(),
                        ServiceTestConstants.REVIEW_EVIDENCE_UPDATED_TITLE, ServiceTestConstants.REVIEW_EVIDENCE_DESCRIPTION, TaskStatus.OPEN, ServiceTestConstants.VALID_DUE_DATE),

                // Scenario for update description
                Arguments.of(
                        UpdateTaskRequest.builder()
                                .id(ServiceTestConstants.REVIEW_EVIDENCE_ID)
                                .description(ServiceTestConstants.REVIEW_EVIDENCE_UPDATED_DESCRIPTION)
                                .build(),
                        ServiceTestConstants.REVIEW_EVIDENCE_TITLE, ServiceTestConstants.REVIEW_EVIDENCE_UPDATED_DESCRIPTION, TaskStatus.OPEN, ServiceTestConstants.VALID_DUE_DATE),

                // Scenario for update status
                Arguments.of(
                        UpdateTaskRequest.builder()
                                .id(ServiceTestConstants.REVIEW_EVIDENCE_ID)
                                .status(TaskStatus.COMPLETED)
                                .build(),
                        ServiceTestConstants.REVIEW_EVIDENCE_TITLE, ServiceTestConstants.REVIEW_EVIDENCE_DESCRIPTION, TaskStatus.COMPLETED, ServiceTestConstants.VALID_DUE_DATE),

                // Scenario for update due date
                Arguments.of(
                        UpdateTaskRequest.builder()
                                .id(ServiceTestConstants.REVIEW_EVIDENCE_ID)
                                .due(LocalDateTime.now().plusDays(90))
                                .build(),
                        ServiceTestConstants.REVIEW_EVIDENCE_TITLE, ServiceTestConstants.REVIEW_EVIDENCE_DESCRIPTION, TaskStatus.OPEN, LocalDateTime.now().plusDays(90))
        );
    }
}
