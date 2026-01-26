package uk.gov.hmcts.dev.config;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.hmcts.dev.util.helper.ErrorMessageHelper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class ExceptionHandlerConfigTest {
    @Mock
    private ErrorMessageHelper errorMessage;
    @InjectMocks
    private ExceptionHandlerConfig handler;

    @Test
    void handleBadCredentialExceptionHandler() {
        // Arrange
        var exception = new BadCredentialsException("Invalid credentials");
        when(errorMessage.generalErrorMessage()).thenReturn("Validation failed");
        when(errorMessage.failedAuthenticationErrorMessage()).thenReturn("Authentication failed");

        // Act
        var response = handler.handleBadCredentialExceptionHandler(exception);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Validation failed", response.getBody().getMessage());
        assertEquals("Authentication failed", response.getBody().getData().getError());
    }

    @Test
    void handleExpiredJwtTokenExceptionHandler() {
        var exception = mock(ExpiredJwtException.class);
        when(exception.getMessage()).thenReturn("JWT expired");
        when(errorMessage.generalErrorMessage()).thenReturn("Validation failed");

        var response = handler.handleExpiredJwtTokenExceptionHandler(exception);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Validation failed", response.getBody().getMessage());
        assertEquals("JWT expired", response.getBody().getData().getError());
    }

    @Test
    void handleAuthorizationDeniedException() {
        var exception = new AuthorizationDeniedException("Access denied");
        when(errorMessage.generalErrorMessage()).thenReturn("Validation failed");
        when(errorMessage.unauthorizedErrorMessage()).thenReturn("You are not authorized");

        var response = handler.handleAuthorizationDeniedException(exception);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Validation failed", response.getBody().getMessage());
        assertEquals("You are not authorized", response.getBody().getData().getError());
    }

    @Test
    void handleEntityNotFoundExceptionHandler() {
        var exception = new EntityNotFoundException("Entity not found");
        when(errorMessage.fieldValidationFailedErrorMessage()).thenReturn("Field validation failed");

        var response = handler.handleEntityNotFoundExceptionHandler(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Field validation failed", response.getBody().getMessage());
        assertEquals("Entity not found", response.getBody().getData().getError());
    }

    @Test
    void testHandleEntityNotFoundExceptionHandler() {
        var exception = new UsernameNotFoundException("User not found");
        when(errorMessage.fieldValidationFailedErrorMessage()).thenReturn("Field validation failed");

        var response = handler.handleEntityNotFoundExceptionHandler(exception);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Field validation failed", response.getBody().getMessage());
        assertEquals("User not found", response.getBody().getData().getError());
    }

    @Test
    void handleArgumentNotValidExceptionHandler() {
        var exception = new RuntimeException("Something went wrong");
        when(errorMessage.generalErrorMessage()).thenReturn("Unexpected error occurred");
        when(errorMessage.unexpectedErrorMessage()).thenReturn("Internal server error");

        var response = handler.handleUnexpectedException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected error occurred", response.getBody().getMessage());
        assertEquals("Internal server error", response.getBody().getData().getError());
    }

    @Test
    void handleUnexpectedException() {
        var fieldError = new FieldError("object", "username", "must not be blank");
        var bindingResult = mock(BindingResult.class);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        var exception = new MethodArgumentNotValidException(null, bindingResult);
        when(errorMessage.fieldValidationFailedErrorMessage()).thenReturn("Validation failed");

        var response = handler.handleArgumentNotValidExceptionHandler(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation failed", response.getBody().getMessage());
        assertEquals("must not be blank", response.getBody().getData().getErrors().get("username"));
    }
}