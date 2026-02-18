package io.api.app.user_service.controller;

import io.api.app.user_service.dto.UserProfileResponse;
import io.api.app.user_service.model.UserProfile;
import io.api.app.user_service.model.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserProfileRepository userProfileRepository;

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        String userIdRaw = (String) authentication.getPrincipal();
        UUID userId = UUID.fromString(userIdRaw);

        return userProfileRepository.findById(userId)
                .<ResponseEntity<?>>map(user -> ResponseEntity.ok(toResponse(user)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private UserProfileResponse toResponse(UserProfile user) {
        return new UserProfileResponse(user.getId(), user.getEmail(), user.getName());
    }
}
