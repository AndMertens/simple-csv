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

    public static List<String> flattenObject(Object obj, Field parentField, CsvConfig config)
            throws ElementConversionException {

        if (obj == null) {
            return Collections.nCopies(countEmbeddedFields(parentField), "");
        }

        List<Field> fields = FieldMappingUtils.getAllFields(obj.getClass());
        fields.removeIf(f -> f.isAnnotationPresent(CsvIgnore.class));

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
                result.add(ValueConverter.toString(value, f, config));
            } catch (Exception ex) {
                throw new ElementConversionException(
                        "Failed to flatten field '" + f.getName()
                                + "' of embedded object " + parentField.getName(), ex
                );
            }
        }

        return result;
    }

    public static String flattenToCsvString(Object obj, Field parentField, CsvConfig config)
            throws ElementConversionException {
        return String.join(",", flattenObject(obj, parentField, config));
    }

    private static int countEmbeddedFields(Field parentField) {
        return (int) FieldMappingUtils
                .getAllFields(parentField.getType())
                .stream()
                .filter(f -> !f.isAnnotationPresent(CsvIgnore.class))
                .count();
    }
}