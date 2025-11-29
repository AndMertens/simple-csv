package be.bornput.csv.csv.converter.simple;

public final class EnumConverter {
    public static Object convert(Class<?> type, String raw) {
        return Enum.valueOf(type.asSubclass(Enum.class), raw);
    }
}
