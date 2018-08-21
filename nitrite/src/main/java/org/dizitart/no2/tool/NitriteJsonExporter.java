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

package org.dizitart.no2.tool;

import com.fasterxml.jackson.core.JsonGenerator;
import org.dizitart.no2.Document;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Cursor;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.PersistentCollection;
import org.dizitart.no2.collection.objects.ObjectRepository;
import org.dizitart.no2.index.Index;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.dizitart.no2.common.Constants.*;

/**
 * @author Anindya Chatterjee
 */
class NitriteJsonExporter {
    private Nitrite db;
    private JsonGenerator generator;
    private ExportOptions options;

    public NitriteJsonExporter(Nitrite db) {
        this.db = db;
    }

    public void setGenerator(JsonGenerator generator) {
        this.generator = generator;
    }

    public void exportData() throws IOException, ClassNotFoundException {
        List<PersistentCollection<?>> collections = options.getCollections();
        if (collections.isEmpty()) {
            Set<String> collectionNames = db.listCollectionNames();
            Set<String> repositoryNames = db.listRepositories();

            generator.writeStartObject();

            generator.writeFieldName(TAG_COLLECTIONS);
            generator.writeStartArray();
            for (String collectionName : collectionNames) {
                NitriteCollection nitriteCollection = db.getCollection(collectionName);
                writeCollection(nitriteCollection);
            }
            generator.writeEndArray();

            generator.writeFieldName(TAG_REPOSITORIES);
            generator.writeStartArray();
            for (String repoName : repositoryNames) {
                Class<?> type = Class.forName(repoName);
                ObjectRepository<?> repository = db.getRepository(type);
                writeRepository(repository);
            }
            generator.writeEndArray();

            generator.writeEndObject();
        } else {
            for (PersistentCollection<?> collection : collections) {
                if (collection != null) {
                    generator.writeStartObject();
                    if (collection instanceof NitriteCollection) {
                        NitriteCollection nitriteCollection = (NitriteCollection) collection;
                        generator.writeFieldName(TAG_COLLECTIONS);
                        generator.writeStartArray();
                        writeCollection(nitriteCollection);
                        generator.writeEndArray();
                    } else if (collection instanceof ObjectRepository) {
                        ObjectRepository<?> repository = (ObjectRepository<?>) collection;
                        generator.writeFieldName(TAG_REPOSITORIES);
                        generator.writeStartArray();
                        writeRepository(repository);
                        generator.writeEndArray();
                    }
                    generator.writeEndObject();
                }
            }
        }
        generator.close();
    }

    private void writeRepository(ObjectRepository<?> repository) throws IOException {
        generator.writeStartObject();
        generator.writeFieldName(TAG_TYPE);
        generator.writeString(repository.getType().getName());

        Collection<Index> indices = repository.listIndices();
        writeIndices(indices);

        Cursor cursor = repository.getDocumentCollection().find();
        writeContent(cursor);
        generator.writeEndObject();
    }

    private void writeCollection(NitriteCollection nitriteCollection) throws IOException {
        generator.writeStartObject();
        generator.writeFieldName(TAG_NAME);
        generator.writeString(nitriteCollection.getName());

        Collection<Index> indices = nitriteCollection.listIndices();
        writeIndices(indices);

        Cursor cursor = nitriteCollection.find();
        writeContent(cursor);
        generator.writeEndObject();
    }

    private void writeIndices(Collection<Index> indices) throws IOException {
        generator.writeFieldName(TAG_INDICES);
        generator.writeStartArray();
        if (options.isExportIndices()) {
            for (Index index : indices) {
                generator.writeStartObject();
                generator.writeFieldName(TAG_INDEX);
                generator.writeObject(index);
                generator.writeEndObject();
            }
        }
        generator.writeEndArray();
    }

    private void writeContent(Cursor cursor) throws IOException {
        generator.writeFieldName(TAG_DATA);
        generator.writeStartArray();
        if (options.isExportData()) {
            for (Document document : cursor) {
                generator.writeStartObject();
                generator.writeFieldName(TAG_KEY);
                generator.writeObject(document.get(DOC_ID));

                generator.writeFieldName(TAG_VALUE);
                generator.writeObject(document);
                generator.writeEndObject();
            }
        }
        generator.writeEndArray();
    }

    public void setOptions(ExportOptions options) {
        this.options = options;
    }
}
