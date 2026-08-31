package com.simpatico.crm;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Basic application integration test verifying context loads successfully.
 */
@SpringBootTest
@ActiveProfiles("test")
class CrmApplicationTests {

    @Test
    void contextLoads() {
        // Verifies that the spring context compiles and loads without error
    }
}
