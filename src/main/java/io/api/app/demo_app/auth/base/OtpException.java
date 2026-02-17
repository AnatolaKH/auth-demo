package io.api.app.demo_app.auth.base;

import io.api.app.demo_app.auth.ErrorResponse;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class OtpException extends RuntimeException {
    private final ErrorResponse errorResponse;
}
