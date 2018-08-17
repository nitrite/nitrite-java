/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
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

package org.dizitart.no2.sync;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.Cursor;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.filters.Filters;
import org.dizitart.no2.meta.Attributes;
import org.dizitart.no2.sync.types.ChangeFeed;

import java.util.ArrayList;
import java.util.List;

import static org.dizitart.no2.common.Constants.*;
import static org.dizitart.no2.filters.Filters.*;
import static org.dizitart.no2.util.DocumentUtils.createUniqueFilter;
import static org.dizitart.no2.util.DocumentUtils.isRecent;

/**
 * @author Anindya Chatterjee.
 */
@Slf4j
class LocalCollection {
    @Getter @Setter
    private NitriteCollection changeLogRepository;
    @Getter
    private NitriteCollection collection;

    LocalCollection(NitriteCollection collection) {
        this.collection = collection;
    }

    Attributes getAttributes() {
        return collection.getAttributes();
    }

    void setAttributes(Attributes attributes) {
        collection.setAttributes(attributes);
    }

    void change(ChangeFeed changeFeed) {
        if (changeFeed.getRemovedDocuments() != null) {
            remove(changeFeed.getRemovedDocuments());
        }

        if (changeFeed.getModifiedDocuments() != null) {
            modify(changeFeed.getModifiedDocuments());
        }
    }

    public void clear() {
        collection.remove(ALL);
    }

    public void insert(Document[] documents) {
        collection.insert(documents);
    }

    public String getName() {
        return collection.getName();
    }

    ChangeFeed changedSince(long lastSequence, long newSequence) {
        ChangeFeed changeFeed = new ChangeFeed();
        changeFeed.setRemovedDocuments(removedSince(lastSequence, newSequence));
        changeFeed.setModifiedDocuments(modifiedSince(lastSequence, newSequence));
        return changeFeed;
    }

    private void remove(List<Document> removed) {
        for (Document document : removed) {
            Document localDocument =
                    collection.getById(document.getId());
            if (localDocument != null) {
                if (isRecent(document, localDocument)) {
                    collection.remove(createUniqueFilter(document));
                }
            }
        }
    }

    private void modify(List<Document> modifiedDocuments) {
        for (Document doc : modifiedDocuments) {
            Document document = new Document(doc);
            document.put(DOC_SOURCE, REPLICATOR);

            Document localDocument = getLocalDocument(document);

            if (localDocument != null) {
                // local document found, so update it
                // but before check which document is most updated
                if (isRecent(document, localDocument)) {
                    // if remote document is most updated
                    // override local with remote
                    localDocument.putAll(document);
                    collection.update(localDocument);
                }
                // if local is most updated, then do nothing
            } else {
                // local document not found
                // check if it has been deleted recently
                Cursor removeLogs = changeLogRepository.find(
                        and(
                                eq(COLLECTION, getName()),
                                eq(DELETED_ID, document.getId().getIdValue()),
                                gt(DELETE_TIME, document.getLastModifiedTime())
                        )
                );
                if (removeLogs == null || removeLogs.size() == 0) {
                    // not recently deleted, so insert it
                    collection.insert(new Document[] { document });
                }
            }
        }
    }

    private Document getLocalDocument(Document document) {
        NitriteId id = document.getId();
        Document doc = collection.getById(id);
        if (doc != null) {
            return new Document(doc);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<Document> removedSince(long lastSequence, long newSequence) {
        Iterable<Document> removeLogs = changeLogRepository.find(
                and(
                        eq(COLLECTION, getName()),
                        gte(DELETE_TIME, lastSequence),
                        lte(DELETE_TIME, newSequence)
                )
        );

        List<Document> result = new ArrayList<>();
        if (removeLogs != null) {
            for (Document logEntry : removeLogs) {
                Document document = new Document(logEntry.get(DELETED_ITEM, Document.class));
                result.add(document);
            }
            log.debug("Removed since in " + getName() + ": from " + lastSequence + " now " + newSequence + " - " + result);
        }

        return result;
    }

    private List<Document> modifiedSince(long lastSequence, long newSequence) {
        Iterable<Document> findResult = collection.find(
                Filters.and(
                        Filters.gte(DOC_MODIFIED, lastSequence),
                        Filters.lte(DOC_MODIFIED, newSequence)
                ));

        List<Document> result = new ArrayList<>();
        for (Document document : findResult) {
            Document doc = new Document(document);
            result.add(doc);
        }

        log.debug("Modified since in " + getName() + ": from " + lastSequence + " to " + newSequence + " - " + result);

        return result;
    }
}
