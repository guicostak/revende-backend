package com.revende.backend.identity.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Corpo do cadastro. Espelha o {@code RegisterPayload} do frontend — os quatro campos que
 * ele envia, nada além.
 *
 * <p>CPF, chave PIX e endereço existem na entidade mas não entram aqui: são dados de
 * vendedor, e ninguém precisa deles para criar uma conta. Pedir na primeira tela é
 * atrito antes de haver valor.
 *
 * <p>Os limites de tamanho repetem os das colunas de propósito. Sem isso o erro só
 * apareceria como violação de constraint no banco, que vira 500 em vez de 400.
 */
public record RegisterRequest(
        @NotBlank(message = "Informe o nome.") @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
                String name,
        @NotBlank(message = "Informe o e-mail.")
                @Email(message = "E-mail inválido.")
                @Size(max = 320, message = "O e-mail deve ter no máximo 320 caracteres.")
                String email,
        // O mínimo de 6 espelha o do formulário do frontend. Divergir criaria senha aceita
        // na tela e recusada pela API. É fraco, e subir é decisão de produto — mas tem de
        // subir nos dois lados no mesmo dia.
        //
        // O máximo de 72 não é estética: o BCrypt ignora silenciosamente tudo além de 72
        // bytes. Sem o limite, senha longa seria truncada sem ninguém saber, e o usuário
        // acharia que tem uma senha mais forte do que de fato tem.
        @NotBlank(message = "Informe a senha.")
                @Size(min = 6, max = 72, message = "A senha deve ter entre 6 e 72 caracteres.")
                String password,
        @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres.") String phone) {}
