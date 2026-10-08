package se.pbt.curiositas.api;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that the OpenAPI contract is a valid document, since code generation, documentation
 * and contract tests all depend on it and would fail in less obvious ways otherwise.
 */
class OpenApiContractTest {

    /** Parses the contract and fails with the parser's messages if anything is invalid. */
    @Test
    void contractIsValidOpenApi() throws IOException {
        SwaggerParseResult result = new OpenAPIParser().readContents(readContract(), null, null);

        assertThat(result.getMessages()).isEmpty();
        assertThat(result.getOpenAPI()).isNotNull();
    }

    /** Reads the contract from the classpath, where it is packaged with the application. */
    private static String readContract() throws IOException {
        try (InputStream contract = OpenApiContractTest.class.getResourceAsStream("/openapi.yaml")) {
            assertThat(contract).as("openapi.yaml on the classpath").isNotNull();
            return new String(contract.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
