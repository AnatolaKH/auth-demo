package io.api.app.demo_app.auth;

public record ErrorResponseData(int httpStatus, String code, String message, Object errorDetails) {
}
