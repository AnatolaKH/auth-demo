package io.api.app.demo_app.auth;

import io.api.app.demo_app.User;
import io.api.app.demo_app.UserRepository;
import io.api.app.demo_app.auth.client.CreateUserProfileRequest;
import io.api.app.demo_app.auth.client.UserServiceClient;
import io.api.app.demo_app.auth.base.OtpErrorMapper;
import io.api.app.demo_app.auth.dto.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserServiceClient userServiceClient;
    private final OtpService otpService;
    private final OtpErrorMapper otpErrorMapper;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            return new ResponseEntity<>(
                    new ErrorResponse(
                            "V3AU1",
                            "Email already exists"
                    ),
                    HttpStatus.CONFLICT
            );
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setUserId(UUID.randomUUID());

        try {
            userRepository.save(user);
            userServiceClient.createUserProfile(new CreateUserProfileRequest(user.getUserId(), user.getEmail(), user.getName()));
        } catch (DataIntegrityViolationException e) {
            return new ResponseEntity<>(new ErrorResponse("V3AU1", "Email already exists"), HttpStatus.CONFLICT);
        } catch (RestClientException e) {
            userRepository.delete(user);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(new ErrorResponse("V3AU5", "User service unavailable"));
        }

        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
        );

        final User user = (User) userDetailsService.loadUserByUsername(authRequest.getEmail());
        final String subject = user.getUserId().toString();
        final String accessToken = jwtUtil.generateAccessToken(subject);
        final String refreshToken = jwtUtil.generateRefreshToken(subject);

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenRequest request) {
        if (request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("V3AU2", "Refresh token is required"));
        }

        try {
            String subject = jwtUtil.extractSubject(request.getRefreshToken());
            userDetailsService.loadUserByUserId(UUID.fromString(subject));

            if (!jwtUtil.validateRefreshToken(request.getRefreshToken(), subject)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("V3AU3", "Invalid refresh token"));
            }

            String newAccessToken = jwtUtil.generateAccessToken(subject);
            String newRefreshToken = jwtUtil.generateRefreshToken(subject);
            return ResponseEntity.ok(new AuthResponse(newAccessToken, newRefreshToken));
        } catch (Exception ignored) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("V3AU3", "Invalid refresh token"));
        }
    }

    @PostMapping("/otp/send")
    public ResponseEntity<?> sendOtp(@RequestParam String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(otpErrorMapper::userNotFound);
        String otp = otpService.generateOtp(user);
        // In a real application, you would send the OTP via email or SMS
        System.out.println("Generated OTP: " + otp);
        return ResponseEntity.ok("OTP sent successfully");
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<?> verifyOtp(@RequestParam String email, @RequestParam String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(otpErrorMapper::userNotFound);
        if (otpService.validateOtp(user, code)) {
            final String subject = user.getUserId().toString();
            final String accessToken = jwtUtil.generateAccessToken(subject);
            final String refreshToken = jwtUtil.generateRefreshToken(subject);
            return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
        } else {
            return ResponseEntity.badRequest().body("Invalid OTP");
        }
    }
}
