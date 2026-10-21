package com.youssef3b4afi.financetracker.category;

import com.jayway.jsonpath.JsonPath;
import com.youssef3b4afi.financetracker.TestAuth;
import com.youssef3b4afi.financetracker.TestcontainersConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
class CategoryIntegrationTest {

    @Autowired
    MockMvc mvc;

    /** A fresh user for every test, so tests don't see each other's categories. */
    String token;

    @BeforeEach
    void setUp() throws Exception {
        token = TestAuth.newUserToken(mvc);
    }

    @Test
    void newUserHasDefaultCategories() throws Exception {
        mvc.perform(get("/api/categories").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(9)))
                .andExpect(jsonPath("$[?(@.name == 'Salary')].type", hasItem("INCOME")))
                .andExpect(jsonPath("$[?(@.name == 'Food')].type", hasItem("EXPENSE")));
    }

    @Test
    void createReturns201WithLocation() throws Exception {
        create(token, "  Pets ", "EXPENSE")
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.name").value("Pets"))
                .andExpect(jsonPath("$.type").value("EXPENSE"));
    }

    @Test
    void createRejectsDuplicateNameIgnoringCase() throws Exception {
        create(token, "food", "EXPENSE").andExpect(status().isConflict());
    }

    @Test
    void createRejectsMissingFields() throws Exception {
        create(token, " ", null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.type").exists());
    }

    @Test
    void createRejectsUnknownType() throws Exception {
        create(token, "Pets", "SPENDING")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Malformed request body"));
    }

    @Test
    void renameAllowsChangingOnlyLetterCase() throws Exception {
        rename(token, idOf(token, "Food"), "FOOD")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("FOOD"));
    }

    @Test
    void renameRejectsNameOfAnotherCategory() throws Exception {
        rename(token, idOf(token, "Food"), "rent").andExpect(status().isConflict());
    }

    @Test
    void deleteRemovesCategory() throws Exception {
        long id = idOf(token, "Shopping");

        mvc.perform(delete("/api/categories/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/categories/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    /** US-3: another user's category behaves exactly like one that doesn't exist, and stays untouched. */
    @Test
    void otherUsersCategoryIsNotFound() throws Exception {
        long aliceFood = idOf(token, "Food");
        String bob = TestAuth.newUserToken(mvc);

        mvc.perform(get("/api/categories/" + aliceFood).header("Authorization", "Bearer " + bob))
                .andExpect(status().isNotFound());
        rename(bob, aliceFood, "Hacked").andExpect(status().isNotFound());
        mvc.perform(delete("/api/categories/" + aliceFood).header("Authorization", "Bearer " + bob))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/categories/" + aliceFood).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Food"));
    }

    @Test
    void requiresToken() throws Exception {
        mvc.perform(get("/api/categories")).andExpect(status().isUnauthorized());
    }

    // --- helpers ---------------------------------------------------------------

    private ResultActions create(String token, String name, String type) throws Exception {
        String typeJson = type == null ? "null" : "\"" + type + "\"";
        return mvc.perform(post("/api/categories")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "%s", "type": %s}
                        """.formatted(name, typeJson)));
    }

    private ResultActions rename(String token, long id, String name) throws Exception {
        return mvc.perform(put("/api/categories/" + id)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "%s"}
                        """.formatted(name)));
    }

    /** Looks up the id of one of the user's categories by name, e.g. a default like "Food". */
    private long idOf(String token, String name) throws Exception {
        String body = mvc.perform(get("/api/categories").header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        List<Number> ids = JsonPath.read(body, "$[?(@.name == '" + name + "')].id");
        return ids.getFirst().longValue();
    }
}
