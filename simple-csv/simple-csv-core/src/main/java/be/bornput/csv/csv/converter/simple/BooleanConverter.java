package be.bornput.csv.csv.converter.simple;

public final class BooleanConverter {
    public static Boolean convert(String raw) {
        return Boolean.parseBoolean(raw);
    }
}
