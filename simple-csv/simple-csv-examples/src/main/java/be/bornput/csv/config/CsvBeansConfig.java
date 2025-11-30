package be.bornput.csv.config;

import be.bornput.csv.registry.CsvRegistry;
import be.bornput.csv.registry.CsvRegistryLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"be.bornput.csv.model" })     // <= mapper is here
public class CsvBeansConfig {

    @Bean
    public CsvConfig csvConfig() {
        // You can customize this for your Spring Boot application
        return CsvConfigFactory.defaultConfig();
    }

    @Bean
    public CsvRegistry csvRegistry() {
        return new CsvRegistryLoader("be.bornput.csv.generated"); // package with generated mappers
    }

}
