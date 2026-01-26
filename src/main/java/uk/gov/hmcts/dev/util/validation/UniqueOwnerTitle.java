package uk.gov.hmcts.dev.util.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Constraint(validatedBy = UniqueOwnerTitleValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueOwnerTitle {
    String message() default "UniqueOwnerTitle.message";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
