package se.pbt.curiositas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the Curiositas API, a public database of curious historical people.
 */
@SpringBootApplication
public class CuriositasApplication {

    /**
     * Starts the application.
     *
     * @param args command line arguments passed on to Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(CuriositasApplication.class, args);
    }
}
