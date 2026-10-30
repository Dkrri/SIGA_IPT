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
 * Converts Keycloak roles into Spring Security "authorities".
 *
 * <p>Keycloak stores realm roles inside the claim:
 * <pre>
 * {
 *   "realm_access": {
 *     "roles": ["ADMIN", "TEACHER"]
 *   }
 * }
 * </pre>
 *
 * <p>Spring Security expects authorities prefixed with {@code ROLE_}.
 * Therefore each role is transformed into {@code ROLE_<role>}: this way the
 * Keycloak {@code ADMIN} role can be required in the backend with
 * {@code hasRole("ADMIN")} (Spring adds the prefix automatically).
 *
 * <p>Without this class, the token would be valid but the user would have no
 * role recognized by the backend (the most common mistake when integrating Keycloak).
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
