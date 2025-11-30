package be.bornput.csv.strategy;

import be.bornput.csv.config.CsvConfig;
import be.bornput.csv.exception.ConversionException;

import java.lang.reflect.Field;

public interface EmbeddedStrategy {
    String flatten(Object obj, java.lang.reflect.Field parentField, CsvConfig config) throws ConversionException;

    Object createInstance(Field field) throws ConversionException;
}
