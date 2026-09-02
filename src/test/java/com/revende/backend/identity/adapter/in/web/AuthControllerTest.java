package com.revende.backend.identity.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.revende.backend.identity.application.EmailAlreadyRegisteredException;
import com.revende.backend.identity.application.InvalidCredentialsException;
import com.revende.backend.identity.application.InvalidRefreshTokenException;
import com.revende.backend.identity.application.port.in.AuthenticatedUser;
import com.revende.backend.identity.application.port.in.LoginUseCase;
import com.revende.backend.identity.application.port.in.RefreshSessionUseCase;
import com.revende.backend.identity.application.port.in.RegisterUserCommand;
import com.revende.backend.identity.application.port.in.RegisterUserUseCase;
import com.revende.backend.shared.security.ActiveUserCache;
import com.revende.backend.shared.security.SecurityConfig;
import com.revende.backend.shared.web.ApiExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, ApiExceptionHandler.class})
@TestPropertySource(
        properties = {
            "revende.cors.allowed-origins=https://revende.net",
            "revende.jwt.secret=segredo-de-teste-com-mais-de-32-bytes-para-hs256",
            "revende.jwt.expiration-ms=3600000",
            "revende.jwt.refresh-expiration-ms=604800000"
        })
class AuthControllerTest {

    private static final String CADASTRO =
            """
            {"name":"Ana Souza","email":"ana@exemplo.com","password":"senha123","phone":"11999998888"}
            """;

    private static final String LOGIN =
            """
            {"email":"ana@exemplo.com","password":"senha123"}
            """;

    private static final AuthenticatedUser AUTENTICADA =
            new AuthenticatedUser("token-jwt", "refresh-token", 42L, "Ana Souza", "ana@exemplo.com");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RegisterUserUseCase registerUser;

    @MockBean
    private LoginUseCase login;

    @MockBean
    private RefreshSessionUseCase refreshSession;

    // O SecurityConfig importado arrasta o filtro JWT, que depende do cache. Este teste é
    // sobre a borda HTTP, não sobre validação de token — o mock existe para o contexto
    // subir, e o filtro fica inerte, que é o correto para rotas públicas.
    @MockBean
    private ActiveUserCache activeUserCache;

    // ---------- cadastro ----------

    @Test
    void shouldReturn201WithBothTokensAndLocationWhenRegistrationSucceeds() throws Exception {
        when(registerUser.register(any())).thenReturn(AUTENTICADA);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CADASTRO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/users/42"))
                .andExpect(jsonPath("$.token").value("token-jwt"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.userId").value(42));
    }

    @Test
    void shouldReturn409WhenEmailAlreadyRegistered() throws Exception {
        when(registerUser.register(any())).thenThrow(new EmailAlreadyRegisteredException());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CADASTRO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Já existe uma conta com este e-mail."));
    }

    @Test
    void shouldReturn400WithFieldDetailWhenEmailIsInvalid() throws Exception {
        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"name":"Ana","email":"nao-e-email","password":"senha123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.email").value("E-mail inválido."));

        verify(registerUser, never()).register(any(RegisterUserCommand.class));
    }

    @Test
    void shouldReturn400WhenPasswordIsShorterThanFrontendMinimum() throws Exception {
        // O formulário do frontend exige 6. A API precisa exigir o mesmo, senão existe
        // senha que passa na tela e é recusada aqui.
        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"name":"Ana","email":"ana@exemplo.com","password":"12345"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.password").exists());
    }

    @Test
    void shouldAcceptRegistrationWithoutPhone() throws Exception {
        when(registerUser.register(any())).thenReturn(AUTENTICADA);

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"name":"Ana","email":"ana@exemplo.com","password":"senha123"}
                                """))
                .andExpect(status().isCreated());
    }

    // ---------- login ----------

    @Test
    void shouldReturn200WithBothTokensWhenLoginSucceeds() throws Exception {
        when(login.login(any())).thenReturn(AUTENTICADA);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOGIN))
                // 200 e não 201: login não cria recurso, abre sessão sobre um que existe.
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-jwt"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void shouldReturn401WithGenericMessageWhenCredentialsAreWrong() throws Exception {
        when(login.login(any())).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOGIN))
                .andExpect(status().isUnauthorized())
                // A mensagem não pode dizer se o e-mail existe: seria sonda de enumeração.
                .andExpect(jsonPath("$.message").value("E-mail ou senha inválidos."));
    }

    @Test
    void shouldNotValidatePasswordFormatOnLogin() throws Exception {
        when(login.login(any())).thenThrow(new InvalidCredentialsException());

        // Senha curta no login é credencial errada (401), não erro de formato (400).
        // Responder 400 com detalhe de campo entregaria a política de senha.
        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"email":"ana@exemplo.com","password":"x"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn400WhenLoginBodyIsIncomplete() throws Exception {
        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"email":"ana@exemplo.com"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.password").value("Informe a senha."));
    }

    // ---------- renovação ----------

    @Test
    void shouldReturn200WithRotatedTokensWhenRefreshSucceeds() throws Exception {
        when(refreshSession.refresh(anyString()))
                .thenReturn(new AuthenticatedUser("novo-access", "novo-refresh", 42L, "Ana", "ana@exemplo.com"));

        mockMvc.perform(
                        post("/api/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"refreshToken":"refresh-antigo"}
                                """))
                .andExpect(status().isOk())
                // Rotação: os dois voltam diferentes do que foi mandado.
                .andExpect(jsonPath("$.token").value("novo-access"))
                .andExpect(jsonPath("$.refreshToken").value("novo-refresh"));
    }

    @Test
    void shouldReturn401WhenRefreshTokenIsInvalid() throws Exception {
        when(refreshSession.refresh(anyString())).thenThrow(new InvalidRefreshTokenException());

        mockMvc.perform(
                        post("/api/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"refreshToken":"ja-usado"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Sessão expirada. Entre novamente."));
    }

    // ---------- rotas públicas ----------

    @Test
    void shouldExposeAllAuthRoutesWithoutAuthentication() throws Exception {
        when(registerUser.register(any())).thenReturn(AUTENTICADA);
        when(login.login(any())).thenReturn(AUTENTICADA);
        when(refreshSession.refresh(anyString())).thenReturn(AUTENTICADA);

        // Nenhuma das três pode exigir token: é por elas que se obtém um.
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CADASTRO))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOGIN))
                .andExpect(status().isOk());
        mockMvc.perform(
                        post("/api/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"refreshToken":"qualquer"}
                                """))
                .andExpect(status().isOk());
    }
}
