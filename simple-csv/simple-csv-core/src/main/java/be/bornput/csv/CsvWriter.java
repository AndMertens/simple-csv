package be.bornput.csv;

import be.bornput.csv.config.CsvConfig;
import be.bornput.csv.mapper.CsvMapper;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class CsvWriter {

    private final CsvConfig config;

    public CsvWriter(CsvConfig config) {
        this.config = config;
    }

    public <T> void write(Writer writer, List<T> data, CsvMapper<T> mapper) throws IOException {
        char delimiter = config.getDelimiter();

        try (BufferedWriter bw = new BufferedWriter(writer)) {
            // Write header
            String[] headers = mapper.getHeaders();
            bw.write(String.join(String.valueOf(delimiter), headers));
            bw.newLine();

            // Write rows
            for (T obj : data) {
                String[] row = mapper.toCsv(obj);
                bw.write(String.join(String.valueOf(delimiter), row));
                bw.newLine();
            }

            bw.flush();
        }
    }
}