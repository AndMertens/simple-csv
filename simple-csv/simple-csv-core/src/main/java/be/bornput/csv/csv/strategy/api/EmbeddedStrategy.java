package be.bornput.csv.csv.strategy.api;

import be.bornput.csv.csv.exception.ConversionException;

public interface EmbeddedStrategy {
    String flatten(Object obj, java.lang.reflect.Field parentField) throws ConversionException;
}
