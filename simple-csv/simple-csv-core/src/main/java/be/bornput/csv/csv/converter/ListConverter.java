package be.bornput.csv.csv.converter;

import be.bornput.csv.csv.config.CsvConfig;
import be.bornput.csv.csv.exception.ConversionException;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

public final class ListConverter {

    private ListConverter() {}

    public static <T> List<T> convert(Field field, String raw, CsvConfig config) throws ConversionException {
        try {
            Class<?> elementType = getListElementType(field);

            String[] parts = raw.split(String.valueOf(config.getListDelimiter()), -1); // configurable delimiter

            List<T> result = new ArrayList<>(parts.length);

            for (String part : parts) {
                @SuppressWarnings("unchecked")
                T converted = (T) ValueConverter.convertValueForType(elementType, field, part, config);
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

    /** Nested Array Converter */
    public static final class ArrayConverter {
        private ArrayConverter() {}

        public static Object convert(Field field, String raw, CsvConfig config) throws ConversionException {
            try {
                Class<?> componentType = field.getType().getComponentType();
                String[] parts = raw.split(String.valueOf(config.getListDelimiter()), -1);

                Object array = Array.newInstance(componentType, parts.length);

                for (int i = 0; i < parts.length; i++) {
                    Object converted = ValueConverter.convertValueForType(componentType, field, parts[i], config);
                    Array.set(array, i, converted);
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