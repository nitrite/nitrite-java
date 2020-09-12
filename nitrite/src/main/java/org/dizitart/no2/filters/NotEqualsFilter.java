package org.dizitart.no2.filters;

import lombok.ToString;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.index.ComparableIndexer;
import org.dizitart.no2.index.TextIndexer;
import org.dizitart.no2.store.NitriteMap;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.dizitart.no2.common.util.ObjectUtils.deepEquals;

/**
 * @author Anindya Chatterjee
 */
@ToString
class NotEqualsFilter extends IndexAwareFilter {
    protected NotEqualsFilter(String field, Object value) {
        super(field, value);
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected Set<NitriteId> findIndexedIdSet() {
        Set<NitriteId> idSet = new LinkedHashSet<>();
        if (getIsFieldIndexed()) {
            if (getValue() == null || getValue() instanceof Comparable) {
                if (getIndexer() instanceof ComparableIndexer) {
                    ComparableIndexer comparableIndexer = (ComparableIndexer) getIndexer();
                    idSet = comparableIndexer.findNotEqual(getCollectionName(), getField(), (Comparable) getValue());
                } else if (getIndexer() instanceof TextIndexer && getValue() instanceof String) {
                    // notEq filter is not compatible with TextIndexer
                    setIsFieldIndexed(false);
                } else {
                    throw new FilterException("notEq filter is not supported on indexed field "
                        + getField());
                }
            } else {
                throw new FilterException(getValue() + " is not comparable");
            }
        }
        return idSet;
    }

    @Override
    protected Set<NitriteId> findIdSet(NitriteMap<NitriteId, Document> collection) {
        Set<NitriteId> idSet = new LinkedHashSet<>();
        if (getOnIdField() && getValue() instanceof String) {
            NitriteId nitriteId = NitriteId.createId((String) getValue());
            if (!collection.containsKey(nitriteId)) {
                idSet.add(nitriteId);
            }
        }
        return idSet;
    }

    @Override
    public boolean apply(Pair<NitriteId, Document> element) {
        Document document = element.getSecond();
        Object fieldValue = document.get(getField());
        return !deepEquals(fieldValue, getValue());
    }

    @Override
    public void setIsFieldIndexed(Boolean isFieldIndexed) {
        if (!(getIndexer() instanceof TextIndexer && getValue() instanceof String)) {
            super.setIsFieldIndexed(isFieldIndexed);
        }
    }
}
