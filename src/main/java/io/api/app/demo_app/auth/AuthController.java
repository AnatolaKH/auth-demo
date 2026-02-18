package io.api.app.demo_app.auth;

import io.api.app.demo_app.User;
import io.api.app.demo_app.UserRepository;
import io.api.app.demo_app.auth.base.OtpErrorMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final OtpService otpService;
    private final OtpErrorMapper otpErrorMapper;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return new ResponseEntity<>(
                    new ErrorResponse(
                            "V3AU1",
                            "Email already exists"
                    ),
                    HttpStatus.CONFLICT
            );
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
        );

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getEmail());
        final String accessToken = jwtUtil.generateAccessToken(userDetails);
        final String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenRequest request) {
        if (request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("V3AU2", "Refresh token is required"));
        }

        try {
            String username = jwtUtil.extractUsername(request.getRefreshToken());
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (!jwtUtil.validateRefreshToken(request.getRefreshToken(), userDetails)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("V3AU3", "Invalid refresh token"));
            }

            String newAccessToken = jwtUtil.generateAccessToken(userDetails);
            String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);
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
            final UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            final String accessToken = jwtUtil.generateAccessToken(userDetails);
            final String refreshToken = jwtUtil.generateRefreshToken(userDetails);
            return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
        } else {
            return ResponseEntity.badRequest().body("Invalid OTP");
        }
    }
}
