package se.pbt.curiositas.security;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * The account allowed to change data. The password has no default, so the application refuses to
 * start without one instead of running with a guessable password. It is normally given through
 * the environment variable {@code CURIOSITAS_ADMIN_PASSWORD}.
 *
 * @param username the admin's username, "admin" unless configured otherwise
 * @param password the admin's password, required
 */
@Validated
@ConfigurationProperties("curiositas.admin")
public record AdminProperties(
        @DefaultValue("admin") @NotBlank String username,
        @NotBlank String password) {
}
