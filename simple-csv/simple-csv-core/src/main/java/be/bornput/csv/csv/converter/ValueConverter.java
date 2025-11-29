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

    // ---------------- Entry Point: CSV → Object ----------------
    public static Object convertValue(Field field, String raw, CsvConfig config) throws ConversionException {
        Class<?> type = field.getType();

        raw = DefaultValueResolver.resolve(field, raw);
        Object empty = EmptyHandler.handle(type, raw);
        if (empty != EmptyHandler.NOT_EMPTY) return empty;

        // List or Array
        if (TypeInspector.isList(field)) return ListConverter.convert(field, raw, config);
        if (TypeInspector.isArray(field)) return ListConverter.ArrayConverter.convert(field, raw, config);

        // Embedded POJO
        if (TypeInspector.isEmbedded(field)) {
            try {
                return field.getType().getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new ConversionException("Failed to instantiate embedded object for field " + field.getName(), e);
            }
        }

        // Date
        if (field.isAnnotationPresent(CsvDate.class)) {
            return config.getDateStrategy().parse(raw, field);
        }

        // Number
        if (field.isAnnotationPresent(CsvNumber.class)) {
            return config.getNumberStrategy().parse(raw, field);
        }

        // Enum
        if (type.isEnum()) return Enum.valueOf(type.asSubclass(Enum.class), raw);

        // Primitive / wrapper / String
        return PrimitiveConverterHandler.convert(type, raw);
    }

    // ---------------- Object → CSV String ----------------
    public static String toString(Object value, Field field, CsvConfig config) throws ConversionException {
        if (value == null) return "";

        // Date
        if (field.isAnnotationPresent(CsvDate.class)) {
            switch (value) {
                case Date date ->        { return config.getDateStrategy().format(date, field); }
                case LocalDate ld ->     { return ld.format(DateTimeFormatter.ofPattern(field.getAnnotation(CsvDate.class).dateFormat())); }
                case LocalDateTime ldt ->{ return ldt.format(DateTimeFormatter.ofPattern(field.getAnnotation(CsvDate.class).dateFormat())); }
                default -> throw new ValueConversionException(
                        "Field " + field.getName() + " uses @CsvDate but type is unsupported: " + value.getClass().getName()
                );
            }
        }

        // Number
        if (field.isAnnotationPresent(CsvNumber.class)) {
            if (value instanceof Number number) {
                return config.getNumberStrategy().format(number, field);
            }
            return config.getNumberStrategy().format(null, field);
        }

        // Embedded
        if (field.isAnnotationPresent(CsvEmbedded.class)) {
            return config.getEmbeddedStrategy().flatten(value, field, config);
        }

        // Fallback: String/primitive
        return value.toString();
    }

    public static Object convertValueForType(Class<?> type, Field parentField, String raw, CsvConfig config)
            throws ConversionException {

        // Resolve default + empty
        raw = DefaultValueResolver.resolve(parentField, raw);
        Object empty = EmptyHandler.handle(type, raw);
        if (empty != EmptyHandler.NOT_EMPTY) return empty;

        // Enum
        if (type.isEnum()) return Enum.valueOf(type.asSubclass(Enum.class), raw);

        // Date
        if (parentField.isAnnotationPresent(CsvDate.class) && type.equals(Date.class)) {
            return config.getDateStrategy().parse(raw, parentField);
        }

        // Number
        if (parentField.isAnnotationPresent(CsvNumber.class) &&
                Number.class.isAssignableFrom(type)) {
            return config.getNumberStrategy().parse(raw, parentField);
        }

        // Primitive / wrapper / String
        return PrimitiveConverterHandler.convert(type, raw);
    }

    // ---------------- Type Check ----------------
    public static boolean isEmbedded(Field f) {
        return TypeInspector.isEmbedded(f);
    }

    // ---------------- Helper Classes ----------------
    private static final class DefaultValueResolver {
        private DefaultValueResolver() {}
        static String resolve(Field field, String raw) {
            if ((raw == null || raw.isEmpty()) && field.isAnnotationPresent(CsvColumn.class)) {
                String def = field.getAnnotation(CsvColumn.class).defaultValue();
                if (!def.isEmpty()) return def;
            }
            return raw;
        }
    }

    private static final class EmptyHandler {
        private static final Object NOT_EMPTY = new Object();
        private static final java.util.Map<Class<?>, Object> PRIMITIVE_DEFAULTS = java.util.Map.of(
                boolean.class, false, byte.class, (byte)0, short.class, (short)0,
                int.class, 0, long.class, 0L, float.class, 0f, double.class, 0d, char.class, '\0'
        );
        private EmptyHandler() {}
        static Object handle(Class<?> type, String raw) {
            if (raw != null && !raw.isEmpty()) return NOT_EMPTY;
            return type.isPrimitive() ? PRIMITIVE_DEFAULTS.get(type) : null;
        }
    }

    private static final class TypeInspector {
        private TypeInspector() {}
        static boolean isList(Field f) { return java.util.List.class.isAssignableFrom(f.getType()); }
        static boolean isArray(Field f) { return f.getType().isArray(); }
        static boolean isEmbedded(Field f) {
            Class<?> t = f.getType();
            if (t.isPrimitive() || t == String.class) return false;
            if (Number.class.isAssignableFrom(t) || t == Boolean.class) return false;
            if (Date.class.equals(t) || t.isEnum()) return false;
            if (java.util.Collection.class.isAssignableFrom(t) || t.isArray()) return false;
            return t.getPackage() == null || !t.getPackage().getName().startsWith("java.");
        }
    }
}


