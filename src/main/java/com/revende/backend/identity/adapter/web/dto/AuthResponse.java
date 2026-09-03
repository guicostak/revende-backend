package com.revende.backend.identity.adapter.web.dto;

import com.revende.backend.identity.application.port.AuthenticatedUser;

public record AuthResponse(String token, String refreshToken, Long userId, String name, String email) {

    public static AuthResponse from(AuthenticatedUser user) {
        return new AuthResponse(user.token(), user.refreshToken(), user.userId(), user.name(), user.email());
    }
}
