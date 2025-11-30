package be.bornput.csv.csv.config;

import be.bornput.csv.csv.strategy.api.*;
import lombok.Builder;
import lombok.Data;

@Builder(toBuilder = true)
@Data
public final class CsvConfig {

    private final char delimiter;
    @Builder.Default
    private final char listDelimiter = ';';
    private final boolean trimValues;
    private final boolean writeHeader;

    private final QuoteStrategy quoteStrategy;
    private final DateStrategy dateStrategy;
    private final NumberStrategy numberStrategy;
    private final EmbeddedStrategy embeddedStrategy;
    private final FieldOrderStrategy fieldOrderStrategy;

}

