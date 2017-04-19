package org.dizitart.no2.objects;

import org.dizitart.no2.Filter;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.internals.NitriteMapper;

/**
 * An interface to specify filtering criteria during {@link ObjectRepository}'s
 * find operations. When a filter is applied to a repository, based on the
 * criteria it returns a set of {@link NitriteId}s of matching records.
 *
 * Each filtering criteria is based on a field in the object. If the field is
 * indexed, the find operation takes the advantage of it and only scans the
 * index map for that field. But if the field is not indexed, it scans the
 * whole {@link ObjectRepository}.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 * @see org.dizitart.no2.Filter
 */
public interface ObjectFilter extends Filter {
    /**
     * Sets {@link NitriteMapper} to the filter.
     *
     * @param nitriteMapper the {@link NitriteMapper}.
     */
    void setNitriteMapper(NitriteMapper nitriteMapper);
}
