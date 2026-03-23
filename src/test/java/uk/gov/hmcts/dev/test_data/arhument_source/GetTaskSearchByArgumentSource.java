package uk.gov.hmcts.dev.test_data.arhument_source;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.ParameterDeclarations;

import java.util.Map;
import java.util.stream.Stream;

import static uk.gov.hmcts.dev.test_data.constants.ServiceTestConstants.*;

public class GetTaskSearchByArgumentSource implements ArgumentsProvider {
    @Override
    public @NonNull Stream<? extends Arguments> provideArguments(@NonNull ParameterDeclarations declarations, @NonNull ExtensionContext context){

        return Stream.of(
                // Scenario for update title
                Arguments.of(
                        Map.of(
                                "title", CLIENT_ASSESSMENT_TITLE
                        ),
                        1,
                        CLIENT_ASSESSMENT_TITLE
                ),

                Arguments.of(
                        Map.of(
                                "createdBy", CREATED_BY_USER_ID.toString(),
                                "sortBy", "title",
                                "sortOrder", "ASC"
                        ),
                        2,
                        CLIENT_ASSESSMENT_TITLE
                ),

                Arguments.of(
                        Map.of(
                                "dueFrom", VALID_DUE_DATE.toString(),
                                "sortBy", "title",
                                "sortOrder", "ASC"
                        ),
                        2,
                        CLIENT_ASSESSMENT_TITLE
                ),

                Arguments.of(
                        Map.of(
                                "page", "0",
                                "limit", "1",
                                "sortBy", "title",
                                "sortOrder", "ASC"
                        ),
                        1,
                        CLIENT_ASSESSMENT_TITLE
                )

        );
    }
}
