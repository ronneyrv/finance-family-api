package com.ronney.finance;

import com.ronney.finance.config.PostgresTestContainerConfig;
import com.ronney.finance.config.TestDataInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import({
        TestDataInitializer.class,
        PostgresTestContainerConfig.class
})
class FinanceApplicationTests {

    @Test
    void contextLoads() {
    }

}
