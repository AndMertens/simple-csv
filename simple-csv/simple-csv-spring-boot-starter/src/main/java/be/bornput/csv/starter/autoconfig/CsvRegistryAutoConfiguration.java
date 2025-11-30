package be.bornput.csv.starter.autoconfig;

import be.bornput.csv.registry.CsvRegistry;
import be.bornput.csv.starter.loader.CsvRegistryLoaderFactory;
import be.bornput.csv.starter.properties.SimpleCsvProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(SimpleCsvProperties.class)
public class CsvRegistryAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(CsvRegistryAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean(CsvRegistry.class)
    public CsvRegistry csvRegistry(SimpleCsvProperties props) {
        log.info("Auto-configuring CsvRegistry with base-packages: {}", props.getBasePackages());
        return CsvRegistryLoaderFactory.create(props);
    }
}
