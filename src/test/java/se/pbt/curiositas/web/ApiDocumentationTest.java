package se.pbt.curiositas.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import se.pbt.curiositas.TestcontainersConfiguration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that the API documentation is reachable without logging in, since it is how visitors
 * and future clients learn the API.
 */
@SpringBootTest(properties = "curiositas.admin.password=test-password")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ApiDocumentationTest {

    @Autowired
    private MockMvcTester mvc;

    /** Publishes exactly the contract the server code is generated from. */
    @Test
    void publishesTheContract() throws IOException {
        String contract = new ClassPathResource("openapi.yaml").getContentAsString(StandardCharsets.UTF_8);

        assertThat(mvc.get().uri("/openapi.yaml"))
                .hasStatusOk()
                .bodyText().isEqualTo(contract);
    }

    /** Shows Swagger UI, configured to read the published contract. */
    @Test
    void showsSwaggerUiForTheContract() {
        assertThat(mvc.get().uri("/swagger-ui.html"))
                .hasStatusOk()
                .bodyText().contains("url: '/openapi.yaml'");
    }

    /** Serves the Swagger UI files the page loads, without version numbers in their paths. */
    @Test
    void servesSwaggerUiFiles() {
        assertThat(mvc.get().uri("/webjars/swagger-ui/swagger-ui-bundle.js")).hasStatusOk();
        assertThat(mvc.get().uri("/webjars/swagger-ui/swagger-ui.css")).hasStatusOk();
    }
}
