#!/bin/bash
set -e

# Create the application user/database and a separate Keycloak database.
mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" <<-EOSQL
    CREATE DATABASE IF NOT EXISTS \`${DB_NAME}\`
        CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

    CREATE USER IF NOT EXISTS '${DB_USER}'@'%'
        IDENTIFIED BY '${DB_PASSWORD}';
    GRANT ALL PRIVILEGES ON \`${DB_NAME}\`.* TO '${DB_USER}'@'%';

    CREATE DATABASE IF NOT EXISTS \`keycloak\`
        CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

    CREATE USER IF NOT EXISTS '${KEYCLOAK_DB_USER}'@'%'
        IDENTIFIED BY '${KEYCLOAK_DB_PASSWORD}';
    GRANT ALL PRIVILEGES ON \`keycloak\`.* TO '${KEYCLOAK_DB_USER}'@'%';

    FLUSH PRIVILEGES;
EOSQL
