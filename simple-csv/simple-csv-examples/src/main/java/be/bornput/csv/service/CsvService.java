package be.bornput.csv.service;

import be.bornput.csv.config.CsvConfig;
import be.bornput.csv.mapper.CsvMapper;
import be.bornput.csv.registry.CsvRegistry;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
@Service
public class CsvService {

    private final CsvRegistry registry;
    private final CsvConfig config;

    public CsvService(CsvRegistry registry, CsvConfig config) {
        this.registry = registry;
        this.config = config;
    }

    public <T> void writeToFile(List<T> objects, Class<T> clazz,File file) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {

            if (objects.isEmpty()) return;

            CsvMapper<T> mapper = registry.getMapper(clazz);

            // write header
            bw.write(String.join(";", mapper.toCsv(objects.get(0))));
            bw.newLine();

            // write rows
            for (T obj : objects) {
                bw.write(String.join(";", mapper.toCsv(obj)));
                bw.newLine();
            }
        }
    }

    public <T> List<T> readFromFile(File file, Class<T> clazz) throws IOException {
        List<T> result = new ArrayList<>();
        CsvMapper<T> mapper = registry.getMapper(clazz);

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            // skip header
            String headerLine = br.readLine();
            if (headerLine == null) return result;

            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(";");
                result.add(mapper.fromCsv(values));
            }
        }

        return result;
    }
}
