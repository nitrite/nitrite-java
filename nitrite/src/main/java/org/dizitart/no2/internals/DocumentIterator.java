package org.dizitart.no2.internals;

import org.dizitart.no2.Document;
import org.dizitart.no2.Resettable;
import org.dizitart.no2.exceptions.InvalidOperationException;

import java.util.Iterator;

import static org.dizitart.no2.exceptions.ErrorMessage.REMOVE_ON_DOCUMENT_ITERATOR_NOT_SUPPORTED;

/**
 * @author Anindya Chatterjee.
 */
abstract class DocumentIterator implements Iterator<Document> {
    private Resettable<Document> resettable;
    Document nextElement;

    DocumentIterator(Resettable<Document> resettable) {
        this.resettable = resettable;
    }

    @Override
    public boolean hasNext() {
        boolean hasNext = true;
        try {
            hasNext = nextElement != null;
            return hasNext;
        } finally {
            if (!hasNext) resettable.reset();
        }
    }

    @Override
    public Document next() {
        Document returnValue = nextElement;
        nextMatch();
        return returnValue;
    }

    @Override
    public void remove() {
        throw new InvalidOperationException(REMOVE_ON_DOCUMENT_ITERATOR_NOT_SUPPORTED);
    }

    abstract void nextMatch();
}
