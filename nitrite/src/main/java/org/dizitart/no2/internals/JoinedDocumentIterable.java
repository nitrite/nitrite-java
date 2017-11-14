package org.dizitart.no2.internals;

import org.dizitart.no2.*;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.util.Iterables;

import java.util.*;

import static org.dizitart.no2.util.EqualsUtils.deepEquals;

/**
 * @author Anindya Chatterjee.
 */
class JoinedDocumentIterable implements RecordIterable<Document> {
    private final Set<NitriteId> resultSet;
    private final NitriteMap<NitriteId, Document> underlyingMap;
    private boolean hasMore;
    private int totalCount;
    private Iterator<Document> iterator;
    private Cursor foreignCursor;
    private Lookup lookup;

    JoinedDocumentIterable(FindResult findResult, Cursor foreignCursor, Lookup lookup) {
        this.foreignCursor = foreignCursor;
        this.lookup = lookup;
        if (findResult.getIdSet() != null) {
            resultSet = findResult.getIdSet();
        } else {
            resultSet = new TreeSet<>();
        }
        this.underlyingMap = findResult.getUnderlyingMap();
        this.hasMore = findResult.isHasMore();
        this.totalCount = findResult.getTotalCount();
        this.iterator = new JoinedDocumentIterator(this);
    }

    @Override
    public void reset() {
        this.iterator = new JoinedDocumentIterator(this);
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
    public Iterator<Document> iterator() {
        return iterator;
    }

    @Override
    public String toString() {
        return toList().toString();
    }

    private class JoinedDocumentIterator extends DocumentIterator {
        private Iterator<NitriteId> iterator;

        JoinedDocumentIterator(Resettable<Document> resettable) {
            super(resettable);
            iterator = resultSet.iterator();
            nextMatch();
        }

        @Override
        void nextMatch() {
            while (iterator.hasNext()) {
                NitriteId next = iterator.next();
                Document document = underlyingMap.get(next);
                Document joined = join(document, foreignCursor, lookup);
                if (joined != null) {
                    nextElement = joined;
                    return;
                }
            }
            nextElement = null;
        }

        private Document join(Document localDocument, Cursor foreignCursor, Lookup lookup) {
            Object localObject = localDocument.get(lookup.getLocalField());
            if (localObject == null) return localDocument;
            Document resultDocument = new Document(localDocument);
            Set<Document> target = new HashSet<>();

            for (Document foreignDocument: foreignCursor) {
                Object foreignObject = foreignDocument.get(lookup.getForeignField());
                if (foreignObject != null) {
                    if (deepEquals(foreignObject, localObject)) {
                        target.add(foreignDocument);
                    }
                }
            }
            if (!target.isEmpty()) {
                resultDocument.put(lookup.getTargetField(), target);
            }
            return resultDocument;
        }
    }
}
