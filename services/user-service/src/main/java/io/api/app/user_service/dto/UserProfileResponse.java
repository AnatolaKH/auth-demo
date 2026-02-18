package io.api.app.user_service.dto;

import java.util.UUID;

public record UserProfileResponse(UUID id, String email, String name) {
}
