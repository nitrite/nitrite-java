package org.dizitart.no2.objects;

import org.dizitart.no2.FindOptions;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.RecordIterable;

/**
 * A collection of {@link NitriteId}s of the database records,
 * as a result of a find operation.
 *
 * @author Anindya Chatterjee
 * @see ObjectRepository#find(ObjectFilter)
 * @see ObjectRepository#find(ObjectFilter, FindOptions)
 * @see ObjectRepository#find()
 * @see ObjectRepository#find(FindOptions)
 * @since 1.0
 */
public interface Cursor<T> extends RecordIterable<T> {

    /**
     * Projects the result of one type into an {@link Iterable} of other type.
     *
     * @param <P>               the type of the target objects.
     * @param projectionType    the projection type.
     * @return `Iterable` of projected objects.
     */
    <P> RecordIterable<P> project(Class<P> projectionType);
}
