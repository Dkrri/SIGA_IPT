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
 * Central security configuration of the backend.
 *
 * <p>The backend acts as an <b>OAuth2 Resource Server</b>: it does not issue tokens
 * or store passwords; it only <b>validates</b> the JWT tokens issued by Keycloak.
 *
 * <p>Responsibilities of this class:
 * <ol>
 *   <li>Define which endpoints are public and which require authentication.</li>
 *   <li>Configure the backend as a resource server (JWT validation).</li>
 *   <li>Translate token roles into Spring authorities (see
 *       {@link KeycloakRealmRoleConverter}).</li>
 *   <li>Build the {@link JwtDecoder} used to verify the signature and the issuer.</li>
 * </ol>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // enables @PreAuthorize in controllers and services
public class SecurityConfig {

    /**
     * Security filter chain: defines the access rules.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {

        http
                // Stateless REST API: no sessions and no login forms.
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults()) // uses the CorsConfigurationSource bean
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Authorization rules per route.
                .authorizeHttpRequests(auth -> auth
                        // The CORS preflight (OPTIONS) must always pass.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Public endpoints and documentation.
                        .requestMatchers(
                                "/api/public/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()
                        // Every other request requires a valid token.
                        .anyRequest().authenticated()
                )

                // Enable JWT validation and role conversion.
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                );

        return http.build();
    }

    /**
     * Converts the Keycloak {@code realm_access.roles} claim into
     * {@code ROLE_*} authorities recognized by Spring Security.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        return converter;
    }

    /**
     * Builds the JWT decoder.
     *
     * <p>The {@code jwk-set-uri} (public keys) is used explicitly instead of
     * only {@code issuer-uri}, because this way the application does not
     * contact Keycloak on startup. The keys are downloaded the first time
     * a token arrives. The issuer ("iss") is also validated for security.
     */
    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") String jwkSetUri,
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri) {

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

        // Validate signature, expiration and that the issuer is the expected one.
        OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer(issuerUri);
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(issuerValidator));

        return decoder;
    }
}
