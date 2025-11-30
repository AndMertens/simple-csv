package be.bornput.csv.reflection.converter;

import java.util.Map;
import java.util.function.Function;

public class PrimitiveConverterHandler {

    private PrimitiveConverterHandler() {}

    private static final Map<Class<?>, Function<String, Object>> CONVERTERS;

    static {

        CONVERTERS = Map.ofEntries(
                Map.entry(byte.class, Byte::parseByte),
                Map.entry(Byte.class, Byte::valueOf),
                Map.entry(short.class, Short::parseShort),
                Map.entry(Short.class, Short::valueOf),
                Map.entry(int.class, Integer::parseInt),
                Map.entry(Integer.class, Integer::valueOf),
                Map.entry(long.class, Long::parseLong),
                Map.entry(Long.class, Long::valueOf),
                Map.entry(float.class, Float::parseFloat),
                Map.entry(Float.class, Float::valueOf),
                Map.entry(double.class, Double::parseDouble),
                Map.entry(Double.class, Double::valueOf),
                Map.entry(boolean.class, Boolean::parseBoolean),
                Map.entry(Boolean.class, Boolean::valueOf),

                // For char and Character, method references are not directly available, keep small lambdas
                Map.entry(char.class,
                        s -> s.isEmpty() ? '\0' : s.trim().charAt(0)),
                            Map.entry(Character.class, s -> s.isEmpty() ? null : s.trim().charAt(0)),
                                Map.entry(String.class, s -> s));
    }

    public static Object convert(Class<?> type, String raw) {
        Function<String, Object> converter = CONVERTERS.get(type);
        if (converter != null) {
            return converter.apply(raw);
        }
        // fallback: return raw string
        return raw;
    }
}

