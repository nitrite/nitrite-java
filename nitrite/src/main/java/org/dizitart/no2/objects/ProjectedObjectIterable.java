package org.dizitart.no2.objects;

import org.dizitart.no2.Document;
import org.dizitart.no2.RecordIterable;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.internals.NitriteMapper;
import org.dizitart.no2.util.Iterables;

import java.util.Iterator;
import java.util.List;

import static org.dizitart.no2.Constants.DOC_ID;
import static org.dizitart.no2.exceptions.ErrorMessage.OBJ_REMOVE_ON_PROJECTED_OBJECT_ITERATOR_NOT_SUPPORTED;

/**
 * @author Anindya Chatterjee.
 */
class ProjectedObjectIterable<T> implements RecordIterable<T> {
    private RecordIterable<Document> recordIterable;
    private Class<T> projectionType;
    private ProjectedObjectIterator iterator;
    private NitriteMapper nitriteMapper;

    ProjectedObjectIterable(NitriteMapper nitriteMapper,
                            RecordIterable<Document> recordIterable,
                            Class<T> projectionType) {
        this.recordIterable = recordIterable;
        this.projectionType = projectionType;
        this.nitriteMapper = nitriteMapper;
        this.iterator = new ProjectedObjectIterator(nitriteMapper);
    }

    @Override
    public Iterator<T> iterator() {
        return iterator;
    }

    @Override
    public boolean hasMore() {
        return recordIterable.hasMore();
    }

    @Override
    public int size() {
        return recordIterable.size();
    }

    @Override
    public int totalCount() {
        return recordIterable.totalCount();
    }

    @Override
    public T firstOrDefault() {
        T item = Iterables.firstOrDefault(this);
        reset();
        return item;
    }

    @Override
    public List<T> toList() {
        List<T> list = Iterables.toList(this);
        reset();
        return list;
    }

    @Override
    public void reset() {
        this.recordIterable.reset();
        this.iterator = new ProjectedObjectIterator(nitriteMapper);
    }

    @Override
    public String toString() {
        return toList().toString();
    }

    private class ProjectedObjectIterator implements Iterator<T> {
        private NitriteMapper objectMapper;

        ProjectedObjectIterator(NitriteMapper nitriteMapper) {
            this.objectMapper = nitriteMapper;
        }

        @Override
        public boolean hasNext() {
            boolean hasNext = true;
            try {
                hasNext = recordIterable.iterator().hasNext();
                return hasNext;
            } finally {
                if (!hasNext) reset();
            }
        }

        @Override
        public T next() {
            Document record = new Document(recordIterable.iterator().next());
            record.remove(DOC_ID);
            return objectMapper.asObject(record, projectionType);
        }

        @Override
        public void remove() {
            throw new InvalidOperationException(OBJ_REMOVE_ON_PROJECTED_OBJECT_ITERATOR_NOT_SUPPORTED);
        }
    }
}
