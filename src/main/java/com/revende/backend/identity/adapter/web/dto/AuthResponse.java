package com.revende.backend.identity.adapter.web.dto;

import com.revende.backend.identity.application.port.AuthenticatedUser;

/**
 * Resposta de cadastro, login e renovação.
 *
 * <p>{@code token} é o access token, mandado em toda requisição no header
 * {@code Authorization}. {@code refreshToken} é opaco e só volta ao servidor em
 * {@code /api/auth/refresh}.
 *
 * <p>Existe separada do {@code AuthenticatedUser} da aplicação porque são contratos com
 * donos diferentes: este é público e muda junto com o frontend; aquele é interno. Colar
 * os dois faria uma mudança de borda vazar para dentro da aplicação.
 */
public record AuthResponse(String token, String refreshToken, Long userId, String name, String email) {

    public static AuthResponse from(AuthenticatedUser user) {
        return new AuthResponse(user.token(), user.refreshToken(), user.userId(), user.name(), user.email());
    }
}
