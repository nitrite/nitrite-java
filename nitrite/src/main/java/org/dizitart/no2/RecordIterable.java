package org.dizitart.no2;

import java.util.List;

/**
 * An iterable of database records which supports pagination
 * and projection.
 *
 * @since 1.0
 * @author Anindya Chatterjee
 * @see org.dizitart.no2.Cursor
 * @see org.dizitart.no2.objects.Cursor
 */
public interface RecordIterable<T> extends Resettable<T> {

    /**
     * Specifies if there are more elements in the database that
     * has not been retrieved yet.
     *
     * @return `true` if the cursor has more elements; otherwise `false`.
     */
    boolean hasMore();

    /**
     * Gets the size of the current record set.
     *
     * @return the size of the current record set.
     */
    int size();

    /**
     * Gets the total count of the records in the database matching a filter criteria.
     *
     * [icon="{@docRoot}/note.png"]
     * [NOTE]
     * ====
     * If pagination is used during find operation, {@link #totalCount()}
     * and {@link #size()} may not be equal. In that case {@link #size()}
     * denotes the size of the current page and {@link #totalCount()} denotes
     * the size of all matching results in the database that may or may not
     * be retrieved yet.
     *
     * ====
     *
     * @return total count of matching documents.
     * @see NitriteCollection#find(Filter, FindOptions)
     * @see NitriteCollection#find(FindOptions)
     * @see FindOptions#limit(int, int)
     */
    int totalCount();

    /**
     * Gets the first element of the result or
     * `null` if it is empty.
     *
     * @return the first element or `null`
     */
    T firstOrDefault();

    /**
     * Returns a list of all elements.
     *
     * @return list of all elements.
     * */
    List<T> toList();
}
