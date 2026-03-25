package uk.gov.hmcts.dev.config;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.hmcts.dev.util.helper.ErrorMessageHelper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExceptionHandlerConfigTest {
    @Mock
    private ErrorMessageHelper errorMessage;
    @InjectMocks
    private ExceptionHandlerConfig handler;

    private static final String VALIDATION_FAILED = "Validation failed";
    private static final String AUTHENTICATION_FAILED = "Authentication failed";
    private static final String UNAUTHORISED = "You are not authorized";
    private static final String JWT_EXPIRED = "JWT expired";
    private static final String FIELD_VALIDATION_FAILED = "Field validation failed";
    private static final String FIELD_USERNAME = "username";
    private static final String MUST_NOT_BE_BLANK = "must not be blank";
    private static final String GROUPED_RESPONSE_ASSERTIONS = "Grouped Response Assertions";
    private static final String RESPONSE_SHOULD_NOT_BE_NULL = "Response body should not be null";
    private static final String UNEXPECTED_ERROR_OCCURRED = "Unexpected error occurred";
    private static final String INTERNAL_ERROR_OCCURRED = "An internal error occurred";

    @Test
    void handleBadCredentialExceptionHandler() {
        // Arrange
        var exception = mock(BadCredentialsException.class);

        // Given
        given(errorMessage.generalErrorMessage()).willReturn(VALIDATION_FAILED);
        given(errorMessage.failedAuthenticationErrorMessage()).willReturn(AUTHENTICATION_FAILED);

        // When
        var response = handler.handleBadCredentialExceptionHandler(exception);

        // Then
        assertNotNull(response.getBody());
        assertAll(
                () -> assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode()),
                () -> assertEquals(VALIDATION_FAILED, response.getBody().getMessage()),
                () -> assertEquals(AUTHENTICATION_FAILED, response.getBody().getData().getError())
        );
    }

    @Test
    void handleExpiredJwtTokenExceptionHandler() {
        // Arrange
        var exception = mock(ExpiredJwtException.class);

        // Given
        given(exception.getMessage()).willReturn(JWT_EXPIRED);
        given(errorMessage.generalErrorMessage()).willReturn(VALIDATION_FAILED);

        // When
        var response = handler.handleExpiredJwtTokenExceptionHandler(exception);

        // Then
        assertNotNull(response.getBody());
        assertAll(
                () -> assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode()),
                () -> assertEquals(VALIDATION_FAILED, response.getBody().getMessage()),
                () -> assertEquals(JWT_EXPIRED, response.getBody().getData().getError())
        );
    }

    @Test
    void handleAuthorizationDeniedException() {
        var exception = mock(AuthorizationDeniedException.class);

        // Given
        given(errorMessage.generalErrorMessage()).willReturn(VALIDATION_FAILED);
        given(errorMessage.unauthorizedErrorMessage()).willReturn(UNAUTHORISED);

        // When
        var response = handler.handleAuthorizationDeniedException(exception);

        // Then
        assertNotNull(response.getBody());
        assertAll(
                () -> assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode()),
                () -> assertEquals(VALIDATION_FAILED, response.getBody().getMessage()),
                () -> assertEquals(UNAUTHORISED, response.getBody().getData().getError())
        );
    }

    @Test
    void handleEntityNotFoundExceptionHandler() {
        var exception = mock(UsernameNotFoundException.class);

        // Given
        given(errorMessage.fieldValidationFailedErrorMessage()).willReturn(FIELD_VALIDATION_FAILED);

        // When
        var response = handler.handleEntityNotFoundExceptionHandler(exception);

        // Then
        assertNotNull(response.getBody(), RESPONSE_SHOULD_NOT_BE_NULL);
        assertAll(GROUPED_RESPONSE_ASSERTIONS,
                () -> assertNotNull(response.getBody()),
                () -> assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode()),
                () -> assertEquals(FIELD_VALIDATION_FAILED, response.getBody().getMessage())
        );

        // Verify
        then(errorMessage).should().fieldValidationFailedErrorMessage();
    }

    @Test
    void handleArgumentNotValidExceptionHandler() {
        var exception = mock(RuntimeException.class);

        // Given
        given(errorMessage.generalErrorMessage()).willReturn(UNEXPECTED_ERROR_OCCURRED);
        given(errorMessage.unexpectedErrorMessage()).willReturn(INTERNAL_ERROR_OCCURRED);

        // When
        var response = handler.handleUnexpectedException(exception);

        // Then
        assertNotNull(response.getBody(), RESPONSE_SHOULD_NOT_BE_NULL);
        assertAll(GROUPED_RESPONSE_ASSERTIONS,
                () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode()),
                () -> assertEquals(UNEXPECTED_ERROR_OCCURRED, response.getBody().getMessage()),
                () -> assertEquals(INTERNAL_ERROR_OCCURRED, response.getBody().getData().getError())
        );

        // Verify
        then(errorMessage).should(times(2)).generalErrorMessage();
        then(errorMessage).should().unexpectedErrorMessage();
    }

    @Test
    void handleUnexpectedException() {

        var target = new Object();
        var bindingResult = new BeanPropertyBindingResult(target, "object");
        bindingResult.addError(new FieldError("object", FIELD_USERNAME, MUST_NOT_BE_BLANK));
        var exception = new MethodArgumentNotValidException(null, bindingResult);

        when(errorMessage.fieldValidationFailedErrorMessage()).thenReturn(VALIDATION_FAILED);

        var response = handler.handleArgumentNotValidExceptionHandler(exception);

        // Then
        assertNotNull(response.getBody(), RESPONSE_SHOULD_NOT_BE_NULL);
        assertAll(GROUPED_RESPONSE_ASSERTIONS,
                () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()),
                () -> assertEquals(VALIDATION_FAILED, response.getBody().getMessage()),
                () -> assertEquals(MUST_NOT_BE_BLANK, response.getBody().getData().getErrors().get(FIELD_USERNAME))
        );

        // Verify
        then(errorMessage).should().fieldValidationFailedErrorMessage();
    }
}