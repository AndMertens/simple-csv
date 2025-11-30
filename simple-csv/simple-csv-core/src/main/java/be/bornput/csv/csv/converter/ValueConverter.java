package be.bornput.csv.csv.converter;

import be.bornput.csv.csv.annotations.CsvColumn;
import be.bornput.csv.csv.annotations.CsvDate;
import be.bornput.csv.csv.annotations.CsvEmbedded;
import be.bornput.csv.csv.annotations.CsvNumber;
import be.bornput.csv.csv.config.CsvConfig;
import be.bornput.csv.csv.exception.ConversionException;
import be.bornput.csv.csv.exception.ValueConversionException;

import java.lang.reflect.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Converts CSV string values into Java objects using strategies from CsvConfig.
 */
public final class ValueConverter {

    private ValueConverter() {}

    public static Object convertValue(Field field, String raw, CsvConfig config) throws ConversionException {
        Class<?> type = field.getType();

        raw = DefaultValueResolver.resolve(field, raw);
        Object empty = EmptyHandler.handle(type, raw);
        if (empty != EmptyHandler.NOT_EMPTY) return empty;

        try {
            // LIST
            if (List.class.isAssignableFrom(type)) {
                return ListConverter.convert(field, raw, config);
            }

            // ARRAY
            if (type.isArray()) {
                return ListConverter.ArrayConverter.convert(field, raw, config);
            }

            // EMBEDDED
            if (TypeInspector.isEmbedded(field)) {
                return config.getEmbeddedStrategy().createInstance(field);
            }

            // DATE
            if (field.isAnnotationPresent(CsvDate.class) && type.equals(Date.class)) {
                return config.getDateStrategy().parse(raw, field);
            }

            // NUMBER
            if (field.isAnnotationPresent(CsvNumber.class)) {
                return config.getNumberStrategy().parse(raw, field);
            }

            // ENUM
            if (type.isEnum()) {
                return Enum.valueOf(type.asSubclass(Enum.class), raw);
            }

            // DEFAULT primitive/string
            return PrimitiveConverterHandler.convert(type, raw);

        } catch (Exception e) {
            throw new ValueConversionException(
                    "Failed to convert value '" + raw + "' for field " + field.getName(), e
            );
        }
    }

    public static Object convertValueForType(Class<?> type, Field parentField, String raw, CsvConfig config)
            throws ConversionException {

        raw = DefaultValueResolver.resolve(parentField, raw);
        Object empty = EmptyHandler.handle(type, raw);
        if (empty != EmptyHandler.NOT_EMPTY) return empty;

        // LIST
        if (List.class.isAssignableFrom(type)) {
            return ListConverter.convert(parentField, raw, config);
        }

        // ARRAY
        if (type.isArray()) {
            return ListConverter.ArrayConverter.convert(parentField, raw, config);
        }

        // ENUM
        if (type.isEnum()) {
            return Enum.valueOf(type.asSubclass(Enum.class), raw);
        }

        // DATE
        if (parentField.isAnnotationPresent(CsvDate.class) && type.equals(Date.class)) {
            return config.getDateStrategy().parse(raw, parentField);
        }

        // NUMBER
        if (parentField.isAnnotationPresent(CsvNumber.class)) {
            return config.getNumberStrategy().parse(raw, parentField);
        }

        // EMBEDDED
        if (TypeInspector.isEmbedded(parentField)) {
            return config.getEmbeddedStrategy().createInstance(parentField);
        }

        // DEFAULT primitive/string
        return PrimitiveConverterHandler.convert(type, raw);
    }

    public static String toString(Object value, Field field, CsvConfig config) throws ConversionException {
        if (value == null) return "";

        // DATE
        if (field.isAnnotationPresent(CsvDate.class)) {
            switch (value) {
                case Date date -> {
                    return config.getDateStrategy().format(date, field);
                }
                case LocalDate ld -> {
                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern(
                            field.getAnnotation(CsvDate.class).dateFormat()
                    );
                    return ld.format(fmt);
                }
                case LocalDateTime ldt -> {
                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern(
                            field.getAnnotation(CsvDate.class).dateFormat()
                    );
                    return ldt.format(fmt);
                }
                default -> throw new ValueConversionException(
                        "Field " + field.getName() + " uses @CsvDate but value type is not supported: "
                                + value.getClass().getName()
                );
            }
        }

        // NUMBER
        if (field.isAnnotationPresent(CsvNumber.class)) {
            if (value instanceof Number number) {
                return config.getNumberStrategy().format(number, field);
            } else {
                return config.getNumberStrategy().format(null, field);
            }
        }

        // EMBEDDED
        if (field.isAnnotationPresent(CsvEmbedded.class)) {
            return config.getEmbeddedStrategy().flatten(value, field, config);
        }

        return value.toString();
    }

    /* ================== Default Value Resolver ================== */
    private static final class DefaultValueResolver {
        private DefaultValueResolver() {}
        static String resolve(Field field, String raw) {
            String defaultValue = "";
            if (field.isAnnotationPresent(CsvColumn.class)) {
                defaultValue = field.getAnnotation(CsvColumn.class).defaultValue();
            }
            return (raw == null || raw.isEmpty()) && !defaultValue.isEmpty() ? defaultValue : raw;
        }
    }

    /* ================== Empty Handler ================== */
    private static final class EmptyHandler {
        private static final Object NOT_EMPTY = new Object();
        private static final Map<Class<?>, Object> PRIMITIVE_DEFAULTS = Map.of(
                boolean.class, false,
                byte.class, (byte) 0,
                short.class, (short) 0,
                int.class, 0,
                long.class, 0L,
                float.class, 0f,
                double.class, 0d,
                char.class, '\0'
        );
        private EmptyHandler() {}
        static Object handle(Class<?> type, String raw) {
            if (raw != null && !raw.isEmpty()) return NOT_EMPTY;
            return type.isPrimitive() ? PRIMITIVE_DEFAULTS.get(type) : null;
        }
    }

    /* ================== Type Inspector ================== */
    public static final class TypeInspector {
        private TypeInspector() {}
        public static boolean isEmbedded(Field f) {
            Class<?> t = f.getType();
            return !(t.isPrimitive() || t.equals(String.class) || Number.class.isAssignableFrom(t)
                    || Boolean.class.equals(t) || Date.class.equals(t) || t.isEnum()
                    || Collection.class.isAssignableFrom(t) || t.isArray()
                    || (t.getPackage() != null && t.getPackage().getName().startsWith("java.")));
        }
    }
}


