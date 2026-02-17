package io.api.app.demo_app.auth.base;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class OtpExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({OtpException.class})
    ResponseEntity<Object> otpException(OtpException exception) {
        return ResponseEntity.badRequest().body(exception.getErrorResponse());
    }
}
