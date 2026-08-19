package com.ronney.finance.controller;

import com.ronney.finance.BaseIntegrationTest;
import com.ronney.finance.domain.entity.Category;
import com.ronney.finance.domain.entity.SubCategory;
import com.ronney.finance.domain.enums.TransactionType;
import com.ronney.finance.repository.CategoryRepository;
import com.ronney.finance.repository.SubCategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PurchaseControllerIT extends BaseIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SubCategoryRepository subCategoryRepository;

    private UUID createCreditCard(String token) throws Exception {
        return createCreditCard(token, 20, 28);
    }

    private UUID createCreditCard(
            String token,
            int closingDay,
            int dueDay
    ) throws Exception {
        String body = """
                {
                    "name":"Nubank",
                    "creditLimit":10000,
                    "closingDay":%d,
                    "dueDay":%d
                }
                """.formatted(closingDay, dueDay);

        String response = mockMvc.perform(
                post("/api/v1/credit-cards")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
                        .contentType(APPLICATION_JSON)
                        .content(body)
        )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(objectMapper
                .readTree(response)
                .get("id")
                .asText()
        );
    }

    private UUID createFinancialAccount(
            String token
    ) throws Exception {

        String body = """
            {
                "name": "Nubank Account",
                "accountType": "DIGITAL_ACCOUNT",
                "initialBalance": 5000.00
            }
            """;

        String response = mockMvc.perform(
                        post("/api/v1/financial-accounts")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(
                objectMapper
                        .readTree(response)
                        .get("id")
                        .asText()
        );
    }

    private void payInvoice(
            String token,
            UUID cardId,
            UUID accountId
    ) throws Exception {

        String body = """
            {
                "accountId": "%s",
                "paymentMethod": "PIX"
            }
            """.formatted(accountId);

        mockMvc.perform(
                        post(
                                "/api/v1/credit-cards/{id}/invoice/pay",
                                cardId
                        )
                                .param("month", "10")
                                .param("year", "2026")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isNoContent());
    }

    private void createPurchase(
            String token,
            UUID cardId
    ) throws Exception {
        String body = """
        {
            "description":"Notebook Dell",
            "totalAmount":12000,
            "installments":12,
            "purchaseDate":"2026-09-25"
        }
        """;

        mockMvc.perform(
                post("/api/v1/credit-cards/{id}/purchases", cardId)
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
                        .contentType(APPLICATION_JSON)
                        .content(body)
                )
                .andExpect(status().isCreated());
    }

    @Test
    void shouldCreatePurchaseWithCorrectInstallmentDistribution() throws Exception {

        String token = getToken();
        UUID cardId = createCreditCard(token);

        String body = """
            {
                "description":"Compra teste aggregate",
                "totalAmount":100.00,
                "installments":3,
                "purchaseDate":"2026-07-08"
            }
            """;

        mockMvc.perform(
                        post("/api/v1/credit-cards/{id}/purchases", cardId)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].description")
                        .value("Compra teste aggregate"))
                .andExpect(jsonPath("$[0].installmentNumber").value(1))
                .andExpect(jsonPath("$[0].totalInstallments").value(3))
                .andExpect(jsonPath("$[0].amount").value(33.33))
                .andExpect(jsonPath("$[1].amount").value(33.33))
                .andExpect(jsonPath("$[2].amount").value(33.34));
    }

    @Test
    void shouldGetInvoiceWithPurchaseData() throws Exception {

        String token = getToken();
        UUID cardId = createCreditCard(token);

        createPurchase(token, cardId);

        mockMvc.perform(
                        get("/api/v1/credit-cards/{id}/invoice", cardId)
                                .param("month", "10")
                                .param("year", "2026")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.card").value("Nubank"))
                .andExpect(jsonPath("$.month").value(10))
                .andExpect(jsonPath("$.year").value(2026))
                .andExpect(jsonPath("$.total").value(1000))
                .andExpect(jsonPath("$.availableLimit").value(-2000))
                .andExpect(jsonPath("$.installments.length()").value(1))
                .andExpect(jsonPath("$.installments[0].description")
                        .value("Notebook Dell"))
                .andExpect(jsonPath("$.installments[0].installment")
                        .value("1/12"))
                .andExpect(jsonPath("$.installments[0].amount")
                        .value(1000))
                .andExpect(jsonPath("$.installments[0].paid")
                        .value(false))
                .andExpect(jsonPath("$.installments[0].paidAt")
                        .doesNotExist());
    }

    @Test
    void shouldCalculateInvoiceDueDateInFollowingMonth() throws Exception {
        String token = getToken();
        UUID cardId = createCreditCard(token, 30, 5);

        mockMvc.perform(
                        get("/api/v1/credit-cards/{id}/invoice", cardId)
                                .param("month", "8")
                                .param("year", "2026")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.closingDay").value(30))
                .andExpect(jsonPath("$.dueDay").value(5))
                .andExpect(jsonPath("$.dueDate").value("2026-09-05"));
    }

    @Test
    void shouldCalculateInvoiceDueDateInFollowingYear() throws Exception {
        String token = getToken();
        UUID cardId = createCreditCard(token, 30, 5);

        mockMvc.perform(
                        get("/api/v1/credit-cards/{id}/invoice", cardId)
                                .param("month", "12")
                                .param("year", "2026")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.closingDay").value(30))
                .andExpect(jsonPath("$.dueDay").value(5))
                .andExpect(jsonPath("$.dueDate").value("2027-01-05"));
    }

    @Test
    void shouldReturnEmptyInvoiceWhenNoInstallmentsExist() throws Exception {

        String token = getToken();

        UUID cardId = createCreditCard(token);

        mockMvc.perform(
                        get("/api/v1/credit-cards/{id}/invoice", cardId)
                                .param("month", "10")
                                .param("year", "2026")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.card").value("Nubank"))
                .andExpect(jsonPath("$.month").value(10))
                .andExpect(jsonPath("$.year").value(2026))
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.installments").isArray())
                .andExpect(jsonPath("$.installments").isEmpty());
    }

    @Test
    void shouldPayInvoiceAndRestoreAvailableLimit() throws Exception {

        String token = getToken();

        UUID cardId = createCreditCard(token);
        UUID accountId = createFinancialAccount(token);

        createPurchase(token, cardId);

        payInvoice(
                token,
                cardId,
                accountId
        );

        mockMvc.perform(
                        get("/api/v1/credit-cards/{id}/invoice", cardId)
                                .param("month", "10")
                                .param("year", "2026")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1000))
                .andExpect(jsonPath("$.availableLimit").value(-1000))
                .andExpect(jsonPath("$.installments.length()").value(1))
                .andExpect(jsonPath("$.installments[0].description")
                        .value("Notebook Dell"))
                .andExpect(jsonPath("$.installments[0].installment")
                        .value("1/12"))
                .andExpect(jsonPath("$.installments[0].amount")
                        .value(1000))
                .andExpect(jsonPath("$.installments[0].paid")
                        .value(true))
                .andExpect(jsonPath("$.installments[0].paidAt")
                        .isNotEmpty());
    }

    @Test
    void shouldNotPayInvoiceTwice() throws Exception {

        String token = getToken();

        UUID cardId = createCreditCard(token);
        UUID accountId = createFinancialAccount(token);

        createPurchase(token, cardId);

        payInvoice(
                token,
                cardId,
                accountId
        );

        String body = """
            {
                "accountId": "%s",
                "paymentMethod": "PIX"
            }
            """.formatted(accountId);

        mockMvc.perform(
                        post(
                                "/api/v1/credit-cards/{id}/invoice/pay",
                                cardId
                        )
                                .param("month", "10")
                                .param("year", "2026")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isConflict());
    }

    @Test
    void shouldCreateTransactionWhenPayingInvoice() throws Exception {

        String token = getToken();

        UUID cardId = createCreditCard(token);
        UUID accountId = createFinancialAccount(token);

        createPurchase(token, cardId);

        payInvoice(
                token,
                cardId,
                accountId
        );

        mockMvc.perform(
                        get("/api/v1/transactions")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath(
                                "$.content[?(@.transactionKind == 'CREDIT_CARD_PAYMENT' && @.accountId == '%s')].description"
                                        .formatted(accountId)
                        )
                                .value(
                                        hasItem(
                                                "Pagamento da fatura - Nubank (10/2026)"
                                        )
                                )
                )
                .andExpect(
                        jsonPath(
                                "$.content[?(@.transactionKind == 'CREDIT_CARD_PAYMENT' && @.accountId == '%s')].amount"
                                        .formatted(accountId)
                        )
                                .value(
                                        hasItem(1000.0)
                                )
                )
                .andExpect(
                        jsonPath(
                                "$.content[?(@.transactionKind == 'CREDIT_CARD_PAYMENT' && @.accountId == '%s')].type"
                                        .formatted(accountId)
                        )
                                .value(
                                        hasItem("EXPENSE")
                                )
                )
                .andExpect(
                        jsonPath(
                                "$.content[?(@.transactionKind == 'CREDIT_CARD_PAYMENT' && @.accountId == '%s')].paymentMethod"
                                        .formatted(accountId)
                        )
                                .value(
                                        hasItem("PIX")
                                )
                )
                .andExpect(
                        jsonPath(
                                "$.content[?(@.transactionKind == 'CREDIT_CARD_PAYMENT' && @.accountId == '%s')].accountName"
                                        .formatted(accountId)
                        )
                                .value(
                                        hasItem("Nubank Account")
                                )
                );
    }

    @Test
    void shouldNotPayInvoiceUsingAnotherUsersFinancialAccount()
            throws Exception {

        String userOneToken = getToken();

        String userTwoToken = getToken(
                "user.two@example.test",
                "test-password"
        );

        UUID cardId = createCreditCard(userOneToken);

        UUID userTwoAccountId =
                createFinancialAccount(userTwoToken);

        createPurchase(
                userOneToken,
                cardId
        );

        String body = """
            {
                "accountId": "%s",
                "paymentMethod": "PIX"
            }
            """.formatted(userTwoAccountId);

        mockMvc.perform(
                        post(
                                "/api/v1/credit-cards/{id}/invoice/pay",
                                cardId
                        )
                                .param("month", "10")
                                .param("year", "2026")
                                .header(
                                        "Authorization",
                                        "Bearer " + userOneToken
                                )
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isNotFound());

        mockMvc.perform(
                        get(
                                "/api/v1/credit-cards/{id}/invoice",
                                cardId
                        )
                                .param("month", "10")
                                .param("year", "2026")
                                .header(
                                        "Authorization",
                                        "Bearer " + userOneToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.installments[0].paid")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.installments[0].paidAt")
                                .doesNotExist()
                );
    }

    @Test
    void shouldReduceFinancialAccountBalanceWhenPayingInvoice()
            throws Exception {

        String token = getToken();

        UUID cardId = createCreditCard(token);
        UUID accountId = createFinancialAccount(token);

        createPurchase(
                token,
                cardId
        );

        payInvoice(
                token,
                cardId,
                accountId
        );

        mockMvc.perform(
                        get(
                                "/api/v1/financial-accounts/{id}",
                                accountId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.initialBalance")
                                .value(5000.00)
                )
                .andExpect(
                        jsonPath("$.currentBalance")
                                .value(4000.00)
                );
    }

    @Test
    void shouldListPendingCreditCardPurchases() throws Exception {
        String token = getToken();
        UUID cardId = createCreditCard(token);

        String body = """
        {
            "description":"Compra pendente",
            "totalAmount":1200,
            "installments":3,
            "purchaseDate":"2026-09-10"
        }
        """;

        mockMvc.perform(
                        post("/api/v1/credit-cards/{id}/purchases", cardId)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        get("/api/v1/credit-cards/purchases/pending")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description")
                        .value("Compra pendente"))
                .andExpect(jsonPath("$[0].purchaseDate")
                        .value("2026-09-10"))
                .andExpect(jsonPath("$[0].totalAmount")
                        .value(1200))
                .andExpect(jsonPath("$[0].installmentCount")
                        .value(3))
                .andExpect(jsonPath("$[0].creditCardId")
                        .value(cardId.toString()))
                .andExpect(jsonPath("$[0].creditCardName")
                        .value("Nubank"));
    }

    @Test
    void shouldNotListPurchaseAfterAllInstallmentsArePaid() throws Exception {
        String token = getToken();
        UUID cardId = createCreditCard(token);
        UUID accountId = createFinancialAccount(token);

        String body = """
        {
            "description":"Compra à vista no crédito",
            "totalAmount":500,
            "installments":1,
            "purchaseDate":"2026-09-25"
        }
        """;

        mockMvc.perform(
                        post("/api/v1/credit-cards/{id}/purchases", cardId)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        get("/api/v1/credit-cards/purchases/pending")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description")
                        .value("Compra à vista no crédito"));

        payInvoice(
                token,
                cardId,
                accountId
        );

        mockMvc.perform(
                        get("/api/v1/credit-cards/purchases/pending")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldListPendingPurchasesOrderedByPurchaseDateDesc() throws Exception {
        String token = getToken();
        UUID cardId = createCreditCard(token);

        String olderPurchase = """
        {
            "description":"Compra antiga",
            "totalAmount":300,
            "installments":1,
            "purchaseDate":"2026-09-01"
        }
        """;

        String newerPurchase = """
        {
            "description":"Compra recente",
            "totalAmount":500,
            "installments":1,
            "purchaseDate":"2026-09-15"
        }
        """;

        mockMvc.perform(
                        post("/api/v1/credit-cards/{id}/purchases", cardId)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(olderPurchase)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/api/v1/credit-cards/{id}/purchases", cardId)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(newerPurchase)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        get("/api/v1/credit-cards/purchases/pending")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].description")
                        .value("Compra recente"))
                .andExpect(jsonPath("$[1].description")
                        .value("Compra antiga"));
    }

    @Test
    void shouldUpdatePendingPurchaseCategoryAndSubCategory() throws Exception {
        String token = getToken();
        UUID cardId = createCreditCard(token);

        Category category = categoryRepository
                .findByName("Pessoal")
                .orElseThrow();

        SubCategory subCategory = subCategoryRepository
                .findByName("Vestuário")
                .orElseThrow();

        String body = """
        {
            "description":"Compra para classificar",
            "totalAmount":900,
            "installments":3,
            "purchaseDate":"2026-09-10"
        }
        """;

        mockMvc.perform(
                        post("/api/v1/credit-cards/{id}/purchases", cardId)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated());

        String pendingResponse = mockMvc.perform(
                        get("/api/v1/credit-cards/purchases/pending")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID purchaseId = UUID.fromString(
                objectMapper
                        .readTree(pendingResponse)
                        .get(0)
                        .get("id")
                        .asText()
        );

        String categoryBody = """
        {
            "categoryId":"%s",
            "subCategoryId":"%s"
        }
        """.formatted(
                category.getId(),
                subCategory.getId()
        );

        mockMvc.perform(
                        patch(
                                "/api/v1/credit-cards/purchases/{purchaseId}/category",
                                purchaseId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(categoryBody)
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        get("/api/v1/credit-cards/purchases/pending")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryId")
                        .value(category.getId().toString()))
                .andExpect(jsonPath("$[0].categoryName")
                        .value("Pessoal"))
                .andExpect(jsonPath("$[0].subCategoryId")
                        .value(subCategory.getId().toString()))
                .andExpect(jsonPath("$[0].subCategoryName")
                        .value("Vestuário"));
    }

    @Test
    void shouldUpdatePendingPurchaseCategoryWithoutSubCategory() throws Exception {
        String token = getToken();
        UUID cardId = createCreditCard(token);

        Category category = categoryRepository
                .findByName("Pessoal")
                .orElseThrow();

        String body = """
        {
            "description":"Compra somente categoria",
            "totalAmount":600,
            "installments":2,
            "purchaseDate":"2026-09-10"
        }
        """;

        mockMvc.perform(
                        post("/api/v1/credit-cards/{id}/purchases", cardId)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated());

        String pendingResponse = mockMvc.perform(
                        get("/api/v1/credit-cards/purchases/pending")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID purchaseId = UUID.fromString(
                objectMapper
                        .readTree(pendingResponse)
                        .get(0)
                        .get("id")
                        .asText()
        );

        String categoryBody = """
        {
            "categoryId":"%s",
            "subCategoryId":null
        }
        """.formatted(category.getId());

        mockMvc.perform(
                        patch(
                                "/api/v1/credit-cards/purchases/{purchaseId}/category",
                                purchaseId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(categoryBody)
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        get("/api/v1/credit-cards/purchases/pending")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryId")
                        .value(category.getId().toString()))
                .andExpect(jsonPath("$[0].categoryName")
                        .value("Pessoal"))
                .andExpect(jsonPath("$[0].subCategoryId")
                        .doesNotExist())
                .andExpect(jsonPath("$[0].subCategoryName")
                        .doesNotExist());
    }

    @Test
    void shouldRemovePendingPurchaseCategoryAndSubCategory() throws Exception {
        String token = getToken();
        UUID cardId = createCreditCard(token);

        Category category = categoryRepository
                .findByName("Pessoal")
                .orElseThrow();

        SubCategory subCategory = subCategoryRepository
                .findByName("Vestuário")
                .orElseThrow();

        String purchaseBody = """
        {
            "description":"Compra para remover categoria",
            "totalAmount":700,
            "installments":2,
            "purchaseDate":"2026-09-10"
        }
        """;

        mockMvc.perform(
                        post("/api/v1/credit-cards/{id}/purchases", cardId)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(purchaseBody)
                )
                .andExpect(status().isCreated());

        String pendingResponse = mockMvc.perform(
                        get("/api/v1/credit-cards/purchases/pending")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID purchaseId = UUID.fromString(
                objectMapper
                        .readTree(pendingResponse)
                        .get(0)
                        .get("id")
                        .asText()
        );

        String categoryBody = """
        {
            "categoryId":"%s",
            "subCategoryId":"%s"
        }
        """.formatted(
                category.getId(),
                subCategory.getId()
        );

        mockMvc.perform(
                        patch(
                                "/api/v1/credit-cards/purchases/{purchaseId}/category",
                                purchaseId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(categoryBody)
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        patch(
                                "/api/v1/credit-cards/purchases/{purchaseId}/category",
                                purchaseId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content("""
                                {
                                    "categoryId":null,
                                    "subCategoryId":null
                                }
                                """)
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        get("/api/v1/credit-cards/purchases/pending")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryId")
                        .doesNotExist())
                .andExpect(jsonPath("$[0].categoryName")
                        .doesNotExist())
                .andExpect(jsonPath("$[0].subCategoryId")
                        .doesNotExist())
                .andExpect(jsonPath("$[0].subCategoryName")
                        .doesNotExist());
    }

    @Test
    void shouldRejectSubCategoryWithoutCategory() throws Exception {
        String token = getToken();
        UUID cardId = createCreditCard(token);

        SubCategory subCategory = subCategoryRepository
                .findByName("Vestuário")
                .orElseThrow();

        String purchaseBody = """
        {
            "description":"Compra inválida",
            "totalAmount":500,
            "installments":2,
            "purchaseDate":"2026-09-10"
        }
        """;

        mockMvc.perform(
                        post("/api/v1/credit-cards/{id}/purchases", cardId)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(purchaseBody)
                )
                .andExpect(status().isCreated());

        String pendingResponse = mockMvc.perform(
                        get("/api/v1/credit-cards/purchases/pending")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID purchaseId = UUID.fromString(
                objectMapper
                        .readTree(pendingResponse)
                        .get(0)
                        .get("id")
                        .asText()
        );

        String categoryBody = """
        {
            "categoryId":null,
            "subCategoryId":"%s"
        }
        """.formatted(subCategory.getId());

        mockMvc.perform(
                        patch(
                                "/api/v1/credit-cards/purchases/{purchaseId}/category",
                                purchaseId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(categoryBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(
                                "Category is required when SubCategory is provided."
                        ));
    }

    @Test
    void shouldRejectIncomeCategory() throws Exception {
        String token = getToken();
        UUID cardId = createCreditCard(token);

        Category category = categoryRepository
                .findByName("Receita")
                .orElseThrow();

        String purchaseBody = """
        {
            "description":"Compra inválida",
            "totalAmount":500,
            "installments":2,
            "purchaseDate":"2026-09-10"
        }
        """;

        mockMvc.perform(
                        post("/api/v1/credit-cards/{id}/purchases", cardId)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(purchaseBody)
                )
                .andExpect(status().isCreated());

        String pendingResponse = mockMvc.perform(
                        get("/api/v1/credit-cards/purchases/pending")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID purchaseId = UUID.fromString(
                objectMapper
                        .readTree(pendingResponse)
                        .get(0)
                        .get("id")
                        .asText()
        );

        String categoryBody = """
        {
            "categoryId":"%s",
            "subCategoryId":null
        }
        """.formatted(category.getId());

        mockMvc.perform(
                        patch(
                                "/api/v1/credit-cards/purchases/{purchaseId}/category",
                                purchaseId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(categoryBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Category does not match purchase type."));
    }

    @Test
    void shouldRejectSubCategoryFromDifferentCategory() throws Exception {
        String token = getToken();
        UUID cardId = createCreditCard(token);

        Category category = categoryRepository
                .findByName("Pessoal")
                .orElseThrow();

        SubCategory subCategory = subCategoryRepository
                .findByName("Vestuário")
                .orElseThrow();

        Category anotherCategory = categoryRepository.save(
                Category.builder()
                        .id(UUID.randomUUID())
                        .name("Outra categoria " + UUID.randomUUID())
                        .type(TransactionType.EXPENSE)
                        .build()
        );

        String purchaseBody = """
        {
            "description":"Compra inválida",
            "totalAmount":500,
            "installments":2,
            "purchaseDate":"2026-09-10"
        }
        """;

        mockMvc.perform(
                        post("/api/v1/credit-cards/{id}/purchases", cardId)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(purchaseBody)
                )
                .andExpect(status().isCreated());

        String pendingResponse = mockMvc.perform(
                        get("/api/v1/credit-cards/purchases/pending")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID purchaseId = UUID.fromString(
                objectMapper
                        .readTree(pendingResponse)
                        .get(0)
                        .get("id")
                        .asText()
        );

        String categoryBody = """
        {
            "categoryId":"%s",
            "subCategoryId":"%s"
        }
        """.formatted(
                anotherCategory.getId(),
                subCategory.getId()
        );

        mockMvc.perform(
                        patch(
                                "/api/v1/credit-cards/purchases/{purchaseId}/category",
                                purchaseId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(categoryBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(
                                "SubCategory does not belong to Category."
                        ));
    }

    @Test
    void shouldNotUpdatePurchaseCategoryUsingAnotherUser() throws Exception {
        String ownerToken = getToken();

        UUID cardId = createCreditCard(ownerToken);

        String purchaseBody = """
        {
            "description":"Compra de outro usuário",
            "totalAmount":800,
            "installments":2,
            "purchaseDate":"2026-09-10"
        }
        """;

        mockMvc.perform(
                        post("/api/v1/credit-cards/{id}/purchases", cardId)
                                .header(
                                        "Authorization",
                                        "Bearer " + ownerToken
                                )
                                .contentType(APPLICATION_JSON)
                                .content(purchaseBody)
                )
                .andExpect(status().isCreated());

        String pendingResponse = mockMvc.perform(
                        get("/api/v1/credit-cards/purchases/pending")
                                .header(
                                        "Authorization",
                                        "Bearer " + ownerToken
                                )
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID purchaseId = UUID.fromString(
                objectMapper
                        .readTree(pendingResponse)
                        .get(0)
                        .get("id")
                        .asText()
        );

        String otherUserToken = getToken(
                "user.two@example.test",
                "test-password"
        );

        Category category = categoryRepository
                .findByName("Pessoal")
                .orElseThrow();

        String categoryBody = """
        {
            "categoryId":"%s",
            "subCategoryId":null
        }
        """.formatted(category.getId());

        mockMvc.perform(
                        patch(
                                "/api/v1/credit-cards/purchases/{purchaseId}/category",
                                purchaseId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + otherUserToken
                                )
                                .contentType(APPLICATION_JSON)
                                .content(categoryBody)
                )
                .andExpect(status().isNotFound());

        mockMvc.perform(
                        get("/api/v1/credit-cards/purchases/pending")
                                .header(
                                        "Authorization",
                                        "Bearer " + ownerToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description")
                        .value("Compra de outro usuário"))
                .andExpect(jsonPath("$[0].categoryId")
                        .doesNotExist())
                .andExpect(jsonPath("$[0].subCategoryId")
                        .doesNotExist());
    }

    @Test
    void shouldDeletePurchaseAndItsInstallments() throws Exception {
        String token = getToken();
        UUID cardId = createCreditCard(token);

        String body = """
        {
            "description":"Compra para excluir",
            "totalAmount":900,
            "installments":3,
            "purchaseDate":"2026-09-10"
        }
        """;

        mockMvc.perform(
                        post("/api/v1/credit-cards/{id}/purchases", cardId)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated());

        String pendingResponse = mockMvc.perform(
                        get("/api/v1/credit-cards/purchases/pending")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID purchaseId = UUID.fromString(
                objectMapper
                        .readTree(pendingResponse)
                        .get(0)
                        .get("id")
                        .asText()
        );

        mockMvc.perform(
                        delete(
                                "/api/v1/credit-cards/purchases/{purchaseId}",
                                purchaseId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        get("/api/v1/credit-cards/purchases/pending")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldNotDeletePurchaseBelongingToAnotherUser() throws Exception {
        String ownerToken = getToken();
        UUID cardId = createCreditCard(ownerToken);

        String body = """
        {
            "description":"Compra de outro usuário",
            "totalAmount":900,
            "installments":3,
            "purchaseDate":"2026-09-10"
        }
        """;

        mockMvc.perform(
                        post("/api/v1/credit-cards/{id}/purchases", cardId)
                                .header(
                                        "Authorization",
                                        "Bearer " + ownerToken
                                )
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated());

        String pendingResponse = mockMvc.perform(
                        get("/api/v1/credit-cards/purchases/pending")
                                .header(
                                        "Authorization",
                                        "Bearer " + ownerToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID purchaseId = UUID.fromString(
                objectMapper
                        .readTree(pendingResponse)
                        .get(0)
                        .get("id")
                        .asText()
        );

        String otherUserToken = getToken(
                "user.two@example.test",
                "test-password"
        );

        mockMvc.perform(
                        delete(
                                "/api/v1/credit-cards/purchases/{purchaseId}",
                                purchaseId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + otherUserToken
                                )
                )
                .andExpect(status().isNotFound());

        mockMvc.perform(
                        get("/api/v1/credit-cards/purchases/pending")
                                .header(
                                        "Authorization",
                                        "Bearer " + ownerToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description")
                        .value("Compra de outro usuário"));
    }

    @Test
    void shouldCreatePendingPurchaseWithCategoryAndSubCategory() throws Exception {
        String token = getToken();
        UUID cardId = createCreditCard(token);

        Category category = Category.builder()
                .id(UUID.randomUUID())
                .name("Purchase Test " + UUID.randomUUID())
                .type(TransactionType.EXPENSE)
                .build();

        category = categoryRepository.save(category);

        SubCategory subCategory = SubCategory.builder()
                .id(UUID.randomUUID())
                .name("Subcategory Test " + UUID.randomUUID())
                .category(category)
                .build();

        subCategory = subCategoryRepository.save(subCategory);

        String body = """
    {
        "description":"Compra classificada",
        "totalAmount":1200,
        "installments":3,
        "purchaseDate":"2026-09-10",
        "categoryId":"%s",
        "subCategoryId":"%s"
    }
    """.formatted(
                category.getId(),
                subCategory.getId()
        );

        mockMvc.perform(
                        post("/api/v1/credit-cards/{id}/purchases", cardId)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        get("/api/v1/credit-cards/purchases/pending")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description")
                        .value("Compra classificada"))
                .andExpect(jsonPath("$[0].categoryId")
                        .value(category.getId().toString()))
                .andExpect(jsonPath("$[0].categoryName")
                        .value(category.getName()))
                .andExpect(jsonPath("$[0].subCategoryId")
                        .value(subCategory.getId().toString()))
                .andExpect(jsonPath("$[0].subCategoryName")
                        .value(subCategory.getName()));
    }

    @Test
    void shouldRejectPurchaseWithSubCategoryWithoutCategory() throws Exception {
        String token = getToken();
        UUID cardId = createCreditCard(token);

        String body = """
    {
        "description":"Compra inválida",
        "totalAmount":500,
        "installments":2,
        "purchaseDate":"2026-09-10",
        "subCategoryId":"7bb5b3de-8ec9-4c7f-a001-000000000069"
    }
    """;

        mockMvc.perform(
                        post("/api/v1/credit-cards/{id}/purchases", cardId)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(
                                "Category is required when SubCategory is provided."
                        ));
    }

    @Test
    void shouldRejectPurchaseWithInvalidCategory() throws Exception {
        String token = getToken();
        UUID cardId = createCreditCard(token);

        UUID categoryId = UUID.randomUUID();

        String body = """
    {
        "description":"Compra inválida",
        "totalAmount":500,
        "installments":2,
        "purchaseDate":"2026-09-10",
        "categoryId":"%s"
    }
    """.formatted(categoryId);

        mockMvc.perform(
                        post("/api/v1/credit-cards/{id}/purchases", cardId)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Category not found."));
    }

    @Test
    void shouldRejectPurchaseWithSubCategoryFromAnotherCategory() throws Exception {
        String token = getToken();
        UUID cardId = createCreditCard(token);

        Category firstCategory = Category.builder()
                .id(UUID.randomUUID())
                .name("Purchase Category A " + UUID.randomUUID())
                .type(TransactionType.EXPENSE)
                .build();

        firstCategory = categoryRepository.save(firstCategory);

        Category secondCategory = Category.builder()
                .id(UUID.randomUUID())
                .name("Purchase Category B " + UUID.randomUUID())
                .type(TransactionType.EXPENSE)
                .build();

        secondCategory = categoryRepository.save(secondCategory);

        SubCategory subCategory = SubCategory.builder()
                .id(UUID.randomUUID())
                .name("Purchase Subcategory " + UUID.randomUUID())
                .category(firstCategory)
                .build();

        subCategory = subCategoryRepository.save(subCategory);

        String body = """
    {
        "description":"Compra inválida",
        "totalAmount":500,
        "installments":2,
        "purchaseDate":"2026-09-10",
        "categoryId":"%s",
        "subCategoryId":"%s"
    }
    """.formatted(
                secondCategory.getId(),
                subCategory.getId()
        );

        mockMvc.perform(
                        post("/api/v1/credit-cards/{id}/purchases", cardId)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("SubCategory does not belong to Category."));
    }

    @Test
    void shouldRejectPurchaseWithIncomeCategory() throws Exception {
        String token = getToken();
        UUID cardId = createCreditCard(token);

        Category category = categoryRepository
                .findByName("Receita")
                .orElseThrow();

        String body = """
        {
            "description":"Compra com categoria inválida",
            "totalAmount":500,
            "installments":2,
            "purchaseDate":"2026-09-10",
            "categoryId":"%s"
        }
        """.formatted(category.getId());

        mockMvc.perform(
                        post("/api/v1/credit-cards/{id}/purchases", cardId)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Category does not match purchase type."));
    }
}
