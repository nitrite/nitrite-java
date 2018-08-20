package org.dizitart.no2.index;

/**
 * @author Anindya Chatterjee.
 */
public interface IndexedQueryTemplate {
    boolean hasIndex(String field);

    boolean isIndexing(String field);

    ComparableIndexer getComparableIndexer();

    TextIndexer getTextIndexer();
}
