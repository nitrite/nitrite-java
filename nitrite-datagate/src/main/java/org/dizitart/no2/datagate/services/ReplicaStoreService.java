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

package org.dizitart.no2.datagate.services;

import com.mongodb.CommandResult;
import org.dizitart.no2.Document;
import org.dizitart.no2.collection.FindOptions;
import org.dizitart.no2.datagate.models.NitriteDocument;
import org.dizitart.no2.meta.Attributes;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.dizitart.no2.common.Constants.COLLECTION;
import static org.dizitart.no2.common.Constants.DOC_SOURCE;
import static org.dizitart.no2.datagate.Constants.ATTRIBUTE_REPO;
import static org.dizitart.no2.datagate.Constants.STORAGE_VENDOR;
import static org.dizitart.no2.datagate.models.NitriteDocument.*;

/**
 * Storage service for replication. Current storage
 * container implementation is based on mongo database.
 *
 * @since 1.0
 * @author Anindya Chatterjee
 */
@Service
public class ReplicaStoreService {
    private Jongo jongo;
    private MongoCollection attributeRepository;

    @Autowired
    public ReplicaStoreService(Jongo jongo) {
        this.jongo = jongo;
        this.attributeRepository = jongo.getCollection(ATTRIBUTE_REPO);
        this.attributeRepository.ensureIndex("{" + COLLECTION + ": 1 }", "{ unique: true }");
    }

    public String getStoreVendor() {
        return STORAGE_VENDOR;
    }

    public String getStoreVersion() {
        CommandResult buildInfo = jongo.getDatabase().command("buildInfo");
        return buildInfo.getString("version");
    }

    public List<Document> removedItems(String collection, long fromSequence, long newSequence) {
        List<Document> documentList = new ArrayList<>();
        MongoCollection userCollection = jongo.getCollection(collection);

        MongoCursor<NitriteDocument> removeLogs = userCollection
            .find("{" +
                " $and: [" +
                " {" + DELETED + ": { $eq: true }}," +
                " {" + DELETE_TIME + ": { $gte: # }}," +
                " {" + DELETE_TIME + ": { $lte: # }}" +
                " ]}", fromSequence, newSequence)
            .as(NitriteDocument.class);

        if (removeLogs != null) {
            for (NitriteDocument logEntry : removeLogs) {
                Document document = new Document(logEntry.getDocument());
                document.remove(DOC_SOURCE);
                documentList.add(document);
            }
        }

        return documentList;
    }

    public List<Document> modifiedItems(String collection, long fromSequence, long newSequence) {
        MongoCollection userCollection = jongo.getCollection(collection);
        MongoCursor<NitriteDocument> documents = userCollection
            .find("{" +
                " $and: [" +
                " {" + DELETED + ": { $eq: false }}," +
                " {" + SYNC_TIME + ": { $gte: # }}," +
                " {" + SYNC_TIME + ": { $lte: # }}" +
                " ]}", fromSequence, newSequence)
            .as(NitriteDocument.class);

        List<Document> result = new ArrayList<>();
        for (NitriteDocument nitriteDocument : documents) {
            Document doc = new Document(nitriteDocument.getDocument());
            doc.remove(DOC_SOURCE);
            result.add(doc);
        }

        return result;
    }

    public void remove(String collection, List<Document> removedDocuments) {
        long time = System.currentTimeMillis();
        MongoCollection mongoCollection = jongo.getCollection(collection);
        for (Document document : removedDocuments) {
            NitriteDocument nitriteDocument = mongoCollection
                .findOne("{ _id: #}", document.getId().getIdValue())
                .as(NitriteDocument.class);

            if (nitriteDocument != null && !nitriteDocument.isDeleted()) {
                nitriteDocument.setDeleted(true);
                nitriteDocument.setDeleteTime(time);
                nitriteDocument.setSyncTime(time);
                mongoCollection.save(nitriteDocument);
            }
        }
    }

    public void modify(String collection, List<Document> modifiedDocuments) {
        long time = System.currentTimeMillis();
        MongoCollection mongoCollection = jongo.getCollection(collection);

        for (Document document : modifiedDocuments) {
            NitriteDocument nitriteDocument = mongoCollection
                .findOne("{ _id: #}", document.getId().getIdValue())
                .as(NitriteDocument.class);

            if (nitriteDocument != null) {
                if (nitriteDocument.isDeleted()) {
                    continue;
                }
                // update operation
                Document existing = nitriteDocument.getDocument();
                existing.putAll(document);

                nitriteDocument.setSyncTime(time);
                nitriteDocument.setDocument(existing);

                mongoCollection.save(nitriteDocument);
            } else {
                // insert operation
                nitriteDocument = new NitriteDocument();
                if (document.getId() != null
                    && document.getId().getIdValue() != null) {
                    nitriteDocument.setId(document.getId().getIdValue());
                }
                nitriteDocument.setSyncTime(time);
                nitriteDocument.setDeleted(false);
                nitriteDocument.setDocument(document);

                mongoCollection.save(nitriteDocument);
            }
        }
    }

    public List<Document> findAll(String collection, FindOptions findOptions) {
        MongoCollection mongoCollection = jongo.getCollection(collection);
        MongoCursor<NitriteDocument> documents = mongoCollection
            .find("{" + DELETED + ": { $eq: false }}")
            .skip(findOptions.getOffset())
            .limit(findOptions.getSize())
            .as(NitriteDocument.class);

        return StreamSupport.stream(documents.spliterator(), false)
            .map(NitriteDocument::getDocument)
            .collect(Collectors.toList());
    }

    public long size(String collection) {
        MongoCollection mongoCollection = jongo.getCollection(collection);
        return mongoCollection
                .find("{" + DELETED + ": { $eq: false }}")
                .as(NitriteDocument.class)
                .count();
    }

    public void clear(String collection) {
        MongoCollection mongoCollection = jongo.getCollection(collection);
        mongoCollection.remove();
    }

    public Attributes getAttributes(String collection) {
        return attributeRepository.findOne("{" + COLLECTION + ": # }", collection)
            .as(Attributes.class);
    }

    public void setAttributes(String collection, Attributes attributes) {
        attributes.setCollection(collection);
        String query = "{" + COLLECTION + ": { $eq: # }}";
        if (attributeRepository.find(query, collection).as(Attributes.class).count() == 1) {
            attributeRepository.update(query, collection).with(attributes);
        } else {
            attributeRepository.save(attributes);
        }
    }
}
