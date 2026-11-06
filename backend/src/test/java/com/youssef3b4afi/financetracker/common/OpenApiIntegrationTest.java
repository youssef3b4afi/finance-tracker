package com.youssef3b4afi.financetracker.common;

import com.youssef3b4afi.financetracker.TestcontainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The API docs are public, and the generated spec describes our endpoints and the JWT login. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
class OpenApiIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Test
    void specIsPublicAndListsEndpointsAndBearerAuth() throws Exception {
        mvc.perform(get("/api/docs/openapi.json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Finance Tracker API"))
                .andExpect(jsonPath("$.paths['/api/auth/login']").exists())
                .andExpect(jsonPath("$.paths['/api/transactions']").exists())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"));
    }

    @Test
    void swaggerUiIsPublic() throws Exception {
        // /api/docs is the address to remember; springdoc redirects it to the page next to it.
        mvc.perform(get("/api/docs"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/swagger-ui/index.html"));
        // The page, and the config it loads to find the spec, must open without a token.
        mvc.perform(get("/api/swagger-ui/index.html"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/docs/openapi.json/swagger-config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("/api/docs/openapi.json"));
    }
}
