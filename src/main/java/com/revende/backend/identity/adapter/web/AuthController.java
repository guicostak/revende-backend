package com.revende.backend.identity.adapter.web;

import com.revende.backend.identity.adapter.web.dto.AuthResponse;
import com.revende.backend.identity.adapter.web.dto.LoginRequest;
import com.revende.backend.identity.adapter.web.dto.RefreshRequest;
import com.revende.backend.identity.adapter.web.dto.RegisterRequest;
import com.revende.backend.identity.application.port.AuthenticatedUser;
import com.revende.backend.identity.application.port.LoginCommand;
import com.revende.backend.identity.application.port.LoginUseCase;
import com.revende.backend.identity.application.port.RefreshSessionUseCase;
import com.revende.backend.identity.application.port.RegisterUserCommand;
import com.revende.backend.identity.application.port.RegisterUserUseCase;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUserUseCase registerUser;
    private final LoginUseCase login;
    private final RefreshSessionUseCase refreshSession;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthenticatedUser user = registerUser.register(
                new RegisterUserCommand(request.name(), request.email(), request.password(), request.phone()));

        return ResponseEntity.created(URI.create("/api/users/" + user.userId())).body(AuthResponse.from(user));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return AuthResponse.from(login.login(new LoginCommand(request.email(), request.password())));
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return AuthResponse.from(refreshSession.refresh(request.refreshToken()));
    }
}
