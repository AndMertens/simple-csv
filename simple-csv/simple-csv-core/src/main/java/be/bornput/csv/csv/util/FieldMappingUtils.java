package be.bornput.csv.csv.util;

import java.lang.reflect.Field;
import java.util.*;

import be.bornput.csv.csv.annotations.CsvColumn;
import be.bornput.csv.csv.annotations.CsvEmbedded;
import be.bornput.csv.csv.annotations.CsvOrder;
import be.bornput.csv.csv.annotations.CsvIgnore;

public final class FieldMappingUtils {

    private FieldMappingUtils() {}

    /**
     * Returns the CSV header name for a field.
     * Uses @CsvColumn.name() if present, otherwise field name.
     */
    public static String getHeaderName(Field field) {
        CsvColumn annotation = field.getAnnotation(CsvColumn.class);
        if (annotation != null && !annotation.name().isEmpty()) {
            return annotation.name();
        }
        return field.getName();
    }


    /** Get all fields including superclasses */
    public static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field f : current.getDeclaredFields()) {
                if (!f.isAnnotationPresent(CsvIgnore.class)) {
                    fields.add(f);
                }
            }
            current = current.getSuperclass();
        }
        return fields;
    }

    /** Map CSV headers to fields, honoring @CsvColumn.name */
    public static Map<String, Field> mapHeaders(Class<?> clazz, List<String> headers) {
        List<Field> fields = getAllFields(clazz);
        Map<String, Field> map = new HashMap<>();
        for (String header : headers) {
            fields.stream()
                    .filter(f -> matchesHeader(f, header))
                    .findFirst()
                    .ifPresent(f -> map.put(header, f));
        }
        return map;
    }

    private static boolean matchesHeader(Field f, String header) {
        CsvColumn col = f.getAnnotation(CsvColumn.class);
        if (col != null && !col.name().isEmpty()) return col.name().equalsIgnoreCase(header);
        return f.getName().equalsIgnoreCase(header);
    }

    /** Flatten embedded objects recursively, producing keys like 'address.street' */
    public static Map<String, Field> mapEmbeddedFields(Class<?> clazz, String prefix) {
        Map<String, Field> map = new LinkedHashMap<>();
        for (Field field : getAllFields(clazz)) {
            if (field.isAnnotationPresent(CsvEmbedded.class) || isEmbedded(field)) {
                String nestedPrefix = prefix.isEmpty() ? field.getName() : prefix + "." + field.getName();
                map.putAll(mapEmbeddedFields(field.getType(), nestedPrefix));
            } else {
                String key = prefix.isEmpty() ? field.getName() : prefix + "." + field.getName();
                map.put(key, field);
            }
        }
        return map;
    }

    public static List<Field> getOrderedFields(Class<?> type) {
        List<Field> fields = getAllFields(type);

        fields.removeIf(f -> f.isAnnotationPresent(CsvIgnore.class));

        fields.sort((f1, f2) -> {
            int o1 = f1.isAnnotationPresent(CsvOrder.class)
                    ? f1.getAnnotation(CsvOrder.class).value()
                    : Integer.MAX_VALUE;
            int o2 = f2.isAnnotationPresent(CsvOrder.class)
                    ? f2.getAnnotation(CsvOrder.class).value()
                    : Integer.MAX_VALUE;
            return Integer.compare(o1, o2);
        });

        return fields;
    }

    /**
     * Determines if a field should be treated as an embedded (nested) object
     */
    public static boolean isEmbedded(Field f) {
        Class<?> t = f.getType();
        if (t.isPrimitive() || t.equals(String.class)) return false;
        if (Number.class.isAssignableFrom(t) || Boolean.class.equals(t)) return false;
        if (Date.class.equals(t) || t.isEnum()) return false;
        if (Collection.class.isAssignableFrom(t) || t.isArray()) return false;
        // User-defined classes outside java.* packages
        return t.getPackage() == null || !t.getPackage().getName().startsWith("java.");
    }

}
