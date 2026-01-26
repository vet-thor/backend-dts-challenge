package uk.gov.hmcts.dev.dto;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseHandler {
    public static <T> ResponseEntity<ResponseData<T>> generateResponse(String message, HttpStatus status, T responseObj) {

        return new ResponseEntity<>(
                ResponseData.<T>builder()
                        .message(message)
                        .status(status)
                        .data(responseObj)
                        .build(),
                status);
    }
}