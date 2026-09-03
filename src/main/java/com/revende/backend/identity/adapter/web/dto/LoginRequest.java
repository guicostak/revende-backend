package com.revende.backend.identity.adapter.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Corpo do login. Espelha o {@code LoginPayload} do frontend.
 *
 * <p>Só {@code @NotBlank}, de propósito. Aplicar aqui as mesmas regras de tamanho e
 * formato do cadastro faria a API responder 400 com detalhe de campo para uma senha que
 * simplesmente não confere — e isso conta a quem estiver sondando qual é a política de
 * senha, além de diferenciar "formato errado" de "credencial errada". Credencial inválida
 * é sempre 401 com a mesma mensagem.
 */
public record LoginRequest(
        @NotBlank(message = "Informe o e-mail.") String email,
        @NotBlank(message = "Informe a senha.") String password) {}
