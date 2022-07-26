package org.dizitart.no2.rocksdb.formatter;


import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import com.esotericsoftware.kryo.kryo5.serializers.JavaSerializer;
import com.esotericsoftware.kryo.kryo5.serializers.MapSerializer;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.meta.Attributes;
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.common.DBValue;
import org.dizitart.no2.index.IndexDescriptor;
import org.dizitart.no2.index.IndexMeta;
import org.dizitart.no2.store.UserCredential;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Anindya Chatterjee
 */
public class NitriteSerializers {
    private static class NitriteIdSerializer extends ComparableKeySerializer<NitriteId> {

        @Override
        protected void writeKeyInternal(Kryo kryo, Output output, NitriteId object) {
            output.writeString(object.getIdValue());
        }

        @Override
        protected NitriteId readKeyInternal(Kryo kryo, String input, Class<NitriteId> type) {
            return NitriteId.createId(input);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static class PairSerializer extends Serializer<Pair> {

        @Override
        public void write(Kryo kryo, Output output, Pair entry) {
            kryo.writeClassAndObject(output, entry.getFirst());
            kryo.writeClassAndObject(output, entry.getSecond());
        }

        @Override
        public Pair read(Kryo kryo, Input input, Class<? extends Pair> type) {
            Pair pair = new Pair<>();
            kryo.reference(pair);

            Object key = kryo.readClassAndObject(input);
            pair.setFirst(key);

            Object value = kryo.readClassAndObject(input);
            pair.setSecond(value);
            return new Pair<>(key, value);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static class DocumentSerializer extends Serializer<Document> {
        private final MapSerializer mapSerializer = new MapSerializer() {
            @Override
            protected Map create(Kryo kryo, Input input, Class type, int size) {
                return (Map) Document.createDocument();
            }
        };

        @Override
        public void write(Kryo kryo, Output output, Document document) {
            mapSerializer.write(kryo, output, (Map) document);
        }

        @Override
        public Document read(Kryo kryo, Input input, Class<? extends Document> type) {
            Document document = Document.createDocument();
            Map<?, ?> map = mapSerializer.read(kryo, input, Map.class);
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                document.put((String) entry.getKey(), entry.getValue());
            }

            return document;
        }
    }

    private static class IndexMetaSerializer extends Serializer<IndexMeta> {

        @Override
        public void write(Kryo kryo, Output output, IndexMeta object) {
            kryo.writeObject(output, object.getIndexDescriptor());
            output.writeString(object.getIndexMap());
            output.writeBoolean(object.getIsDirty().get());
        }

        @Override
        public IndexMeta read(Kryo kryo, Input input, Class<? extends IndexMeta> type) {
            IndexDescriptor indexDescriptor = kryo.readObject(input, IndexDescriptor.class);
            String indexMap = input.readString();
            boolean isDirty = input.readBoolean();
            IndexMeta indexMeta = new IndexMeta();
            indexMeta.setIndexDescriptor(indexDescriptor);
            indexMeta.setIndexMap(indexMap);
            indexMeta.setIsDirty(new AtomicBoolean(isDirty));
            return indexMeta;
        }
    }

    private static class IndexDescriptorSerializer extends Serializer<IndexDescriptor> {

        @Override
        public void write(Kryo kryo, Output output, IndexDescriptor object) {
            kryo.writeObject(output, object.getIndexFields());
            output.writeString(object.getCollectionName());
            output.writeString(object.getIndexType());
        }

        @Override
        public IndexDescriptor read(Kryo kryo, Input input, Class<? extends IndexDescriptor> type) {
            Fields fields = kryo.readObject(input, Fields.class);
            String collectionName = input.readString();
            String indexType = input.readString();
            return new IndexDescriptor(indexType, fields, collectionName);
        }
    }

    private static class UserCredentialSerializer extends Serializer<UserCredential> {

        @Override
        public void write(Kryo kryo, Output output, UserCredential object) {
            output.writeInt(object.getPasswordHash().length);
            output.writeInt(object.getPasswordSalt().length);
            output.writeBytes(object.getPasswordHash());
            output.writeBytes(object.getPasswordSalt());
        }

        @Override
        public UserCredential read(Kryo kryo, Input input, Class<? extends UserCredential> type) {
            int hashLength = input.readInt();
            int saltLength = input.readInt();

            byte[] hash = input.readBytes(hashLength);
            byte[] salt = input.readBytes(saltLength);

            UserCredential credential = new UserCredential();
            credential.setPasswordHash(hash);
            credential.setPasswordSalt(salt);

            return credential;
        }
    }

    private static class AttributesSerializer extends Serializer<Attributes> {
        @Override
        public void write(Kryo kryo, Output output, Attributes object) {
            kryo.writeObject(output, object.getAttributes(), new MapSerializer<HashMap<String, String>>());
        }

        @Override
        @SuppressWarnings("unchecked")
        public Attributes read(Kryo kryo, Input input, Class<? extends Attributes> type) {
            Map<String, String> map = (Map<String, String>) kryo.readObject(input, HashMap.class, new MapSerializer<HashMap<String, String>>());
            Attributes attributes = new Attributes();
            attributes.setAttributes(map);
            return attributes;
        }
    }

    private static class FieldsSerializer extends Serializer<Fields> {
        @Override
        public void write(Kryo kryo, Output output, Fields object) {
            kryo.writeObject(output, object.getFieldNames());
        }

        @Override
        @SuppressWarnings("unchecked")
        public Fields read(Kryo kryo, Input input, Class<? extends Fields> type) {
            List<String> fieldNames = (List<String>) kryo.readObject(input, ArrayList.class);
            return Fields.withNames(fieldNames.toArray(new String[0]));
        }
    }

    public static void registerAll(KryoObjectFormatter kryoObjectFormatter) {
        kryoObjectFormatter.registerSerializer(NitriteId.class, new NitriteIdSerializer());
        kryoObjectFormatter.registerSerializer(Pair.class, new PairSerializer());
        kryoObjectFormatter.registerSerializer(Document.class, new DocumentSerializer());
        kryoObjectFormatter.registerSerializer(IndexMeta.class, new IndexMetaSerializer());
        kryoObjectFormatter.registerSerializer(IndexDescriptor.class, new IndexDescriptorSerializer());
        kryoObjectFormatter.registerSerializer(UserCredential.class, new UserCredentialSerializer());
        kryoObjectFormatter.registerSerializer(Attributes.class, new AttributesSerializer());
        kryoObjectFormatter.registerSerializer(Fields.class, new FieldsSerializer());
        kryoObjectFormatter.registerSerializer(DBValue.class, new JavaSerializer());
    }
}
