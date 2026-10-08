package se.pbt.curiositas.security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Keeps the database open for reading and closed for writing. Anyone may read; creating,
 * replacing and deleting require the admin account through HTTP Basic authentication.
 */
@Configuration
@EnableConfigurationProperties(AdminProperties.class)
public class SecurityConfig {

    /**
     * Defines which requests need authentication and how failures are answered.
     *
     * <p>CSRF protection is off because the API keeps no session and uses no cookies: every write
     * carries its own credentials, so another site cannot make a browser send them unknowingly.
     *
     * @param http              Spring Security's builder for the filter chain
     * @param exceptionResolver resolves errors the same way as in controllers, so a failed login
     *                          is answered with Problem Details like every other error
     * @return the security filter chain
     */
    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) {
        AuthenticationEntryPoint problemDetails =
                (request, response, exception) -> exceptionResolver.resolveException(request, response, null, exception);

        return http
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(HttpMethod.GET, "/**").permitAll()
                        .requestMatchers(HttpMethod.HEAD, "/**").permitAll()
                        .anyRequest().authenticated())
                .httpBasic(basic -> basic.authenticationEntryPoint(problemDetails))
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(problemDetails))
                .sessionManagement(sessions -> sessions.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }

    /**
     * Provides the single admin account. The password is hashed at startup, so the plain text is
     * never kept in the user store.
     *
     * @param admin           the configured username and password
     * @param passwordEncoder hashes the password
     * @return the user store with the admin account
     */
    @Bean
    UserDetailsService userDetailsService(AdminProperties admin, PasswordEncoder passwordEncoder) {
        return new InMemoryUserDetailsManager(User.withUsername(admin.username())
                .password(passwordEncoder.encode(admin.password()))
                .roles("ADMIN")
                .build());
    }

    /**
     * Hashes passwords with BCrypt, prefixed with the algorithm name so it can be changed later
     * without invalidating existing hashes.
     *
     * @return the password encoder
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
