package com.revende.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Prova que o contexto da aplicação sobe: beans resolvem, o mapeamento JPA é válido
 * e a configuração de segurança é aceita. Sem este teste a CI prova apenas que o
 * código compila, o que não é a mesma coisa.
 */
@SpringBootTest
class RevendeBackendApplicationTests {

    @Test
    void contextLoads() {}
}
