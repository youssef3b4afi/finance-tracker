package com.youssef3b4afi.financetracker.dashboard;

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

import java.time.YearMonth;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Runs the real SUM/GROUP BY queries against Postgres. Every test user gets the same small ledger:
 *   2026-09  Salary 1000.00                         (outside October)
 *   2026-10  Salary 2000.00, Food 30.00 + 20.00, Rent 800.00
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
class DashboardIntegrationTest {

    @Autowired
    MockMvc mvc;

    String token;

    @BeforeEach
    void setUp() throws Exception {
        token = TestAuth.newUserToken(mvc);
        long salary = categoryId(token, "Salary");
        long food = categoryId(token, "Food");
        long rent = categoryId(token, "Rent");

        createTransaction(token, salary, "1000.00", "2026-09-30");
        createTransaction(token, salary, "2000.00", "2026-10-01");
        createTransaction(token, food, "30.00", "2026-10-05");
        createTransaction(token, food, "20.00", "2026-10-31");
        createTransaction(token, rent, "800.00", "2026-10-15");
    }

    @Test
    void summaryTotalsOneMonth() throws Exception {
        getJson(token, "/api/dashboard/summary?month=2026-10")
                .andExpect(jsonPath("$.month").value("2026-10"))
                .andExpect(jsonPath("$.income").value(2000.00))
                .andExpect(jsonPath("$.expenses").value(850.00))
                .andExpect(jsonPath("$.balance").value(1150.00));
    }

    @Test
    void summaryOfEmptyMonthIsZero() throws Exception {
        getJson(token, "/api/dashboard/summary?month=2025-01")
                .andExpect(jsonPath("$.income").value(0))
                .andExpect(jsonPath("$.expenses").value(0))
                .andExpect(jsonPath("$.balance").value(0));
    }

    @Test
    void summaryDefaultsToCurrentMonth() throws Exception {
        getJson(token, "/api/dashboard/summary")
                .andExpect(jsonPath("$.month").value(YearMonth.now().toString()));
    }

    @Test
    void expensesByCategoryIsBiggestFirstAndExcludesIncome() throws Exception {
        getJson(token, "/api/dashboard/expenses-by-category?month=2026-10")
                .andExpect(jsonPath("$[*].categoryName", contains("Rent", "Food")))
                .andExpect(jsonPath("$[1].total").value(50.00));
    }

    @Test
    void monthlyFillsEmptyMonthsOldestFirst() throws Exception {
        getJson(token, "/api/dashboard/monthly?month=2026-10&months=3")
                .andExpect(jsonPath("$[*].month", contains("2026-08", "2026-09", "2026-10")))
                .andExpect(jsonPath("$[0].income").value(0))
                .andExpect(jsonPath("$[1].income").value(1000.00))
                .andExpect(jsonPath("$[1].expenses").value(0))
                .andExpect(jsonPath("$[2].income").value(2000.00))
                .andExpect(jsonPath("$[2].expenses").value(850.00));
    }

    @Test
    void monthlyClampsMonthCount() throws Exception {
        getJson(token, "/api/dashboard/monthly?months=1000").andExpect(jsonPath("$", hasSize(24)));
    }

    @Test
    void rejectsBadMonth() throws Exception {
        mvc.perform(get("/api/dashboard/summary?month=October").header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Invalid value for 'month'"));
    }

    /** US-3: another user's dashboard never includes these transactions. */
    @Test
    void otherUsersTransactionsAreNotCounted() throws Exception {
        String bob = TestAuth.newUserToken(mvc);

        getJson(bob, "/api/dashboard/summary?month=2026-10")
                .andExpect(jsonPath("$.income").value(0))
                .andExpect(jsonPath("$.expenses").value(0));
        getJson(bob, "/api/dashboard/expenses-by-category?month=2026-10").andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void requiresToken() throws Exception {
        mvc.perform(get("/api/dashboard/summary")).andExpect(status().isUnauthorized());
    }

    // --- helpers ---------------------------------------------------------------

    private ResultActions getJson(String token, String url) throws Exception {
        return mvc.perform(get(url).header("Authorization", "Bearer " + token)).andExpect(status().isOk());
    }

    private void createTransaction(String token, long categoryId, String amount, String date) throws Exception {
        mvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoryId": %d, "amount": %s, "date": "%s"}
                                """.formatted(categoryId, amount, date)))
                .andExpect(status().isCreated());
    }

    /** Looks up the id of one of the user's default categories by name. */
    private long categoryId(String token, String name) throws Exception {
        String body = mvc.perform(get("/api/categories").header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        List<Number> ids = JsonPath.read(body, "$[?(@.name == '" + name + "')].id");
        return ids.getFirst().longValue();
    }
}
