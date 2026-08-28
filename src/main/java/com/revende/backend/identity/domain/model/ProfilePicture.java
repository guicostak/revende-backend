package com.revende.backend.identity.domain.model;

import com.revende.backend.identity.domain.exception.InvalidProfilePictureException;

/**
 * Endereço da foto de perfil. Exige https porque imagem em http dentro de página https
 * é bloqueada pelo navegador como conteúdo misto — a foto some sem erro visível.
 */
public record ProfilePicture(String value) {

    private static final int MAX_LENGTH = 500;

    public ProfilePicture {
        if (value == null || value.isBlank()) {
            throw new InvalidProfilePictureException("Endereço da foto é obrigatório");
        }
        value = value.trim();
        if (value.length() > MAX_LENGTH) {
            throw new InvalidProfilePictureException("Endereço excede " + MAX_LENGTH + " caracteres");
        }
        if (!value.startsWith("https://")) {
            throw new InvalidProfilePictureException("Endereço da foto deve usar https");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
