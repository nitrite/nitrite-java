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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.dizitart.no2.Document;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.IndexOptions;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.PersistentCollection;
import org.dizitart.no2.collection.objects.ObjectRepository;
import org.dizitart.no2.index.Index;

import java.io.IOException;
import java.util.Map;

import static org.dizitart.no2.common.Constants.*;

/**
 * @author Anindya Chatterjee.
 */
class NitriteJsonImporter {
    private JsonParser parser;
    private Nitrite db;

    public NitriteJsonImporter(Nitrite db) {
        this.db = db;
    }

    public void setParser(JsonParser parser) {
        this.parser = parser;
    }

    public void importData() throws IOException, ClassNotFoundException {
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = parser.getCurrentName();

            if (TAG_COLLECTIONS.equals(fieldName)) {
                readCollection();
            }

            if (TAG_REPOSITORIES.equals(fieldName)) {
                readRepository();
            }
        }
    }

    private void readRepository() throws IOException, ClassNotFoundException {
        ObjectRepository<?> repository = null;
        // move to [
        parser.nextToken();

        // loop till token equal to "]"
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            // loop until end of collection object
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = parser.getCurrentName();

                if (TAG_TYPE.equals(fieldName)) {
                    // move to next token
                    parser.nextToken();

                    String typeId = parser.getText();
                    Class<?> type = Class.forName(typeId);
                    repository = db.getRepository(type);
                }

                if (TAG_INDICES.equals(fieldName)) {
                    readIndices(repository);
                }

                if (TAG_DATA.equals(fieldName) && repository != null) {
                    readCollectionData(repository.getDocumentCollection());
                }
            }
        }
    }

    private void readCollection() throws IOException {
        NitriteCollection collection = null;
        // move to [
        parser.nextToken();

        // loop till token equal to "]"
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            // loop until end of collection object
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = parser.getCurrentName();

                if (TAG_NAME.equals(fieldName)) {
                    // move to next token
                    parser.nextToken();

                    String collectionName = parser.getText();
                    collection = db.getCollection(collectionName);
                }

                if (TAG_INDICES.equals(fieldName)) {
                    readIndices(collection);
                }

                if (TAG_DATA.equals(fieldName)) {
                    readCollectionData(collection);
                }
            }
        }
    }

    private void readIndices(PersistentCollection<?> collection) throws IOException {
        // move to [
        parser.nextToken();

        // loop till token equal to "]"
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            // loop until end of collection object
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = parser.getCurrentName();

                if (TAG_INDEX.equals(fieldName)) {
                    parser.nextToken();

                    Index index = parser.readValueAs(Index.class);
                    if (collection != null && index != null
                            && index.getField() != null
                            && !collection.hasIndex(index.getField())) {
                        collection.createIndex(index.getField(),
                                IndexOptions.indexOptions(index.getIndexType()));
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void readCollectionData(NitriteCollection collection) throws IOException {
        // move to [
        parser.nextToken();

        // loop till token equal to "]"
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            // loop until end of collection object
            Long id = null;
            Map<String, Object> objectMap = null;
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = parser.getCurrentName();

                if (TAG_KEY.equals(fieldName)) {
                    parser.nextToken();
                    id = parser.readValueAs(Long.class);
                }

                if (TAG_VALUE.equals(fieldName)) {
                    parser.nextToken();
                    objectMap = (Map<String, Object>) parser.readValueAs(Map.class);
                    objectMap.put(DOC_ID, id);
                }

                if (objectMap != null) {
                    Document document = new Document(objectMap);

                    if (collection != null) {
                        collection.insert(document);
                    }
                }
            }
        }
    }
}
