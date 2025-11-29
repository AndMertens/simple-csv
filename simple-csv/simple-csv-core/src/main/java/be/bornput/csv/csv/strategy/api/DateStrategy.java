package be.bornput.csv.csv.strategy.api;

import be.bornput.csv.csv.exception.ValueConversionException;

import java.lang.reflect.Field;

public interface DateStrategy {
    String format(Object value, Field field) throws ValueConversionException;
    Object parse(String raw, Field field) throws ValueConversionException;   // <-- new
}
