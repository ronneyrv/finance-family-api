package com.ronney.finance.controller;

import com.ronney.finance.BaseIntegrationTest;
import com.ronney.finance.domain.entity.CreditCard;
import com.ronney.finance.repository.CategoryRepository;
import com.ronney.finance.repository.CreditCardRepository;
import com.ronney.finance.repository.SubCategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PurchaseControllerIT extends BaseIntegrationTest {
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SubCategoryRepository subCategoryRepository;

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
    void shouldCreatePurchase() throws Exception {
        String token = getToken();

        UUID cardId = createCreditCard(token);

        createPurchase(
                token,
                cardId
        );
    }

    @Test
    void shouldGetInvoice() throws Exception {
        String token = getToken();

        UUID cardId = createCreditCard(token);

        createPurchase(
                token,
                cardId
        );

        mockMvc.perform(get("/api/v1/credit-cards/{id}/invoice", cardId)
                                .param(
                                        "month",
                                        "10"
                                )
                                .param(
                                        "year",
                                        "2026"
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.card").value("Nubank"))
                .andExpect(jsonPath("$.total").value(1000));
    }

    @Test
    void shouldPayInvoice() throws Exception {
        String token = getToken();

        UUID cardId = createCreditCard(token);

        createPurchase(
                token,
                cardId
        );

        mockMvc.perform(post("/api/v1/credit-cards/{id}/invoice/pay", cardId)
                                .param(
                                        "month",
                                        "10"
                                )
                                .param(
                                        "year",
                                        "2026"
                                )
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
                                .header("Authorization", "Bearer " + token)
                )
                .andDo(print())
                .andReturn();
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
