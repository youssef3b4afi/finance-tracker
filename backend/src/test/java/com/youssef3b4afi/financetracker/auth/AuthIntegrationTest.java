package com.youssef3b4afi.financetracker.auth;

import com.jayway.jsonpath.JsonPath;
import com.youssef3b4afi.financetracker.TestcontainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full app + real Postgres (Testcontainers). MockMvc sends requests through the whole
 * stack — security filters, controller, service, database — without opening a network port.
 * The database is shared between tests, so each test uses its own email address.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
class AuthIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Test
    void registerCreatesUserWithNormalizedEmail() throws Exception {
        register("  New.User@Example.COM ", "secret123")
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/auth/me"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("new.user@example.com"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void registerRejectsDuplicateEmailIgnoringCase() throws Exception {
        register("dup@example.com", "secret123").andExpect(status().isCreated());

        register("DUP@example.com", "other-secret")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Email is already registered"));
    }

    @Test
    void registerRejectsInvalidInputWithFieldErrors() throws Exception {
        register("not-an-email", "short")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void loginFailsWithWrongPassword() throws Exception {
        register("wrongpw@example.com", "secret123");

        login("wrongpw@example.com", "not-the-password")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid email or password"));
    }

    @Test
    void loginFailsForUnknownEmailWithSameMessage() throws Exception {
        login("nobody@example.com", "secret123")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid email or password"));
    }

    @Test
    void meRequiresToken() throws Exception {
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void meRejectsTamperedToken() throws Exception {
        register("tamper@example.com", "secret123");
        String token = tokenFor("tamper@example.com", "secret123");

        // Change one character of the signature: the token must no longer be accepted.
        String last = token.substring(token.length() - 1);
        String tampered = token.substring(0, token.length() - 1) + (last.equals("A") ? "B" : "A");

        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + tampered))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerLoginAndMeFlow() throws Exception {
        register("flow@example.com", "secret123").andExpect(status().isCreated());
        String token = tokenFor("flow@example.com", "secret123");

        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("flow@example.com"));
    }

    // --- helpers ---------------------------------------------------------------

    private ResultActions register(String email, String password) throws Exception {
        return mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(credentials(email, password)));
    }

    private ResultActions login(String email, String password) throws Exception {
        return mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(credentials(email, password)));
    }

    private String tokenFor(String email, String password) throws Exception {
        String body = login(email, password)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    private static String credentials(String email, String password) {
        return """
                {"email": "%s", "password": "%s"}
                """.formatted(email, password);
    }
}
