package org.dizitart.no2.internals;

import org.dizitart.no2.*;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.util.Iterables;

import java.util.*;

import static org.dizitart.no2.util.EqualsUtils.deepEquals;

/**
 * @author Anindya Chatterjee.
 */
class ProjectedDocumentIterable implements RecordIterable<Document> {
    private final Set<NitriteId> resultSet;
    private final NitriteMap<NitriteId, Document> underlyingMap;
    private Document projection;
    private Iterator<Document> iterator;
    private boolean hasMore;
    private int totalCount;

    ProjectedDocumentIterable(Document projection, FindResult findResult) {
        this.projection = projection;
        if (findResult.getIdSet() != null) {
            resultSet = findResult.getIdSet();
        } else {
            resultSet = new TreeSet<>();
        }
        this.underlyingMap = findResult.getUnderlyingMap();
        this.hasMore = findResult.isHasMore();
        this.totalCount = findResult.getTotalCount();
        this.iterator = new ProjectedDocumentIterator(this);
    }

    @Override
    public Iterator<Document> iterator() {
        return iterator;
    }

    @Override
    public boolean hasMore() {
        return hasMore;
    }

    @Override
    public int size() {
        return resultSet.size();
    }

    @Override
    public int totalCount() {
        return totalCount;
    }

    @Override
    public Document firstOrDefault() {
        Document item = Iterables.firstOrDefault(this);
        reset();
        return item;
    }

    @Override
    public List<Document> toList() {
        List<Document> list = Iterables.toList(this);
        reset();
        return list;
    }

    @Override
    public void reset() {
        this.iterator = new ProjectedDocumentIterator(this);
    }

    @Override
    public String toString() {
        return toList().toString();
    }

    private class ProjectedDocumentIterator extends DocumentIterator {
        private Iterator<NitriteId> iterator;

        ProjectedDocumentIterator(Resettable<Document> resettable) {
            super(resettable);
            iterator = resultSet.iterator();
            nextMatch();
        }

        @Override
        void nextMatch() {
            while (iterator.hasNext()) {
                NitriteId next = iterator.next();
                Document document = underlyingMap.get(next);
                Document projected = project(document);
                if (projected != null) {
                    nextElement = projected;
                    return;
                }
            }

            nextElement = null;
        }

        private Document project(Document original) {
            if (projection == null) return original;
            Document result = new Document(original);

            for (KeyValuePair keyValuePair : original) {
                if (!projection.containsKey(keyValuePair.getKey())) {
                    result.remove(keyValuePair.getKey());
                } else {
                    // find by example
                    if (projection.get(keyValuePair.getKey()) != null) {
                        if (!deepEquals(projection.get(keyValuePair.getKey()),
                                keyValuePair.getValue())) {
                            return null;
                        }
                    }
                }
            }
            return result;
        }
    }
}
