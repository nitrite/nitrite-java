package org.dizitart.no2.bridge;

import org.dizitart.dbinspect.QueryPage;
import org.dizitart.dbinspect.StoreInfo;
import org.dizitart.dbinspect.WriteRequest;
import org.dizitart.dbinspect.WriteResult;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.mvstore.MVStoreModule;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.rocksdb.RocksDBModule;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * "Both MVStore- and RocksDB-backed databases browse correctly" is an M3 acceptance criterion in
 * as many words, and neither is exercised by the in-memory store the other tests use — a
 * persistent store round-trips every value through a serialiser, which is where a blob or a
 * document field stops being the object the adapter thought it had.
 */
@RunWith(Parameterized.class)
public class StoreMatrixTest {

    @Rule public TemporaryFolder folder = new TemporaryFolder();

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> stores() {
        return Arrays.asList(
                new Object[] {"memory", null},
                new Object[] {
                    "mvstore",
                    (Function<File, Nitrite>)
                            file ->
                                    Nitrite.builder()
                                            .loadModule(
                                                    MVStoreModule.withConfig()
                                                            .filePath(file)
                                                            .build())
                                            .registerEntityConverter(
                                                    new Fixtures.Note.NoteConverter())
                                            .openOrCreate()
                },
                new Object[] {
                    "rocksdb",
                    (Function<File, Nitrite>)
                            file ->
                                    Nitrite.builder()
                                            .loadModule(
                                                    RocksDBModule.withConfig()
                                                            .filePath(file)
                                                            .build())
                                            .registerEntityConverter(
                                                    new Fixtures.Note.NoteConverter())
                                            .openOrCreate()
                });
    }

    @Parameterized.Parameter public String engine;

    @Parameterized.Parameter(1)
    public Function<File, Nitrite> open;

    private Nitrite db;

    @After
    public void close() {
        if (db != null && !db.isClosed()) {
            db.close();
        }
    }

    private NitriteAdapter adapter(ObjectRepository<?>... repositories) throws Exception {
        db =
                open == null
                        ? Fixtures.memoryDb()
                        : open.apply(new File(folder.newFolder(), "app.db"));
        Fixtures.fill(db);
        return NitriteAdapter.builder(db, "main", "app.db").repositories(repositories).build();
    }

    @Test
    public void reportsTheEngineTheWireVocabularyNames() throws Exception {
        assertEquals(engine, adapter().engine());
    }

    @Test
    public void browsesEveryStoreItReports() throws Exception {
        NitriteAdapter adapter = adapter();
        for (StoreInfo store : adapter.listStores()) {
            // Both calls a client makes before it can draw anything, on every store — an engine
            // that cannot answer one of them is a store the developer cannot open at all.
            assertTrue(adapter.getSchema(store.name()).inferred());
            QueryPage page = adapter.queryPage(NitriteAdapterTest.page(store.name(), 0, 5));
            assertEquals(store.approxCount(), page.total());
        }
    }

    @Test
    public void aPagePastTheEndIsEmptyRatherThanAnErrorOnEveryEngine() throws Exception {
        // The client pages until it runs out, so this is an ordinary request rather than an edge
        // case. On RocksDB it used to take the whole JVM down with it: the skip loop exhausted the
        // native iterator, which closed it, and the next hasNext() dereferenced the freed handle.
        // It only shows up with assertions off — see the surefire configuration in pom.xml.
        NitriteAdapter adapter = adapter();
        assertTrue(adapter.queryPage(NitriteAdapterTest.page("users", 1_000_000, 5)).rows().isEmpty());
    }

    @Test
    public void survivesTheRoundTripThroughTheStoresSerialiser() throws Exception {
        NitriteAdapter adapter = adapter();
        Map<String, Object> first = adapter.queryPage(NitriteAdapterTest.page("users", 0, 1)).rows().get(0);

        assertEquals("user 0", first.get("name"));
        assertEquals(0.0, ((Number) first.get("score")).doubleValue(), 0.0);
        @SuppressWarnings("unchecked")
        Map<String, Object> blob = (Map<String, Object>) first.get("avatar");
        assertEquals(100 * 1024, ((Number) blob.get("len")).intValue());
        assertEquals(Boolean.TRUE, blob.get("truncated"));
    }

    @Test
    public void filtersAndSortsOnEveryEngine() throws Exception {
        NitriteAdapter adapter = adapter();
        QueryPage filtered =
                adapter.queryPage(
                        NitriteAdapterTest.filtered("users", NitriteAdapterTest.leaf("id", "lt", 10L)));
        assertEquals(Long.valueOf(10), filtered.total());

        List<Map<String, Object>> descending =
                adapter.queryPage(NitriteAdapterTest.sorted("users", "id", true)).rows();
        assertEquals(249, ((Number) descending.get(0).get("id")).intValue());
    }

    @Test
    public void watchesAPersistentStore() throws Exception {
        NitriteAdapter adapter = adapter();
        CountDownLatch inserted = new CountDownLatch(1);
        Runnable unwatch = adapter.watch("users", event -> inserted.countDown());
        db.getCollection("users").insert(Document.createDocument().put("id", 9001));
        assertTrue(engine + " reported no change", inserted.await(5, TimeUnit.SECONDS));
        unwatch.run();
    }

    @Test
    public void writesRoundTripByDocumentIdOnEveryEngine() throws Exception {
        // The write path deserves the store matrix for the same reason browsing does: an id is a
        // key in a persistent store, and MVStore and RocksDB order and serialise one differently.
        db =
                open == null
                        ? Fixtures.memoryDb()
                        : open.apply(new File(folder.newFolder(), "app.db"));
        Fixtures.fill(db);
        NitriteAdapter adapter =
                NitriteAdapter.builder(db, "main", "app.db").allowWrite(true).build();

        Map<String, Object> insert = new LinkedHashMap<>();
        insert.put("store", "users");
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("name", "ada");
        insert.put("values", values);
        WriteResult inserted =
                adapter.write(
                        WriteRequest.fromParams(
                                insert, adapter.capabilities(), WriteRequest.Op.INSERT));
        assertEquals(1, inserted.changes());

        Map<String, Object> delete = new LinkedHashMap<>();
        delete.put("store", "users");
        delete.put("rowId", inserted.id());
        assertEquals(
                engine + " could not address the row it had just assigned an id to",
                1,
                adapter.write(
                                WriteRequest.fromParams(
                                        delete, adapter.capabilities(), WriteRequest.Op.DELETE))
                        .changes());
    }

    @Test
    public void browsesARepositoryWithANestedDocumentInIt() throws Exception {
        db =
                open == null
                        ? Fixtures.memoryDb()
                        : open.apply(new File(folder.newFolder(), "app.db"));
        ObjectRepository<Fixtures.Note> notes = db.getRepository(Fixtures.Note.class);
        notes.insert(new Fixtures.Note("first", 12, "ada"));
        NitriteAdapter adapter =
                NitriteAdapter.builder(db, "main", "app.db").repositories(notes).build();

        String store = Fixtures.Note.class.getName();
        assertEquals("document", NitriteAdapterTest.columnType(adapter.getSchema(store), "author"));
        @SuppressWarnings("unchecked")
        Map<String, Object> author =
                (Map<String, Object>)
                        adapter.queryPage(NitriteAdapterTest.page(store, 0, 1))
                                .rows()
                                .get(0)
                                .get("author");
        // The nested document is a JSON object rather than the toString of a Document.
        assertEquals("ada", author.get("name"));
    }
}
