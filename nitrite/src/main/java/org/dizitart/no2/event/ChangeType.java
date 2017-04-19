package org.dizitart.no2.event;

import org.dizitart.no2.*;
import org.dizitart.no2.objects.ObjectFilter;
import org.dizitart.no2.objects.ObjectRepository;

/**
 * Represents different types of collection modification
 * actions.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
public enum ChangeType {
    /**
     * Insert action.
     *
     * @see org.dizitart.no2.NitriteCollection#insert(Object[])
     * @see org.dizitart.no2.objects.ObjectRepository#insert(Object, Object[])
     * @see org.dizitart.no2.objects.ObjectRepository#insert(Object[])
     */
    INSERT,

    /**
     * Update action.
     *
     * @see org.dizitart.no2.NitriteCollection#update(Filter, Document)
     * @see org.dizitart.no2.NitriteCollection#update(Filter, Document, UpdateOptions)
     * @see org.dizitart.no2.objects.ObjectRepository#update(ObjectFilter, Object)
     * @see org.dizitart.no2.objects.ObjectRepository#update(ObjectFilter, Object, UpdateOptions)
     */
    UPDATE,

    /**
     * Remove action.
     *
     * @see org.dizitart.no2.NitriteCollection#remove(Filter)
     * @see org.dizitart.no2.NitriteCollection#remove(Filter, RemoveOptions)
     * @see org.dizitart.no2.objects.ObjectRepository#remove(ObjectFilter, RemoveOptions)
     */
    REMOVE,

    /**
     * Collection Drop action.
     *
     * @see NitriteCollection#drop()
     * @see ObjectRepository#drop()
     */
    DROP,

    /**
     * Collection Close action.
     *
     * @see NitriteCollection#close()
     * @see ObjectRepository#close()
     */
    CLOSE
}
