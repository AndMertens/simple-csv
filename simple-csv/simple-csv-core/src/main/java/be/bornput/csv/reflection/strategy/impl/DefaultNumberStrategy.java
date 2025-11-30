package be.bornput.csv.reflection.strategy.impl;

import be.bornput.csv.config.CsvConfig;
import be.bornput.csv.exception.ConversionException;
import be.bornput.csv.strategy.NumberStrategy;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class DefaultNumberStrategy implements NumberStrategy {
    private final CsvConfig config;
    private final DecimalFormat format;

    public DefaultNumberStrategy(CsvConfig config, String pattern, Locale locale) {
        this.config = config;
        this.format = (DecimalFormat) NumberFormat.getNumberInstance(locale);
        this.format.applyPattern(pattern);
    }

    @Override
    public String format(Number value, Field field) {
        return value == null ? "" : format.format(value);
    }

    @Override
    public Number parse(String raw, Field field) throws ConversionException {
        if (raw == null || raw.isEmpty()) return null;
        try {
            return format.parse(raw);
        } catch (ParseException e) {
            throw new ConversionException(e);
        }
    }
}
