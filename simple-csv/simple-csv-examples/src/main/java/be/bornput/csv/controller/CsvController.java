package be.bornput.csv.controller;

import be.bornput.csv.model.ExamplePojo;
import be.bornput.csv.service.CsvService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@RestController
@RequestMapping("/")
public class CsvController {

    private final CsvService csvService;

    public CsvController(CsvService csvService) {
        this.csvService = csvService;
    }

    @GetMapping("/csv-demo")
    public List<ExamplePojo> csvDemo() throws IOException {
        List<ExamplePojo> objects = List.of(
                new ExamplePojo("Johannes", 30),
                new ExamplePojo("Frieda", 25)
        );

        // Create a temp file
        File tempFile = Files.createTempFile("csv_demo_", ".csv").toFile();

        // Write CSV to a temp file
        try {
            csvService.writeToFile(objects, tempFile);

            // Read back from a temp CSV
            return csvService.readFromFile(tempFile, ExamplePojo.class);
        } finally {
            Files.deleteIfExists(tempFile.toPath());
        }
    }
}