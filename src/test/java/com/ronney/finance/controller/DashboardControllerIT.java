package com.ronney.finance.controller;

import com.ronney.finance.BaseIntegrationTest;
import com.ronney.finance.domain.entity.Category;
import com.ronney.finance.domain.entity.SubCategory;
import com.ronney.finance.repository.CategoryRepository;
import com.ronney.finance.repository.RecurringTransactionRepository;
import com.ronney.finance.repository.SubCategoryRepository;
import com.ronney.finance.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DashboardControllerIT extends BaseIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SubCategoryRepository subCategoryRepository;

    @Autowired
    private RecurringTransactionRepository recurringTransactionRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void cleanTestData() {
        recurringTransactionRepository.deleteAll();
        transactionRepository.deleteAll();
    }

    private void createIncome(
            String token,
            int amount
    ) throws Exception {
        Category category =
                categoryRepository
                        .findByName("Receita")
                        .orElseThrow();
        SubCategory subCategory =
                subCategoryRepository
                        .findByName("Salário")
                        .orElseThrow();
        String body = """
            {
                "description":"Salário",
                "amount":%d,
                "transactionDate":"2026-07-01",
                "type":"INCOME",
                "paymentMethod":"BANK_TRANSFER",
                "categoryId":"%s",
                "subCategoryId":"%s"
            }
            """.formatted(
                amount,
                category.getId(),
                subCategory.getId()
        );
        mockMvc.perform(
                        post("/api/v1/transactions")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(body)
                )
                .andExpect(status().isCreated());
    }

    private void createRecurringIncome(
            String token,
            int amount
    ) throws Exception {

        Category category = categoryRepository
                .findByName("Receita")
                .orElseThrow();

        SubCategory subCategory = subCategoryRepository
                .findByName("Salário")
                .orElseThrow();

        String body = """
            {
                "description":"Salário recorrente",
                "amount":%d,
                "type":"INCOME",
                "paymentMethod":"BANK_TRANSFER",
                "dayOfMonth":5,
                "startDate":"2026-07-01",
                "endDate":null,
                "categoryId":"%s",
                "subCategoryId":"%s"
            }
            """.formatted(
                amount,
                category.getId(),
                subCategory.getId()
        );

        mockMvc.perform(
                        post("/api/v1/recurring-transactions")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(body)
                )
                .andExpect(status().isCreated());
    }

    private void createRecurringExpense(
            String token,
            int amount
    ) throws Exception {

        Category category = categoryRepository
                .findByName("Alimentação")
                .orElseThrow();

        SubCategory subCategory = subCategoryRepository
                .findByName("Supermercado")
                .orElseThrow();

        String body = """
            {
                "description":"Supermercado mensal",
                "amount":%d,
                "type":"EXPENSE",
                "paymentMethod":"PIX",
                "dayOfMonth":10,
                "startDate":"2026-07-01",
                "endDate":null,
                "categoryId":"%s",
                "subCategoryId":"%s"
            }
            """.formatted(
                amount,
                category.getId(),
                subCategory.getId()
        );

        mockMvc.perform(
                        post("/api/v1/recurring-transactions")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(body)
                )
                .andExpect(status().isCreated());
    }

    private void createTransaction(
            String token,
            int amount,
            String type,
            String paymentMethod
    ) throws Exception {

        boolean income = type.equals("INCOME");

        Category category = categoryRepository
                .findByName(income ? "Receita" : "Alimentação")
                .orElseThrow();

        SubCategory subCategory = subCategoryRepository
                .findByName(income ? "Salário" : "Supermercado")
                .orElseThrow();

        String body = """
            {
                "description":"Transaction test",
                "amount":%d,
                "transactionDate":"2026-07-01",
                "type":"%s",
                "paymentMethod":"%s",
                "categoryId":"%s",
                "subCategoryId":"%s"
            }
            """.formatted(
                amount,
                type,
                paymentMethod,
                category.getId(),
                subCategory.getId()
        );

        mockMvc.perform(
                        post("/api/v1/transactions")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturnDashboardSummary()
            throws Exception {
        String token = getToken();
        createIncome(token, 10000);
        mockMvc.perform(
                        get("/api/v1/dashboard/summary")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.totalIncome")
                                .value(10000)
                );
    }

    @Test
    void shouldReturnExpensesByCategory()
            throws Exception {
        String token = getToken();
        mockMvc.perform(
                        get("/api/v1/dashboard/categories")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnRecurringIncomeProjection()
            throws Exception {

        String token = getToken();

        createRecurringIncome(token, 10000);

        mockMvc.perform(
                        get("/api/v1/dashboard/projection")
                                .param("year", "2026")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(12))
                .andExpect(
                        jsonPath("$[5].month")
                                .value("JUNE")
                )
                .andExpect(
                        jsonPath("$[5].projectedIncome")
                                .value(0)
                )
                .andExpect(
                        jsonPath("$[6].month")
                                .value("JULY")
                )
                .andExpect(
                        jsonPath("$[6].projectedIncome")
                                .value(10000)
                )
                .andExpect(
                        jsonPath("$[11].month")
                                .value("DECEMBER")
                )
                .andExpect(
                        jsonPath("$[11].projectedIncome")
                                .value(10000)
                );
    }

    @Test
    void shouldHandleShortMonthAndPartialDateRange()
            throws Exception {

        String token = getToken();

        Category category = categoryRepository
                .findByName("Receita")
                .orElseThrow();

        SubCategory subCategory = subCategoryRepository
                .findByName("Salário")
                .orElseThrow();

        String body = """
            {
                "description":"Receita dia 31",
                "amount":3000,
                "type":"INCOME",
                "paymentMethod":"BANK_TRANSFER",
                "dayOfMonth":31,
                "startDate":"2027-01-15",
                "endDate":"2027-03-15",
                "categoryId":"%s",
                "subCategoryId":"%s"
            }
            """.formatted(
                category.getId(),
                subCategory.getId()
        );

        mockMvc.perform(
                        post("/api/v1/recurring-transactions")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(body)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        get("/api/v1/dashboard/projection")
                                .param("year", "2027")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())

                .andExpect(
                        jsonPath("$[0].month")
                                .value("JANUARY")
                )
                .andExpect(
                        jsonPath("$[0].projectedIncome")
                                .value(3000)
                )

                .andExpect(
                        jsonPath("$[1].month")
                                .value("FEBRUARY")
                )
                .andExpect(
                        jsonPath("$[1].projectedIncome")
                                .value(3000)
                )

                .andExpect(
                        jsonPath("$[2].month")
                                .value("MARCH")
                )
                .andExpect(
                        jsonPath("$[2].projectedIncome")
                                .value(0)
                );

    }

    @Test
    void shouldCombineRecurringTransactionsAndCreditCardInstallmentsInProjection()
            throws Exception {

        String token = getToken();

        createRecurringIncome(token, 10000);
        createRecurringExpense(token, 2000);

        String cardBody = """
            {
                "name":"Nubank",
                "creditLimit":20000,
                "closingDay":20,
                "dueDay":28
            }
            """;

        String cardResponse = mockMvc.perform(
                        post("/api/v1/credit-cards")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(cardBody)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID cardId = UUID.fromString(
                objectMapper
                        .readTree(cardResponse)
                        .get("id")
                        .asText()
        );

        String purchaseBody = """
            {
                "description":"Notebook",
                "totalAmount":12000,
                "installments":12,
                "purchaseDate":"2026-09-25"
            }
            """;

        mockMvc.perform(
                        post(
                                "/api/v1/credit-cards/{id}/purchases",
                                cardId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(purchaseBody)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        get("/api/v1/dashboard/projection")
                                .param("year", "2026")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())

                .andExpect(
                        jsonPath("$[9].month")
                                .value("OCTOBER")
                )
                .andExpect(
                        jsonPath("$[9].projectedIncome")
                                .value(10000)
                )
                .andExpect(
                        jsonPath("$[9].projectedRecurringExpense")
                                .value(2000)
                )
                .andExpect(
                        jsonPath("$[9].projectedCreditCardExpense")
                                .value(1000)
                )
                .andExpect(
                        jsonPath("$[9].projectedTotalExpense")
                                .value(3000)
                )
                .andExpect(
                        jsonPath("$[9].projectedBalance")
                                .value(7000)
                );
    }

    @Test
    void shouldReturnCashAndBankBalances()
            throws Exception {

        String token = getToken();

        createTransaction(
                token,
                10000,
                "INCOME",
                "BANK_TRANSFER"
        );

        createTransaction(
                token,
                1000,
                "INCOME",
                "CASH"
        );

        createTransaction(
                token,
                2000,
                "EXPENSE",
                "PIX"
        );

        createTransaction(
                token,
                500,
                "EXPENSE",
                "DEBIT_CARD"
        );

        createTransaction(
                token,
                300,
                "EXPENSE",
                "CASH"
        );

        mockMvc.perform(
                        get("/api/v1/dashboard/summary")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.totalIncome")
                                .value(11000)
                )
                .andExpect(
                        jsonPath("$.totalExpense")
                                .value(2800)
                )
                .andExpect(
                        jsonPath("$.balance")
                                .value(8200)
                )
                .andExpect(
                        jsonPath("$.cashBalance")
                                .value(700)
                )
                .andExpect(
                        jsonPath("$.bankBalance")
                                .value(7500)
                );
    }
}
