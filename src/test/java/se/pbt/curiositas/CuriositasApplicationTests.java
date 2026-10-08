package se.pbt.curiositas;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * Verifies that the full application starts against a real database, catching wiring and
 * configuration errors early.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "curiositas.admin.password=test-password")
class CuriositasApplicationTests {

    /** Fails if the Spring context cannot start. */
    @Test
    void contextLoads() {
    }
}
