package be.bornput.csv.csv.converter;

import be.bornput.csv.csv.config.CsvConfig;
import be.bornput.csv.csv.exception.ConversionException;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

public final class ListConverter {

    private ListConverter() {}

    /**
     * Converts a CSV cell into a List<T>.
     * Example: "10;20;30" → List<Integer>
     */
    public static List<?> convert(Field field, String raw, CsvConfig config) throws ConversionException {
        try {
            // Determine the list element type T from List<T>
            Class<?> elementType = getListElementType(field);

            // Split by delimiter (could be configurable later)
            String[] parts = raw.split(";", -1);

            List<Object> result = new ArrayList<>(parts.length);

            for (String part : parts) {
                // Convert each item using the main converter pipeline
                Object converted = ValueConverter.convertValueForType(elementType, field, part, config);
                result.add(converted);
            }

            return result;

        } catch (Exception ex) {
            throw new ConversionException("Failed to convert list field '" + field.getName() + "'", ex);
        }
    }

    private static Class<?> getListElementType(Field field) {
        ParameterizedType pt = (ParameterizedType) field.getGenericType();
        return (Class<?>) pt.getActualTypeArguments()[0];
    }

    /**
     * Nested converter for arrays.
     */
    public static final class ArrayConverter {

        private ArrayConverter() {}

        /**
         * Converts a CSV cell into an array T[].
         * Example: "1;2;3" → Integer[]
         */
        public static Object convert(Field field, String raw, CsvConfig config) throws ConversionException {
            try {
                Class<?> componentType = field.getType().getComponentType();
                String[] parts = raw.split(";", -1);

                Object array = java.lang.reflect.Array.newInstance(componentType, parts.length);

                for (int i = 0; i < parts.length; i++) {
                    String part = parts[i];
                    Object converted = ValueConverter.convertValueForType(componentType, field, part, config);
                    java.lang.reflect.Array.set(array, i, converted);
                }

                return array;

            } catch (Exception ex) {
                throw new ConversionException(
                        "Failed to convert array field '" + field.getName() + "'",
                        ex
                );
            }
        }
    }
}