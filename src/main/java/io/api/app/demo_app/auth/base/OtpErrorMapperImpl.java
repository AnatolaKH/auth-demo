package io.api.app.demo_app.auth.base;

import io.api.app.demo_app.auth.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OtpErrorMapperImpl implements OtpErrorMapper {

    @Override
    public OtpException userNotFound() {
        return otpException("V3OTP1", "User not found");
    }

    @Override
    public OtpException noOtpFound() {
        return otpException("V3OTP2", "No OTP found for this user");
    }

    @Override
    public OtpException otpExpired() {
        return otpException("V3OTP3", "OTP has expired");
    }

    @Override
    public OtpException maxAttemptsReached() {
        return otpException("V3OTP4", "Max attempts reached");
    }

    @Override
    public OtpException invalidOTP() {
        return otpException("V3OTP3", "Invalid OTP");
    }

    private OtpException otpException(String code, String error) {
        return new OtpException(new ErrorResponse(code, error));
    }
}
