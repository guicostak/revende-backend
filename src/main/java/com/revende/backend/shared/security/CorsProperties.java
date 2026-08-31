package com.revende.backend.shared.security;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Origens autorizadas a chamar a API pelo navegador.
 *
 * <p>Vem de {@code CORS_ALLOWED_ORIGINS}, lista separada por vírgula. Em produção é o
 * domínio real do frontend; o default de {@code application.yml} serve ao ambiente local.
 *
 * <p>Não existe curinga aqui de propósito: {@code *} em API que recebe token no header
 * transforma qualquer site em cliente autenticado da conta de quem estiver logado.
 */
@ConfigurationProperties(prefix = "revende.cors")
public record CorsProperties(List<String> allowedOrigins) {}
