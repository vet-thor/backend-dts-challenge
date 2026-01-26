package uk.gov.hmcts.dev.config;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import uk.gov.hmcts.dev.dto.ResponseData;
import uk.gov.hmcts.dev.dto.ResponseError;
import uk.gov.hmcts.dev.dto.ResponseHandler;
import uk.gov.hmcts.dev.util.helper.ErrorMessageHelper;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ExceptionHandlerConfig {
    private final ErrorMessageHelper errorMessage;

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ResponseData<ResponseError<String>>> handleBadCredentialExceptionHandler(BadCredentialsException e){
        return ResponseHandler.generateResponse(
                errorMessage.generalErrorMessage(),
                HttpStatus.UNAUTHORIZED,
                ResponseError.<String>builder()
                        .error(errorMessage.failedAuthenticationErrorMessage())
                        .build()
        );
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ResponseData<ResponseError<String>>> handleExpiredJwtTokenExceptionHandler(ExpiredJwtException e){
        return ResponseHandler.generateResponse(
                errorMessage.generalErrorMessage(),
                HttpStatus.UNAUTHORIZED,
                ResponseError.<String>builder()
                        .error(e.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ResponseData<ResponseError<String>>> handleAuthorizationDeniedException(AuthorizationDeniedException e){
        return ResponseHandler.generateResponse(
                errorMessage.generalErrorMessage(),
                HttpStatus.FORBIDDEN,
                ResponseError.<String>builder()
                        .error(errorMessage.unauthorizedErrorMessage())
                        .build()
        );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ResponseData<ResponseError<String>>> handleEntityNotFoundExceptionHandler(EntityNotFoundException e){
        return ResponseHandler.generateResponse(
                errorMessage.fieldValidationFailedErrorMessage(),
                HttpStatus.NOT_FOUND,
                ResponseError.<String>builder()
                        .error(e.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ResponseData<ResponseError<String>>> handleEntityNotFoundExceptionHandler(UsernameNotFoundException e){
        return ResponseHandler.generateResponse(
                errorMessage.fieldValidationFailedErrorMessage(),
                HttpStatus.UNAUTHORIZED,
                ResponseError.<String>builder()
                        .error(e.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseData<ResponseError<Map<String, String>>>> handleArgumentNotValidExceptionHandler(MethodArgumentNotValidException e){
        var errors = new HashMap<String, String >();
        e.getBindingResult().getAllErrors().forEach(error -> {
            var fieldName = ((FieldError)error).getField();
            var errorMessage = error.getDefaultMessage();

            errors.put(fieldName, errorMessage);
        });

        return ResponseHandler.generateResponse(
                errorMessage.fieldValidationFailedErrorMessage(),
                HttpStatus.BAD_REQUEST,
                ResponseError.<Map<String, String>>builder()
                        .errors(errors)
                        .build()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseData<ResponseError<String>>> handleUnexpectedException(Exception e){
        log.error(errorMessage.generalErrorMessage(), e);
        return ResponseHandler.generateResponse(
                errorMessage.generalErrorMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                ResponseError.<String>builder()
                        .error(errorMessage.unexpectedErrorMessage())
                        .build()
        );
    }
}


