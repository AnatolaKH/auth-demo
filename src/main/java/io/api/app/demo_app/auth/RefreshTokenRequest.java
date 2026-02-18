package io.api.app.demo_app.auth;

import lombok.Data;

@Data
public class RefreshTokenRequest {
    private String refreshToken;
}
