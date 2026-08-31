package org.dizitart.no2.bridge;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.mapper.EntityConverter;
import org.dizitart.no2.common.mapper.NitriteMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** The dataset every test and the reference bridge browse, so all of them look at the same rows. */
public final class Fixtures {

    /** Rows in the largest store — enough that paging and the page clamp mean something. */
    public static final int USER_COUNT = 250;

    private Fixtures() {}

    /** An in-memory database. The store matrix covers MVStore and RocksDB. */
    public static Nitrite memoryDb() {
        return Nitrite.builder().registerEntityConverter(new Note.NoteConverter()).openOrCreate();
    }

    /**
     * Fills a database with the same three collections the Dart and JVM reference bridges use, so
     * a conformance run against this bridge is comparable to theirs row for row.
     */
    public static Nitrite fill(Nitrite db) {
        // One blob larger than the 64 KB inline ceiling, on the first row, so the suite has a
        // truncated value in the first page to check rather than skipping that shape.
        Random random = new Random(7);
        byte[] avatar = new byte[100 * 1024];
        random.nextBytes(avatar);

        NitriteCollection users = db.getCollection("users");
        List<Document> batch = new ArrayList<>();
        for (int i = 0; i < USER_COUNT; i++) {
            Document row =
                    Document.createDocument()
                            .put("id", i)
                            .put("name", i == 3 ? "user with a ünicode name" : "user " + i)
                            // Absent rather than null on the odd rows: a document store has no
                            // declared nullability, and a missing field is what the schema sample
                            // is actually reporting on.
                            .put("score", i / 3.0);
            if (i % 2 == 0) {
                row.put("age", 20 + (i % 50));
            }
            if (i == 0) {
                row.put("avatar", avatar);
            }
            batch.add(row);
        }
        users.insert(batch.toArray(new Document[0]));

        // A name that needs quoting in a SQL bridge, kept so the fixture stays parallel to the
        // sqflite one.
        NitriteCollection orders = db.getCollection("order details");
        for (int i = 0; i < 12; i++) {
            orders.insert(Document.createDocument().put("id", i).put("qty", i));
        }

        // Opened and left empty: getSchema on a store with no rows is its own shape.
        db.getCollection("empty_collection");
        return db;
    }

    /**
     * A repository entity with a nested document in it, which is what makes the adapter's own
     * encoding step worth having.
     *
     * <p>Every field is nullable on purpose: a repository is opened by round-tripping an
     * <i>empty</i> instance through the converter to validate the type, so a converter that
     * dereferences a field there fails at {@code getRepository} with a confusing mapping error.
     */
    public static class Note {
        private String title;
        private Integer words;
        private Document author;

        public Note() {}

        public Note(String title, Integer words, String authorName) {
            this.title = title;
            this.words = words;
            this.author = Document.createDocument().put("name", authorName);
        }

        public static class NoteConverter implements EntityConverter<Note> {
            @Override
            public Class<Note> getEntityType() {
                return Note.class;
            }

            @Override
            public Document toDocument(Note entity, NitriteMapper mapper) {
                return Document.createDocument()
                        .put("title", entity.title)
                        .put("words", entity.words)
                        .put("author", entity.author);
            }

            @Override
            public Note fromDocument(Document document, NitriteMapper mapper) {
                Note note = new Note();
                note.title = document.get("title", String.class);
                note.words = document.get("words", Integer.class);
                note.author = document.get("author", Document.class);
                return note;
            }
        }
    }
}
