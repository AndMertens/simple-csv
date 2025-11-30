package be.bornput.csv.csv.config;

import be.bornput.csv.csv.strategy.impl.*;

import java.util.Locale;

public final class CsvConfigFactory {

    private CsvConfigFactory() {}

    public static CsvConfig defaultConfig() {

        CsvConfig base = CsvConfig.builder()
                .delimiter(',')
                .listDelimiter(';')
                .trimValues(true)
                .writeHeader(true)
                .build();

        return base.toBuilder()
                .quoteStrategy(new DefaultQuoteStrategy(base))
                .dateStrategy(new DefaultDateStrategy(base, "yyyy-MM-dd"))
                .numberStrategy(new DefaultNumberStrategy(base, "#.##", Locale.US))
                .embeddedStrategy(new DefaultEmbeddedStrategy(base))
                .fieldOrderStrategy(new DefaultFieldOrderStrategy())
                .build();
    }

    public static CsvConfig copyWithCustomListDelimiter(CsvConfig config, char listDelimiter) {
        return CsvConfig.builder()
                .delimiter(config.getDelimiter())
                .trimValues(config.isTrimValues())
                .writeHeader(config.isWriteHeader())
                .quoteStrategy(config.getQuoteStrategy())
                .dateStrategy(config.getDateStrategy())
                .numberStrategy(config.getNumberStrategy())
                .embeddedStrategy(config.getEmbeddedStrategy())
                .listDelimiter(listDelimiter)
                .build();
    }
}