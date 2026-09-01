package com.revende.backend.identity.application;

/**
 * E-mail já pertence a outra conta.
 *
 * <p>É conflito de estado, não erro de formato: o dado enviado é válido, o que impede é o
 * mundo. Por isso vira {@code 409} e não {@code 400} — ver CLAUDE.md §2.2.
 */
public class EmailAlreadyRegisteredException extends RuntimeException {

    public EmailAlreadyRegisteredException() {
        // A mensagem não repete o e-mail recebido: ela chega ao cliente, e ecoar entrada
        // do usuário na resposta é o que transforma erro em vetor de injeção.
        super("Já existe uma conta com este e-mail.");
    }
}
