package org.dizitart.no2.bridge;

import org.dizitart.dbinspect.AdapterTransaction;
import org.dizitart.dbinspect.BridgeErrorKind;
import org.dizitart.dbinspect.BridgeException;
import org.dizitart.dbinspect.PageRequest;
import org.dizitart.dbinspect.WriteRequest;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.repository.ObjectRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * {@code docs/PROTOCOL.md} §3.1 against a real Nitrite database.
 *
 * <p>Nitrite's transaction lives above the storage engine, so what is proved here — a rollback
 * really takes the documents back, and a read inside the transaction sees what is staged — holds
 * for MVStore and RocksDB too. {@link StoreMatrixTest} is where the engines themselves are covered.
 */
public class TransactionTest {

    private Nitrite db;
    private ObjectRepository<Fixtures.Note> notes;
    private NitriteAdapter adapter;

    @Before
    public void open() {
        db = Fixtures.fill(Fixtures.memoryDb());
        notes = db.getRepository(Fixtures.Note.class);
        notes.insert(new Fixtures.Note("first", 12, "ada"));
        adapter =
                NitriteAdapter.builder(db, "main", "app.db")
                        .repositories(notes)
                        .allowWrite(true)
                        .build();
    }

    @After
    public void close() {
        db.close();
    }

    private static Map<String, Object> params(Object... pairs) {
        Map<String, Object> params = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            params.put((String) pairs[i], pairs[i + 1]);
        }
        return params;
    }

    private WriteRequest insert(String store, String name) {
        return WriteRequest.fromParams(
                params("store", store, "values", params("name", name)),
                adapter.capabilities(),
                WriteRequest.Op.INSERT);
    }

    private WriteRequest delete(String store, Object rowId) {
        return WriteRequest.fromParams(
                params("store", store, "rowId", rowId),
                adapter.capabilities(),
                WriteRequest.Op.DELETE);
    }

    private static PageRequest page(String store) {
        return PageRequest.fromParams(params("store", store));
    }

    private long count(String store) {
        return db.getCollection(store).size();
    }

    // --- capability --------------------------------------------------------

    @Test
    public void aWritableAdapterReportsTransactions() {
        assertTrue(adapter.capabilities().transactions());
    }

    @Test
    public void aReadOnlyAdapterDoesNot() {
        // `allowWrite` is the permission; `transactions` reports what the engine can undo. Without
        // the first there is nothing to undo.
        assertFalse(
                NitriteAdapter.builder(db, "ro", "app.db").build().capabilities().transactions());
    }

    @Test
    public void theTransactionalTwinDoesNotOfferToNestAnother() {
        AdapterTransaction tx = adapter.beginTransaction();
        try {
            assertFalse(tx.adapter().capabilities().transactions());
            // Everything else carried over: a gate that changed inside a transaction would be a
            // second, invisible permission model.
            assertTrue(tx.adapter().capabilities().edit());
            assertTrue(tx.adapter().capabilities().watch());
            assertEquals(
                    adapter.capabilities().filterOps(), tx.adapter().capabilities().filterOps());
        } finally {
            tx.rollback();
        }
    }

    // --- rollback and commit -----------------------------------------------

    @Test
    public void aRollbackTakesTheDocumentsBack() {
        long before = count("users");
        AdapterTransaction tx = adapter.beginTransaction();
        tx.adapter().write(insert("users", "ada"));
        tx.adapter().write(insert("users", "grace"));
        tx.rollback();
        assertEquals(before, count("users"));
    }

    @Test
    public void aCommitKeepsThem() {
        long before = count("users");
        AdapterTransaction tx = adapter.beginTransaction();
        tx.adapter().write(insert("users", "ada"));
        tx.commit();
        assertEquals(before + 1, count("users"));
    }

    @Test
    public void aRolledBackDeleteBringsTheDocumentBack() {
        Document first = db.getCollection("users").find().firstOrNull();
        assertNotNull(first);
        Object id = first.getId().getIdValue();
        long before = count("users");

        AdapterTransaction tx = adapter.beginTransaction();
        assertEquals(1, tx.adapter().write(delete("users", id)).changes());
        tx.rollback();

        assertEquals(before, count("users"));
        assertNotNull(db.getCollection("users").getById(first.getId()));
    }

    @Test
    public void aRolledBackUpdateLeavesTheOldValue() {
        Document first = db.getCollection("users").find().firstOrNull();
        assertNotNull(first);
        Object id = first.getId().getIdValue();
        Object name = first.get("name");

        AdapterTransaction tx = adapter.beginTransaction();
        tx.adapter()
                .write(
                        WriteRequest.fromParams(
                                params("store", "users", "rowId", id, "values",
                                        params("name", "changed")),
                                adapter.capabilities(),
                                WriteRequest.Op.UPDATE));
        tx.rollback();

        assertEquals(name, db.getCollection("users").getById(first.getId()).get("name"));
    }

    // --- read-your-own-writes ----------------------------------------------

    @Test
    public void aReadInsideTheTransactionSeesThePendingInsert() {
        long before = count("users");
        AdapterTransaction tx = adapter.beginTransaction();
        try {
            tx.adapter().write(insert("users", "ada"));
            assertEquals(
                    Long.valueOf(before + 1), tx.adapter().queryPage(page("users")).total());
        } finally {
            tx.rollback();
        }
    }

    @Test
    public void listStoresCountsThroughTheTransaction() {
        long before = count("users");
        AdapterTransaction tx = adapter.beginTransaction();
        try {
            tx.adapter().write(insert("users", "ada"));
            long counted =
                    tx.adapter().listStores().stream()
                            .filter(store -> "users".equals(store.name()))
                            .findFirst()
                            .orElseThrow(AssertionError::new)
                            .approxCount();
            assertEquals(before + 1, counted);
        } finally {
            tx.rollback();
        }
    }

    @Test
    public void aRepositoryIsResolvedThroughTheTransactionToo() {
        String store = notes.getDocumentCollection().getName();
        long before = notes.getDocumentCollection().size();

        AdapterTransaction tx = adapter.beginTransaction();
        try {
            tx.adapter().write(insert(store, "note in flight"));
            // Resolved by type through the transaction, not through the repository handle the
            // adapter was constructed with — a write through that one would land outside.
            assertEquals(
                    Long.valueOf(before + 1), tx.adapter().queryPage(page(store)).total());
        } finally {
            tx.rollback();
        }
        assertEquals(before, notes.getDocumentCollection().size());
    }

    // --- failures ----------------------------------------------------------

    @Test
    public void aRefusedWriteLeavesTheTransactionUsable() {
        long before = count("users");
        AdapterTransaction tx = adapter.beginTransaction();
        tx.adapter().write(insert("users", "ada"));
        try {
            tx.adapter().write(insert("no_such_store", "x"));
            fail("expected a refusal");
        } catch (BridgeException refused) {
            assertEquals(BridgeErrorKind.BAD_REQUEST, refused.kind());
        }
        tx.adapter().write(insert("users", "grace"));
        tx.commit();
        assertEquals(before + 2, count("users"));
    }

    @Test
    public void anotherReaderDoesNotSeeUncommittedDocuments() {
        long before = count("users");
        AdapterTransaction tx = adapter.beginTransaction();
        try {
            tx.adapter().write(insert("users", "ada"));
            // The base adapter is what another connection resolves to, and §3.1 says it must not
            // see this connection's uncommitted rows.
            assertEquals(Long.valueOf(before), adapter.queryPage(page("users")).total());
        } finally {
            tx.rollback();
        }
    }
}
