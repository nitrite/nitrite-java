package org.dizitart.no2;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents options to apply while creating an {@link Index}.
 *
 * @author Anindya Chatterjee
 * @since 1.0
 * @see NitriteCollection#createIndex(String, IndexOptions)
 */
public class IndexOptions {

    /**
     * Specifies the type of an index to createId.
     *
     * @param indexType type of an index.
     * @returns type of an index to createId.
     * */
    @Getter @Setter
    private IndexType indexType;

    /**
     * Indicates whether an index to be created in a non-blocking
     * way.
     *
     * @param async if set to `true` then the index will be created asynchronously;
     *              otherwise createId index operation will wait until all existing
     *              documents are indexed.
     * @returns `true` if index is to be created asynchronously; otherwise `false`.
     * @see NitriteCollection#createIndex(String, IndexOptions)
     * */
    @Getter @Setter
    private boolean async = false;

    /**
     * Creates an {@link IndexOptions} with the specified `indexType`. Index creation
     * will be synchronous with this option.
     *
     * @param indexType the type of index to be created.
     * @return a new synchronous index creation option.
     */
    public static IndexOptions indexOptions(IndexType indexType) {
        return indexOptions(indexType, false);
    }

    /**
     * Creates an {@link IndexOptions} with the specified `indexType` and `async` flag.
     *
     * @param indexType the type of index to be created.
     * @param async     if set to `true` then the index would be created asynchronously;
     *                  otherwise synchronously.
     * @return a new index creation option.
     */
    public static IndexOptions indexOptions(IndexType indexType, boolean async) {
        IndexOptions options = new IndexOptions();
        options.setIndexType(indexType);
        options.setAsync(async);
        return options;
    }
}
