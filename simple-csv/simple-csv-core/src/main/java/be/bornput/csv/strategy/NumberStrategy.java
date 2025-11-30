package be.bornput.csv.strategy;

import be.bornput.csv.exception.ConversionException;

import java.lang.reflect.Field;

public interface NumberStrategy {
    String format(Number value, Field field);
    Object parse(String raw, Field field) throws ConversionException;   // <-- new
}
