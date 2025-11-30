package be.bornput.csv.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface CsvField {
    String name() default "";       // column header (default -> field name)
    int order() default Integer.MAX_VALUE; // ordering for flatten/headers
    String defaultValue() default "";
    boolean ignore() default false;
    String dateFormat() default ""; // optional date format for this field
}