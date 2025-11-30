package be.bornput.csv.reflection.strategy.impl;

import be.bornput.csv.reflection.annotation.CsvQuote;
import be.bornput.csv.config.CsvConfig;
import be.bornput.csv.strategy.QuoteStrategy;

import java.lang.reflect.Field;

public class DefaultQuoteStrategy implements QuoteStrategy {

    private final CsvConfig config;

    public DefaultQuoteStrategy(CsvConfig config) {
        this.config = config;
    }

    @Override
    public String applyQuotes(String raw, Field field) {
        boolean forceQuote = field.isAnnotationPresent(CsvQuote.class);
        if (forceQuote || raw.contains(String.valueOf(config.getDelimiter()))
                || raw.contains("\n") || raw.contains("\"")) {
            raw = raw.replace("\"", "\"\""); // escape quotes
            return "\"" + raw + "\"";
        }
        return raw;
    }
}