package com.transporter.assignment.config;

import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.tool.schema.extract.spi.ExtractionContext;
import org.hibernate.tool.schema.extract.spi.SequenceInformation;
import org.hibernate.tool.schema.extract.spi.SequenceInformationExtractor;

import java.util.Collections;

/**
 * Custom PostgreSQL dialect for Hibernate 6 that bypasses database sequence metadata extraction.
 *
 * In Hibernate 6, SequenceInformationExtractorLegacyImpl queries database sequence metadata
 * during schema management. When run against modern PostgreSQL (e.g. on Render) or databases with
 * restricted catalog permissions, it causes startup crashes (Numeric Overflow / metadata query errors).
 * None of this project's entities use Hibernate-managed sequences (they use IDENTITY or client-supplied IDs),
 * so returning an empty list avoids the crash while preserving full Hibernate schema update support.
 */
public class CustomPostgreSQLDialect extends PostgreSQLDialect {

    @Override
    public SequenceInformationExtractor getSequenceInformationExtractor() {
        return new SequenceInformationExtractor() {
            @Override
            public Iterable<SequenceInformation> extractMetadata(ExtractionContext extractionContext) {
                return Collections.emptyList();
            }
        };
    }
}
