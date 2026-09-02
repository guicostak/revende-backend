package com.revende.backend.identity.application.port.out;

import com.revende.backend.identity.entity.User;
import java.util.Optional;

/**
 * O que o domínio de identidade precisa de persistência.
 *
 * <p>Existe para que {@code application} não importe {@code JpaRepository}: a aplicação
 * declara o que precisa, o adapter escolhe como. Trocar o banco é reimplementar aqui.
 */
public interface UserRepositoryPort {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    User save(User user);
}
