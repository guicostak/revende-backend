package com.revende.backend.shared.domain.exception;

/**
 * Valor rejeitado por não satisfazer a forma exigida — formato, tamanho, dígito
 * verificador. Todas mapeiam para HTTP 400 na borda, então não há ganho em distinguir por
 * tipo: a mensagem carrega o que o chamador precisa saber.
 *
 * <p>Violação de <em>regra</em>, e não de forma, tem exceção própria — porque aí o status
 * muda.
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
