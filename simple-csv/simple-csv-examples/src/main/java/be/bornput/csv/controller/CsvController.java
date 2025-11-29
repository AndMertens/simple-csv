package be.bornput.csv.controller;

import be.bornput.csv.model.MyPojo;
import be.bornput.csv.service.CsvService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CsvController {

    private final CsvService csvService;

    public CsvController(CsvService csvService) {
        this.csvService = csvService;
    }

    @GetMapping("/csv-demo")
    public List<MyPojo> csvDemo() throws Exception {
        String file = "example.csv";

        List<MyPojo> objects = List.of(
                new MyPojo("Alice", 30),
                new MyPojo("Bob", 25)
        );

        csvService.writeCsv(file, objects);
        return csvService.readCsv(file);
    }
}