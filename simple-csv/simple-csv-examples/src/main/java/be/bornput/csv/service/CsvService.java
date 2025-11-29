package be.bornput.csv.service;

import be.bornput.csv.csv.config.CsvConfig;
import be.bornput.csv.csv.mapper.CsvReaderMapper;
import be.bornput.csv.csv.mapper.CsvWriterMapper;
import be.bornput.csv.model.MyPojo;
import org.springframework.stereotype.Service;


import java.io.*;
import java.util.List;

@Service
public class CsvService {

    private final CsvConfig config;

    public CsvService(CsvConfig config) {
        this.config = config;
    }

    public void writeCsv(String filePath, List<MyPojo> objects) throws Exception {
        CsvWriterMapper writer = new CsvWriterMapper(config);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(bw, objects, MyPojo.class);
        }
    }

    public List<MyPojo> readCsv(String filePath) throws Exception {
        CsvReaderMapper reader = new CsvReaderMapper(config);
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            return reader.read(br, MyPojo.class);
        }
    }
}