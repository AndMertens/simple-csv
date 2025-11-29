package be.bornput.csv.csv.converter;

public class PrimitiveConverterHandler {

    public static Object convert(Class<?> type, String raw) {

        if (type == String.class) return String.valueOf(raw);
        if (type == Integer.class || type == int.class) return Integer.parseInt(raw);
        if (type == Long.class || type == long.class) return Long.parseLong(raw);
        if (type == Double.class || type == double.class) return Double.parseDouble(raw);
        if (type == Boolean.class || type == boolean.class) return Boolean.parseBoolean(raw);

        return raw;
    }
}
