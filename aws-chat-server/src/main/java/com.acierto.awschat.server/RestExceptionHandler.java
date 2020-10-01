package com.acierto.awschat.server;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity<CustomErrorResponse> handleGenericRuntimeException(RuntimeException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        CustomErrorResponse error = new CustomErrorResponse(status, e.getMessage());
        error.setTimestamp(LocalDateTime.now());
        return new ResponseEntity<>(error, status);
    }
}
