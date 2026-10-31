import Keycloak from 'keycloak-js';

/**
 * Single Keycloak instance for the whole application.
 * The backend validates the JWT issued by this realm.
 */
const keycloak = new Keycloak({
  url: import.meta.env.VITE_KEYCLOAK_URL,
  realm: import.meta.env.VITE_KEYCLOAK_REALM,
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID,
});

export function getRoles() {
  return keycloak.realmAccess?.roles ?? [];
}

export function hasRole(role) {
  return getRoles().includes(role);
}

/** Returns the main application role, or null when the user has none. */
export function getPrimaryRole() {
  const roles = getRoles();
  if (roles.includes('ADMIN')) return 'ADMIN';
  if (roles.includes('TEACHER')) return 'TEACHER';
  if (roles.includes('STUDENT')) return 'STUDENT';
  return null;
}

/**
 * Human-friendly name for greetings.
 * Prefers the full name from the token, then the email prefix.
 */
export function getDisplayName() {
  const token = keycloak.tokenParsed ?? {};
  const fullName = [token.given_name, token.family_name]
    .filter(Boolean)
    .join(' ');
  return (
    fullName ||
    token.name ||
    token.preferred_username?.split('@')[0] ||
    ''
  );
}

export default keycloak;
