package be.bornput.csv.registry;

import be.bornput.csv.mapper.CsvMapper;

public interface CsvRegistry {
    /**
     * Returns the type-safe mapper for a given class.
     *
     * @param type the POJO class
     * @param <T>  type of the POJO
     * @return the CsvMapper for this class
     */
    <T> CsvMapper<T> getMapper(Class<T> type);
}
