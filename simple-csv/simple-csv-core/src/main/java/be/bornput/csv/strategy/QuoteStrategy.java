package be.bornput.csv.strategy;

import java.lang.reflect.Field;

public interface QuoteStrategy {
    String applyQuotes(String raw, Field field);
}
