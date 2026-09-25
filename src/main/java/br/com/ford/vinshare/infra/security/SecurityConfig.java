package br.com.ford.vinshare.infra.security;

import br.com.ford.vinshare.domain.usuario.Perfil;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Clock;
import java.util.List;

/**
 * Configuração central de segurança.
 *
 * <ul>
 *   <li>API stateless (sem sessão e sem CSRF): cada requisição carrega seu JWT.</li>
 *   <li>A matriz de permissões por perfil fica aqui, na cadeia de filtros: a requisição é barrada
 *       (401/403) antes de chegar ao controller, sem nem ler o corpo.</li>
 *   <li>Os controllers repetem a regra com {@code @PreAuthorize} (defesa em profundidade) e os
 *       services aplicam o escopo por concessionária (dados de outra concessionária = 403).</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] DOCUMENTACAO = {"/swagger-ui.html", "/swagger-ui/**", "/api-docs", "/api-docs/**"};
    private static final String ADMIN = Perfil.ADMIN.name();
    private static final String ANALISTA = Perfil.ANALISTA.name();
    private static final String CONCESSIONARIA = Perfil.CONCESSIONARIA.name();

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   SecurityFilter securityFilter,
                                                   RestSecurityHandler restSecurityHandler) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ---------- Públicos
                        .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.GET, "/health-check").permitAll()
                        .requestMatchers(HttpMethod.GET, "/concessionarias", "/concessionarias/{id}").permitAll()
                        .requestMatchers(DOCUMENTACAO).permitAll()
                        .requestMatchers("/error").permitAll()
                        // ---------- Usuários: /me para qualquer perfil, gestão apenas ADMIN
                        .requestMatchers(HttpMethod.GET, "/usuarios/me").authenticated()
                        .requestMatchers("/usuarios/**").hasRole(ADMIN)
                        // ---------- Concessionárias: escrita apenas ADMIN (leitura é pública)
                        .requestMatchers("/concessionarias/**").hasRole(ADMIN)
                        // ---------- Veículos, clientes e serviços: leitura para todos, escrita ADMIN/CONCESSIONARIA
                        .requestMatchers(HttpMethod.DELETE, "/veiculos/**", "/servicos/**").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.GET, "/veiculos/**", "/clientes/**", "/servicos/**").authenticated()
                        .requestMatchers("/veiculos/**", "/clientes/**", "/servicos/**").hasAnyRole(ADMIN, CONCESSIONARIA)
                        // ---------- VIN Share: visão da rede apenas ADMIN/ANALISTA; demais consultas com escopo no service
                        .requestMatchers(HttpMethod.GET, "/vinshare/dashboard", "/vinshare/concessionarias").hasAnyRole(ADMIN, ANALISTA)
                        .requestMatchers(HttpMethod.GET, "/vinshare/**").authenticated()
                        // ---------- Qualquer outra rota exige autenticação
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restSecurityHandler)
                        .accessDeniedHandler(restSecurityHandler))
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /** Evita que o Spring Boot registre o SecurityFilter uma segunda vez fora da cadeia de segurança. */
    @Bean
    public FilterRegistrationBean<SecurityFilter> securityFilterRegistration(SecurityFilter filter) {
        var registro = new FilterRegistrationBean<>(filter);
        registro.setEnabled(false);
        return registro;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
        var configuracao = new CorsConfiguration();
        configuracao.setAllowedOrigins(corsProperties.allowedOrigins() != null ? corsProperties.allowedOrigins() : List.of());
        configuracao.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracao.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        configuracao.setExposedHeaders(List.of("Location", "WWW-Authenticate"));
        configuracao.setMaxAge(3600L);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuracao);
        return source;
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
