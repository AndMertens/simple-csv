package be.bornput.csv.csv.strategy.api;

import java.lang.reflect.Field;
import java.util.List;

public interface FieldOrderStrategy {
    List<Field> sort(List<Field> fields);
}
