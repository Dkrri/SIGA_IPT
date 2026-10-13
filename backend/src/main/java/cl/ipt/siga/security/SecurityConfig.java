package cl.ipt.siga.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuracion central de seguridad del backend.
 *
 * <p>El backend actua como <b>OAuth2 Resource Server</b>: no emite tokens ni
 * guarda contrasenas, solo <b>valida</b> los tokens JWT que emite Keycloak.
 *
 * <p>Responsabilidades de esta clase:
 * <ol>
 *   <li>Definir que endpoints son publicos y cuales requieren autenticacion.</li>
 *   <li>Configurar el backend como resource server (validacion del JWT).</li>
 *   <li>Traducir los roles del token a autoridades de Spring (ver
 *       {@link KeycloakRealmRoleConverter}).</li>
 *   <li>Construir el {@link JwtDecoder} con el cual se verifica la firma y el emisor.</li>
 * </ol>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // habilita @PreAuthorize en controladores y servicios
public class SecurityConfig {

    /**
     * Cadena de filtros de seguridad: define las reglas de acceso.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {

        http
                // API REST stateless: no se usan sesiones ni formularios de login.
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults()) // usa el bean CorsConfigurationSource
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Reglas de autorizacion por ruta.
                .authorizeHttpRequests(auth -> auth
                        // El preflight de CORS (OPTIONS) siempre debe pasar.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Endpoints publicos y documentacion.
                        .requestMatchers(
                                "/api/public/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()
                        // Todo lo demas exige un token valido.
                        .anyRequest().authenticated()
                )

                // Activar la validacion de JWT y la conversion de roles.
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                );

        return http.build();
    }

    /**
     * Convierte el claim {@code realm_access.roles} del token de Keycloak
     * en autoridades {@code ROLE_*} reconocidas por Spring Security.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        return converter;
    }

    /**
     * Construye el decodificador de JWT.
     *
     * <p>Se usa explicitamente el {@code jwk-set-uri} (llaves publicas) en lugar
     * de solo el {@code issuer-uri}, porque asi la aplicacion no intenta
     * contactar a Keycloak al arrancar. Las llaves se descargan la primera vez
     * que llega un token. Ademas se valida el emisor ("iss") por seguridad.
     */
    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") String jwkSetUri,
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri) {

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

        // Valida firma, expiracion y que el emisor sea el esperado.
        OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer(issuerUri);
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(issuerValidator));

        return decoder;
    }
}
