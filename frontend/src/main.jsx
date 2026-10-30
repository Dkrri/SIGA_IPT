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
      <div style={{ fontFamily: 'sans-serif', padding: 24 }}>
        <h2>Could not connect to Keycloak</h2>
        <p>{error?.message ?? String(error)}</p>
        <p>Check that Keycloak is running on http://localhost:8081.</p>
      </div>
    );
  });
