package com.revende.backend.shared.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest
@Import(SecurityConfig.class)
@TestPropertySource(properties = "revende.cors.allowed-origins=https://revende.net")
class SecurityConfigTest {

    private static final String ALLOWED_ORIGIN = "https://revende.net";
    private static final String FOREIGN_ORIGIN = "https://site-de-terceiro.example";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldNotChallengeWithHttpBasicOnProtectedRoute() throws Exception {
        mockMvc.perform(get("/api/listings"))
                .andExpect(status().isUnauthorized())
                // O header de desafio é o que faz o navegador abrir caixa de login nativa.
                .andExpect(header().doesNotExist("WWW-Authenticate"));
    }

    @Test
    void shouldNotRequireAuthenticationOnAuthRoutes() throws Exception {
        // Ainda não há controller de autenticação: 404 prova que a rota passou pela
        // cadeia de segurança em vez de ser barrada por ela.
        mockMvc.perform(get("/api/auth/login")).andExpect(status().isNotFound());
    }

    @Test
    void shouldNotRequireAuthenticationOnHealthProbe() throws Exception {
        // O actuator não entra no slice do @WebMvcTest, então não há handler para a rota.
        // 404 em vez de 401 é o que se quer provar: a cadeia liberou a sonda.
        mockMvc.perform(get("/actuator/health")).andExpect(status().isNotFound());
    }

    @Test
    void shouldAcceptPreflightFromAllowedOrigin() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", ALLOWED_ORIGIN)
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ALLOWED_ORIGIN));
    }

    @Test
    void shouldRejectPreflightFromForeignOrigin() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", FOREIGN_ORIGIN)
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}
