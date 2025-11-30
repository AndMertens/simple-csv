package be.bornput.csv.reflection.strategy.impl;

import be.bornput.csv.config.CsvConfig;
import be.bornput.csv.exception.ElementConversionException;
import be.bornput.csv.exception.ValueConversionException;
import be.bornput.csv.strategy.EmbeddedStrategy;
import be.bornput.csv.reflection.converter.util.EmbeddedUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class DefaultEmbeddedStrategy implements EmbeddedStrategy {

    private final CsvConfig config;

    public DefaultEmbeddedStrategy(CsvConfig config) {
        this.config = config;
    }

    @Override
    public String flatten(Object value, Field parentField, CsvConfig config) throws ElementConversionException {
        return EmbeddedUtils.flattenToCsvString(value, parentField, config);
    }

    public Object createInstance(Field field) throws ValueConversionException {
        Class<?> type = field.getType();
        try {
            Constructor<?> ctor = type.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (NoSuchMethodException e) {
            throw new ValueConversionException(
                    "Embedded field '" + field.getName() + "' of type " + type.getName()
                            + " requires a no-arg constructor.", e
            );
        } catch (Exception e) {
            throw new ValueConversionException(
                    "Failed to create embedded field '" + field.getName() + "' of type " + type.getName(), e
            );
        }
    }

}
