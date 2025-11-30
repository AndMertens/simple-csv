package be.bornput.csv.csv.mapper;

import be.bornput.csv.csv.annotations.CsvIgnore;
import be.bornput.csv.csv.config.CsvConfig;
import be.bornput.csv.csv.exception.ConversionException;
import be.bornput.csv.csv.exception.ElementConversionException;
import be.bornput.csv.csv.util.FieldMappingUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

public class CsvWriterMapper extends BaseCsvMapper {

    private final char delimiter;

    public CsvWriterMapper(CsvConfig config) {
        super(config);
        this.delimiter = config.getDelimiter();
    }

    /**
     * Write a list of objects to CSV.
     */
    public <T> void write(BufferedWriter writer, List<T> objects, Class<T> clazz) throws IOException, ConversionException {
        List<Field> fields = getSortedFields(clazz);

        // Write header if configured
        if (config.isWriteHeader() && !objects.isEmpty()) {
            String headerLine = buildHeaderLine(fields);
            writer.write(headerLine);
            writer.newLine();
        }

        // Write each object
        for (T obj : objects) {
            String line = buildCsvLine(obj, fields);
            writer.write(line);
            writer.newLine();
        }

        writer.flush();
    }

    /**
     * Build CSV line for a single object.
     */
    private <T> String buildCsvLine(T obj, List<Field> fields) throws ConversionException {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            field.setAccessible(true);

            try {
                Object value = field.get(obj);
                String cell = toCsvString(value, field);

                // Escape using quote strategy
                cell = config.getQuoteStrategy().applyQuotes(cell, field);

                sb.append(cell);
                if (i < fields.size() - 1) sb.append(delimiter);
            } catch (IllegalAccessException e) {
                throw new ElementConversionException("Failed to access field " + field.getName(), e);
            } catch (ConversionException e) {
                throw new ConversionException(e);
            }
        }

        return sb.toString();
    }

    /**
     * Build header line using field names or @CsvColumn.name.
     */
    private String buildHeaderLine(List<Field> fields) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            String header = FieldMappingUtils.getHeaderName(field);
            sb.append(config.getQuoteStrategy().applyQuotes(header,field));
            if (i < fields.size() - 1) sb.append(delimiter);
        }

        return sb.toString();
    }

    /**
     * Get all fields sorted by FieldOrderStrategy, ignoring @CsvIgnore fields.
     */
    protected List<Field> getSortedFields(Class<?> clazz) {
        List<Field> fields = FieldMappingUtils.getAllFields(clazz);
        fields.removeIf(f -> f.isAnnotationPresent(CsvIgnore.class));
        return config.getFieldOrderStrategy().sort(fields);
    }
}

