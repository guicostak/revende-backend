package com.revende.backend.identity.domain.model;

import com.revende.backend.shared.domain.UuidV7;
import java.util.Objects;
import java.util.UUID;

/**
 * Identidade do usuário, gerada no domínio. Isso permite que o agregado nasça completo,
 * em vez de existir num estado sem identidade até o flush do Hibernate — que é a origem
 * do problema clássico de equals/hashCode em entidade JPA.
 */
public record UserId(UUID value) {

    public UserId {
        Objects.requireNonNull(value, "id é obrigatório");
    }

    public static UserId newId() {
        return new UserId(UuidV7.generate());
    }

    public static UserId of(String value) {
        return new UserId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
