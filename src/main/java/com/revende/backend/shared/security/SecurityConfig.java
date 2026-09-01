package com.revende.backend.shared.security;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Cadeia de segurança da API.
 *
 * <p>Existe porque {@code spring-boot-starter-security} está no classpath: sem nenhuma
 * configuração, a cadeia padrão do Spring exige HTTP Basic em <b>toda</b> rota — inclusive
 * nas que não existem, que passam a responder 401 em vez de 404. Era esse o estado da
 * revisão implantada no Cloud Run.
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties({CorsProperties.class, JwtProperties.class})
public class SecurityConfig {

    /** Rotas de autenticação. Únicas públicas: é por elas que se obtém o token. */
    private static final String AUTH_ROUTES = "/api/auth/**";

    /** Sondas de liveness e readiness do Cloud Run. Sem isso a revisão não sobe. */
    private static final String[] PROBE_ROUTES = {"/actuator/health", "/actuator/health/**"};

    @Bean
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        return http
                // Resolve pelo *nome* do bean (`corsConfigurationSource`), de propósito.
                // Injetar por tipo seria ambíguo: o `mvcHandlerMappingIntrospector` do
                // Spring MVC também implementa CorsConfigurationSource.
                .cors(Customizer.withDefaults())
                // API sem sessão e sem cookie: o token vai no header `Authorization`, que
                // o navegador não anexa sozinho em requisição de outro site. Sem credencial
                // ambiente não há o que forjar, então CSRF protegeria contra um ataque que
                // este desenho não permite — e exigiria um token de CSRF que o cliente não
                // teria como obter sem sessão.
                //
                // ATENÇÃO: isto vale ENQUANTO a autenticação não usar cookie. No dia em que
                // o login emitir cookie de sessão ou de refresh, esta linha deixa de ser
                // segura e a proteção precisa voltar. O CodeQL sinaliza esta linha
                // (java/spring-disabled-csrf-protection) exatamente por isso.
                .csrf(AbstractHttpConfigurer::disable)
                // Desliga os dois mecanismos que a autoconfiguração ligaria sozinha.
                // O `WWW-Authenticate: Basic` do Basic faz o navegador abrir caixa de
                // login nativa em cima de uma chamada de API.
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.requestMatchers(PROBE_ROUTES)
                        .permitAll()
                        .requestMatchers(AUTH_ROUTES)
                        .permitAll()
                        // Nega por padrão: rota nova nasce protegida, e liberar é um ato
                        // deliberado acima desta linha.
                        .anyRequest()
                        .authenticated())
                // Sem mecanismo de autenticação configurado, o default responderia 403.
                // Para um cliente de API, 401 é a informação correta: falta credencial.
                .exceptionHandling(handling ->
                        handling.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProperties properties) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(properties.allowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        // O token viaja no header `Authorization`, não em cookie. Ligar credentials aqui
        // só ampliaria a superfície sem que nada no fluxo precise disso.
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(Duration.ofHours(1));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * {@code DelegatingPasswordEncoder} prefixa o hash com o algoritmo usado
     * ({@code {bcrypt}$2a$...}). É o que permite trocar de algoritmo no futuro sem
     * invalidar as senhas já gravadas: cada hash carrega a informação de como conferi-lo.
     * Um {@code BCryptPasswordEncoder} puro não guarda isso, e a migração depois exige
     * forçar todo mundo a redefinir a senha.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
