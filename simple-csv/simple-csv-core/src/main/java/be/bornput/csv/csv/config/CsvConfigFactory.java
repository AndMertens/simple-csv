package be.bornput.csv.csv.config;

import be.bornput.csv.csv.strategy.impl.*;

public final class CsvConfigFactory {

    private CsvConfigFactory() {}

    public static CsvConfig defaultConfig() {
        // 1) Build a "base" config that contains the simple, non-strategy values.
        //    Strategies are set to null for now.
        CsvConfig base = CsvConfig.builder()
                .delimiter(',')
                .trimValues(true)
                .writeHeader(true)
                .quoteStrategy(null)
                .dateStrategy(null)
                .numberStrategy(null)
                .embeddedStrategy(null)
                .fieldOrderStrategy(new DefaultFieldOrderStrategy()) // safe: this doesn't depend on CsvConfig
                .build();

        // 2) Create default strategy instances that need a CsvConfig reference.
        //    They can accept the base config here (they should not use strategies from the config in their ctor).
        DefaultQuoteStrategy quote = new DefaultQuoteStrategy(base);
        DefaultDateStrategy date = new DefaultDateStrategy();
        DefaultNumberStrategy number = new DefaultNumberStrategy(base);
        DefaultEmbeddedStrategy embedded = new DefaultEmbeddedStrategy(base);

        // 3) Build the final CsvConfig including the concrete strategy objects.
        return CsvConfig.builder()
                .delimiter(base.getDelimiter())
                .trimValues(base.isTrimValues())
                .writeHeader(base.isWriteHeader())
                .quoteStrategy(quote)
                .dateStrategy(date)
                .numberStrategy(number)
                .embeddedStrategy(embedded)
                .fieldOrderStrategy(base.getFieldOrderStrategy())
                .build();
    }
}