package be.bornput.csv.reflection.converter.simple;

public final class BooleanConverter {
    public static Boolean convert(String raw) {
        return Boolean.parseBoolean(raw);
    }
}
