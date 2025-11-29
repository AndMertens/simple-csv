package be.bornput.csv.csv.strategy.impl;

import be.bornput.csv.csv.annotations.CsvNumber;
import be.bornput.csv.csv.config.CsvConfig;
import be.bornput.csv.csv.exception.ValueConversionException;
import be.bornput.csv.csv.strategy.api.NumberStrategy;

import java.lang.reflect.Field;
import java.text.DecimalFormat;

public class DefaultNumberStrategy implements NumberStrategy {
    private final CsvConfig config;

    public DefaultNumberStrategy(CsvConfig config) {
        this.config = config;
    }


    @Override
    public String format(Object value, Field field) throws ValueConversionException {
        if (value == null) return "";

        CsvNumber ann = field.getAnnotation(CsvNumber.class);
        String pattern = ann != null ? ann.numberFormat() : "#.##";

        try {
            return new DecimalFormat(pattern).format(value);
        } catch (Exception e) {
            throw new ValueConversionException(
                    "Failed to format number for field " + field.getName(), e
            );
        }
    }

    @Override
    public Object parse(String raw, Field field) throws ValueConversionException {
        if (raw == null || raw.isEmpty()) return null;

        CsvNumber ann = field.getAnnotation(CsvNumber.class);
        try {
            String pattern = ann != null ? ann.numberFormat() : "#.##";
            return new DecimalFormat(pattern).parse(raw);
        } catch (Exception e) {
            throw new ValueConversionException("Failed to parse number for field " + field.getName(), e);
        }
    }
}
