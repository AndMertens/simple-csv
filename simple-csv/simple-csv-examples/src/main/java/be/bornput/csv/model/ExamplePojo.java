package be.bornput.csv.model;

import be.bornput.csv.annotation.CsvColumn;
import be.bornput.csv.annotation.CsvRecord;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@CsvRecord
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ExamplePojo
{
    @CsvColumn("name")
    private String name;
    @CsvColumn("Age")
    private int age;

}
