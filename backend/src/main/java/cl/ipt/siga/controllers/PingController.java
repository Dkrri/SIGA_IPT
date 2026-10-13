package cl.ipt.siga.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador minimo para verificar que la seguridad funciona.
 * (En el Dia 2 del plan se valida el Hito 0 con estos endpoints.)
 */
@RestController
@RequestMapping("/api")
public class PingController {

    /** Publico: no requiere token. */
    @GetMapping("/public/ping")
    public Map<String, Object> publicPing() {
        Map<String, Object> body = new HashMap<>();
        body.put("status", "ok");
        body.put("scope", "public");
        body.put("message", "Endpoint publico: no requiere token");
        return body;
    }

    /** Requiere token valido (cualquier rol). Devuelve datos del usuario del token. */
    @GetMapping("/ping")
    public Map<String, Object> privatePing(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", "ok");
        body.put("scope", "authenticated");
        body.put("subject", jwt.getSubject());
        body.put("username", jwt.getClaimAsString("preferred_username"));
        body.put("roles", jwt.getClaimAsMap("realm_access"));
        return body;
    }

    /** Solo ADMIN: demuestra la verificacion de roles con @PreAuthorize. */
    @GetMapping("/admin/ping")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> adminPing() {
        Map<String, Object> body = new HashMap<>();
        body.put("status", "ok");
        body.put("scope", "admin");
        body.put("message", "Solo accesible con rol ADMIN");
        return body;
    }
}
