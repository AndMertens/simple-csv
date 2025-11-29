package be.bornput.csv.csv.strategy.impl;

import be.bornput.csv.csv.config.CsvConfig;
import be.bornput.csv.csv.strategy.api.QuoteStrategy;

public class DefaultQuoteStrategy implements QuoteStrategy {

    private final CsvConfig config;

    public DefaultQuoteStrategy(CsvConfig config) {
        this.config = config;
    }


    @Override
    public String escape(String value) {
        if (value == null) return "";

        boolean mustQuote = value.contains(String.valueOf(config.getDelimiter()) )||
                value.contains("\"") ||
                value.contains("\n");

        String escaped = value.replace("\"", "\"\"");

        return mustQuote ? "\"" + escaped + "\"" : escaped;
    }
}