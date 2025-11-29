package be.bornput.csv.config;


import be.bornput.csv.csv.config.CsvConfig;
import be.bornput.csv.csv.config.CsvConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CsvLibraryConfig {

    @Bean
    public CsvConfig csvConfig() {
        // You can customize this for your Spring Boot application
        return CsvConfigFactory.defaultConfig();
    }
}
