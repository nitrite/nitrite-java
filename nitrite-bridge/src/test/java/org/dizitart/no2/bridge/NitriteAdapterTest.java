package org.dizitart.no2.bridge;

import org.dizitart.dbinspect.BridgeErrorKind;
import org.dizitart.dbinspect.BridgeException;
import org.dizitart.dbinspect.PageRequest;
import org.dizitart.dbinspect.QueryPage;
import org.dizitart.dbinspect.StoreInfo;
import org.dizitart.dbinspect.StoreSchema;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.repository.ObjectRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class NitriteAdapterTest {

    private Nitrite db;
    private ObjectRepository<Fixtures.Note> notes;
    private NitriteAdapter adapter;

    @Before
    public void open() {
        db = Fixtures.fill(Fixtures.memoryDb());
        notes = db.getRepository(Fixtures.Note.class);
        notes.insert(new Fixtures.Note("first", 12, "ada"));
        adapter = NitriteAdapter.builder(db, "main", "app.db").repositories(notes).build();
    }

    @After
    public void close() {
        db.close();
    }

    // --- identity and capabilities -----------------------------------------------------------

    @Test
    public void describesItselfWithTheEngineTheStoreReports() {
        assertEquals("nitrite", adapter.kind());
        assertEquals("main", adapter.id());
        assertEquals("app.db", adapter.displayName());
        // The wire vocabulary's name for InMemory/<version>.
        assertEquals("memory", adapter.engine());
    }

    @Test
    public void everythingDangerousIsOffUnlessTheDeveloperOptedIn() {
        assertFalse("write must be off by default", adapter.capabilities().edit());
        assertFalse("snapshot must be off by default", adapter.capabilities().snapshot());
        assertFalse("regex must be off by default", adapter.capabilities().filterOps().contains("regex"));
        assertTrue(adapter.capabilities().watch());
    }

    @Test
    public void regexIsAdvertisedOnlyWhenItIsAllowed() {
        NitriteAdapter permissive =
                NitriteAdapter.builder(db, "main", "app.db").allowRegex(true).build();
        assertTrue(permissive.capabilities().filterOps().contains("regex"));
    }

    @Test
    public void existsIsAdvertisedNowThatNitriteHasTheFilter() {
        // capabilities.filterOps is authoritative, and it said "no" for as long as nitrite-java had
        // no presence filter. 5.0.0 has one, so the client's operator is no longer greyed out.
        assertTrue(adapter.capabilities().filterOps().contains("exists"));
    }

    // --- listStores ---------------------------------------------------------------------------

    @Test
    public void listsCollectionsAndTheRepositoriesItWasHandedWithTheirCounts() {
        Map<String, StoreInfo> stores = byName(adapter.listStores());

        assertEquals("collection", stores.get("users").kind());
        assertEquals(Long.valueOf(Fixtures.USER_COUNT), stores.get("users").approxCount());
        assertEquals(Long.valueOf(12), stores.get("order details").approxCount());
        assertEquals(Long.valueOf(0), stores.get("empty_collection").approxCount());

        StoreInfo repository = stores.get(Fixtures.Note.class.getName());
        assertNotNull("the handed-in repository must be listed", repository);
        assertEquals("repository", repository.kind());
        assertEquals(Long.valueOf(1), repository.approxCount());
        assertNull("an unkeyed repository has no key", repository.key());
    }

    @Test
    public void aKeyedRepositoryIsNamedByEntityAndKeyAndReportsTheKeyBeside() {
        ObjectRepository<Fixtures.Note> keyed = db.getRepository(Fixtures.Note.class, "2026");
        keyed.insert(new Fixtures.Note("kept", 1, "ada"));
        NitriteAdapter keyedAdapter =
                NitriteAdapter.builder(db, "main", "app.db").repositories(keyed).build();

        StoreInfo store = byName(keyedAdapter.listStores()).get(Fixtures.Note.class.getName() + "+2026");
        assertNotNull("a keyed repository is addressed by entityName+key", store);
        assertEquals("2026", store.key());
    }

    @Test
    public void aRepositoryThatWasNotHandedInIsNotListed() {
        db.getRepository(Fixtures.Note.class, "hidden").insert(new Fixtures.Note("x", 1, "ada"));
        assertFalse(
                byName(adapter.listStores()).containsKey(Fixtures.Note.class.getName() + "+hidden"));
    }

    // --- getSchema ----------------------------------------------------------------------------

    @Test
    public void infersTheSchemaFromASampleAndNeverClaimsItIsAGuarantee() {
        StoreSchema schema = adapter.getSchema("users");
        assertTrue("a sampled schema is always inferred", schema.inferred());

        Map<String, StoreSchema.Column> columns = columns(schema);
        assertEquals("id", columns.get("_id").type());
        assertEquals("int", columns.get("id").type());
        assertEquals("text", columns.get("name").type());
        assertEquals("real", columns.get("score").type());
        assertEquals("blob", columns.get("avatar").type());
    }

    @Test
    public void aFieldMissingFromAnySampledDocumentIsNullable() {
        Map<String, StoreSchema.Column> columns = columns(adapter.getSchema("users"));
        // Present on every row.
        assertFalse(columns.get("name").nullable());
        // Only on the even rows.
        assertTrue(columns.get("age").nullable());
    }

    @Test
    public void theSampleIsBoundedAndTheCountIsReported() {
        // 250 rows in the store, five read: getSchema must never walk a large store.
        StoreSchema schema =
                NitriteAdapter.builder(db, "main", "app.db").sampleSize(5).build().getSchema("users");
        assertEquals(Integer.valueOf(5), schema.sampledDocs());
        assertTrue(schema.inferred());
        // The blob is on row 0 and the age gap starts at row 1, so five rows is enough to see
        // both — this is a bound on the read, not on what the sample can say.
        assertEquals(
                "the default sample is the protocol's 50",
                Integer.valueOf(NitriteAdapter.DEFAULT_SAMPLE_SIZE),
                adapter.getSchema("users").sampledDocs());
    }

    @Test
    public void aNestedDocumentIsReportedAsADocumentRatherThanAList() {
        // Document is itself an Iterable of its fields, so the order of those two checks matters.
        assertEquals(
                "document",
                columns(adapter.getSchema(Fixtures.Note.class.getName())).get("author").type());
    }

    @Test
    public void anUnknownStoreIsRefusedRatherThanCreated() {
        try {
            adapter.getSchema("__no_store__");
            fail("expected a badRequest");
        } catch (BridgeException refused) {
            assertEquals(BridgeErrorKind.BAD_REQUEST, refused.kind());
        }
        // The point of the allow-list: getCollection creates, so an unchecked name would litter
        // the developer's database with empty collections.
        assertFalse(db.listCollectionNames().contains("__no_store__"));
    }

    // --- queryPage ----------------------------------------------------------------------------

    @Test
    public void pagesInOrderWithATotalAndAnHonestHasMore() {
        QueryPage first = adapter.queryPage(page("users", 0, 5));
        assertEquals(5, first.rows().size());
        assertEquals(0, first.rows().get(0).get("id"));
        assertEquals(4, first.rows().get(4).get("id"));

        QueryPage last = adapter.queryPage(page("users", 49, 5));
        assertEquals(245, last.rows().get(0).get("id"));
        assertFalse(last.hasMore());
    }

    @Test
    public void aPagePastTheEndIsEmptyRatherThanAnError() {
        assertTrue(adapter.queryPage(page("users", 1_000_000, 5)).rows().isEmpty());
    }

    @Test
    public void sortsByAColumnItReportedInBothDirections() {
        assertEquals(249, adapter.queryPage(sorted("users", "id", true)).rows().get(0).get("id"));
        assertEquals(0, adapter.queryPage(sorted("users", "id", false)).rows().get(0).get("id"));
    }

    @Test
    public void refusesASortByAColumnTheSchemaDidNotReport() {
        // Nitrite would happily sort by a field no document has: every value is null and the order
        // is arbitrary, which is showing rows in an order the client did not ask for.
        try {
            adapter.queryPage(sorted("users", "__no_column__", false));
            fail("expected a badRequest");
        } catch (BridgeException refused) {
            assertEquals(BridgeErrorKind.BAD_REQUEST, refused.kind());
        }
    }

    @Test
    public void aBlobIsTruncatedWithItsRealLengthKept() {
        @SuppressWarnings("unchecked")
        Map<String, Object> blob =
                (Map<String, Object>) adapter.queryPage(page("users", 0, 1)).rows().get(0).get("avatar");
        assertEquals(100 * 1024, ((Number) blob.get("len")).intValue());
        assertEquals(Boolean.TRUE, blob.get("truncated"));
    }

    @Test
    public void aFilterNarrowsTheTotalAsWellAsThePage() {
        QueryPage page = adapter.queryPage(filtered("users", leaf("id", "lt", 10L)));
        assertEquals(10, page.rows().size());
        assertEquals(Long.valueOf(10), page.total());
    }

    // --- watch --------------------------------------------------------------------------------

    @Test
    public void watchReportsTheProtocolsEventNamesAndStopsWhenUnsubscribed() throws Exception {
        List<String> events = new CopyOnWriteArrayList<>();
        CountDownLatch inserted = new CountDownLatch(1);
        Runnable unwatch =
                adapter.watch(
                        "users",
                        event -> {
                            events.add(event);
                            inserted.countDown();
                        });

        db.getCollection("users").insert(Document.createDocument().put("id", 9001));
        assertTrue("no change notification arrived", inserted.await(5, TimeUnit.SECONDS));
        // Nitrite's enum constants are capitalised; the wire names are not.
        assertEquals(Arrays.asList("insert"), events);

        unwatch.run();
        db.getCollection("users").insert(Document.createDocument().put("id", 9002));
        Thread.sleep(200);
        assertEquals("unwatch must leave no listener of ours behind", 1, events.size());
    }

    @Test
    public void watchOnAnUnknownStoreIsRefusedRatherThanCreatingIt() {
        try {
            adapter.watch("__no_store__", event -> {});
            fail("expected a badRequest");
        } catch (BridgeException refused) {
            assertEquals(BridgeErrorKind.BAD_REQUEST, refused.kind());
        }
    }

    // --- helpers ------------------------------------------------------------------------------

    private static Map<String, StoreInfo> byName(List<StoreInfo> stores) {
        Map<String, StoreInfo> map = new LinkedHashMap<>();
        for (StoreInfo store : stores) {
            map.put(store.name(), store);
        }
        return map;
    }

    static String columnType(StoreSchema schema, String name) {
        return columns(schema).get(name).type();
    }

    private static Map<String, StoreSchema.Column> columns(StoreSchema schema) {
        Map<String, StoreSchema.Column> map = new LinkedHashMap<>();
        for (StoreSchema.Column column : schema.columns()) {
            map.put(column.name(), column);
        }
        return map;
    }

    static PageRequest page(String store, long page, int pageSize) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("store", store);
        params.put("page", page);
        params.put("pageSize", (long) pageSize);
        return PageRequest.fromParams(params);
    }

    static PageRequest sorted(String store, String sortBy, boolean desc) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("store", store);
        params.put("pageSize", 5L);
        params.put("sortBy", sortBy);
        params.put("desc", desc);
        return PageRequest.fromParams(params);
    }

    static PageRequest filtered(String store, Map<String, Object> filter) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("store", store);
        params.put("pageSize", 200L);
        params.put("filter", filter);
        return PageRequest.fromParams(params);
    }

    static Map<String, Object> leaf(String field, String op, Object value) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("field", field);
        node.put("op", op);
        node.put("value", value);
        return node;
    }

    @SafeVarargs
    static Map<String, Object> tree(String combinator, Map<String, Object>... children) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put(combinator, new ArrayList<>(Arrays.asList(children)));
        return node;
    }
}
