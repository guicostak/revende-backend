package com.revende.backend.identity.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

/** Corpo da renovação. O refresh token vai no corpo, não em header nem em query. */
public record RefreshRequest(@NotBlank(message = "Informe o refresh token.") String refreshToken) {}
