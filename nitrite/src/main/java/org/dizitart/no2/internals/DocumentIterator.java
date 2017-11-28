/*
 *
 * Copyright 2017 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
