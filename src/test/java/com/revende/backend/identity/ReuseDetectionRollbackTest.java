package com.revende.backend.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Prova que a detecção de reuso persiste a revogação em massa.
 *
 * <p>O teste unitário de RefreshSessionService usa mock e só verifica que
 * `revokeAllForUser` foi CHAMADO — não que o efeito sobreviveu à transação.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ReuseDetectionRollbackTest {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine");

    @DynamicPropertySource
    static void configuracao(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("revende.jwt.secret", () -> "segredo-de-teste-com-mais-de-32-bytes-para-hs256");
    }

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper json = new ObjectMapper();

    private JsonNode postJson(String rota, String corpo, int esperado) throws Exception {
        String resposta = mockMvc.perform(
                        post(rota).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().is(esperado))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return json.readTree(resposta);
    }

    @Test
    void shouldKillTheWholeSessionWhenAnAlreadyUsedTokenReappears() throws Exception {
        JsonNode cadastro = postJson(
                "/api/auth/register",
                """
                {"name":"Ana","email":"reuso@exemplo.com","password":"senha123"}
                """,
                201);
        String refreshOriginal = cadastro.get("refreshToken").asText();

        // Rotação normal: o original morre, nasce o segundo.
        JsonNode primeira = postJson("/api/auth/refresh", "{\"refreshToken\":\"" + refreshOriginal + "\"}", 200);
        String refreshAtual = primeira.get("refreshToken").asText();
        assertThat(refreshAtual).isNotEqualTo(refreshOriginal);

        // Reuso: apresenta o token já revogado. Deve disparar a revogação em massa.
        postJson("/api/auth/refresh", "{\"refreshToken\":\"" + refreshOriginal + "\"}", 401);

        // O token ATUAL, que era válido, precisa ter morrido junto — é isso que significa
        // "derrubar a sessão inteira". Se ele ainda funcionar, a revogação não persistiu.
        postJson("/api/auth/refresh", "{\"refreshToken\":\"" + refreshAtual + "\"}", 401);
    }
}
