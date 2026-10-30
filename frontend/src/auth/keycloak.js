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

export default keycloak;
