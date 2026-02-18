package io.api.app.demo_app.auth;

import io.api.app.demo_app.User;
import io.api.app.demo_app.auth.base.OtpErrorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
public class OtpService {

    private final OtpRepository otpRepository;
    private final OtpConfig otpConfig;
    private final OtpErrorMapper otpErrorMapper;

    @Transactional
    public String generateOtp(User user) {
        String code = generateCode();
        Otp otp = otpRepository.findByUserId(user.getId()).orElse(new Otp());
        otp.setUser(user);
        otp.setCode(code);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(otpConfig.getExpiration() / 60000));
        otp.setAttempts(0);
        otpRepository.save(otp);
        return code;
    }

    public boolean validateOtp(User user, String code) {
        Otp otp = otpRepository.findByUserId(user.getId())
                .orElseThrow(otpErrorMapper::noOtpFound);

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw otpErrorMapper.otpExpired();
        }

        if (otp.getAttempts() >= otpConfig.getMaxAttempts()) {
            throw otpErrorMapper.maxAttemptsReached();
        }

        if (!otp.getCode().equals(code)) {
            otp.setAttempts(otp.getAttempts() + 1);
            otpRepository.save(otp);
            throw otpErrorMapper.invalidOTP();
        }

        otpRepository.delete(otp);
        return true;
    }

    private String generateCode() {
        Random random = new Random();
        int otpLength = otpConfig.getLength();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
}
