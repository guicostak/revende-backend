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

/** Borda HTTP da identidade. Recebe, valida, delega e mapeia — sem regra de negócio. */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    // Depende dos ports de entrada, nunca das classes concretas.
    private final RegisterUserUseCase registerUser;
    private final LoginUseCase login;
    private final RefreshSessionUseCase refreshSession;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthenticatedUser user = registerUser.register(
                new RegisterUserCommand(request.name(), request.email(), request.password(), request.phone()));

        // 201 com Location, como manda o CLAUDE.md §2.2. O corpo traz os tokens porque quem
        // acabou de se cadastrar já está autenticado — não faz sentido pedir login em
        // seguida para a mesma pessoa que acabou de provar quem é.
        return ResponseEntity.created(URI.create("/api/users/" + user.userId())).body(AuthResponse.from(user));
    }

    /** 200, não 201: login não cria recurso, abre sessão sobre um que já existe. */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return AuthResponse.from(login.login(new LoginCommand(request.email(), request.password())));
    }

    /**
     * Devolve um par novo e invalida o apresentado. O refresh token vale uma vez só: se um
     * vazar e for usado, a próxima tentativa do dono legítimo denuncia o roubo.
     */
    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return AuthResponse.from(refreshSession.refresh(request.refreshToken()));
    }
}
