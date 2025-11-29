package be.bornput.csv.csv.strategy.api;

import be.bornput.csv.csv.exception.ConversionException;
import be.bornput.csv.csv.exception.ValueConversionException;

import java.lang.reflect.Field;
import java.util.Date;

public interface DateStrategy {
    String format(Date format, Field field) throws ValueConversionException;

    Date parse(String raw, Field field) throws ConversionException;   // <-- new
}
