package org.dizitart.no2.tool;

import com.fasterxml.jackson.core.JsonGenerator;
import org.dizitart.no2.*;
import org.dizitart.no2.internals.NitriteMapper;
import org.dizitart.no2.objects.ObjectRepository;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.dizitart.no2.Constants.*;

/**
 * @author Anindya Chatterjee
 */
class NitriteJsonExporter {
    private Nitrite db;
    private JsonGenerator generator;
    private ExportOptions options;
    private NitriteMapper nitriteMapper;

    public NitriteJsonExporter(Nitrite db) {
        this.db = db;
        this.nitriteMapper = db.getContext().getNitriteMapper();
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

        org.dizitart.no2.Cursor cursor = repository.getDocumentCollection().find();
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
