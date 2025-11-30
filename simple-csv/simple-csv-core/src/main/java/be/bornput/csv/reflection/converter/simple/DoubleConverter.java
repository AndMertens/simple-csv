package be.bornput.csv.reflection.converter.simple;

public final class DoubleConverter {
    public static Double convert(String raw) {
        return Double.parseDouble(raw);
    }
}
