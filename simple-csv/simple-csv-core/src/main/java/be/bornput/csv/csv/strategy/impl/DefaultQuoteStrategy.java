package be.bornput.csv.csv.strategy.impl;

import be.bornput.csv.csv.annotations.CsvQuote;
import be.bornput.csv.csv.config.CsvConfig;
import be.bornput.csv.csv.strategy.api.QuoteStrategy;

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