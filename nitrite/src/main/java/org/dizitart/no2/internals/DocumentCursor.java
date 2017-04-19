package org.dizitart.no2.internals;

import org.dizitart.no2.*;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.util.Iterables;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Anindya Chatterjee.
 */
class DocumentCursor implements Cursor {
    private final Set<NitriteId> resultSet;
    private final NitriteMap<NitriteId, Document> underlyingMap;
    private boolean hasMore;
    private int totalCount;
    private Iterator<Document> documentIterator;
    private FindResult findResult;

    DocumentCursor(FindResult findResult) {
        if (findResult.getIdSet() != null) {
            resultSet = findResult.getIdSet();
        } else {
            resultSet = new TreeSet<>();
        }
        this.underlyingMap = findResult.getUnderlyingMap();
        this.hasMore = findResult.isHasMore();
        this.totalCount = findResult.getTotalCount();
        this.documentIterator = new DocumentCursorIterator(this);
        this.findResult = findResult;
    }

    @Override
    public RecordIterable<Document> project(Document projection) {
        return new ProjectedDocumentIterable(projection, findResult);
    }

    @Override
    public Iterator<Document> iterator() {
        return documentIterator;
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
        this.documentIterator = new DocumentCursorIterator(this);
    }

    private class DocumentCursorIterator extends DocumentIterator {
        private Iterator<NitriteId> iterator;

        DocumentCursorIterator(Resettable<Document> resettable) {
            super(resettable);
            iterator = resultSet.iterator();
            nextMatch();
        }

        @Override
        void nextMatch() {
            while (iterator.hasNext()) {
                NitriteId next = iterator.next();
                Document document = underlyingMap.get(next);
                if (document != null) {
                    nextElement = document;
                    return;
                }
            }

            nextElement = null;
        }
    }
}
