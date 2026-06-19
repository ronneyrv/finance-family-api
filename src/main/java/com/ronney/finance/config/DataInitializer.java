package com.ronney.finance.config;

import com.ronney.finance.domain.entity.Household;
import com.ronney.finance.domain.entity.User;
import com.ronney.finance.repository.HouseholdRepository;
import com.ronney.finance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {
    private final HouseholdRepository householdRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initDatabase() {
        return args -> {
            if (userRepository.count() > 0) {
                return;
            }

            Household household = Household.builder()
                    .id(java.util.UUID.randomUUID())
                    .name("B Family")
                    .build();
            householdRepository.save(household);

            User user1 = User.builder()
                    .id(UUID.randomUUID())
                    .name("Ronney Rocha")
                    .email("ronneyrv@gmail.com")
                    .password(passwordEncoder.encode("123456"))
                    .household(household)
                    .build();

            User user2 = User.builder()
                    .id(UUID.randomUUID())
                    .name("Beatriz Bloc")
                     .email("beatrizbloc@gmail.com")
                    .password(passwordEncoder.encode("123456"))
                    .household(household)
                    .build();

            userRepository.save(user1);
            userRepository.save(user2);
        };
    }
}
