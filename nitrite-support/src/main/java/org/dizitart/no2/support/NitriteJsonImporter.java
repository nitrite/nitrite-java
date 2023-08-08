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
 import lombok.Setter;
 import org.apache.commons.codec.binary.Hex;
 import org.dizitart.no2.Nitrite;
 import org.dizitart.no2.collection.Document;
 import org.dizitart.no2.collection.NitriteCollection;
 import org.dizitart.no2.collection.NitriteId;
 import org.dizitart.no2.collection.operation.IndexManager;
 import org.dizitart.no2.common.PersistentCollection;
 import org.dizitart.no2.exceptions.NitriteIOException;
 import org.dizitart.no2.index.IndexDescriptor;
 import org.dizitart.no2.repository.ObjectRepository;
 import org.dizitart.no2.store.NitriteMap;
 import org.dizitart.no2.store.NitriteStore;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import static org.dizitart.no2.common.Constants.*;
 import static org.dizitart.no2.index.IndexOptions.indexOptions;
 
 /**
  * @author Anindya Chatterjee.
  */
 @Setter
 class NitriteJsonImporter {
     private JsonParser parser;
     private ImportOptions options;
 
     public void importData() throws IOException, ClassNotFoundException {
         try (Nitrite db = options.getNitriteFactory().create()) {
             while (parser.nextToken() != JsonToken.END_OBJECT) {
                 String fieldName = parser.getCurrentName();
 
                 if (TAG_COLLECTIONS.equals(fieldName)) {
                     readNitriteMap(db);
                 }
 
                 if (TAG_REPOSITORIES.equals(fieldName)) {
                     readNitriteMap(db);
                 }
 
                 if (TAG_KEYED_REPOSITORIES.equals(fieldName)) {
                     readNitriteMap(db);
                 }
             }
         }
     }
 
     private void readNitriteMap(Nitrite db) throws IOException {
         // move to [
         parser.nextToken();
         NitriteStore<?> nitriteStore = db.getStore();
 
         // loop till token equal to "]"
         while (parser.nextToken() != JsonToken.END_ARRAY) {
             // loop until end of collection object
             NitriteMap<NitriteId, Document> nitriteMap = null;
             List<IndexDescriptor> indexDescriptors = new ArrayList<>();
 
             while (parser.nextToken() != JsonToken.END_OBJECT) {
                 String fieldName = parser.getCurrentName();
 
                 if (TAG_NAME.equals(fieldName)) {
                     // move to next token
                     parser.nextToken();
 
                     String mapName = parser.getText();
                     nitriteMap = nitriteStore.openMap(mapName, NitriteId.class, Document.class);
                 }
 
                 if (TAG_INDICES.equals(fieldName)) {
                     indexDescriptors = readIndices();
                 }
 
                 if (TAG_DATA.equals(fieldName) && nitriteMap != null) {
                     readNitriteMapData(nitriteMap);
 
                     // write index information
                     try(IndexManager indexManager = new IndexManager(nitriteMap.getName(), db.getConfig())) {
                         // during next data insertion, index will be rebuilt
                         indexDescriptors.forEach(indexManager::markIndexDirty);
                     }
                 }
             }
         }
     }
 
     private List<IndexDescriptor> readIndices() throws IOException {
         List<IndexDescriptor> indexDescriptors = new ArrayList<>();
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
                     IndexDescriptor index = (IndexDescriptor) readEncodedObject(data);
                     indexDescriptors.add(index);
                 }
             }
         }
         return indexDescriptors;
     }
 
     private void readNitriteMapData(NitriteMap<NitriteId, Document> nitriteMap) throws IOException {
         // move to [
         parser.nextToken();
 
         // loop till token equal to "]"
         while (parser.nextToken() != JsonToken.END_ARRAY) {
             // loop until end of collection object
             NitriteId nitriteId = null;
             while (parser.nextToken() != JsonToken.END_OBJECT) {
                 String fieldName = parser.getCurrentName();
 
                 if (TAG_KEY.equals(fieldName)) {
                     parser.nextToken();
                     String data = parser.readValueAs(String.class);
                     nitriteId = (NitriteId) readEncodedObject(data);
                 }
 
                 if (TAG_VALUE.equals(fieldName)) {
                     parser.nextToken();
                     String data = parser.readValueAs(String.class);
                     Document document = (Document) readEncodedObject(data);
                     if (nitriteMap != null) {
                         nitriteMap.put(nitriteId, document);
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
             throw new NitriteIOException("Error while reading data", e);
         }
     }
 }
 