package com.ronney.finance.config;

import com.ronney.finance.domain.entity.Category;
import com.ronney.finance.domain.entity.Household;
import com.ronney.finance.domain.entity.SubCategory;
import com.ronney.finance.domain.entity.User;
import com.ronney.finance.domain.enums.TransactionType;
import com.ronney.finance.repository.CategoryRepository;
import com.ronney.finance.repository.HouseholdRepository;
import com.ronney.finance.repository.SubCategoryRepository;
import com.ronney.finance.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@TestConfiguration
public class TestDataInitializer {

    @Bean
    CommandLineRunner initTestDatabase(
            HouseholdRepository householdRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            SubCategoryRepository subCategoryRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            Household household = Household.builder()
                    .id(UUID.randomUUID())
                    .name("Test Household")
                    .build();

            householdRepository.save(household);

            User userOne = User.builder()
                    .id(UUID.randomUUID())
                    .name("Test User One")
                    .email("user.one@example.test")
                    .password(passwordEncoder.encode("test-password"))
                    .household(household)
                    .build();

            User userTwo = User.builder()
                    .id(UUID.randomUUID())
                    .name("Test User Two")
                    .email("user.two@example.test")
                    .password(passwordEncoder.encode("test-password"))
                    .household(household)
                    .build();

            Category incomeCategory = Category.builder()
                    .id(UUID.randomUUID())
                    .name("Receita")
                    .type(TransactionType.INCOME)
                    .build();

            categoryRepository.save(incomeCategory);
            Category expenseCategory = Category.builder()
                    .id(UUID.randomUUID())
                    .name("Alimentação")
                    .type(TransactionType.EXPENSE)
                    .build();

            categoryRepository.save(expenseCategory);

            SubCategory salarySubCategory = SubCategory.builder()
                    .id(UUID.randomUUID())
                    .name("Salário")
                    .category(incomeCategory)
                    .build();

            subCategoryRepository.save(salarySubCategory);
            SubCategory groceriesSubCategory = SubCategory.builder()
                    .id(UUID.randomUUID())
                    .name("Supermercado")
                    .category(expenseCategory)
                    .build();

            subCategoryRepository.save(groceriesSubCategory);

            userRepository.save(userOne);
            userRepository.save(userTwo);
        };
    }
}
