package io.api.app.demo_app.auth;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация JWT токенов
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    private String secret;
    private AccessToken accessToken = new AccessToken();
    private RefreshToken refreshToken = new RefreshToken();

    @Data
    public static class AccessToken {
        private long expiration = 900000; // 15 минут по умолчанию
    }

    @Data
    public static class RefreshToken {
        private long expiration = 604800000; // 7 дней по умолчанию
    }
}
