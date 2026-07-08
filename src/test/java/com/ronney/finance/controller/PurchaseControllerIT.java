package com.ronney.finance.controller;

import com.ronney.finance.BaseIntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PurchaseControllerIT extends BaseIntegrationTest {

    private UUID createCreditCard(String token) throws Exception {
        String body = """
                {
                    "name":"Nubank",
                    "creditLimit":10000,
                    "closingDay":20,
                    "dueDay":28
                }
                """;

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
    void shouldPayInvoiceAndRestoreAvailableLimit() throws Exception {

        String token = getToken();
        UUID cardId = createCreditCard(token);

        createPurchase(token, cardId);

        mockMvc.perform(
                        post("/api/v1/credit-cards/{id}/invoice/pay", cardId)
                                .param("month", "10")
                                .param("year", "2026")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isNoContent());

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

        createPurchase(token, cardId);

        // Primeiro pagamento
        mockMvc.perform(
                        post("/api/v1/credit-cards/{id}/invoice/pay", cardId)
                                .param("month", "10")
                                .param("year", "2026")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isNoContent());

        // Segundo pagamento
        mockMvc.perform(
                        post("/api/v1/credit-cards/{id}/invoice/pay", cardId)
                                .param("month", "10")
                                .param("year", "2026")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isConflict());
    }
}
