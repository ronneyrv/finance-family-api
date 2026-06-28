package com.ronney.finance.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "credit_card_installments")
@Getter
@Setter
@Builder@NoArgsConstructor@AllArgsConstructor
public class CreditCardInstallment {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false,precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "installment_number", nullable = false)
    private Integer installmentNumber;

    @Column(name = "total_installments", nullable = false)
    private Integer totalInstallments;

    @Column(name = "invoice_month", nullable = false)
    private Integer invoiceMonth;

    @Column(name = "invoice_year", nullable = false)
    private Integer invoiceYear;

    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    @Column(nullable = false)
    private Boolean paid;

    @Column(name = "paid_at")
    private LocalDate paidAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_card_id", nullable = false)
    private CreditCard creditCard;
}
