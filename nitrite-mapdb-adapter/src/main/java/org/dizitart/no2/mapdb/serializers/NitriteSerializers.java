package org.dizitart.no2.mapdb.serializers;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.meta.Attributes;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.index.IndexEntry;
import org.dizitart.no2.index.IndexMeta;
import org.dizitart.no2.store.UserCredential;
import org.jetbrains.annotations.NotNull;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.serializer.GroupSerializer;
import org.mapdb.serializer.GroupSerializerObjectArray;
import org.mapdb.serializer.SerializerJava;
import org.mapdb.serializer.SerializerString;

import java.io.IOException;
import java.util.Comparator;

/**
 * @author Anindya Chatterjee
 */
public class NitriteSerializers {
    private static class NitriteIdSerializer implements GroupSerializer<NitriteId> {
        private final SerializerString serializer = new SerializerString();

        @Override
        public int valueArraySearch(Object keys, NitriteId key) {
            return serializer.valueArraySearch(keys, key.getIdValue());
        }

        @Override
        public int valueArraySearch(Object keys, NitriteId key, Comparator comparator) {
            return serializer.valueArraySearch(keys, key.getIdValue(), comparator);
        }

        @Override
        public void valueArraySerialize(DataOutput2 out, Object vals) throws IOException {
            serializer.valueArraySerialize(out, vals);
        }

        @Override
        public Object valueArrayDeserialize(DataInput2 in, int size) throws IOException {
            return serializer.valueArrayDeserialize(in, size);
        }

        @Override
        public NitriteId valueArrayGet(Object vals, int pos) {
            return NitriteId.createId(serializer.valueArrayGet(vals, pos));
        }

        @Override
        public int valueArraySize(Object vals) {
            return serializer.valueArraySize(vals);
        }

        @Override
        public Object valueArrayEmpty() {
            return serializer.valueArrayEmpty();
        }

        @Override
        public Object valueArrayPut(Object vals, int pos, NitriteId newValue) {
            return serializer.valueArrayPut(vals, pos, newValue.getIdValue());
        }

        @Override
        public Object valueArrayUpdateVal(Object vals, int pos, NitriteId newValue) {
            return serializer.valueArrayUpdateVal(vals, pos, newValue.getIdValue());
        }

        @Override
        public Object valueArrayFromArray(Object[] objects) {
            return serializer.valueArrayFromArray(objects);
        }

        @Override
        public Object valueArrayCopyOfRange(Object vals, int from, int to) {
            return serializer.valueArrayCopyOfRange(vals, from, to);
        }

        @Override
        public Object valueArrayDeleteValue(Object vals, int pos) {
            return serializer.valueArrayDeleteValue(vals, pos);
        }

        @Override
        public void serialize(@NotNull DataOutput2 out, @NotNull NitriteId value) throws IOException {
            serializer.serialize(out, value.getIdValue());
        }

        @Override
        public NitriteId deserialize(@NotNull DataInput2 input, int available) throws IOException {
            return NitriteId.createId(serializer.deserialize(input, available));
        }
    }

    @SuppressWarnings({"rawtypes"})
    private static class KeyValuePairSerializer extends GroupSerializerObjectArray<Pair> {
        private final SerializerJava serializer = new SerializerJava();

        @Override
        public void serialize(@NotNull DataOutput2 out, @NotNull Pair value) throws IOException {
            serializer.serialize(out, value);
        }

        @Override
        public Pair deserialize(@NotNull DataInput2 input, int available) throws IOException {
            return (Pair) serializer.deserialize(input, available);
        }
    }

    private static class DocumentSerializer extends GroupSerializerObjectArray<Document> {
        private final SerializerJava serializer = new SerializerJava();

        @Override
        public void serialize(@NotNull DataOutput2 out, @NotNull Document value) throws IOException {
            serializer.serialize(out, value);
        }

        @Override
        public Document deserialize(@NotNull DataInput2 input, int available) throws IOException {
            return (Document) serializer.deserialize(input, available);
        }
    }

    private static class IndexMetaSerializer extends GroupSerializerObjectArray<IndexMeta> {
        private final SerializerJava serializer = new SerializerJava();

        @Override
        public void serialize(@NotNull DataOutput2 out, @NotNull IndexMeta value) throws IOException {
            serializer.serialize(out, value);
        }

        @Override
        public IndexMeta deserialize(@NotNull DataInput2 input, int available) throws IOException {
            return (IndexMeta) serializer.deserialize(input, available);
        }
    }

    private static class IndexEntrySerializer extends GroupSerializerObjectArray<IndexEntry> {

        @Override
        public void serialize(@NotNull DataOutput2 output, @NotNull IndexEntry value) throws IOException {
            output.writeUTF(value.getCollectionName());
            output.writeUTF(value.getField());
            output.writeUTF(value.getIndexType());
        }

        @Override
        public IndexEntry deserialize(@NotNull DataInput2 input, int available) throws IOException {
            String collectionName = input.readUTF();
            String field = input.readUTF();
            String indexType = input.readUTF();
            return new IndexEntry(indexType, field, collectionName);
        }
    }

    private static class UserCredentialSerializer extends GroupSerializerObjectArray<UserCredential> {

        @Override
        public void serialize(@NotNull DataOutput2 output, @NotNull UserCredential value) throws IOException {
            output.writeInt(value.getPasswordHash().length);
            output.writeInt(value.getPasswordSalt().length);
            output.write(value.getPasswordHash());
            output.write(value.getPasswordSalt());
        }

        @Override
        public UserCredential deserialize(@NotNull DataInput2 input, int available) throws IOException {
            int hashLength = input.readInt();
            int saltLength = input.readInt();

            byte[] hash = new byte[hashLength];
            input.readFully(hash);

            byte[] salt = new byte[saltLength];
            input.readFully(salt);

            UserCredential credential = new UserCredential();
            credential.setPasswordHash(hash);
            credential.setPasswordSalt(salt);

            return credential;
        }
    }

    private static class AttributesSerializer extends GroupSerializerObjectArray<Attributes> {
        private final SerializerJava serializer = new SerializerJava();


        @Override
        public void serialize(@NotNull DataOutput2 out, @NotNull Attributes value) throws IOException {
            serializer.serialize(out, value);
        }

        @Override
        public Attributes deserialize(@NotNull DataInput2 input, int available) throws IOException {
            return (Attributes) serializer.deserialize(input, available);
        }
    }

    public static void registerAll() {
        Serializers.registerSerializer(NitriteId.class, new NitriteIdSerializer());
        Serializers.registerSerializer(Pair.class, new KeyValuePairSerializer());
        Serializers.registerSerializer(Document.class, new DocumentSerializer());
        Serializers.registerSerializer(IndexMeta.class, new IndexMetaSerializer());
        Serializers.registerSerializer(IndexEntry.class, new IndexEntrySerializer());
        Serializers.registerSerializer(UserCredential.class, new UserCredentialSerializer());
        Serializers.registerSerializer(Attributes.class, new AttributesSerializer());
    }
}
