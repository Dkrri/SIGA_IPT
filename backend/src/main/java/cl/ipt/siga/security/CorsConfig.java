package cl.ipt.siga.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS (Cross-Origin Resource Sharing) configuration.
 *
 * <p>By default, the browser blocks requests from the frontend (for example
 * http://localhost:5173) to the backend (http://localhost:8080) because they
 * are different origins. CORS tells the browser that those origins are
 * authorized.
 *
 * <p>Allowed origins are read from the environment variable
 * {@code CORS_ALLOWED_ORIGINS} (defaults to the development frontend on
 * port 5173). {@code *} is never used because credentials (the token) are sent.
 */
@Configuration
public class CorsConfig {

    @Value("${siga.security.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
