package se.pbt.curiositas.web;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Publishes the OpenAPI contract at {@code /openapi.yaml}, so Swagger UI and API clients read the
 * same file that the server code is generated from.
 */
@RestController
public class ContractController {

    private static final Resource CONTRACT = new ClassPathResource("openapi.yaml");

    /**
     * Returns the contract as YAML.
     *
     * @return the contract file
     */
    @GetMapping(value = "/openapi.yaml", produces = "application/yaml;charset=UTF-8")
    public Resource contract() {
        return CONTRACT;
    }
}
