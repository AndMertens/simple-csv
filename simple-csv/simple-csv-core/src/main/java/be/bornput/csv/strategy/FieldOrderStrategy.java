package be.bornput.csv.strategy;

import java.lang.reflect.Field;
import java.util.List;

public interface FieldOrderStrategy {
    List<Field> sort(List<Field> fields);
}
