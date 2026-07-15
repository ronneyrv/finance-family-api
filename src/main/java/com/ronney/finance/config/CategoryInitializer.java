package com.ronney.finance.config;

import com.ronney.finance.domain.entity.Category;
import com.ronney.finance.domain.entity.SubCategory;
import com.ronney.finance.domain.enums.TransactionType;
import com.ronney.finance.repository.CategoryRepository;
import com.ronney.finance.repository.SubCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.UUID;

@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class CategoryInitializer {

    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;

    @Bean
    CommandLineRunner initializeCategories() {
        return args -> {

            if (categoryRepository.count() > 0) {
                return;
            }

            Category receita =
                    createCategory(
                            "Receita",
                            TransactionType.INCOME
                    );

            Category alimentacao =
                    createCategory(
                            "Alimentação",
                            TransactionType.EXPENSE
                    );

            Category moradia =
                    createCategory(
                            "Moradia",
                            TransactionType.EXPENSE
                    );

            Category transporte =
                    createCategory(
                            "Transporte",
                            TransactionType.EXPENSE
                    );

            Category saude =
                    createCategory(
                            "Saúde",
                            TransactionType.EXPENSE
                    );

            Category educacao =
                    createCategory(
                            "Educação",
                            TransactionType.EXPENSE
                    );

            Category lazer =
                    createCategory(
                            "Lazer",
                            TransactionType.EXPENSE
                    );

            Category assinaturas =
                    createCategory(
                            "Assinaturas",
                            TransactionType.EXPENSE
                    );

            Category investimentos =
                    createCategory(
                            "Investimentos",
                            TransactionType.EXPENSE
                    );

            Category financeiro =
                    createCategory(
                            "Financeiro",
                            TransactionType.EXPENSE
                    );

            Category outros =
                    createCategory(
                            "Outros",
                            TransactionType.EXPENSE
                    );

            /*
             * RECEITAS
             */
            createSubCategories(
                    receita,
                    List.of(
                            "Salário",
                            "PLR",
                            "13º Salário",
                            "Férias",
                            "Rendimentos",
                            "Restituição IR",
                            "Outras Receitas"
                    )
            );

            /*
             * ALIMENTAÇÃO
             */
            createSubCategories(
                    alimentacao,
                    List.of(
                            "Supermercado",
                            "Restaurante",
                            "Delivery",
                            "Café",
                            "Outros"
                    )
            );

            /*
             * MORADIA
             */
            createSubCategories(
                    moradia,
                    List.of(
                            "Aluguel",
                            "Condomínio",
                            "Energia",
                            "Água",
                            "Gás",
                            "IPTU",
                            "Internet",
                            "Telefone",
                            "Serviços",
                            "Manutenção Residencial"
                    )
            );

            /*
             * TRANSPORTE
             */
            createSubCategories(
                    transporte,
                    List.of(
                            "Combustível",
                            "Uber",
                            "Estacionamento",
                            "Manutenção",
                            "Seguro",
                            "IPVA",
                            "Licenciamento",
                            "Multa"
                    )
            );

            /*
             * SAÚDE
             */
            createSubCategories(
                    saude,
                    List.of(
                            "Plano de Saúde",
                            "Farmácia",
                            "Consultas",
                            "Exames",
                            "Academia"
                    )
            );

            /*
             * EDUCAÇÃO
             */
            createSubCategories(
                    educacao,
                    List.of(
                            "Faculdade",
                            "Cursos",
                            "Livros",
                            "Certificações"
                    )
            );

            /*
             * LAZER
             */
            createSubCategories(
                    lazer,
                    List.of(
                            "Cinema",
                            "Viagens",
                            "Passeios",
                            "Streaming",
                            "Outros"
                    )
            );

            /*
             * ASSINATURAS
             */
            createSubCategories(
                    assinaturas,
                    List.of(
                            "Netflix",
                            "Spotify",
                            "Amazon Prime",
                            "ChatGPT",
                            "Gemini",
                            "Google One",
                            "Outros"
                    )
            );

            /*
             * INVESTIMENTOS
             */
            createSubCategories(
                    investimentos,
                    List.of(
                            "Tesouro Direto",
                            "Ações",
                            "ETF",
                            "CDI",
                            "Fundo Imobiliário",
                            "Criptomoedas",
                            "Previdência"
                    )
            );

            /*
             * FINANCEIRO
             */
            createSubCategories(
                    investimentos,
                    List.of(
                            "Empréstimo",
                            "Financiamento",
                            "Imposto de Renda",
                            "Taxas Bancárias",
                            "Saque",
                            "Outros Tributos"
                    )
            );

            /*
             * OUTROS
             */
            createSubCategories(
                    outros,
                    List.of(
                            "Presentes",
                            "Doações",
                            "Pet",
                            "Diversos"
                    )
            );
        };
    }

    private Category createCategory(
            String name,
            TransactionType type
    ) {

        Category category =
                Category.builder()
                        .id(UUID.randomUUID())
                        .name(name)
                        .type(type)
                        .build();

        return categoryRepository.save(category);
    }

    private void createSubCategories(
            Category category,
            List<String> names
    ) {

        names.forEach(name ->
                createSubCategory(name, category)
        );
    }

    private void createSubCategory(
            String name,
            Category category
    ) {

        SubCategory subCategory =
                SubCategory.builder()
                        .id(UUID.randomUUID())
                        .name(name)
                        .category(category)
                        .build();

        subCategoryRepository.save(subCategory);
    }
}