package be.bornput.csv.generated;

import be.bornput.csv.annotation.CsvGeneratedMapper;
import be.bornput.csv.mapper.CsvMapper;
import be.bornput.csv.model.ExamplePojo;

@CsvGeneratedMapper(
        forClass = ExamplePojo.class,
        name = "ExamplePojoCsvMapper"
)
public class ExamplePojoCsvMapper implements CsvMapper<ExamplePojo> {

    private static final String[] HEADERS = new String[] { "name", "Age" };

    @Override
    public String[] getHeaders() {
        return HEADERS;
    }

    @Override
    public String[] toCsv(ExamplePojo obj) {
        return new String[] {
                obj.getName(),
                String.valueOf(obj.getAge())
        };
    }

    @Override
    public ExamplePojo fromCsv(String[] values) {
        ExamplePojo p = new ExamplePojo();
        p.setName(values[0]);
        p.setAge(Integer.parseInt(values[1]));
        return p;
    }
}