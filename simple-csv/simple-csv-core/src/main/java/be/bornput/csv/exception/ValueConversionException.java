package be.bornput.csv.exception;

public class ValueConversionException extends ConversionException {
    public ValueConversionException(String message) { super(message); }
    public ValueConversionException(String message, Throwable cause) { super(message, cause); }
}