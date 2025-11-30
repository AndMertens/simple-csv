package be.bornput.csv.service;

import be.bornput.csv.config.CsvConfig;
import be.bornput.csv.reflection.mapper.CsvReaderMapper;
import be.bornput.csv.reflection.mapper.CsvWriterMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

@Service
@Slf4j
public class CsvServiceWithReflection {

    private final CsvConfig config;

    public CsvServiceWithReflection(CsvConfig config) {
        this.config = config;
    }

    public <T> void writeCsv(File file, List<T> objects, Class<T> clazz) throws Exception {
        CsvWriterMapper writer = new CsvWriterMapper(config);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            writer.write(bw, objects, clazz);
        }
    }

    public <T> List<T> readCsv(File file, Class<T> clazz) throws Exception {
        CsvReaderMapper reader = new CsvReaderMapper(config);
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            return reader.read(br, clazz);
        }
    }

    public <T> List<T> writeAndReadTempCsv(List<T> objects, Class<T> clazz) throws Exception {
        Path tempFile = null;
        try {
            // Create a temporary CSV file
            tempFile = Files.createTempFile("csv_demo_", ".csv");
            log.info("Created temporary CSV file at {}", tempFile.toAbsolutePath());

            // Write the objects to CSV
            writeToCsv(objects, clazz, tempFile);

            // Read the objects back from CSV
            return readFromCsv(clazz, tempFile);

        } catch (IOException e) {
            throw new IOException("Error while creating or accessing temporary CSV file", e);

        } finally {
            // Attempt to delete the temp file
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                    log.info("Deleted temporary CSV file at {}", tempFile.toAbsolutePath());
                } catch (IOException e) {
                    log.warn("Could not delete temporary CSV file at {}: {}", tempFile.toAbsolutePath(), e.getMessage());
                }
            }
        }
    }

    private <T> List<T> readFromCsv(Class<T> clazz, Path tempFile) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(tempFile)) {
            CsvReaderMapper csvReader = new CsvReaderMapper(config);
            return csvReader.read(reader, clazz);
        } catch (Exception e) {
            throw new IOException("Failed to read CSV data from temporary file: " + tempFile, e);
        }
    }

    private <T> void writeToCsv(List<T> objects, Class<T> clazz, Path tempFile) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(tempFile)) {
            CsvWriterMapper csvWriter = new CsvWriterMapper(config);
            csvWriter.write(writer, objects, clazz);
        } catch (Exception e) {
            throw new IOException("Failed to write CSV data to temporary file: " + tempFile, e);
        }
    }

    public <T> List<T> writeAndReadTempCsv(List<T> objects, Class<T> clazz, Supplier<File> fileSupplier) throws Exception {
        File tempFile = fileSupplier.get();
        try {
            writeCsv(tempFile, objects, clazz);
            return readCsv(tempFile, clazz);
        } finally {
            if (tempFile.exists()) {
                try {
                    Files.delete(tempFile.toPath());
                } catch (IOException e) {
                    // Provide a clear message on why the file couldn't be deleted
                    log.warn("Could not delete temp CSV file '{}': {}", tempFile.getAbsolutePath(), e.getMessage());
                    tempFile.deleteOnExit(); // fallback
                }
            }
        }
    }
}