package com.revende.backend.identity.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.revende.backend.identity.application.EmailAlreadyRegisteredException;
import com.revende.backend.identity.application.port.in.AuthenticatedUser;
import com.revende.backend.identity.application.port.in.RegisterUserCommand;
import com.revende.backend.identity.application.port.in.RegisterUserUseCase;
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
            "revende.jwt.expiration-ms=86400000"
        })
class AuthControllerTest {

    private static final String CORPO_VALIDO =
            """
            {"name":"Ana Souza","email":"ana@exemplo.com","password":"senha123","phone":"11999998888"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RegisterUserUseCase registerUser;

    @Test
    void shouldReturn201WithTokenAndLocationWhenRegistrationSucceeds() throws Exception {
        when(registerUser.register(any()))
                .thenReturn(new AuthenticatedUser("token-jwt", 42L, "Ana Souza", "ana@exemplo.com"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/users/42"))
                .andExpect(jsonPath("$.token").value("token-jwt"))
                .andExpect(jsonPath("$.userId").value(42))
                .andExpect(jsonPath("$.name").value("Ana Souza"))
                .andExpect(jsonPath("$.email").value("ana@exemplo.com"));
    }

    @Test
    void shouldNotRequireAuthentication() throws Exception {
        when(registerUser.register(any())).thenReturn(new AuthenticatedUser("t", 1L, "Ana", "ana@exemplo.com"));

        // A rota é pública na cadeia de segurança: sem isso o cadastro seria impossível,
        // porque não há como obter token antes de ter conta.
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO_VALIDO))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturn409WhenEmailAlreadyRegistered() throws Exception {
        when(registerUser.register(any())).thenThrow(new EmailAlreadyRegisteredException());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
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
    void shouldReturn400WhenNameIsMissing() throws Exception {
        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"email":"ana@exemplo.com","password":"senha123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.name").value("Informe o nome."));
    }

    @Test
    void shouldAcceptRegistrationWithoutPhone() throws Exception {
        when(registerUser.register(any())).thenReturn(new AuthenticatedUser("t", 7L, "Ana", "ana@exemplo.com"));

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"name":"Ana","email":"ana@exemplo.com","password":"senha123"}
                                """))
                .andExpect(status().isCreated());
    }
}
