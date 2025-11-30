package be.bornput.csv.controller;

import be.bornput.csv.model.ExamplePojo;
import be.bornput.csv.service.CsvServiceWithReflection;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/")
public class CsvControllerWithReflection {

    private final CsvServiceWithReflection csvServiceWithReflection;

    public CsvControllerWithReflection(CsvServiceWithReflection csvServiceWithReflection) {
        this.csvServiceWithReflection = csvServiceWithReflection;
    }

    @GetMapping("/csv-reflection-demo")
    public List<ExamplePojo> csvDemo() throws Exception {
        List<ExamplePojo> objects = List.of(
                new ExamplePojo("Alice", 30),
                new ExamplePojo("Bob", 25)
        );

        return csvServiceWithReflection.writeAndReadTempCsv(objects, ExamplePojo.class);
    }
}