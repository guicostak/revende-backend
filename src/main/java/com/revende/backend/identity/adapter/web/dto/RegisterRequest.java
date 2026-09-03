package com.revende.backend.identity.adapter.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Informe o nome.") @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
                String name,
        @NotBlank(message = "Informe o e-mail.")
                @Email(message = "E-mail inválido.")
                @Size(max = 320, message = "O e-mail deve ter no máximo 320 caracteres.")
                String email,
        @NotBlank(message = "Informe a senha.")
                @Size(min = 6, max = 72, message = "A senha deve ter entre 6 e 72 caracteres.")
                String password,
        @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres.") String phone) {}
