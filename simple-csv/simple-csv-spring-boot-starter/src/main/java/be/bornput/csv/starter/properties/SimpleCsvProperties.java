package be.bornput.csv.starter.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "simple-csv")
public class SimpleCsvProperties {

    /**
     * Base packages to scan for @CsvGeneratedMapper
     */
    private List<String> basePackages = List.of("be.bornput.csv");

    public List<String> getBasePackages() {
        return basePackages;
    }

    public void setBasePackages(List<String> basePackages) {
        this.basePackages = basePackages;
    }
}
