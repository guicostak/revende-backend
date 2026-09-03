package com.revende.backend.identity.adapter.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.revende.backend.identity.entity.User;
import com.revende.backend.identity.model.AccountStatus;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Postgres real, não H2: o que se está verificando aqui — constraint UNIQUE e o schema
 * que o Flyway produz — é justamente onde bancos em memória mentem.
 */
@DataJpaTest
// Sem isto o @DataJpaTest troca o DataSource por um banco em memória — exatamente o que
// este teste não quer, já que o que ele verifica é o comportamento do Postgres.
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Import(UserPersistenceAdapter.class)
class UserPersistenceAdapterTest {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserPersistenceAdapter adapter;

    private User novoUsuario(String email) {
        Instant agora = Instant.now();
        return User.builder()
                .name("Ana Souza")
                .email(email)
                .passwordHash("{bcrypt}$2a$10$hash")
                .status(AccountStatus.ACTIVE)
                .emailVerified(false)
                .createdAt(agora)
                .updatedAt(agora)
                .build();
    }

    @Test
    void shouldPersistUserAndAssignId() {
        User salvo = adapter.save(novoUsuario("ana@exemplo.com"));

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getEmail()).isEqualTo("ana@exemplo.com");
    }

    @Test
    void shouldReportExistingEmail() {
        adapter.save(novoUsuario("ana@exemplo.com"));

        assertThat(adapter.existsByEmail("ana@exemplo.com")).isTrue();
        assertThat(adapter.existsByEmail("outra@exemplo.com")).isFalse();
    }

    @Test
    void shouldRejectDuplicateEmailAtDatabaseLevel() {
        adapter.save(novoUsuario("ana@exemplo.com"));

        // A checagem no service é para a mensagem; quem garante é a constraint. Este teste
        // existe para provar que a garantia está no banco, e não só no código.
        assertThatThrownBy(() -> adapter.save(novoUsuario("ana@exemplo.com")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldPersistOptionalSellerFieldsAsNull() {
        User salvo = adapter.save(novoUsuario("ana@exemplo.com"));

        // CPF, PIX e endereço não são pedidos no cadastro. O schema precisa aceitá-los
        // ausentes, senão o cadastro simplesmente não passa.
        assertThat(salvo.getCpf()).isNull();
        assertThat(salvo.getPixKey()).isNull();
        assertThat(salvo.getPixKeyType()).isNull();
        assertThat(salvo.getZipCode()).isNull();
        assertThat(salvo.getProfilePictureUrl()).isNull();
    }
}
