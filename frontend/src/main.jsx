import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import App from './App';
import keycloak from './auth/keycloak';
import './index.css';

const root = createRoot(document.getElementById('root'));

/**
 * Keycloak initializes BEFORE rendering React.
 * - login-required: redirects to Keycloak when there is no session.
 * - S256: PKCE, required by the public "siga-frontend" client.
 */
keycloak
  .init({ onLoad: 'login-required', pkceMethod: 'S256' })
  .then(() => {
    root.render(
      <StrictMode>
        <App />
      </StrictMode>
    );
  })
  .catch((error) => {
    root.render(
      <div style={{ fontFamily: 'Inter, Roboto, Arial, sans-serif', padding: 32 }}>
        <h2>No se pudo conectar con Keycloak</h2>
        <p>Verifica que Keycloak esté corriendo en http://localhost:8081.</p>
        <p>Detalle: {error?.message ?? String(error)}</p>
      </div>
    );
  });
