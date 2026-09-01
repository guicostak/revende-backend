package com.revende.backend.identity.adapter.in.web.dto;

import com.revende.backend.identity.application.port.in.AuthenticatedUser;

/**
 * Resposta de cadastro e de login. Espelha o {@code AuthResponse} do frontend.
 *
 * <p>Existe separada do {@code AuthenticatedUser} da aplicação porque são contratos com
 * donos diferentes: este é público e muda junto com o frontend; aquele é interno. Colar
 * os dois faria uma mudança de borda vazar para dentro da aplicação.
 */
public record AuthResponse(String token, Long userId, String name, String email) {

    public static AuthResponse from(AuthenticatedUser user) {
        return new AuthResponse(user.token(), user.userId(), user.name(), user.email());
    }
}
