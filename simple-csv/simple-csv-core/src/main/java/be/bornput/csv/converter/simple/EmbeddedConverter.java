package be.bornput.csv.converter.simple;

import be.bornput.csv.csv.config.CsvConfig;
import be.bornput.csv.csv.converter.ValueConverter;
import be.bornput.csv.csv.exception.ConversionException;
import be.bornput.csv.csv.util.FieldMappingUtils;

import java.lang.reflect.Field;
import java.util.List;

public final class EmbeddedConverter {

    private EmbeddedConverter() {}

    /**
     * Converts a CSV raw value into an embedded object instance.
     *
     * Example:
     * raw = "123,John,true"
     * field.getType() = Address.class
     * return new Address(123, "John", true)
     */
    public static Object convert(Field parentField, String raw, CsvConfig config) throws ConversionException {

        Class<?> embeddedType = parentField.getType();

        try {
            // Instantiate embedded object
            Object instance = embeddedType.getDeclaredConstructor().newInstance();

            // Retrieve all embedded fields in correct order (@CsvOrder)
            List<Field> embeddedFields = FieldMappingUtils.getOrderedFields(embeddedType);

            // Split CSV "value1,value2,value3"
            String[] parts = raw.split(",", -1);

            if (parts.length != embeddedFields.size()) {
                throw new ConversionException(
                        "Embedded field count mismatch: expected " +
                                embeddedFields.size() + " values, got " + parts.length +
                                " in value '" + raw + "'");
            }

            // Convert each part and assign to embedded object
            for (int i = 0; i < embeddedFields.size(); i++) {
                Field field = embeddedFields.get(i);
                field.setAccessible(true);

                Object converted = ValueConverter.convertValue(field, parts[i], config);
                field.set(instance, converted);
            }

            return instance;

        } catch (Exception ex) {
            throw new ConversionException(
                    "Failed to convert embedded field '" + parentField.getName() + "' with value '" + raw + "'", ex
            );
        }
    }
}
