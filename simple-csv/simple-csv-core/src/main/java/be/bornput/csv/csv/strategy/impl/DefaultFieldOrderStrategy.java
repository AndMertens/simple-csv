package be.bornput.csv.csv.strategy.impl;

import be.bornput.csv.csv.annotations.CsvOrder;
import be.bornput.csv.csv.strategy.api.FieldOrderStrategy;

import java.lang.reflect.Field;
import java.util.List;

public class DefaultFieldOrderStrategy implements FieldOrderStrategy {

    @Override
    public List<Field> sort(List<Field> fields) {
        fields.sort((f1, f2) -> {
            int o1 = f1.isAnnotationPresent(CsvOrder.class)
                    ? f1.getAnnotation(CsvOrder.class).value() : Integer.MAX_VALUE;
            int o2 = f2.isAnnotationPresent(CsvOrder.class)
                    ? f2.getAnnotation(CsvOrder.class).value() : Integer.MAX_VALUE;
            return Integer.compare(o1, o2);
        });
        return fields;
    }
}
