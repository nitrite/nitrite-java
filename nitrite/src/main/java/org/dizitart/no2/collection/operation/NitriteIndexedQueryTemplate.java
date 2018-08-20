package org.dizitart.no2.collection.operation;

import org.dizitart.no2.index.ComparableIndexer;
import org.dizitart.no2.index.IndexedQueryTemplate;
import org.dizitart.no2.index.TextIndexer;

/**
 * @author Anindya Chatterjee.
 */
class NitriteIndexedQueryTemplate implements IndexedQueryTemplate {

    private ComparableIndexer comparableIndexer;
    private TextIndexer textIndexer;
    private IndexTemplate indexTemplate;

    NitriteIndexedQueryTemplate(IndexTemplate indexTemplate,
                                ComparableIndexer comparableIndexer,
                                TextIndexer textIndexer) {
        this.comparableIndexer = comparableIndexer;
        this.textIndexer = textIndexer;
        this.indexTemplate = indexTemplate;
    }

    @Override
    public ComparableIndexer getComparableIndexer() {
        return comparableIndexer;
    }

    @Override
    public TextIndexer getTextIndexer() {
        return textIndexer;
    }

    @Override
    public boolean isIndexing(String field) {
        return indexTemplate.isIndexing(field);
    }

    @Override
    public boolean hasIndex(String field) {
        return indexTemplate.hasIndex(field);
    }
}
