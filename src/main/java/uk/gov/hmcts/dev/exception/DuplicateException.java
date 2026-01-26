package uk.gov.hmcts.dev.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class DuplicateException extends RuntimeException {
    private String field;
    private String msg;
}
