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

 package org.dizitart.no2.support.exchange;

import com.fasterxml.jackson.core.JsonGenerator;
import lombok.Setter;
import org.apache.commons.codec.binary.Base64;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.operation.IndexManager;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.index.IndexDescriptor;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.stream.Collectors;

import static org.dizitart.no2.common.Constants.*;
import static org.dizitart.no2.common.util.ObjectUtils.findRepositoryName;

 /**
  * @author Anindya Chatterjee
  */
 @Setter
 class NitriteJsonExporter {
     private JsonGenerator generator;
     private ExportOptions options;

     public void exportData() throws IOException, ClassNotFoundException {
         try(Nitrite db = options.getNitriteFactory().create()) {
             Set<String> collectionNames = options.getCollections() == null ? db.listCollectionNames() : new HashSet<>();
             Set<String> repositoryNames = options.getRepositories() == null ? db.listRepositories() : new HashSet<>();
             Map<String, Set<String>> keyedRepositoryNames = options.getKeyedRepositories() == null
                 ? db.listKeyedRepositories() : new HashMap<>();

             List<IndexDescriptor> indexDescriptors = new ArrayList<>();
             if (options.getCollections() != null && !options.getCollections().isEmpty()) {
                 collectionNames = new HashSet<>(options.getCollections());
             }

             if (options.getRepositories() != null && !options.getRepositories().isEmpty()) {
                 repositoryNames = new HashSet<>(options.getRepositories());
             }

             if (options.getKeyedRepositories() != null && !options.getKeyedRepositories().isEmpty()) {
                 keyedRepositoryNames = options.getKeyedRepositories();
             }

             if (options.isExportIndices()) {
                 for (String collectionName : collectionNames) {
                     try(IndexManager indexManager = new IndexManager(collectionName, db.getConfig())) {
                         indexDescriptors.addAll(indexManager.getIndexDescriptors());
                     }
                 }

                 for (String repositoryName : repositoryNames) {
                     try(IndexManager indexManager = new IndexManager(repositoryName, db.getConfig())) {
                         indexDescriptors.addAll(indexManager.getIndexDescriptors());
                     }
                 }

                 for (Map.Entry<String, Set<String>> entry : keyedRepositoryNames.entrySet()) {
                     String key = entry.getKey();
                     Set<String> entityNameSet = entry.getValue();
                     for (String entityName : entityNameSet) {
                         String repositoryName = findRepositoryName(key, entityName);
                         try(IndexManager indexManager = new IndexManager(repositoryName, db.getConfig())) {
                             indexDescriptors.addAll(indexManager.getIndexDescriptors());
                         }
                     }
                 }
             }

             exportData(db, collectionNames, repositoryNames, keyedRepositoryNames, indexDescriptors);
             generator.close();
         }
     }

     private void exportData(Nitrite db,
                             Set<String> collectionNames,
                             Set<String> repositoryNames,
                             Map<String, Set<String>> keyedRepositoryNames,
                             List<IndexDescriptor> indexDescriptors) throws IOException {
         NitriteStore<?> nitriteStore = db.getStore();

         generator.writeStartObject();

         writeMaps(collectionNames, indexDescriptors, nitriteStore, TAG_COLLECTIONS);

         writeMaps(repositoryNames, indexDescriptors, nitriteStore, TAG_REPOSITORIES);

         writeKeyedMaps(keyedRepositoryNames, indexDescriptors, nitriteStore);

         generator.writeEndObject();
     }

     private void writeMaps(Set<String> mapNames, List<IndexDescriptor> indexDescriptors,
                            NitriteStore<?> nitriteStore, String tagName) throws IOException {
         generator.writeFieldName(tagName);
         generator.writeStartArray();
         for (String mapName : mapNames) {
             try(NitriteMap<NitriteId, Document> nitriteMap
                 = nitriteStore.openMap(mapName, NitriteId.class, Document.class)) {
                 List<IndexDescriptor> indexes = indexDescriptors.stream().filter(d ->
                     mapName.equalsIgnoreCase(d.getCollectionName())).collect(Collectors.toList());
                 writeNitriteMap(nitriteMap, indexes);
             }
         }
         generator.writeEndArray();
     }

     private void writeKeyedMaps(Map<String, Set<String>> keyedMapNames, List<IndexDescriptor> indexDescriptors,
                                 NitriteStore<?> nitriteStore) throws IOException {
         generator.writeFieldName(TAG_KEYED_REPOSITORIES);
         generator.writeStartArray();
         for (Map.Entry<String, Set<String>> entry : keyedMapNames.entrySet()) {
             String key = entry.getKey();
             Set<String> typeNames = entry.getValue();
             for (String typeName : typeNames) {
                 String repoName = findRepositoryName(typeName, key);
                 try(NitriteMap<NitriteId, Document> nitriteMap
                         = nitriteStore.openMap(repoName, NitriteId.class, Document.class)) {
                     List<IndexDescriptor> indexes = indexDescriptors.stream().filter(d ->
                         repoName.equalsIgnoreCase(d.getCollectionName())).collect(Collectors.toList());
                     writeNitriteMap(nitriteMap, indexes);
                 }
             }
         }
         generator.writeEndArray();
     }

     private void writeNitriteMap(NitriteMap<NitriteId, Document> nitriteMap,
                                  List<IndexDescriptor> indexes) throws IOException {
         generator.writeStartObject();
         generator.writeFieldName(TAG_NAME);
         generator.writeString(nitriteMap.getName());
         writeIndices(indexes);
         writeContent(nitriteMap);
         generator.writeEndObject();
     }

     private void writeIndices(Collection<IndexDescriptor> indices) throws IOException {
         generator.writeFieldName(TAG_INDICES);
         generator.writeStartArray();
         if (options.isExportIndices()) {
             for (IndexDescriptor index : indices) {
                 generator.writeStartObject();
                 generator.writeFieldName(TAG_INDEX);
                 generator.writeObject(writeEncodedObject(index));
                 generator.writeEndObject();
             }
         }
         generator.writeEndArray();
     }

     private void writeContent(NitriteMap<NitriteId, Document> nitriteMap) throws IOException {
         generator.writeFieldName(TAG_DATA);
         generator.writeStartArray();
         if (options.isExportData()) {
             for (Pair<NitriteId, Document> entry : nitriteMap.entries()) {
                 generator.writeStartObject();
                 generator.writeFieldName(TAG_KEY);
                 generator.writeObject(writeEncodedObject(entry.getFirst()));

                 generator.writeFieldName(TAG_VALUE);
                 generator.writeObject(writeEncodedObject(entry.getSecond()));
                 generator.writeEndObject();
             }
         }
         generator.writeEndArray();
     }

     private String writeEncodedObject(Object object) {
         try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
             try (ObjectOutputStream oos = new ObjectOutputStream(os)) {
                 oos.writeObject(object);
                 byte[] data = os.toByteArray();
                 return Base64.encodeBase64URLSafeString(data);
             }
         } catch (IOException e) {
             throw new NitriteIOException("Failed to write object", e);
         }
     }
 }
 