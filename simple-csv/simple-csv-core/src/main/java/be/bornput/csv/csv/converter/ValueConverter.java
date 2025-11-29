package be.bornput.csv.csv.converter;

import be.bornput.csv.converter.simple.EmbeddedConverter;
import be.bornput.csv.csv.annotations.CsvColumn;
import be.bornput.csv.csv.annotations.CsvDate;
import be.bornput.csv.csv.annotations.CsvEmbedded;
import be.bornput.csv.csv.annotations.CsvNumber;
import be.bornput.csv.csv.config.CsvConfig;
import be.bornput.csv.csv.converter.simple.EnumConverter;
import be.bornput.csv.csv.exception.ConversionException;

import java.lang.reflect.*;
import java.util.*;

/*
 * Converts CSV string values into Java objects.
 * Supports primitives, wrappers, enums, Date, List, Array, and embedded objects.
 * Use a dedicated exception hierarchy for better error handling.
 */

/**
 * Converts CSV string values into Java objects using strategies from CsvConfig.
 */
public final class ValueConverter {

    private ValueConverter() { /* static helpers only */ }

    public static Object convertValue(Field field, String raw, CsvConfig config) throws ConversionException {
        // default + empty handling
        raw = DefaultValueResolver.resolve(field, raw);
        Object empty = EmptyHandler.handle(field.getType(), raw);
        if (empty != EmptyHandler.NOT_EMPTY) return empty;

        // list
        if (TypeInspector.isList(field)) {
            return ListConverter.convert(field, raw, config);
        }

        // array
        if (TypeInspector.isArray(field)) {
            return ListConverter.ArrayConverter.convert(field, raw, config);
        }

        // embedded
        if (TypeInspector.isEmbedded(field)) {
            return EmbeddedConverter.convert(field, raw, config);
        }

        // date
        if (field.isAnnotationPresent(CsvDate.class)) {
            return config.getDateStrategy().parse(raw, field);
        }

        // number
        if (field.isAnnotationPresent(CsvNumber.class)) {
            return config.getNumberStrategy().parse(raw, field);
        }

        // enum
        if (field.getType().isEnum()) {
            return EnumConverter.convert(field.getType(), raw);
        }

        // primitive / wrapper / String
        return PrimitiveConverterHandler.convert(field.getType(), raw);
    }

    public static Object convertValueForType(Class<?> type, Field parentField, String raw, CsvConfig config)
            throws ConversionException {

        // Default + empty logic
        raw = DefaultValueResolver.resolve(parentField, raw);
        Object empty = EmptyHandler.handle(type, raw);
        if (empty != EmptyHandler.NOT_EMPTY) return empty;

        // Enum
        if (type.isEnum()) {
            return Enum.valueOf(type.asSubclass(Enum.class), raw);
        }

        // Date
        if (parentField.isAnnotationPresent(CsvDate.class) && type.equals(java.util.Date.class)) {
            return config.getDateStrategy().parse(raw, parentField);
        }

        // Number
        if (parentField.isAnnotationPresent(CsvNumber.class)) {
            return config.getNumberStrategy().parse(raw, parentField);
        }

        // Simple types
        return PrimitiveConverterHandler.convert(type, raw);
    }


    public static String toString(Object value, Field field, CsvConfig config) throws ConversionException {
        if (value == null) return "";

        // DATE
        if (field.isAnnotationPresent(CsvDate.class)) {
            return config.getDateStrategy().format(value, field);
        }

        // NUMBER
        if (field.isAnnotationPresent(CsvNumber.class) && value instanceof Number) {
            return config.getNumberStrategy().format(value, field);
        }

        // EMBEDDED
        if (field.isAnnotationPresent(CsvEmbedded.class)) {
            return config.getEmbeddedStrategy().flatten(value, field);
        }

        // default: convert to string and apply quote strategy
        String str = value.toString();
        return config.getQuoteStrategy().escape(str);
    }

    public static boolean isEmbedded(Field f) {
        return TypeInspector.isEmbedded(f);
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
    private static final class TypeInspector {
        private TypeInspector() {}

        static boolean isList(Field f) {
            return List.class.isAssignableFrom(f.getType());
        }

        static boolean isArray(Field f) {
            return f.getType().isArray();
        }

        static boolean isEmbedded(Field f) {
            Class<?> t = f.getType();
            if (t.isPrimitive() || String.class.equals(t)) return false;
            if (Number.class.isAssignableFrom(t) || Boolean.class.equals(t)) return false;
            if (Date.class.equals(t) || t.isEnum()) return false;
            if (Collection.class.isAssignableFrom(t) || t.isArray()) return false;
            return t.getPackage() == null || !t.getPackage().getName().startsWith("java.");
        }
    }


}


