package com.revende.backend.identity.application;

/**
 * E-mail inexistente, senha errada ou conta bloqueada — uma exceção só para os três.
 *
 * <p>Distinguir "e-mail não cadastrado" de "senha incorreta" entrega uma sonda de
 * enumeração: qualquer um descobre quais e-mails têm conta aqui. A mensagem é a mesma
 * sempre, e o motivo real fica no log do servidor, não na resposta.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("E-mail ou senha inválidos.");
    }
}
