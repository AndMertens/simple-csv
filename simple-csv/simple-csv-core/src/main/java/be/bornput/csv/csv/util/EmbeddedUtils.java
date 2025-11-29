package be.bornput.csv.csv.util;

import be.bornput.csv.csv.annotations.CsvIgnore;
import be.bornput.csv.csv.annotations.CsvOrder;
import be.bornput.csv.csv.config.CsvConfig;
import be.bornput.csv.csv.converter.ValueConverter;
import be.bornput.csv.csv.exception.ElementConversionException;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Utilities to handle embedded objects in CSV (flattening them into CSV fields).
 * Fully strategy-aware using CsvConfig.
 */
public final class EmbeddedUtils {

    private EmbeddedUtils() {}

    /**
     * Flatten an embedded object into CSV string values (ordered by @CsvOrder).
     */
    public static List<String> flattenObject(Object obj, Field parentField, CsvConfig config) throws ElementConversionException {
        if (obj == null) {
            // Return empty entries matching the embedded field count
            return Collections.nCopies(countEmbeddedFields(parentField), "");
        }

        List<Field> fields = FieldMappingUtils.getAllFields(obj.getClass());
        fields.removeIf(f -> f.isAnnotationPresent(CsvIgnore.class));

        // Sort fields by @CsvOrder
        fields.sort(Comparator.comparingInt(f ->
                f.isAnnotationPresent(CsvOrder.class) ? f.getAnnotation(CsvOrder.class).value() : Integer.MAX_VALUE
        ));

        List<String> result = new ArrayList<>();
        for (Field f : fields) {
            f.setAccessible(true);
            try {
                Object value = f.get(obj);
                result.add(ValueConverter.toString(value, f, config));  // Pass CsvConfig here
            } catch (Exception ex) {
                throw new ElementConversionException(
                        "Failed to flatten field '" + f.getName() + "' of embedded object " +
                                parentField.getName(), ex
                );
            }
        }

        return result;
    }

    /**
     * Flatten an embedded object into a single CSV string using the configured delimiter.
     */
    public static String flattenToCsvString(Object obj, Field parentField, CsvConfig config) throws ElementConversionException {
        List<String> parts = flattenObject(obj, parentField, config);
        return String.join(String.valueOf(config.getDelimiter()), parts);
    }

    /**
     * Count the number of CSV fields an embedded object contributes.
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