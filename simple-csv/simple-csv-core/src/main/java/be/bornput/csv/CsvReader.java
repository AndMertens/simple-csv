package be.bornput.csv;

import be.bornput.csv.config.CsvConfig;
import be.bornput.csv.mapper.CsvMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class CsvReader {

    private final CsvConfig config;

    public CsvReader(CsvConfig config) {
        this.config = config;
    }

    public <T> List<T> read(Reader reader, CsvMapper<T> mapper) throws IOException {
        List<T> result = new ArrayList<>();
        char delimiter = config.getDelimiter();

        try (BufferedReader br = new BufferedReader(reader)) {
            // Skip header
            String headerLine = br.readLine();
            if (headerLine == null) return result;

            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(String.valueOf(delimiter), -1);
                result.add(mapper.fromCsv(values));
            }
        }

        return result;
    }
}