package org.dizitart.no2.migration;

import org.dizitart.no2.collection.Document;

/**
 * @author Anindya Chatterjee
 */
public interface Generator<T> {
    T generate(Document document);
}
