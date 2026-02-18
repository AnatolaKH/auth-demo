package io.api.app.user_service.controller;

import io.api.app.user_service.dto.CreateUserRequest;
import io.api.app.user_service.dto.UserProfileResponse;
import io.api.app.user_service.model.UserProfile;
import io.api.app.user_service.model.UserProfileRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserProfileRepository userProfileRepository;

    @Value("${internal.api-key}")
    private String internalApiKey;

    @PostMapping
    public ResponseEntity<?> createUser(@RequestHeader(value = "X-Internal-Api-Key", required = false) String apiKey,
                                        @Valid @RequestBody CreateUserRequest request) {
        if (apiKey == null || !apiKey.equals(internalApiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized internal call");
        }

        UserProfile existing = userProfileRepository.findById(request.id()).orElse(null);
        if (existing != null) {
            return ResponseEntity.ok(new UserProfileResponse(existing.getId(), existing.getEmail(), existing.getName()));
        }

        if (userProfileRepository.findByEmail(request.email()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
        }

        UserProfile profile = new UserProfile();
        profile.setId(request.id());
        profile.setEmail(request.email());
        profile.setName(request.name());

        UserProfile saved = userProfileRepository.save(profile);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new UserProfileResponse(saved.getId(), saved.getEmail(), saved.getName()));
    }
}
