package se.pbt.curiositas.data;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Makes the data folder settings available to the export and the data commands.
 */
@Configuration
@EnableConfigurationProperties(DataProperties.class)
public class DataConfig {
}
