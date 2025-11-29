package be.bornput.csv.csv.mapper;

import java.lang.reflect.Field;

import be.bornput.csv.csv.config.CsvConfig;
import be.bornput.csv.csv.converter.ValueConverter;
import be.bornput.csv.csv.exception.ConversionException;

public abstract class BaseCsvMapper {

    protected final CsvConfig config;

    protected BaseCsvMapper(CsvConfig config) {
        this.config = config;
    }

    protected Object convertValue(Field f, String raw) throws ConversionException {
        try {
            return ValueConverter.convertValue(f, raw, config);
        } catch (ConversionException e) {
            throw new ConversionException("Failed to convert field: " + f.getName(), e);
        }
    }

    protected String toCsvString(Object value, Field f) throws ConversionException {
        return ValueConverter.toString(value, f, config);
    }


}

