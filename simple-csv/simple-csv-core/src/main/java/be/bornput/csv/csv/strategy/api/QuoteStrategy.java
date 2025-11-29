package be.bornput.csv.csv.strategy.api;

import java.lang.reflect.Field;

public interface QuoteStrategy {
    String applyQuotes(String raw, Field field);
}
