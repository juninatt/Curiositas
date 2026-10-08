package se.pbt.curiositas.security;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the admin configuration, since starting without a password would either leave writes
 * impossible or invite a guessable default.
 */
class AdminPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(PropertiesOnly.class);

    /** Refuses to start when no admin password is configured. */
    @Test
    void refusesToStartWithoutPassword() {
        contextRunner.run(context -> assertThat(context)
                .getFailure()
                .hasStackTraceContaining("curiositas.admin")
                .hasStackTraceContaining("password"));
    }

    /** Uses "admin" as username unless another one is configured. */
    @Test
    void defaultsUsernameToAdmin() {
        contextRunner.withPropertyValues("curiositas.admin.password=secret")
                .run(context -> assertThat(context.getBean(AdminProperties.class))
                        .isEqualTo(new AdminProperties("admin", "secret")));
    }

    /** Binds only the admin properties, without the rest of the application. */
    @Configuration
    @EnableConfigurationProperties(AdminProperties.class)
    static class PropertiesOnly {
    }
}
