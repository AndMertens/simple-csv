package be.bornput.csv.reflection.converter.util;

import be.bornput.csv.reflection.annotation.CsvIgnore;
import be.bornput.csv.reflection.annotation.CsvOrder;
import be.bornput.csv.config.CsvConfig;
import be.bornput.csv.reflection.converter.ValueConverter;
import be.bornput.csv.exception.ElementConversionException;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Utilities to handle embedded objects in CSV (flattening them into CSV fields).
 * Fully strategy-aware using CsvConfig.
 */
public final class EmbeddedUtils {

    private EmbeddedUtils() {}

    /**
     * Flatten an embedded object into CSV values (ordered by @CsvOrder).
     */
    public static List<String> flattenObject(Object obj, Field parentField, CsvConfig config)
            throws ElementConversionException {

        if (obj == null) {
            return Collections.nCopies(countEmbeddedFields(parentField), "");
        }

        List<Field> fields = FieldMappingUtils.getAllFields(obj.getClass());
        fields.removeIf(f -> f.isAnnotationPresent(CsvIgnore.class));

        // Sort by @CsvOrder
        fields.sort(Comparator.comparingInt(f ->
                f.isAnnotationPresent(CsvOrder.class)
                        ? f.getAnnotation(CsvOrder.class).value()
                        : Integer.MAX_VALUE
        ));

        List<String> result = new ArrayList<>();
        for (Field f : fields) {
            f.setAccessible(true);
            try {
                Object value = f.get(obj);
                if (FieldMappingUtils.isEmbedded(f)) {
                    result.addAll(flattenObject(value, f, config));
                } else {
                    result.add(ValueConverter.toString(value, f, config));
                }
            } catch (Exception ex) {
                throw new ElementConversionException(
                        "Failed to flatten field '" + f.getName() + "' of embedded object "
                                + parentField.getName(), ex
                );
            }
        }
        return result;
    }

    /**
     * Convert an embedded object to a CSV string.
     */
    public static String flattenToCsvString(Object obj, Field parentField, CsvConfig config)
            throws ElementConversionException {
        return String.join(",", flattenObject(obj, parentField, config));
    }

    /**
     * Count CSV columns contributed by an embedded field.
     */
    private static int countEmbeddedFields(Field parentField) {
        Class<?> type = parentField.getType();
        return (int) FieldMappingUtils
                .getAllFields(type)
                .stream()
                .filter(f -> !f.isAnnotationPresent(CsvIgnore.class))
                .count();
    }
}