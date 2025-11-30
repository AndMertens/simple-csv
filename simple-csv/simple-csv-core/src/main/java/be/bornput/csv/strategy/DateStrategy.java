package be.bornput.csv.strategy;

import be.bornput.csv.exception.ConversionException;

import java.lang.reflect.Field;
import java.util.Date;

public interface DateStrategy {
    String format(Date format, Field field);

    Date parse(String raw, Field field) throws ConversionException;   // <-- new
}
