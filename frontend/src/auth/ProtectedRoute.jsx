import { Navigate } from 'react-router-dom';
import { hasRole } from './keycloak';

/**
 * Allows the route only when the authenticated user has at least one
 * of the required roles. Otherwise redirects to the unauthorized page.
 */
export default function ProtectedRoute({ roles, children }) {
  const allowed = roles.some((role) => hasRole(role));
  return allowed ? children : <Navigate to="/unauthorized" replace />;
}
