package io.api.app.demo_app.auth;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация OTP
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "otp")
public class OtpConfig {

    private long expiration = 300000; // 5 минут по умолчанию
    private int length = 6; // Длина OTP кода
    private int maxAttempts = 5; // Максимальное количество попыток
    private long resendCooldown = 60000; // Время до повторной отправки (1 минута)
}
