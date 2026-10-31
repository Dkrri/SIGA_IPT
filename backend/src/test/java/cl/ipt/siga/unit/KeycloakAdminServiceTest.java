package cl.ipt.siga.unit;

import cl.ipt.siga.exceptions.BusinessException;
import cl.ipt.siga.services.KeycloakAdminService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * KeycloakAdminService talks to the Keycloak Admin REST API.
 * A tiny local HTTP server simulates Keycloak so no external instance is needed.
 */
class KeycloakAdminServiceTest {

    private HttpServer server;
    private KeycloakAdminService service;
    private final AtomicInteger userCreations = new AtomicInteger();
    private final AtomicInteger roleAssignments = new AtomicInteger();
    private final AtomicInteger userDeletions = new AtomicInteger();

    private volatile int usersStatus = 201;
    private volatile int roleStatus = 200;
    private volatile int tokenStatus = 200;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/realms/siga/protocol/openid-connect/token", exchange -> {
            if (tokenStatus != 200) {
                respond(exchange, tokenStatus, "{}");
                return;
            }
            respond(exchange, 200, "{\"access_token\":\"test-token\"}");
        });
        server.createContext("/admin/realms/siga/users", exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                userCreations.incrementAndGet();
                if (usersStatus == 409) {
                    respond(exchange, 409, "{\"error\":\"conflict\"}");
                    return;
                }
                exchange.getResponseHeaders().add("Location",
                        "http://localhost:" + server.getAddress().getPort()
                                + "/admin/realms/siga/users/user-1");
                exchange.sendResponseHeaders(201, -1);
                exchange.close();
                return;
            }
            respond(exchange, 404, "{}");
        });
        server.createContext("/admin/realms/siga/users/user-1/role-mappings/realm", exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                roleAssignments.incrementAndGet();
                respond(exchange, 204, "");
                return;
            }
            respond(exchange, 404, "{}");
        });
        server.createContext("/admin/realms/siga/users/user-1", exchange -> {
            if ("DELETE".equals(exchange.getRequestMethod())) {
                userDeletions.incrementAndGet();
                respond(exchange, 204, "");
                return;
            }
            respond(exchange, 404, "{}");
        });
        server.createContext("/admin/realms/siga/roles/STUDENT", exchange -> {
            if (roleStatus != 200) {
                respond(exchange, roleStatus, "{}");
                return;
            }
            respond(exchange, 200, "{\"id\":\"role-1\",\"name\":\"STUDENT\"}");
        });
        server.start();

        service = new KeycloakAdminService();
        ReflectionTestUtils.setField(service, "baseUrl",
                "http://localhost:" + server.getAddress().getPort());
        ReflectionTestUtils.setField(service, "realm", "siga");
        ReflectionTestUtils.setField(service, "clientId", "siga-admin");
        ReflectionTestUtils.setField(service, "clientSecret", "siga-admin-secret");
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    @Test
    void createUserAndAssignRole() {
        service.createUser("new@test.cl", "New", "User", "Temp12345", "STUDENT");

        org.junit.jupiter.api.Assertions.assertEquals(1, userCreations.get());
        org.junit.jupiter.api.Assertions.assertEquals(1, roleAssignments.get());
        org.junit.jupiter.api.Assertions.assertEquals(0, userDeletions.get());
    }

    @Test
    void rejectDuplicateUser() {
        usersStatus = 409;

        assertThrows(BusinessException.class,
                () -> service.createUser("new@test.cl", "New", "User", "Temp12345", "STUDENT"));
    }

    @Test
    void rejectWhenTokenCannotBeObtained() {
        tokenStatus = 500;

        assertThrows(BusinessException.class,
                () -> service.createUser("new@test.cl", "New", "User", "Temp12345", "STUDENT"));
    }

    @Test
    void deleteUserWhenRoleAssignmentFails() {
        roleStatus = 500;

        assertThrows(BusinessException.class,
                () -> service.createUser("new@test.cl", "New", "User", "Temp12345", "STUDENT"));
        org.junit.jupiter.api.Assertions.assertEquals(1, userCreations.get());
        org.junit.jupiter.api.Assertions.assertEquals(1, userDeletions.get());
    }
}
