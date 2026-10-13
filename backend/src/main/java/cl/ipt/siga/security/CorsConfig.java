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
 * Configuracion de CORS (Cross-Origin Resource Sharing).
 *
 * <p>El navegador bloquea por defecto las peticiones que el frontend (por ejemplo
 * http://localhost:5173) hace al backend (http://localhost:8080) porque son
 * origenes distintos. CORS le indica al navegador que esos origenes estan
 * autorizados.
 *
 * <p>Los origenes permitidos se leen desde la variable de entorno
 * {@code CORS_ALLOWED_ORIGINS} (por defecto el frontend de desarrollo en el
 * puerto 5173). Nunca se usa {@code *} porque se envian credenciales (token).
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
