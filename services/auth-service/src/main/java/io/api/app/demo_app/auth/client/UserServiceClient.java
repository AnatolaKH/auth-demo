package io.api.app.demo_app.auth.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class UserServiceClient {

    @Value("${user-service.base-url}")
    private String userServiceBaseUrl;

    @Value("${user-service.internal-api-key}")
    private String internalApiKey;

    public void createUserProfile(CreateUserProfileRequest request) {
        RestClient.create(userServiceBaseUrl)
                .post()
                .uri("/internal/users")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Internal-Api-Key", internalApiKey)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}
