/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.no2.support;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.apache.commons.codec.binary.Hex;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.PersistentCollection;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.index.IndexEntry;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.repository.ObjectRepository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import static org.dizitart.no2.common.Constants.*;

/**
 * @author Anindya Chatterjee.
 */
class NitriteJsonImporter {
    private JsonParser parser;
    private final Nitrite db;

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

            if (TAG_KEYED_REPOSITORIES.equals(fieldName)) {
                readKeyedRepository();
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

    private void readKeyedRepository() throws IOException, ClassNotFoundException {
        ObjectRepository<?> repository = null;
        // move to [
        parser.nextToken();

        // loop till token equal to "]"
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            String key = null;

            // loop until end of collection object
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = parser.getCurrentName();

                if (TAG_KEY.equals(fieldName)) {
                    parser.nextToken();
                    key = parser.getText();
                }

                if (key != null && TAG_TYPE.equals(fieldName)) {
                    // move to next token
                    parser.nextToken();

                    String typeId = parser.getText();
                    Class<?> type = Class.forName(typeId);
                    repository = db.getRepository(type, key);
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
                    String data = parser.readValueAs(String.class);
                    IndexEntry index = (IndexEntry) readEncodedObject(data);
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

    private void readCollectionData(NitriteCollection collection) throws IOException {
        // move to [
        parser.nextToken();

        // loop till token equal to "]"
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            // loop until end of collection object
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = parser.getCurrentName();

                if (TAG_KEY.equals(fieldName)) {
                    parser.nextToken();
                    parser.readValueAs(String.class);
                }

                if (TAG_VALUE.equals(fieldName)) {
                    parser.nextToken();
                    String data = parser.readValueAs(String.class);
                    Document document = (Document) readEncodedObject(data);
                    if (collection != null) {
                        collection.insert(document);
                    }
                }
            }
        }
    }

    private Object readEncodedObject(String hexString) {
        try {
            byte[] data = Hex.decodeHex(hexString);
            try (ByteArrayInputStream is = new ByteArrayInputStream(data)) {
                try (ObjectInputStream ois = new ObjectInputStream(is)) {
                    return ois.readObject();
                }
            }
        } catch (Exception e) {
            throw new NitriteIOException("error while reading data", e);
        }
    }
}
