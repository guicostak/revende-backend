package com.revende.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Sobe um PostgreSQL real, roda as migrações do Flyway nele e valida o contexto contra
 * o banco de verdade. Enquanto não houver domínio, prova que o esqueleto está de pé.
 */
@SpringBootTest
@Testcontainers
class RevendeBackendApplicationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine");

    @Test
    void contextLoads() {}
}
