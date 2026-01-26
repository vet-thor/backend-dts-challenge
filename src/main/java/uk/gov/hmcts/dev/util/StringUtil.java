package uk.gov.hmcts.dev.util;

import java.util.Objects;
import java.util.UUID;

public class StringUtil {
    public static UUID stringToUUID(String value) {
        if (Objects.nonNull(value)) {
            return UUID.fromString(value);
        }
        return null;
    }
}
