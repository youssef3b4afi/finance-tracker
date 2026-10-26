package com.youssef3b4afi.financetracker;

import com.jayway.jsonpath.JsonPath;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Test helper: registers a brand-new user (random email, so tests never collide) and returns its token. */
public final class TestAuth {

    private TestAuth() {}

    public static String newUserToken(MockMvc mvc) throws Exception {
        String body = """
                {"email": "user-%s@example.com", "password": "secret-123"}
                """.formatted(UUID.randomUUID());

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        String response = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.token");
    }
}
