package com.revende.backend.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.revende.backend.identity.adapter.persistence.UserJpaRepository;
import com.revende.backend.identity.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Cadastro de ponta a ponta: HTTP, aplicação, Postgres e emissão de token. */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class RegisterUserEndToEndTest {

    private static final String SEGREDO = "segredo-de-teste-com-mais-de-32-bytes-para-hs256";

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine");

    @DynamicPropertySource
    static void configuracao(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("revende.jwt.secret", () -> SEGREDO);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserJpaRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldRegisterUserPersistHashedPasswordAndIssueUsableToken() throws Exception {
        String resposta = mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"name":"Ana Souza","email":"Ana@Exemplo.com","password":"senha123","phone":"11999998888"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("ana@exemplo.com"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        User salvo = repository.findByEmail("ana@exemplo.com").orElseThrow();

        // A senha em texto puro não pode ter sobrevivido em lugar nenhum.
        assertThat(salvo.getPasswordHash()).doesNotContain("senha123");
        assertThat(passwordEncoder.matches("senha123", salvo.getPasswordHash())).isTrue();

        // O token precisa ser verificável com o mesmo segredo e apontar para o id do
        // usuário — é o que o filtro de autenticação vai conferir quando existir.
        String token = resposta.replaceAll(".*\"token\"\\s*:\\s*\"([^\"]+)\".*", "$1");
        String subject = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(SEGREDO.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();

        assertThat(subject).isEqualTo(String.valueOf(salvo.getId()));
    }

    @Test
    void shouldReturn409WhenEmailIsAlreadyTaken() throws Exception {
        String corpo =
                """
                {"name":"Ana","email":"duplicada@exemplo.com","password":"senha123"}
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Já existe uma conta com este e-mail."));
    }
}
