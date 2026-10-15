package com.chatboot.identity;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final PasswordPolicy passwordPolicy;

    public AuthController(AuthService authService, PasswordPolicy passwordPolicy) {
        this.authService = authService;
        this.passwordPolicy = passwordPolicy;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        try {
            passwordPolicy.check(request.password());
        } catch (PasswordPolicy.PasswordTooShortException ex) {
            return ResponseEntity.badRequest().build();
        }
        try {
            RegisteredUser created = authService.register(request.email(), request.password(), request.workspaceName());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new RegisterResponse(created.userId(), created.workspaceId()));
        } catch (DuplicateEmailException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            IssuedToken token = authService.login(request.email(), request.password());
            return ResponseEntity.ok(new LoginResponse(token.accessToken(), JwtService.EXPIRES_IN_SECONDS, "Bearer"));
        } catch (InvalidCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
