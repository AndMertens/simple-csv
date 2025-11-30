package be.bornput.csv.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface CsvRecord {
    String[] headers() default {}; // optional explicit headers: if empty, then use field names
    char delimiter() default ',';  // optional per-record delimiter
}