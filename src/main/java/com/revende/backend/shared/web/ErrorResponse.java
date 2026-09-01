package com.revende.backend.shared.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;

/**
 * Contrato único de erro da API — CLAUDE.md §2.2 pede tipado, não {@code Map}.
 *
 * @param message texto exibível ao usuário final. Nunca traz stack trace, nome de classe
 *     ou detalhe de infraestrutura: isso vira pista para quem estiver sondando a API.
 * @param fields erros por campo, presentes só em falha de validação. Omitido do JSON
 *     quando nulo, para que o cliente não precise distinguir ausente de vazio.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(Instant timestamp, int status, String message, Map<String, String> fields) {

    public static ErrorResponse of(int status, String message) {
        return new ErrorResponse(Instant.now(), status, message, null);
    }

    public static ErrorResponse ofValidation(int status, String message, Map<String, String> fields) {
        return new ErrorResponse(Instant.now(), status, message, fields);
    }
}
