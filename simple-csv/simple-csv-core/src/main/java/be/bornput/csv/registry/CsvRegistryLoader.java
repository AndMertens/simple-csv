package be.bornput.csv.registry;

import be.bornput.csv.mapper.CsvMapper;

import java.util.HashMap;
import java.util.Map;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsvRegistryLoader implements CsvRegistry {

    private static final Logger log = LoggerFactory.getLogger(CsvRegistryLoader.class);
    private final Map<Class<?>, CsvMapper<?>> registry = new HashMap<>();

    public CsvRegistryLoader(String... basePackages) {
        loadGeneratedMappers(basePackages);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> CsvMapper<T> getMapper(Class<T> type) {
        CsvMapper<?> mapper = registry.get(type);
        if (mapper == null) {
            throw new IllegalStateException("No CSV mapper registered for: " + type.getName());
        }
        return (CsvMapper<T>) mapper;
    }

    private void loadGeneratedMappers(String... basePackages) {
        log.info("Scanning for @CsvGeneratedMapper in {}", (Object) basePackages);

        try (ScanResult scan = new ClassGraph()
                .enableAnnotationInfo()
                .acceptPackages(basePackages)
                .scan()) {

            scan.getClassesWithAnnotation("be.bornput.csv.annotation.CsvGeneratedMapper")
                    .forEach(c -> {
                        try {
                            Class<?> mapperClass = c.loadClass();
                            var annotation = mapperClass.getAnnotation(be.bornput.csv.annotation.CsvGeneratedMapper.class);

                            CsvMapper<?> mapperInstance =
                                    (CsvMapper<?>) mapperClass.getDeclaredConstructor().newInstance();

                            registry.put(annotation.forClass(), mapperInstance);

                            String mapperName = annotation.name().isEmpty() ?
                                    annotation.forClass().getSimpleName() + "CsvMapper" :
                                    annotation.name();

                            log.info("Registered CsvMapper: {} for class {} with name '{}'",
                                    mapperClass.getName(),
                                    annotation.forClass().getName(),
                                    mapperName);

                        } catch (Exception ex) {
                            throw new IllegalStateException(
                                    "Failed to load CSV mapper: " + c.getName(), ex
                            );
                        }
                    });
        }
    }
}