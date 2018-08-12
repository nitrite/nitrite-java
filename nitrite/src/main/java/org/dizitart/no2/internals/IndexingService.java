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

package org.dizitart.no2.internals;

import org.dizitart.no2.*;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.exceptions.UniqueConstraintException;
import org.dizitart.no2.fulltext.TextIndexingService;
import org.dizitart.no2.store.NitriteMap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.dizitart.no2.exceptions.ErrorCodes.*;
import static org.dizitart.no2.exceptions.ErrorMessage.NON_STRING_VALUE_IN_FULL_TEXT_INDEX;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;
import static org.dizitart.no2.util.DocumentUtils.getFieldValue;
import static org.dizitart.no2.util.DocumentUtils.getFields;
import static org.dizitart.no2.util.ValidationUtils.validateDocumentIndexField;

/**
 * @author Anindya Chatterjee.
 */
class IndexingService {
    private final NitriteMap<NitriteId, Document> underlyingMap;
    private final IndexMetaService indexMetaService;
    private final Map<String, AtomicBoolean> indexBuildRegistry;
    private final ExecutorService rebuildExecutor;
    private final TextIndexingService textIndexingService;

    private final Object indexLock = new Object();

    IndexingService(IndexMetaService indexMetaService,
                    TextIndexingService textIndexingService,
                    NitriteContext nitriteContext) {
        this.indexBuildRegistry = new ConcurrentHashMap<>();
        this.rebuildExecutor = nitriteContext.getWorkerPool();
        this.indexMetaService = indexMetaService;
        this.textIndexingService = textIndexingService;
        this.underlyingMap = indexMetaService.getUnderlyingMap();
    }


    void createIndex(String field, IndexType indexType, boolean isAsync) {
        Index index;

        synchronized (indexLock) {
            if (!indexMetaService.hasIndex(field)) {
                // if no index create index
                index = indexMetaService.createIndexMetadata(field, indexType);
            } else {
                // if index already there throw
                throw new IndexingException(errorMessage(
                        "index already exists on " + field, IE_INDEX_EXISTS));
            }
        }

        try {
            rebuildIndex(index, isAsync);
        } catch (IllegalStateException ise) {
            throw new IndexingException(errorMessage(
                    ise.getMessage(), IE_CREATE_INDEX_FAILED), ise);
        }
    }

    void updateIndexEntry(Document document, NitriteId nitriteId) {
        Set<String> fields = getFields(document);

        for (String field : fields) {
            Index index = indexMetaService.findIndex(field);
            if (index != null) {
                Object fieldValue = getFieldValue(document, field);

                if (fieldValue == null) continue;
                validateDocumentIndexField(fieldValue, field);

                // if dirty index and currently indexing is not running, rebuild
                if (indexMetaService.isDirtyIndex(field)
                        && indexBuildRegistry.get(field) != null
                        && !indexBuildRegistry.get(field).get()) {
                    // rebuild will also take care of the current document
                    rebuildIndex(index, true);
                } else {
                    IndexType indexType = index.getIndexType();

                    if (indexType == IndexType.Fulltext && fieldValue instanceof String) {
                        // update text index
                        textIndexingService.updateIndex(nitriteId, field, (String) fieldValue);
                    } else {
                        synchronized (indexLock) {
                            NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap
                                    = indexMetaService.getIndexMap(field);

                            // create the nitriteId list associated with the value
                            ConcurrentSkipListSet<NitriteId> nitriteIdList
                                    = indexMap.get((Comparable) fieldValue);

                            if (nitriteIdList == null) {
                                nitriteIdList = new ConcurrentSkipListSet<>();
                            }

                            if (indexType == IndexType.Unique && nitriteIdList.size() == 1
                                    && !nitriteIdList.contains(nitriteId)) {
                                // if key is already exists for unique type, throw error
                                throw new UniqueConstraintException(errorMessage(
                                        "unique key constraint violation for " + field,
                                        UCE_UPDATE_INDEX_CONSTRAINT_VIOLATED));
                            }

                            // add the nitriteId to the list
                            nitriteIdList.add(nitriteId);
                            indexMap.put((Comparable) fieldValue, nitriteIdList);
                        }
                    }
                }
            }
        }
    }

    void removeIndexEntry(Document document, NitriteId nitriteId) {
        Set<String> fields = getFields(document);

        for (String field : fields) {
            Index index = indexMetaService.findIndex(field);
            if (index != null) {
                Object fieldValue = getFieldValue(document, field);

                if (fieldValue == null) continue;

                // if dirty index and currently indexing is not running, rebuild
                if (indexMetaService.isDirtyIndex(field)
                        && indexBuildRegistry.get(field) != null
                        && !indexBuildRegistry.get(field).get()) {
                    // rebuild will also take care of the current document
                    rebuildIndex(index, true);
                } else {
                    IndexType indexType = index.getIndexType();

                    if (indexType == IndexType.Fulltext && fieldValue instanceof String) {
                        textIndexingService.deleteIndex(nitriteId, field, (String) fieldValue);
                    } else {
                        NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap
                                = indexMetaService.getIndexMap(field);

                        // create the nitrite list associated with the value
                        if (fieldValue instanceof Comparable) {
                            ConcurrentSkipListSet<NitriteId> nitriteIdList = indexMap.get((Comparable) fieldValue);
                            if (nitriteIdList != null) {
                                nitriteIdList.remove(nitriteId);
                                if (nitriteIdList.size() == 0) {
                                    indexMap.remove((Comparable) fieldValue);
                                } else {
                                    indexMap.put((Comparable) fieldValue, nitriteIdList);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    void refreshIndexEntry(Document oldDocument, Document newDocument, NitriteId nitriteId) {
        Set<String> fields = getFields(newDocument);

        for (String field : fields) {
            Index index = indexMetaService.findIndex(field);
            if (index != null) {
                Object newValue = getFieldValue(newDocument, field);
                Object oldValue = getFieldValue(oldDocument, field);

                if (newValue == null) continue;
                if (newValue instanceof Comparable && oldValue instanceof Comparable) {
                    if (((Comparable) newValue).compareTo(oldValue) == 0) continue;
                }

                validateDocumentIndexField(newValue, field);

                // if dirty index and currently indexing is not running, rebuild
                if (indexMetaService.isDirtyIndex(field) &&
                        indexBuildRegistry.get(field) != null
                        && !indexBuildRegistry.get(field).get()) {
                    // rebuild will also take care of the current document
                    rebuildIndex(index, true);
                } else {
                    IndexType indexType = index.getIndexType();

                    if (indexType == IndexType.Fulltext && newValue instanceof String) {
                        // update text index
                        textIndexingService.updateIndex(nitriteId, field, (String) newValue);
                    } else {
                        synchronized (indexLock) {
                            NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap
                                    = indexMetaService.getIndexMap(field);

                            // create the nitriteId list associated with the value
                            ConcurrentSkipListSet<NitriteId> nitriteIdList
                                    = indexMap.get((Comparable) newValue);

                            if (nitriteIdList == null) {
                                nitriteIdList = new ConcurrentSkipListSet<>();
                            }

                            if (indexType == IndexType.Unique && nitriteIdList.size() == 1
                                    && !nitriteIdList.contains(nitriteId)) {
                                // if key is already exists for unique type, throw error
                                throw new UniqueConstraintException(errorMessage(
                                        "unique key constraint violation for " + field,
                                        UCE_REFRESH_INDEX_CONSTRAINT_VIOLATED));
                            }

                            // add the nitriteId to the list
                            nitriteIdList.add(nitriteId);
                            indexMap.put((Comparable) newValue, nitriteIdList);

                            nitriteIdList = indexMap.get((Comparable) oldValue);
                            if (nitriteIdList != null && !nitriteIdList.isEmpty()) {
                                nitriteIdList.remove(nitriteId);
                                if (nitriteIdList.size() == 0) {
                                    indexMap.remove((Comparable) oldValue);
                                } else {
                                    indexMap.put((Comparable) oldValue, nitriteIdList);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    Collection<Index> listIndexes() {
        return indexMetaService.listIndexes();
    }

    boolean isIndexing(String field) {
        // has index will only return true, if there is an index on
        // the value and indexing is not running on it
        return indexMetaService.hasIndex(field)
                && indexBuildRegistry.get(field) != null
                && indexBuildRegistry.get(field).get();
    }

    void dropIndex(String field) {
        if (indexBuildRegistry.get(field) != null
                && indexBuildRegistry.get(field).get()) {
            throw new IndexingException(errorMessage(
                    "can not drop index as indexing is running on " + field,
                    IE_CAN_NOT_DROP_RUNNING_INDEX));
        }

        if (indexMetaService.hasTextIndex(field)) {
            textIndexingService.deleteIndexesByField(field);
        } else {
            indexMetaService.dropIndex(field);
        }
        indexBuildRegistry.remove(field);
    }

    void dropAllIndices() {
        for (Map.Entry<String, AtomicBoolean> entry :indexBuildRegistry.entrySet()) {
            if (entry.getValue() != null && entry.getValue().get()) {
                throw new IndexingException(errorMessage(
                        "can not drop index as indexing is running on " + entry.getKey(),
                        IE_CAN_NOT_DROP_ALL_RUNNING_INDEX));
            }
        }

        indexMetaService.dropAll();
        textIndexingService.drop();
        indexBuildRegistry.clear();
    }

    // call to this method is already synchronized, only one thread per value
    // can access it only if rebuild is already not running for that value
    void rebuildIndex(final Index index, boolean isAsync) {
        final String field = index.getField();
        if (getBuildFlag(field).compareAndSet(false, true)) {
            if (isAsync) {
                rebuildExecutor.submit(new Runnable() {
                    @Override
                    public void run() {
                        buildIndexInternal(field, index);
                    }
                });
            } else {
                buildIndexInternal(field, index);
            }
            return;
        }
        throw new IndexingException(errorMessage(
                "indexing is already running on " + index.getField(),
                IE_REBUILD_INDEX_RUNNING));
    }

    private void buildIndexInternal(final String field, final Index index) {
        try {
            // first put dirty marker
            indexMetaService.markDirty(field);

            if (index.getIndexType() != IndexType.Fulltext) {
                // create index map
                NitriteMap<Comparable, ConcurrentSkipListSet<NitriteId>> indexMap
                        = indexMetaService.getIndexMap(field);

                // remove old values
                indexMap.clear();

                for (Map.Entry<NitriteId, Document> entry : underlyingMap.entrySet()) {
                    // create the document
                    Document object = entry.getValue();

                    // retrieved the value from document
                    Object fieldValue = getFieldValue(object, field);

                    if (fieldValue == null) continue;
                    validateDocumentIndexField(fieldValue, field);

                    // create the id list associated with the value
                    ConcurrentSkipListSet<NitriteId> nitriteIdList = indexMap.get((Comparable) fieldValue);
                    if (nitriteIdList == null) {
                        nitriteIdList = new ConcurrentSkipListSet<>();
                    }

                    if (index.getIndexType() == IndexType.Unique
                            && nitriteIdList.size() == 1) {
                        // if key is already exists for unique type, throw error
                        throw new UniqueConstraintException(errorMessage(
                                "unique key constraint violation for " + field,
                                UCE_BUILD_INDEX_CONSTRAINT_VIOLATED));
                    }

                    // add the id to the list
                    nitriteIdList.add(entry.getKey());
                    indexMap.put((Comparable) fieldValue, nitriteIdList);
                }
            } else {
                // for update-rebuild or remove-rebuild this block will never come
                for (Map.Entry<NitriteId, Document> entry : underlyingMap.entrySet()) {
                    // create the document
                    Document object = entry.getValue();

                    // retrieve the value from document
                    Object fieldValue = getFieldValue(object, field);

                    if (fieldValue == null) continue;
                    if (!(fieldValue instanceof String)) {
                        throw new IndexingException(NON_STRING_VALUE_IN_FULL_TEXT_INDEX);
                    }

                    textIndexingService.createIndex(entry.getKey(), field, (String) fieldValue);
                }
            }
        } finally {
            // remove dirty marker to denote indexing completed successfully
            // if dirty marker is found in any index, it needs to be rebuild
            indexMetaService.unmarkDirty(field);
            getBuildFlag(field).set(false);
        }
    }

    private synchronized AtomicBoolean getBuildFlag(String field) {
        AtomicBoolean flag = indexBuildRegistry.get(field);
        if (flag != null) return flag;

        flag = new AtomicBoolean(false);
        indexBuildRegistry.put(field, flag);
        return flag;
    }
}
