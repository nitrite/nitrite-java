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

package org.dizitart.no2.collection.operation;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteContext;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.*;
import org.dizitart.no2.event.ChangeInfo;
import org.dizitart.no2.event.ChangeListener;
import org.dizitart.no2.event.EventBus;
import org.dizitart.no2.index.ComparableIndexer;
import org.dizitart.no2.index.Index;
import org.dizitart.no2.index.TextIndexer;
import org.dizitart.no2.index.fulltext.EnglishTextTokenizer;
import org.dizitart.no2.index.fulltext.TextTokenizer;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.store.IndexStore;
import org.dizitart.no2.store.NitriteMap;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.dizitart.no2.exceptions.ErrorCodes.*;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;
import static org.dizitart.no2.util.ValidationUtils.notNull;

/**
 * A service class for Nitrite database operations.
 *
 * @since 1.0
 * @author Anindya Chatterjee
 */
public class CollectionOperation {
    private NitriteContext nitriteContext;
    private NitriteMap<NitriteId, Document> mapStore;
    private IndexTemplate indexTemplate;
    private ReadWriteOperation readWriteOperation;
    private QueryTemplate queryTemplate;
    private ComparableIndexer comparableIndexer;
    private TextIndexer textIndexer;
    private IndexStore indexStore;
    private EventBus<ChangeInfo, ChangeListener> eventBus;

    /**
     * Instantiates a new Nitrite service.
     *
     * @param mapStore       the map store
     * @param nitriteContext the nitrite context
     */
    public CollectionOperation(NitriteMap<NitriteId, Document> mapStore,
                        NitriteContext nitriteContext,
                        EventBus<ChangeInfo, ChangeListener> eventBus) {
        this.mapStore = mapStore;
        this.nitriteContext = nitriteContext;
        this.eventBus = eventBus;
        init();
    }

    /**
     * Specifies if an indexing operation is currently running.
     *
     * @param field the field
     * @return `true` if operation is still running; `false` otherwise.
     */
    public boolean isIndexing(String field) {
        notNull(field, errorMessage("field can not be null", VE_IS_INDEXING_NULL_FIELD));
        return indexTemplate.isIndexing(field);
    }

    /**
     * Specifies if a value is indexed.
     *
     * @param field the field
     * @return `true` if indexed; `false` otherwise.
     */
    public boolean hasIndex(String field) {
        notNull(field, errorMessage("field can not be null", VE_HAS_INDEX_NULL_FIELD));
        return indexStore.hasIndex(field);
    }

    /**
     * Finds with equal filer using index.
     *
     * @param field the field
     * @param value the value
     * @return the result set.
     */
    public Set<NitriteId> findEqualWithIndex(String field, Object value) {
        notNull(field, errorMessage("field can not be null", VE_FIND_EQUAL_INDEX_NULL_FIELD));
        if (value == null) return new HashSet<>();
        return comparableIndexer.findEqual(field, value);
    }

    /**
     * Finds with greater than filer using index.
     *
     * @param field the field
     * @param value the value
     * @return the result set
     */
    public Set<NitriteId> findGreaterThanWithIndex(String field, Comparable value) {
        notNull(field, errorMessage("field can not be null", VE_FIND_GT_INDEX_NULL_FIELD));
        notNull(value, errorMessage("value can not be null", VE_FIND_GT_INDEX_NULL_VALUE));
        return comparableIndexer.findGreaterThan(field, value);
    }

    /**
     * Finds with greater and equal filer using index.
     *
     * @param field the field
     * @param value the value
     * @return the result set
     */
    public Set<NitriteId> findGreaterEqualWithIndex(String field, Comparable value) {
        notNull(field, errorMessage("field can not be null", VE_FIND_GTE_INDEX_NULL_FIELD));
        notNull(value, errorMessage("value can not be null", VE_FIND_GTE_INDEX_NULL_VALUE));
        return comparableIndexer.findGreaterEqual(field, value);
    }

    /**
     * Finds with lesser filer using index.
     *
     * @param field the field
     * @param value the value
     * @return the result set
     */
    public Set<NitriteId> findLesserThanWithIndex(String field, Comparable value) {
        notNull(field, errorMessage("field can not be null", VE_FIND_LT_INDEX_NULL_FIELD));
        notNull(value, errorMessage("value can not be null", VE_FIND_LT_INDEX_NULL_VALUE));
        return comparableIndexer.findLesserThan(field, value);
    }

    /**
     * Finds with lesser equal filer using index.
     *
     * @param field the field
     * @param value the value
     * @return the result set
     */
    public Set<NitriteId> findLesserEqualWithIndex(String field, Comparable value) {
        notNull(field, errorMessage("field can not be null", VE_FIND_LTE_INDEX_NULL_FIELD));
        notNull(value, errorMessage("value can not be null", VE_FIND_LTE_INDEX_NULL_VALUE));
        return comparableIndexer.findLesserEqual(field, value);
    }

    /**
     * Finds with in filer using index.
     *
     * @param field  the value
     * @param values the values
     * @return the result set
     */
    public Set<NitriteId> findInWithIndex(String field, List<Object> values) {
        notNull(field, errorMessage("field can not be null", VE_FIND_IN_INDEX_NULL_FIELD));
        notNull(values, errorMessage("values can not be null", VE_FIND_IN_INDEX_NULL_VALUE));
        return comparableIndexer.findIn(field, values);
    }

    /**
     * Finds with text filer using full-text index.
     *
     * @param field the value
     * @param value the value
     * @return the result set
     */
    public Set<NitriteId> findTextWithIndex(String field, String value) {
        notNull(field, errorMessage("field can not be null", VE_FIND_TEXT_INDEX_NULL_FIELD));
        notNull(value, errorMessage("value can not be null", VE_FIND_TEXT_INDEX_NULL_VALUE));
        return textIndexer.findText(field, value);
    }

    /**
     * Gets the {@link NitriteMapper} implementation.
     *
     * @return the nitrite mapper
     */
    public NitriteMapper getNitriteMapper() {
        return nitriteContext.getNitriteMapper();
    }

    /**
     * Creates an index.
     *
     * @param field     the value
     * @param indexType the index type
     * @param async     asynchronous operation if set to `true`
     */
    public void createIndex(String field, IndexType indexType, boolean async) {
        notNull(field, errorMessage("field can not be null", VE_CREATE_INDEX_NULL_FIELD));
        notNull(indexType, errorMessage("indexType can not be null", VE_CREATE_INDEX_NULL_INDEX_TYPE));
        indexTemplate.ensureIndex(field, indexType, async);
    }

    /**
     * Rebuilds an index.
     *
     * @param index   the index
     * @param isAsync asynchronous operation if set to `true`
     */
    public void rebuildIndex(Index index, boolean isAsync) {
        notNull(index, errorMessage("index can not be null", VE_REBUILD_INDEX_NULL_INDEX));
        indexTemplate.rebuildIndex(index, isAsync);
    }

    /**
     * Finds the index information of a value.
     *
     * @param field the value
     * @return the index information.
     */
    public Index findIndex(String field) {
        notNull(field, errorMessage("field can not be null", VE_FIND_INDEX_NULL_INDEX));
        return indexStore.findIndex(field);
    }

    /**
     * Drops the index of a value.
     *
     * @param field the value
     */
    public void dropIndex(String field) {
        notNull(field, errorMessage("field can not be null", VE_DROP_INDEX_NULL_FIELD));
        indexTemplate.dropIndex(field);
    }

    /**
     * Drops all indices.
     */
    public void dropAllIndices() {
        indexTemplate.dropAllIndices();
    }

    /**
     * Gets indices information of all indexed fields.
     *
     * @return the collection of index information.
     */
    public Collection<Index> listIndexes() {
        return indexTemplate.listIndexes();
    }

    /**
     * Inserts documents in the database.
     *
     * @param document  the document to insert
     * @param documents other documents to insert
     * @return the write result
     */
    public WriteResultImpl insert(Document document, Document... documents) {
        notNull(document, errorMessage("document can not be null", VE_INSERT_NULL_DOCUMENT));

        int length = documents == null ? 0 : documents.length;

        if (length > 0) {
            Document[] array = new Document[length + 1];
            array[0] = document;
            System.arraycopy(documents, 0, array, 1, length);
            return readWriteOperation.insert(array);
        } else {
            return readWriteOperation.insert(document);
        }
    }

    /**
     * Inserts documents in the database.
     *
     * @param documents the documents to insert
     * @return the write result
     */
    public WriteResult insert(Document[] documents) {
        notNull(documents, errorMessage("documents can not be null", VE_INSERT_NULL_DOCUMENT_ARRAY));
        return readWriteOperation.insert(documents);
    }

    /**
     * Queries the database.
     *
     * @param filter the filter
     * @return the result set
     */
    public Cursor find(Filter filter) {
        return queryTemplate.find(filter);
    }

    /**
     * Returns ids of all records stored in the database.
     *
     * @return the result set
     */
    public Cursor find() {
        return queryTemplate.find();
    }

    /**
     * Queries the database.
     *
     * @param findOptions the find options
     * @return the result set
     */
    public Cursor find(FindOptions findOptions) {
        notNull(findOptions, errorMessage("findOptions can not be null", VE_FIND_NULL_FIND_OPTIONS));
        return queryTemplate.find(findOptions);
    }

    /**
     * Queries the database.
     *
     * @param filter      the filter
     * @param findOptions the find options
     * @return the result set
     */
    public Cursor find(Filter filter, FindOptions findOptions) {
        notNull(findOptions, errorMessage("findOptions can not be null", VE_FIND_FILTERED_NULL_FIND_OPTIONS));
        return queryTemplate.find(filter, findOptions);
    }

    /**
     * Gets a document by its id.
     *
     * @param nitriteId the nitrite id
     * @return the document associated with the id; `null` otherwise.
     */
    public Document getById(NitriteId nitriteId) {
        notNull(nitriteId, errorMessage("nitriteId can not be null", VE_GET_BY_ID_NULL_ID));
        return readWriteOperation.getById(nitriteId);
    }

    /**
     * Updates a document in the database.
     *
     * @param filter        the filter
     * @param update        the update
     * @param updateOptions the update options
     * @return the write result
     */
    public WriteResultImpl update(Filter filter, Document update, UpdateOptions updateOptions) {
        notNull(update, errorMessage("update document can not be null", VE_UPDATE_NULL_DOCUMENT));
        notNull(updateOptions, errorMessage("updateOptions can not be null", VE_UPDATE_NULL_UPDATE_OPTIONS));
        return readWriteOperation.update(filter, update, updateOptions);
    }

    /**
     * Removes documents from the database.
     *
     * @param filter        the filter
     * @param removeOptions the remove options
     * @return the write result
     */
    public WriteResultImpl remove(Filter filter, RemoveOptions removeOptions) {
        return readWriteOperation.remove(filter, removeOptions);
    }

    /**
     * Drops a nitrite collection from the store.
     */
    public void dropCollection() {
        indexTemplate.dropAllIndices();
        mapStore.getStore().removeMap(mapStore);
    }

    /**
     * Gets text indexing service.
     *
     * @return the text indexing service
     */
    private TextIndexer getTextIndexer() {
        TextIndexer textIndexer = nitriteContext.getTextIndexer();
        TextTokenizer textTokenizer = getTextTokenizer();

        if (textIndexer == null) {
            textIndexer = new NitriteTextIndexer(mapStore, textTokenizer, indexStore);
        }
        return textIndexer;
    }

    /**
     * Gets text tokenizer.
     *
     * @return the text tokenizer
     */
    private TextTokenizer getTextTokenizer() {
        TextTokenizer textTokenizer = nitriteContext.getTextTokenizer();
        if (textTokenizer == null) {
            textTokenizer = new EnglishTextTokenizer();
        }
        return textTokenizer;
    }

    private void init() {
        this.indexStore = new NitriteIndexStore(mapStore);
        this.textIndexer = getTextIndexer();
        this.comparableIndexer = new NitriteComparableIndexer(mapStore, indexStore);
        this.indexTemplate = new IndexTemplate(indexStore, comparableIndexer, textIndexer);
        this.queryTemplate = new QueryTemplate(this, mapStore);
        this.readWriteOperation = new ReadWriteOperation(indexTemplate, queryTemplate, mapStore, eventBus);
    }
}
