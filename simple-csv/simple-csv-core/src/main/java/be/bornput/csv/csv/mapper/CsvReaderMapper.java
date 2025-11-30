package be.bornput.csv.csv.mapper;

import be.bornput.csv.csv.annotations.CsvIgnore;
import be.bornput.csv.csv.config.CsvConfig;
import be.bornput.csv.csv.exception.ConversionException;
import be.bornput.csv.csv.util.FieldMappingUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CsvReaderMapper extends BaseCsvMapper {

    private final char delimiter;

    public CsvReaderMapper(CsvConfig config) {
        super(config);
        this.delimiter = config.getDelimiter();
    }

    /**
     * Read CSV into list of objects of type T.
     */
    public <T> List<T> read(BufferedReader reader, Class<T> clazz)
            throws IOException, NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException, ConversionException {

        List<T> result = new ArrayList<>();
        String headerLine = reader.readLine();
        if (headerLine == null) return result;

        // Split header using delimiter from CsvConfig
        List<String> headers = splitCsvLine(headerLine, delimiter);

        // Map headers to class fields
        Map<String, Field> fieldMap = FieldMappingUtils.mapHeaders(clazz, headers);

        String line;
        while ((line = reader.readLine()) != null) {
            List<String> values = splitCsvLine(line, delimiter);

            T instance = clazz.getDeclaredConstructor().newInstance();

            for (int i = 0; i < headers.size(); i++) {
                Field f = fieldMap.get(headers.get(i));

                if (f != null && !f.isAnnotationPresent(CsvIgnore.class)) {
                    f.setAccessible(true);
                    String raw = i < values.size() ? values.get(i) : "";
                    Object val = convertValue(f, raw);
                    f.set(instance, val);
                }
            }

            result.add(instance);
        }

        return result;
    }

    /**
     * Split a CSV line into values using delimiter and respecting quotes.
     */
    private List<String> splitCsvLine(String line, char delimiter) {
        List<String> values = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes; // toggle
            } else if (c == delimiter && !inQuotes) {
                values.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }

        values.add(sb.toString().trim());
        return values;
    }

}


