package be.bornput.csv.csv.strategy.impl;

import be.bornput.csv.csv.config.CsvConfig;
import be.bornput.csv.csv.exception.ConversionException;
import be.bornput.csv.csv.strategy.api.DateStrategy;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DefaultDateStrategy implements DateStrategy {

    private final CsvConfig config;
    private final DateFormat format;

    public DefaultDateStrategy(CsvConfig config, String pattern) {
        this.config = config;
        this.format = new SimpleDateFormat(pattern);
    }

    @Override
    public String format(Date date, Field field) {
        return date == null ? "" : format.format(date);
    }

    @Override
    public Date parse(String raw, Field field) throws ConversionException {
        if (raw == null || raw.isEmpty()) return null;
        try {
            return format.parse(raw);
        } catch (ParseException e) {
            throw new ConversionException(e);
        }
    }
}
