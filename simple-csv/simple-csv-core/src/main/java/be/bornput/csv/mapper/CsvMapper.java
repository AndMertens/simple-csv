package be.bornput.csv.mapper;

public interface CsvMapper<T> {

    String[] toCsv(T obj);          // converts object to CSV columns
    T fromCsv(String[] values);
    String[] getHeaders();
}