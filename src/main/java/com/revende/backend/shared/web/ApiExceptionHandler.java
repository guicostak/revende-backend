package com.revende.backend.shared.web;

import com.revende.backend.identity.application.EmailAlreadyRegisteredException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Traduz exceção em resposta HTTP, em um lugar só. */
@RestControllerAdvice
public class ApiExceptionHandler {

    /** Formato inválido é erro do pedido: 400, com o detalhe por campo. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            // `merge` mantém a primeira mensagem: com duas violações no mesmo campo, a
            // primeira declarada é a mais específica e a mais útil para quem lê.
            fields.merge(error.getField(), error.getDefaultMessage(), (primeira, segunda) -> primeira);
        }
        return ResponseEntity.badRequest()
                .body(ErrorResponse.ofValidation(
                        HttpStatus.BAD_REQUEST.value(), "Alguns campos estão inválidos.", fields));
    }

    /** Dado válido, mundo impede: 409. */
    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<ErrorResponse> handleEmailTaken(EmailAlreadyRegisteredException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(HttpStatus.CONFLICT.value(), exception.getMessage()));
    }

    /**
     * Rede de segurança da corrida entre o `existsByEmail` e o `save`: duas requisições
     * simultâneas com o mesmo e-mail passam as duas pela checagem, e a constraint UNIQUE
     * barra a segunda. Sem este handler isso vira 500 — quando é o mesmo 409 de sempre.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleIntegrity(DataIntegrityViolationException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(HttpStatus.CONFLICT.value(), "Já existe uma conta com este e-mail."));
    }
}
