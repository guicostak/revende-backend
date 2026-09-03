package com.revende.backend.shared.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.revende.backend.identity.application.port.LoginUseCase;
import com.revende.backend.identity.application.port.RefreshSessionUseCase;
import com.revende.backend.identity.application.port.RegisterUserUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest
@Import(SecurityConfig.class)
@TestPropertySource(
        properties = {
            "revende.cors.allowed-origins=https://revende.net",
            // O filtro JWT deriva a chave na construção: sem segredo, o contexto nem sobe.
            "revende.jwt.secret=segredo-de-teste-com-mais-de-32-bytes-para-hs256",
            "revende.jwt.expiration-ms=3600000",
            "revende.jwt.refresh-expiration-ms=604800000"
        })
class SecurityConfigTest {

    private static final String ALLOWED_ORIGIN = "https://revende.net";
    private static final String FOREIGN_ORIGIN = "https://site-de-terceiro.example";

    @Autowired
    private MockMvc mockMvc;

    // O slice carrega todos os controllers, e o AuthController depende dos três casos de
    // uso. Este teste é sobre a cadeia de segurança, não sobre autenticação — os mocks
    // existem só para o contexto subir.
    @MockBean
    private RegisterUserUseCase registerUser;

    @MockBean
    private LoginUseCase login;

    @MockBean
    private RefreshSessionUseCase refreshSession;

    // O filtro JWT depende do cache. Mockado e sem stub, ele devolve Optional vazio para
    // qualquer id — ou seja, nenhuma requisição fica autenticada, que é exatamente o
    // cenário que este teste quer: provar que rota protegida responde 401 sem token.
    @MockBean
    private ActiveUserCache activeUserCache;

    @Test
    void shouldNotChallengeWithHttpBasicOnProtectedRoute() throws Exception {
        mockMvc.perform(get("/api/listings"))
                .andExpect(status().isUnauthorized())
                // O header de desafio é o que faz o navegador abrir caixa de login nativa.
                .andExpect(header().doesNotExist("WWW-Authenticate"));
    }

    @Test
    void shouldNotRequireAuthenticationOnAuthRoutes() throws Exception {
        // `/api/auth/login` existe, mas só em POST. O 405 é a prova que interessa aqui:
        // a requisição atravessou a cadeia de segurança e chegou ao controller, que a
        // recusou pelo método. Se a cadeia estivesse barrando, viria 401 antes disso.
        mockMvc.perform(get("/api/auth/login")).andExpect(status().isMethodNotAllowed());
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
