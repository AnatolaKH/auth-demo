package io.api.app.demo_app.auth.base;

public interface OtpErrorMapper {
    OtpException userNotFound();
    OtpException noOtpFound();
    OtpException otpExpired();
    OtpException maxAttemptsReached();
    OtpException invalidOTP();
}
