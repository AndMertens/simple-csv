package be.bornput.csv.starter.loader;

import be.bornput.csv.registry.CsvRegistryLoader;
import be.bornput.csv.registry.CsvRegistry;
import be.bornput.csv.starter.properties.SimpleCsvProperties;

import java.util.List;

public class CsvRegistryLoaderFactory {

    public static CsvRegistry create(SimpleCsvProperties props) {
        List<String> listBasePackages = props.getBasePackages();
        String[] arr = listBasePackages.toArray(String[]::new);
        return new CsvRegistryLoader(arr);
    }
}