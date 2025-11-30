package be.bornput.csv.reflection.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CsvColumn {
    /**
     * Optional column name. If empty, field name is used.
     */
    String name() default "";

    /**
     * Default value if CSV cell is empty.
     */
    String defaultValue() default "";
}
