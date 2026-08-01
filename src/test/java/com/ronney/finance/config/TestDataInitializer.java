package com.ronney.finance.config;

import com.ronney.finance.domain.entity.Household;
import com.ronney.finance.domain.entity.User;
import com.ronney.finance.repository.HouseholdRepository;
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

            userRepository.save(userOne);
            userRepository.save(userTwo);
        };
    }
}
