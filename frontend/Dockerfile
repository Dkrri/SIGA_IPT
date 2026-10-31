# ------------------------------------------------------------------
# Build stage: compile the React/Vite application
# ------------------------------------------------------------------
FROM node:22-alpine AS build

WORKDIR /app

# Install dependencies using the lock file.
COPY package.json package-lock.json ./
RUN npm ci

# Vite embeds VITE_* variables at build time.
ARG VITE_KEYCLOAK_URL=http://localhost:8081
ARG VITE_KEYCLOAK_REALM=siga
ARG VITE_KEYCLOAK_CLIENT_ID=siga-frontend
ENV VITE_KEYCLOAK_URL=$VITE_KEYCLOAK_URL
ENV VITE_KEYCLOAK_REALM=$VITE_KEYCLOAK_REALM
ENV VITE_KEYCLOAK_CLIENT_ID=$VITE_KEYCLOAK_CLIENT_ID

COPY . .
RUN npm run build

# ------------------------------------------------------------------
# Runtime stage: serve the static bundle with Nginx
# ------------------------------------------------------------------
FROM nginx:1.27-alpine

COPY nginx.default.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD wget -q -O /dev/null http://127.0.0.1/ || exit 1

CMD ["nginx", "-g", "daemon off;"]
