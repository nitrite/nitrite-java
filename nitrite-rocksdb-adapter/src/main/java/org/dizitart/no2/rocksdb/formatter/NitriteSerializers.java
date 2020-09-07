package org.dizitart.no2.rocksdb.formatter;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.MapSerializer;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.meta.Attributes;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.index.IndexEntry;
import org.dizitart.no2.index.IndexMeta;
import org.dizitart.no2.store.UserCredential;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
        public Pair read(Kryo kryo, Input input, Class<Pair> type) {
            Pair pair = new Pair<>();
            kryo.reference(pair);

            Object key = kryo.readClassAndObject(input);
            pair.setFirst(key);

            Object value = kryo.readClassAndObject(input);
            pair.setSecond(value);
            return new Pair<>(key, value);
        }
    }

    @SuppressWarnings("rawtypes")
    private static class DocumentSerializer extends Serializer<Document> {
        private final MapSerializer mapSerializer = new MapSerializer() {
            @Override
            protected Map create(Kryo kryo, Input input, Class<Map> type) {
                return (Map) Document.createDocument();
            }
        };

        @Override
        public void write(Kryo kryo, Output output, Document document) {
            mapSerializer.write(kryo, output, (Map) document);
        }

        @Override
        public Document read(Kryo kryo, Input input, Class<Document> type) {
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
            kryo.writeObject(output, object.getIndexEntry());
            output.writeString(object.getIndexMap());
            output.writeBoolean(object.getIsDirty().get());
        }

        @Override
        public IndexMeta read(Kryo kryo, Input input, Class<IndexMeta> type) {
            IndexEntry indexEntry = kryo.readObject(input, IndexEntry.class);
            String indexMap = input.readString();
            boolean isDirty = input.readBoolean();
            IndexMeta indexMeta = new IndexMeta();
            indexMeta.setIndexEntry(indexEntry);
            indexMeta.setIndexMap(indexMap);
            indexMeta.setIsDirty(new AtomicBoolean(isDirty));
            return indexMeta;
        }
    }

    private static class IndexEntrySerializer extends Serializer<IndexEntry> {

        @Override
        public void write(Kryo kryo, Output output, IndexEntry object) {
            output.writeString(object.getCollectionName());
            output.writeString(object.getField());
            output.writeString(object.getIndexType());
        }

        @Override
        public IndexEntry read(Kryo kryo, Input input, Class<IndexEntry> type) {
            String collectionName = input.readString();
            String field = input.readString();
            String indexType = input.readString();
            return new IndexEntry(indexType, field, collectionName);
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
        public UserCredential read(Kryo kryo, Input input, Class<UserCredential> type) {
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
            kryo.writeObject(output, object.getAttributes());
        }

        @Override
        @SuppressWarnings("unchecked")
        public Attributes read(Kryo kryo, Input input, Class<Attributes> type) {
            Map<String, String> map = (Map<String, String>) kryo.readObject(input, ConcurrentHashMap.class);
            Attributes attributes = new Attributes();
            attributes.setAttributes(map);
            return attributes;
        }
    }

    public static void registerAll(KryoObjectFormatter kryoObjectFormatter) {
        kryoObjectFormatter.registerSerializer(NitriteId.class, new NitriteIdSerializer());
        kryoObjectFormatter.registerSerializer(Pair.class, new PairSerializer());
        kryoObjectFormatter.registerSerializer(Document.class, new DocumentSerializer());
        kryoObjectFormatter.registerSerializer(IndexMeta.class, new IndexMetaSerializer());
        kryoObjectFormatter.registerSerializer(IndexEntry.class, new IndexEntrySerializer());
        kryoObjectFormatter.registerSerializer(UserCredential.class, new UserCredentialSerializer());
        kryoObjectFormatter.registerSerializer(Attributes.class, new AttributesSerializer());
    }
}
