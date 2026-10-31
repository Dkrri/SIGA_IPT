import axios from 'axios';
import keycloak from './auth/keycloak';
import { translateErrorMessage } from './ui/errorMessages';

/**
 * Axios instance used by every service.
 *
 * baseURL is intentionally empty: services call "/api/..." and
 * Vite (development) or Nginx (production) proxies that prefix
 * to the Spring Boot backend.
 */
const http = axios.create({
  headers: { 'Content-Type': 'application/json' },
});

/**
 * Before each request:
 *  - refresh the token when it is about to expire;
 *  - attach the access token as a Bearer header.
 */
http.interceptors.request.use(async (config) => {
  if (keycloak.authenticated) {
    await keycloak.updateToken(30);
    config.headers.Authorization = `Bearer ${keycloak.token}`;
  }
  return config;
});

/** On 401, force a new login. */
http.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      keycloak.login();
    }
    return Promise.reject(error);
  }
);

/** Reads the message from the backend ApiErrorResponse and translates it to Spanish. */
export function getErrorMessage(error) {
  const message =
    error?.response?.data?.message ??
    error?.message ??
    'Unexpected error';
  return translateErrorMessage(message);
}

export default http;
