package cl.ipt.siga.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Convierte los roles de Keycloak en "authorities" de Spring Security.
 *
 * <p>Keycloak guarda los roles de realm dentro del claim:
 * <pre>
 * {
 *   "realm_access": {
 *     "roles": ["ADMIN", "TEACHER"]
 *   }
 * }
 * </pre>
 *
 * <p>Spring Security, en cambio, espera autoridades con el prefijo {@code ROLE_}.
 * Por eso transformamos cada rol en {@code ROLE_<rol>}: asi el rol {@code ADMIN}
 * de Keycloak se puede exigir en el backend con {@code hasRole("ADMIN")}
 * (Spring agrega el prefijo automaticamente al usar {@code hasRole}).
 *
 * <p>Sin esta clase, el token seria valido pero el usuario no tendria ningun rol
 * reconocido por el backend (el error mas comun al integrar Keycloak).
 */
public class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String REALM_ACCESS = "realm_access";
    private static final String ROLES = "roles";

    @Override
    @SuppressWarnings("unchecked")
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap(REALM_ACCESS);
        if (realmAccess == null || realmAccess.get(ROLES) == null) {
            return Collections.emptyList();
        }

        List<String> roles = (List<String>) realmAccess.get(ROLES);
        return roles.stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }
}
