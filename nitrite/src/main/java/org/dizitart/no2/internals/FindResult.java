package org.dizitart.no2.internals;

import lombok.Data;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.store.NitriteMap;

import java.util.Set;

/**
 * @author Anindya Chatterjee.
 */
@Data
class FindResult {
    private boolean hasMore;
    private int totalCount;
    private Set<NitriteId> idSet;
    private NitriteMap<NitriteId, Document> underlyingMap;
}
