package be.bornput.csv.csv.strategy.impl;

import be.bornput.csv.csv.config.CsvConfig;
import be.bornput.csv.csv.exception.ElementConversionException;
import be.bornput.csv.csv.strategy.api.EmbeddedStrategy;
import be.bornput.csv.csv.util.EmbeddedUtils;

import java.lang.reflect.Field;

public class DefaultEmbeddedStrategy implements EmbeddedStrategy {

    private final CsvConfig config;

    public DefaultEmbeddedStrategy(CsvConfig config) {
        this.config = config;
    }

    @Override
    public String flatten(Object value, Field parentField, CsvConfig config) throws ElementConversionException {
        return EmbeddedUtils.flattenToCsvString(value, parentField, config);
    }
}
