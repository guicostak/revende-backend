package com.revende.backend.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIXO = "Bearer ";

    private final SecretKey chave;
    private final ActiveUserCache activeUsers;

    public JwtAuthenticationFilter(JwtProperties properties, ActiveUserCache activeUsers) {
        this.chave = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
        this.activeUsers = activeUsers;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
            throws ServletException, IOException {

        extrairToken(request)
                .flatMap(this::lerUserId)
                .flatMap(activeUsers::find)
                .ifPresent(principal -> autenticar(principal, request));

        chain.doFilter(request, response);
    }

    private Optional<String> extrairToken(HttpServletRequest request) {
        String header = request.getHeader(HEADER);
        if (header == null || !header.startsWith(PREFIXO)) {
            return Optional.empty();
        }
        return Optional.of(header.substring(PREFIXO.length()));
    }

    private Optional<Long> lerUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(chave)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(Long.valueOf(claims.getSubject()));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private void autenticar(AuthenticatedPrincipal principal, HttpServletRequest request) {
        var authentication = new UsernamePasswordAuthenticationToken(principal, null, List.of());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
