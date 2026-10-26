package com.youssef3b4afi.financetracker.transaction;

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

import static org.hamcrest.Matchers.contains;
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
class TransactionIntegrationTest {

    @Autowired
    MockMvc mvc;

    /** A fresh user for every test, so tests don't see each other's transactions. */
    String token;
    long food;
    long salary;

    @BeforeEach
    void setUp() throws Exception {
        token = TestAuth.newUserToken(mvc);
        food = categoryId(token, "Food");
        salary = categoryId(token, "Salary");
    }

    @Test
    void createReturns201WithCategoryAndType() throws Exception {
        create(token, food, "12.50", "2026-10-05", "  Lunch ")
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.amount").value(12.50))
                .andExpect(jsonPath("$.date").value("2026-10-05"))
                .andExpect(jsonPath("$.note").value("Lunch"))
                .andExpect(jsonPath("$.categoryName").value("Food"))
                .andExpect(jsonPath("$.type").value("EXPENSE"));
    }

    @Test
    void createRejectsInvalidFields() throws Exception {
        create(token, null, "-5", null, null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.categoryId").exists())
                .andExpect(jsonPath("$.errors.amount").exists())
                .andExpect(jsonPath("$.errors.date").exists());
    }

    @Test
    void createRejectsMoreThanTwoDecimals() throws Exception {
        create(token, food, "1.234", "2026-10-05", null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.amount").exists());
    }

    @Test
    void updateChangesAllFields() throws Exception {
        long id = idOf(create(token, food, "10.00", "2026-10-01", "old"));

        update(token, id, salary, "2500.00", "2026-10-02", "October pay")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(2500.00))
                .andExpect(jsonPath("$.date").value("2026-10-02"))
                .andExpect(jsonPath("$.note").value("October pay"))
                .andExpect(jsonPath("$.type").value("INCOME"));
    }

    @Test
    void deleteRemovesTransaction() throws Exception {
        long id = idOf(create(token, food, "10.00", "2026-10-01", null));

        mvc.perform(delete("/api/transactions/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/transactions/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void listIsNewestFirstAndFiltersByDateCategoryAndType() throws Exception {
        create(token, food, "1.00", "2026-09-30", "september");
        create(token, food, "2.00", "2026-10-10", "food");
        create(token, salary, "3.00", "2026-10-20", "salary");

        list(token, "")
                .andExpect(jsonPath("$.content[*].note", contains("salary", "food", "september")));
        list(token, "?from=2026-10-01&to=2026-10-31")
                .andExpect(jsonPath("$.content[*].note", contains("salary", "food")));
        list(token, "?categoryId=" + food)
                .andExpect(jsonPath("$.content[*].note", contains("food", "september")));
        list(token, "?type=INCOME")
                .andExpect(jsonPath("$.content[*].note", contains("salary")));
    }

    @Test
    void listIsPaged() throws Exception {
        for (int day = 1; day <= 3; day++) {
            create(token, food, "1.00", "2026-10-0" + day, "day " + day);
        }

        list(token, "?page=1&size=2")
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].note").value("day 1"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void listRejectsBadFilterValue() throws Exception {
        mvc.perform(get("/api/transactions?type=SPENDING").header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Invalid value for 'type'"));
    }

    /** US-3: another user's transaction behaves like a missing one, and another user's category can't be used. */
    @Test
    void otherUsersDataIsNotFound() throws Exception {
        long aliceLunch = idOf(create(token, food, "12.50", "2026-10-05", "Lunch"));
        String bob = TestAuth.newUserToken(mvc);
        long bobFood = categoryId(bob, "Food");

        mvc.perform(get("/api/transactions/" + aliceLunch).header("Authorization", "Bearer " + bob))
                .andExpect(status().isNotFound());
        update(bob, aliceLunch, bobFood, "1.00", "2026-10-05", "hacked").andExpect(status().isNotFound());
        mvc.perform(delete("/api/transactions/" + aliceLunch).header("Authorization", "Bearer " + bob))
                .andExpect(status().isNotFound());
        create(bob, food, "1.00", "2026-10-05", null).andExpect(status().isNotFound());
        list(bob, "").andExpect(jsonPath("$.totalElements").value(0));

        mvc.perform(get("/api/transactions/" + aliceLunch).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note").value("Lunch"));
    }

    @Test
    void requiresToken() throws Exception {
        mvc.perform(get("/api/transactions")).andExpect(status().isUnauthorized());
    }

    // --- helpers ---------------------------------------------------------------

    private ResultActions create(String token, Long categoryId, String amount, String date, String note)
            throws Exception {
        return mvc.perform(post("/api/transactions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(categoryId, amount, date, note)));
    }

    private ResultActions update(String token, long id, Long categoryId, String amount, String date, String note)
            throws Exception {
        return mvc.perform(put("/api/transactions/" + id)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(categoryId, amount, date, note)));
    }

    private ResultActions list(String token, String query) throws Exception {
        return mvc.perform(get("/api/transactions" + query).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    /** null values become JSON null; the amount is written unquoted so it arrives as a number. */
    private static String body(Long categoryId, String amount, String date, String note) {
        return """
                {"categoryId": %s, "amount": %s, "date": %s, "note": %s}
                """.formatted(categoryId, amount, quoted(date), quoted(note));
    }

    private static String quoted(String value) {
        return value == null ? "null" : "\"" + value + "\"";
    }

    private static long idOf(ResultActions created) throws Exception {
        String body = created.andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    /** Looks up the id of one of the user's default categories by name. */
    private long categoryId(String token, String name) throws Exception {
        String body = mvc.perform(get("/api/categories").header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        List<Number> ids = JsonPath.read(body, "$[?(@.name == '" + name + "')].id");
        return ids.getFirst().longValue();
    }
}
