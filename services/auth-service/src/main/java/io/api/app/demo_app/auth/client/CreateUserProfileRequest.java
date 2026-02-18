package io.api.app.demo_app.auth.client;

import java.util.UUID;

public record CreateUserProfileRequest(UUID id, String email, String name) {
}
