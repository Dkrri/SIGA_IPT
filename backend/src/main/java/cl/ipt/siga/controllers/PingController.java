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
 * Minimal controller to verify that security works.
 * (Milestone 0 is validated with these endpoints on Day 2 of the plan.)
 */
@RestController
@RequestMapping("/api")
public class PingController {

    /** Public: no token required. */
    @GetMapping("/public/ping")
    public Map<String, Object> publicPing() {
        Map<String, Object> body = new HashMap<>();
        body.put("status", "ok");
        body.put("scope", "public");
        body.put("message", "Public endpoint: no token required");
        return body;
    }

    /** Requires a valid token (any role). Returns data from the token. */
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

    /** ADMIN only: demonstrates role validation with @PreAuthorize. */
    @GetMapping("/admin/ping")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> adminPing() {
        Map<String, Object> body = new HashMap<>();
        body.put("status", "ok");
        body.put("scope", "admin");
        body.put("message", "Accessible only with ADMIN role");
        return body;
    }
}
