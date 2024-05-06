package org.dizitart.no2.rocksdb.serializers.fury;

import org.apache.fury.Fury;
import org.apache.fury.memory.MemoryBuffer;
import org.apache.fury.serializer.ObjectSerializer;
import org.apache.fury.serializer.Serializer;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.DBValue;
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.common.meta.Attributes;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.common.util.SpatialKey;
import org.dizitart.no2.index.BoundingBox;
import org.dizitart.no2.index.IndexDescriptor;
import org.dizitart.no2.index.IndexMeta;
import org.dizitart.no2.store.UserCredential;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class RegisterNitriteSerializers {

    public static void now(Fury fury) {
        fury.registerSerializer(Document.class, new DocumentSerializer(fury));
        fury.registerSerializer(Pair.class, new PairSerializer(fury));
        fury.registerSerializer(IndexMeta.class, new IndexMetaSerializer(fury));
        fury.registerSerializer(NitriteId.class, new NitriteIdSerializer(fury));
        fury.registerSerializer(IndexDescriptor.class, new IndexDescriptorSerializer(fury));
        fury.registerSerializer(UserCredential.class, new UserCredentialSerializer(fury));
        fury.registerSerializer(Attributes.class, new AttributesSerializer(fury));
        fury.registerSerializer(Fields.class, new FieldsSerializer(fury));
        fury.registerSerializer(DBValue.class, new DBValueSerializer(fury));
        fury.registerSerializer(BoundingBox.class, new BoundingBoxSerializer(fury));
        fury.registerSerializer(SpatialKey.class, new SpatialKeySerializer(fury));
    }


    private static class DBValueSerializer extends Serializer<DBValue> {
        private DBValueSerializer(Fury fury) {
            super(fury, DBValue.class, true);
        }

        @Override
        public void write(MemoryBuffer memoryBuffer, DBValue object) {
            fury.writeRef(memoryBuffer, object.getValue());
        }

        @Override
        public DBValue read(MemoryBuffer memoryBuffer) {
            Comparable<?> comparableValue =
                    (Comparable<?>) fury.readRef(memoryBuffer);
            return new DBValue(comparableValue);
        }

    }

    private static class SpatialKeySerializer extends Serializer<SpatialKey> {
        private SpatialKeySerializer(Fury fury) {
            super(fury, SpatialKey.class);
        }

        @Override
        public void write(MemoryBuffer memoryBuffer, SpatialKey spatialKey) {
            memoryBuffer.writeInt64(spatialKey.getId());
            float[] minMax = spatialKey.getMinMax();
            memoryBuffer.writeInt32(minMax.length);
            for (float v : minMax) {
                memoryBuffer.writeFloat32(v);
            }
        }

        public SpatialKey read(MemoryBuffer memoryBuffer) {
            long id = memoryBuffer.readInt64();
            int length = memoryBuffer.readInt32();
            float[] minMax = new float[length];
            for (int i = 0; i < length; i++) {
                minMax[i] = memoryBuffer.readFloat32();
            }
            return new SpatialKey(id, minMax);
        }
    }

    private static class BoundingBoxSerializer extends Serializer<BoundingBox> {
        private BoundingBoxSerializer(Fury fury) {
            super(fury, BoundingBox.class);
        }

        @Override
        public void write(MemoryBuffer memoryBuffer, BoundingBox object) {
            memoryBuffer.writeFloat32(object.getMinX());
            memoryBuffer.writeFloat32(object.getMaxX());
            memoryBuffer.writeFloat32(object.getMinY());
            memoryBuffer.writeFloat32(object.getMaxY());
        }

        @Override
        public BoundingBox read(MemoryBuffer memoryBuffer) {
            float minX = memoryBuffer.readFloat32();
            float maxX = memoryBuffer.readFloat32();
            float minY = memoryBuffer.readFloat32();
            float maxY = memoryBuffer.readFloat32();
            return new BoundingBox(minX, maxX, minY, maxY);
        }
    }

    private static class FieldsSerializer extends Serializer<Fields> {
        private FieldsSerializer(Fury fury) {
            super(fury, Fields.class);
        }

        @Override
        public void write(MemoryBuffer memoryBuffer, Fields object) {
            List<String> fieldNames = object.getFieldNames();
            memoryBuffer.writeInt32(fieldNames.size());
            for (String fieldName : fieldNames) {
                fury.writeJavaString(memoryBuffer, fieldName);
            }
        }

        @Override
        public Fields read(MemoryBuffer memoryBuffer) {
            int size = memoryBuffer.readInt32();
            List<String> fieldNames = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                String fieldName = fury.readJavaString(memoryBuffer);
                fieldNames.add(fieldName);
            }
            return Fields.withNames(fieldNames.toArray(new String[0]));
        }
    }

    private static class AttributesSerializer extends Serializer<Attributes> {
        private AttributesSerializer(Fury fury) {
            super(fury, Attributes.class, true);
        }

        public void write(MemoryBuffer memoryBuffer, Attributes object) {
            fury.writeRef(memoryBuffer, object.getAttributes());
        }

        @SuppressWarnings("unchecked")
        public Attributes read(MemoryBuffer memoryBuffer) {
            Map<String, String> map = (Map<String, String>) fury.readRef(memoryBuffer);
            Attributes attributes = new Attributes();
            attributes.setAttributes(map);
            return attributes;
        }
    }

    private static class UserCredentialSerializer extends Serializer<UserCredential> {
        private UserCredentialSerializer(Fury fury) {
            super(fury, UserCredential.class);
        }

        public void write(MemoryBuffer memoryBuffer, UserCredential object) {
            byte[] passwordHash = object.getPasswordHash();
            byte[] passwordSalt = object.getPasswordSalt();
            memoryBuffer.writeInt32(passwordHash.length);
            memoryBuffer.writeInt32(passwordSalt.length);
            memoryBuffer.writeBytes(passwordHash);
            memoryBuffer.writeBytes(passwordSalt);
        }

        public UserCredential read(MemoryBuffer memoryBuffer) {
            int hashLength = memoryBuffer.readInt32();
            int saltLength = memoryBuffer.readInt32();
            byte[] hash = memoryBuffer.readBytes(hashLength);
            byte[] salt = memoryBuffer.readBytes(saltLength);
            UserCredential credential = new UserCredential();
            credential.setPasswordHash(hash);
            credential.setPasswordSalt(salt);
            return credential;
        }
    }

    private static class IndexDescriptorSerializer extends Serializer<IndexDescriptor> {
        private IndexDescriptorSerializer(Fury fury) {
            super(fury, IndexDescriptor.class);
        }

        public void write(MemoryBuffer memoryBuffer, IndexDescriptor object) {
            fury.writeRef(memoryBuffer, object.getFields());
            fury.writeJavaString(memoryBuffer, object.getCollectionName());
            fury.writeJavaString(memoryBuffer, object.getIndexType());
        }

        public IndexDescriptor read(MemoryBuffer memoryBuffer) {
            Fields fields = (Fields) fury.readRef(memoryBuffer);
            String collectionName = fury.readJavaString(memoryBuffer);
            String indexType = fury.readJavaString(memoryBuffer);
            return new IndexDescriptor(indexType, fields, collectionName);
        }
    }

    private static class IndexMetaSerializer extends Serializer<IndexMeta> {
        private IndexMetaSerializer(Fury fury) {
            super(fury, IndexMeta.class);
        }

        public void write(MemoryBuffer memoryBuffer, IndexMeta object) {
            fury.writeRef(memoryBuffer, object.getIndexDescriptor());
            fury.writeJavaString(memoryBuffer, object.getIndexMap());
            memoryBuffer.writeBoolean(object.getIsDirty().get());
        }

        public IndexMeta read(MemoryBuffer memoryBuffer) {
            IndexDescriptor indexDescriptor = (IndexDescriptor) fury.readRef(memoryBuffer);
            String indexMap = fury.readJavaString(memoryBuffer);
            boolean isDirty = memoryBuffer.readBoolean();
            IndexMeta indexMeta = new IndexMeta();
            indexMeta.setIndexDescriptor(indexDescriptor);
            indexMeta.setIndexMap(indexMap);
            indexMeta.setIsDirty(new AtomicBoolean(isDirty));
            return indexMeta;
        }
    }

    private static class DocumentSerializer extends Serializer<Document> {
        private DocumentSerializer(Fury fury) {
            super(fury, Document.class);
        }

        public void write(MemoryBuffer memoryBuffer, Document document) {
            fury.writeRef(memoryBuffer, document);
        }

        public Document read(MemoryBuffer memoryBuffer) {
            Document document = Document.createDocument();
            Map<?, ?> map = (Map<?, ?>) fury.readRef(memoryBuffer);

            for (Map.Entry<?, ?> value : map.entrySet()) {
                document.put((String) value.getKey(), value.getValue());
            }
            return document;
        }
    }

    @SuppressWarnings("unchecked")
    private static class PairSerializer extends Serializer<Pair<?, ?>> {
        private final ObjectSerializer<Pair<?, ?>> javaSerializer;

        private PairSerializer(Fury fury) {
            super(fury, (Class<Pair<?, ?>>) (Class<?>) Pair.class, true);
            this.javaSerializer = new ObjectSerializer<>(fury, (Class<Pair<?, ?>>) (Class<?>) Pair.class);
        }

        @Override
        public void write(MemoryBuffer memoryBuffer, Pair<?, ?> pair) {
            javaSerializer.write(memoryBuffer, pair);
        }

        @Override
        public Pair<?, ?> read(MemoryBuffer memoryBuffer) {
            return javaSerializer.read(memoryBuffer);
        }
    }

    private static class NitriteIdSerializer extends Serializer<NitriteId> {
        private NitriteIdSerializer(Fury fury) {
            super(fury, NitriteId.class);
        }

        @Override
        public void write(MemoryBuffer memoryBuffer, NitriteId object) {
            fury.writeJavaString(memoryBuffer, object.getIdValue());
        }

        public NitriteId read(MemoryBuffer memoryBuffer) {
            String idValue = fury.readJavaString(memoryBuffer);
            return NitriteId.createId(idValue);
        }
    }
}
