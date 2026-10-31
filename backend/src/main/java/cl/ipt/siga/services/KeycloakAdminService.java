package cl.ipt.siga.services;

import cl.ipt.siga.exceptions.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Provisions Keycloak users through the Admin REST API.
 *
 * <p>The backend authenticates with the confidential service account
 * {@code siga-admin}; it never uses the Keycloak administrator credentials.
 * This service only creates users and assigns their application role.
 */
@Service
public class KeycloakAdminService {

    @Value("${siga.keycloak.base-url:http://localhost:8081}")
    private String baseUrl;

    @Value("${siga.keycloak.realm:siga}")
    private String realm;

    @Value("${siga.keycloak.admin-client-id:siga-admin}")
    private String clientId;

    @Value("${siga.keycloak.admin-client-secret:siga-admin-secret}")
    private String clientSecret;

    /**
     * Creates a Keycloak user with a temporary password and the given realm role.
     * Keycloak will force the user to update the password on first login.
     */
    public void createUser(String email,
                           String firstName,
                           String lastName,
                           String temporaryPassword,
                           String roleName) {
        String token;
        try {
            token = getAccessToken();
        } catch (BusinessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new BusinessException(
                    "Could not authenticate with the Keycloak Admin API: "
                            + ex.getMessage());
        }

        String userId = null;

        try {
            userId = createKeycloakUser(token, email, firstName, lastName, temporaryPassword);
            assignRealmRole(token, userId, roleName);
        } catch (RestClientResponseException ex) {
            if (userId != null) {
                deleteUserQuietly(token, userId);
            }
            if (ex.getStatusCode().value() == 409) {
                throw new BusinessException(
                        "A Keycloak user already exists with email " + email);
            }
            throw new BusinessException(
                    "Could not provision the Keycloak user: " + ex.getStatusCode());
        } catch (BusinessException ex) {
            if (userId != null) {
                deleteUserQuietly(token, userId);
            }
            throw ex;
        } catch (RuntimeException ex) {
            if (userId != null) {
                deleteUserQuietly(token, userId);
            }
            throw new BusinessException(
                    "Could not provision the Keycloak user: " + ex.getMessage());
        }
    }

    private String getAccessToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);

        Map<String, Object> response = client()
                .post()
                .uri("/realms/{realm}/protocol/openid-connect/token", realm)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        Object token = response == null ? null : response.get("access_token");
        if (token == null) {
            throw new BusinessException("Keycloak did not return an access token");
        }
        return token.toString();
    }

    private String createKeycloakUser(String token,
                                      String email,
                                      String firstName,
                                      String lastName,
                                      String temporaryPassword) {
        Map<String, Object> body = Map.of(
                "username", email,
                "email", email,
                "firstName", firstName,
                "lastName", lastName,
                "enabled", true,
                "emailVerified", true,
                "requiredActions", List.of("UPDATE_PASSWORD"),
                "credentials", List.of(Map.of(
                        "type", "password",
                        "value", temporaryPassword,
                        "temporary", true
                ))
        );

        ResponseEntity<Void> response = client()
                .post()
                .uri("/admin/realms/{realm}/users", realm)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();

        URI location = response.getHeaders().getLocation();
        if (location == null) {
            throw new BusinessException("Keycloak did not return the new user location");
        }
        String path = location.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    private void assignRealmRole(String token, String userId, String roleName) {
        Map<String, Object> role = client()
                .get()
                .uri("/admin/realms/{realm}/roles/{role}", realm, roleName)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        if (role == null) {
            throw new BusinessException("Keycloak role not found: " + roleName);
        }

        client()
                .post()
                .uri("/admin/realms/{realm}/users/{userId}/role-mappings/realm",
                        realm, userId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(List.of(role))
                .retrieve()
                .toBodilessEntity();
    }

    private void deleteUserQuietly(String token, String userId) {
        try {
            client()
                    .delete()
                    .uri("/admin/realms/{realm}/users/{userId}", realm, userId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RuntimeException ignored) {
            // Best-effort compensation: the database transaction will roll back.
        }
    }

    private RestClient client() {
        return RestClient.builder().baseUrl(baseUrl).build();
    }
}
