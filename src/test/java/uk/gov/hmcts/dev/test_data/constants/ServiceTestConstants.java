package uk.gov.hmcts.dev.test_data.constants;

import java.time.LocalDateTime;
import java.util.UUID;

public class ServiceTestConstants {
    public static final UUID CREATED_BY_USER_ID = UUID.randomUUID();
    public static final UUID SECOND_CREATED_BY_USER_ID = UUID.randomUUID();
    public static final UUID INVALID_TASK_ID = UUID.randomUUID();
    public static final LocalDateTime VALID_DUE_DATE = LocalDateTime.now().plusDays(20);

    // Constants for review evidence task by a caseworker.
    public static final UUID REVIEW_EVIDENCE_ID = UUID.randomUUID();
    public static final String REVIEW_EVIDENCE_TITLE = "Review Evidence";
    public static final String REVIEW_EVIDENCE_DESCRIPTION = "Analyze digital evidence";
    public static final String REVIEW_EVIDENCE_UPDATED_TITLE = "Review Evidence Bundle";
    public static final String REVIEW_EVIDENCE_UPDATED_DESCRIPTION = "Analyse digital evidence bundle";

    //
    public static final UUID MONTHLY_PROGRESS_REPORT_ID = UUID.randomUUID();
    public static final String MONTHLY_PROGRESS_REPORT_TITLE = "Monthly Progress Report";
    public static final String MONTHLY_PROGRESS_REPORT_DESCRIPTION = "Prepare monthly progress report and submit to supervisor";

    //
    public static final UUID CLIENT_ASSESSMENT_ID = UUID.randomUUID();
    public static final String CLIENT_ASSESSMENT_TITLE = "Client Intake Assessment";
    public static final String CLIENT_ASSESSMENT_DESCRIPTION = "Conduct initial intake assessment for new client and document risk factors";
    
}
