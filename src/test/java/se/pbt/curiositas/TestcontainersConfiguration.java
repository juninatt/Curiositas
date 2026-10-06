package se.pbt.curiositas;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Provides a real PostgreSQL database in Docker for tests, so they run against the same
 * database engine as production instead of an in-memory substitute.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    /** Same image as in compose.yaml, so local development and tests use the same version. */
    static final String POSTGRES_IMAGE = "postgres:18-alpine";

    /**
     * Starts a PostgreSQL container and connects the application's data source to it.
     *
     * @return the container that Spring Boot manages for the test context
     */
    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        return new PostgreSQLContainer(DockerImageName.parse(POSTGRES_IMAGE));
    }
}
