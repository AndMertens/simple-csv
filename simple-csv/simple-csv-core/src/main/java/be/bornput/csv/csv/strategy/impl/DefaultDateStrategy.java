package be.bornput.csv.csv.strategy.impl;

import be.bornput.csv.csv.annotations.CsvDate;
import be.bornput.csv.csv.exception.ValueConversionException;
import be.bornput.csv.csv.strategy.api.DateStrategy;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DefaultDateStrategy implements DateStrategy {


    public DefaultDateStrategy() {

    }

    @Override
    public String format(Object value, Field field) throws ValueConversionException {
        if (value == null) return "";

        CsvDate ann = field.getAnnotation(CsvDate.class);
        String pattern = ann != null ? ann.dateFormat() : "yyyy-MM-dd";

        try {
            return switch (value) {
                case LocalDate ld -> ld.format(DateTimeFormatter.ofPattern(pattern));
                case LocalDateTime ldt -> ldt.format(DateTimeFormatter.ofPattern(pattern));
                case Date d -> new SimpleDateFormat(pattern).format(d);
                default -> throw new ValueConversionException(
                        "Unsupported type for date formatting: " + value.getClass().getName()
                );
            };
        } catch (Exception e) {
            throw new ValueConversionException("Failed to format date for field " + field.getName(), e);
        }
    }

    public Object parse(String raw, Field field) throws ValueConversionException {
        if (raw == null || raw.isEmpty()) return null;

        CsvDate ann = field.getAnnotation(CsvDate.class);
        String pattern = ann != null ? ann.dateFormat() : "yyyy-MM-dd";

        try {
            // You can add support for LocalDate / LocalDateTime if needed
            return new SimpleDateFormat(pattern).parse(raw);
        } catch (Exception e) {
            throw new ValueConversionException("Failed to parse date for field " + field.getName(), e);
        }
    }


}