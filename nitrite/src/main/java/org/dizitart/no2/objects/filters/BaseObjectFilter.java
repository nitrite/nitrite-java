package org.dizitart.no2.objects.filters;

import org.dizitart.no2.internals.NitriteMapper;
import org.dizitart.no2.internals.NitriteService;
import org.dizitart.no2.objects.ObjectFilter;

/**
 * An abstract implementation of {@link ObjectFilter}.
 *
 * @author Anindya Chatterjee.
 * @since 1.0
 */
public abstract class BaseObjectFilter implements ObjectFilter {
    /**
     * The Nitrite service.
     */
    protected NitriteService nitriteService;

    /**
     * The {@link NitriteMapper} instance.
     */
    protected NitriteMapper nitriteMapper;

    @Override
    public void setNitriteService(NitriteService nitriteService) {
        this.nitriteService = nitriteService;
    }

    @Override
    public void setNitriteMapper(NitriteMapper nitriteMapper) {
        this.nitriteMapper = nitriteMapper;
    }
}
