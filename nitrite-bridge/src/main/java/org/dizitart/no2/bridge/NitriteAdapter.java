package org.dizitart.no2.bridge;

import org.dizitart.dbinspect.AdapterCapabilities;
import org.dizitart.dbinspect.AdapterTransaction;
import org.dizitart.dbinspect.BridgeAdapter;
import org.dizitart.dbinspect.BridgeErrorKind;
import org.dizitart.dbinspect.BridgeException;
import org.dizitart.dbinspect.PageRequest;
import org.dizitart.dbinspect.QueryPage;
import org.dizitart.dbinspect.BlobChunk;
import org.dizitart.dbinspect.BlobRequest;
import org.dizitart.dbinspect.StoreInfo;
import org.dizitart.dbinspect.StoreSchema;
import org.dizitart.dbinspect.WriteRequest;
import org.dizitart.dbinspect.WriteResult;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.FindOptions;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.UpdateOptions;
import org.dizitart.no2.common.Constants;
import org.dizitart.no2.common.SortOrder;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.transaction.Session;
import org.dizitart.no2.transaction.Transaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Inspects a running Nitrite database.
 *
 * <p>The bridge core — protocol, pairing, transport, release guard — is
 * {@code org.dizitart:dbinspect-bridge} and knows about no database at all. This artifact is the
 * only part that knows about Nitrite, so inspecting an H2, MapDB or JDBC database through the same
 * core pulls in none of it.
 *
 * <pre>{@code
 * BridgeServer bridge = DbInspect.start(
 *     DbInspect.options("my_app", Collections.singletonList(
 *         NitriteAdapter.builder(db, "main", "app data")
 *             .repositories(orders)
 *             .build())));
 * }</pre>
 *
 * <p><b>Collections are discovered; repositories are handed in.</b> Nitrite opens an
 * {@link ObjectRepository} by Java type, and a name off the wire is not a type — {@code
 * listRepositories()} answers with entity names, and turning one back into a {@link Class} means
 * loading a class named by a paired client, which is a worse thing to have than a shorter store
 * list. Nor can the underlying document collection be reached by name: {@code getCollection} refuses
 * a name a repository already owns, and re-opening the map behind one would produce a second
 * collection object with its own event bus, so {@code watch} would silently never fire. So the
 * developer passes the repositories they want inspected, the same way a Hive adapter is passed its
 * boxes — the same shape the Dart adapter settled on, arrived at from a different constraint. A
 * repository that was not passed in is not listed, rather than listed and unopenable.
 */
public final class NitriteAdapter implements BridgeAdapter {

    /** How many documents {@code getSchema} reads before answering, per PROTOCOL.md §2. */
    public static final int DEFAULT_SAMPLE_SIZE = 50;

    private final Nitrite db;
    private final String id;
    private final String displayName;
    private final List<ObjectRepository<?>> repositories;
    private final int sampleSize;
    private final boolean allowRegex;
    private final AdapterCapabilities capabilities;

    /**
     * The open transaction this adapter is scoped to, or null on the one that is not.
     *
     * <p>See {@link #beginTransaction()}: a transaction is a second adapter over the same database
     * rather than a mode on this one, so that one connection's uncommitted documents can never
     * reach another connection's reads.
     */
    private final Transaction transaction;

    private NitriteAdapter(Builder builder) {
        this.db = builder.db;
        this.id = builder.id;
        this.displayName = builder.displayName;
        this.repositories = Collections.unmodifiableList(new ArrayList<>(builder.repositories));
        this.sampleSize = builder.sampleSize;
        this.allowRegex = builder.allowRegex;
        this.transaction = null;

        List<String> filterOps = new ArrayList<>(FilterDsl.FILTER_OPS);
        if (builder.allowRegex) {
            filterOps.add(FilterDsl.REGEX_OP);
        }
        this.capabilities =
                AdapterCapabilities.builder(AdapterCapabilities.QueryConsole.FILTER)
                        // Nitrite's own collection-level subscription, so a write from anywhere in
                        // this process is seen — not only this bridge's own.
                        .watch(true, AdapterCapabilities.WatchScope.ENGINE)
                        // Off unless the embedding developer turned it on for this adapter
                        // (threat model rule 5).
                        .edit(builder.allowWrite)
                        // Not an opt-in ({@code docs/PROTOCOL.md} §3.1): Nitrite's transaction is
                        // implemented above the store, so it is available on every engine this
                        // adapter can be pointed at — MVStore, RocksDB and in-memory alike.
                        // `allowWrite` is the permission; this reports what the engine can undo.
                        .transactions(builder.allowWrite)
                        .snapshot(builder.allowSnapshot)
                        // Not an opt-in: `queryPage` already showed the first 64 KB of this very
                        // cell, and `docs/PROTOCOL.md` §2 has always promised the rest on request.
                        // A document is addressed by `_id`, which every row has.
                        .blob(true)
                        .filterOps(filterOps)
                        .build();
    }

    /**
     * The transactional twin of {@code parent}, resolving stores through {@code transaction}.
     *
     * <p>Every capability but {@code transactions} is carried over: a gate that changed inside a
     * transaction would be a second, invisible permission model. That one drops, because Nitrite
     * does not nest one.
     */
    private NitriteAdapter(NitriteAdapter parent, Transaction transaction) {
        this.db = parent.db;
        this.id = parent.id;
        this.displayName = parent.displayName;
        this.repositories = parent.repositories;
        this.sampleSize = parent.sampleSize;
        this.allowRegex = parent.allowRegex;
        this.transaction = transaction;

        AdapterCapabilities from = parent.capabilities;
        this.capabilities =
                AdapterCapabilities.builder(from.query())
                        .watch(from.watch(), from.watchScope())
                        .edit(from.edit())
                        .transactions(false)
                        .snapshot(from.snapshot())
                        .blob(from.blob())
                        .filterOps(from.filterOps())
                        .build();
    }

    public static Builder builder(Nitrite db, String id, String displayName) {
        return new Builder(db, id, displayName);
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String kind() {
        return "nitrite";
    }

    @Override
    public String displayName() {
        return displayName;
    }

    /**
     * {@code mvstore}, {@code rocksdb} or {@code memory} — taken from
     * {@code NitriteStore.getStoreVersion()}, which reads {@code MVStore/2.4.240},
     * {@code RocksDB/10.2.1} or {@code InMemory/4.4.3}.
     */
    @Override
    public String engine() {
        String name = db.getStore().getStoreVersion().split("/")[0].toLowerCase(Locale.ROOT);
        return "inmemory".equals(name) ? "memory" : name;
    }

    @Override
    public AdapterCapabilities capabilities() {
        return capabilities;
    }

    /**
     * The stores, counted through {@link #resolve} rather than off {@code db} directly.
     *
     * <p>Which matters inside a transaction: a count taken from the primary collection would show
     * the person a total that does not include the rows they just staged, and §3.1's
     * read-your-own-writes covers {@code listStores} as much as it covers a page.
     */
    @Override
    public List<StoreInfo> listStores() {
        List<StoreInfo> stores = new ArrayList<>();
        for (String name : db.listCollectionNames()) {
            stores.add(new StoreInfo(name, "collection", resolve(name).size()));
        }
        for (ObjectRepository<?> repository : repositories) {
            String name = repository.getDocumentCollection().getName();
            stores.add(new StoreInfo(name, "repository", resolve(name).size(), keyOf(name)));
        }
        return stores;
    }

    @Override
    public StoreSchema getSchema(String store) {
        List<Document> sample = resolve(store).find(FindOptions.limitBy(sampleSize)).toList();

        // Insertion-ordered: the first document's fields come first, and a field only some
        // documents carry lands after them rather than sorted into the middle, which is closer to
        // how the developer thinks about the store.
        Map<String, String> types = new LinkedHashMap<>();
        Map<String, Integer> seen = new LinkedHashMap<>();
        for (Document document : sample) {
            for (Pair<String, Object> entry : document) {
                String field = entry.getFirst();
                seen.merge(field, 1, Integer::sum);
                String type = DocumentValues.typeOf(entry.getSecond());
                if (type != null) {
                    types.putIfAbsent(field, type);
                } else {
                    types.putIfAbsent(field, "unknown");
                }
            }
        }

        List<StoreSchema.Column> columns = new ArrayList<>();
        for (Map.Entry<String, String> field : types.entrySet()) {
            boolean pk = Constants.DOC_ID.equals(field.getKey());
            columns.add(
                    new StoreSchema.Column(
                            field.getKey(),
                            pk ? "id" : field.getValue(),
                            // A document store has no declared nullability, so this is what the
                            // sample showed: a field missing from any sampled document.
                            !pk && seen.get(field.getKey()) != sample.size(),
                            pk));
        }

        // Never false here. A developer must never mistake a sample for a guarantee (§2).
        return new StoreSchema(columns, true, sample.size());
    }

    @Override
    public QueryPage queryPage(PageRequest request) {
        NitriteCollection collection = resolve(request.store());
        Filter filter =
                request.filter() == null
                        ? Filter.ALL
                        : FilterDsl.parse(request.filter(), allowRegex, collection);

        FindOptions options = new FindOptions().skip(request.offset()).limit(request.pageSize());
        if (request.sortBy() != null) {
            // Nitrite will happily sort by a field no document has — every value is null and the
            // order is arbitrary. Showing rows in an order the client did not ask for is the same
            // failure as showing rows it filtered out, so the sort column is checked against the
            // sampled schema: exactly the set of columns the client was given to offer.
            //
            // ponytail: re-samples per sorted page. Cache the schema per store if a sorted page
            // ever misses the budget.
            if (!columnNames(getSchema(request.store())).contains(request.sortBy())) {
                throw new BridgeException(BridgeErrorKind.BAD_REQUEST, "unknown sort column");
            }
            options.thenOrderBy(
                    request.sortBy(),
                    request.desc() ? SortOrder.Descending : SortOrder.Ascending);
        }

        long startedAt = System.nanoTime();
        List<Document> documents = collection.find(filter, options).toList();
        // An unfiltered count is O(1) off the map; a filtered one has to walk the cursor.
        //
        // ponytail: the filtered branch is a second pass. Cache it per store and filter if a large
        // filtered store ever misses the page budget.
        long total =
                request.filter() == null ? collection.size() : collection.find(filter).size();

        List<Map<String, Object>> rows = new ArrayList<>(documents.size());
        for (Document document : documents) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (Pair<String, Object> cell : document) {
                row.put(cell.getFirst(), DocumentValues.encode(cell.getSecond()));
            }
            rows.add(row);
        }

        return new QueryPage(
                rows,
                total,
                request.offset() + rows.size() < total,
                (System.nanoTime() - startedAt) / 1_000_000,
                request.pageSizeClamped());
    }

    /**
     * One row, addressed by {@code _id} — the identity {@code docs/PROTOCOL.md} §3 gives every
     * Nitrite implementation.
     */
    /**
     * Opens one Nitrite transaction ({@code docs/PROTOCOL.md} §3.1).
     *
     * <p>Nitrite's transaction lives above the storage engine — a {@code TransactionStore} buffers
     * the writes and a journal replays them on commit — so this works identically on MVStore,
     * RocksDB and in memory. Nothing here re-implements either half; the session and the
     * transaction are the engine's own.
     *
     * <p><b>Not everything Nitrite does is transactional</b>, and the {@link Transaction}
     * javadoc is the list: creating or dropping an index, clearing a collection and dropping one
     * are auto-committed. The protocol's writes are insert, update and delete, none of which is on
     * that list, so nothing this bridge can be asked to do falls through — but an adapter that grows
     * a sixth method must check that list before it does.
     */
    @Override
    public AdapterTransaction beginTransaction() {
        Session session = db.createSession();
        Transaction started;
        try {
            started = session.beginTransaction();
        } catch (RuntimeException failure) {
            session.close();
            throw new BridgeException(
                    BridgeErrorKind.ADAPTER,
                    "the database would not begin a transaction",
                    failure.toString());
        }

        NitriteAdapter scoped = new NitriteAdapter(this, started);
        return new AdapterTransaction() {
            @Override
            public BridgeAdapter adapter() {
                return scoped;
            }

            @Override
            public void commit() {
                try {
                    started.commit();
                } catch (RuntimeException failure) {
                    throw new BridgeException(
                            BridgeErrorKind.ADAPTER,
                            "the database refused the commit",
                            failure.toString());
                } finally {
                    // Closing a session rolls back anything still open in it, so this is safe on
                    // both paths and is the only thing that releases the transactional store.
                    session.close();
                }
            }

            @Override
            public void rollback() {
                try {
                    started.rollback();
                } finally {
                    session.close();
                }
            }
        };
    }

    @Override
    public WriteResult write(WriteRequest request) {
        NitriteCollection collection = resolve(request.store());

        switch (request.op()) {
            case INSERT:
                org.dizitart.no2.common.WriteResult inserted =
                        collection.insert(documentOf(request.values()));
                Iterator<NitriteId> assigned = inserted.iterator();
                return new WriteResult(
                        inserted.getAffectedCount(),
                        // The value the client addresses the row by afterwards, in the rendering
                        // `_id` already has in a page — which here is the number itself, because
                        // nitrite-java keeps the id in the document as a long.
                        assigned.hasNext() ? assigned.next().getIdValue() : null);
            case UPDATE:
                if (request.values().containsKey(Constants.DOC_ID)) {
                    // Nitrite merges the update document, so an `_id` in it would rewrite the
                    // identity of the row it just matched. The identity is `rowId`, and it is not
                    // editable.
                    throw new BridgeException(
                            BridgeErrorKind.BAD_REQUEST,
                            "_id is not an editable field",
                            "a row is addressed by rowId; the engine owns its identity");
                }
                return new WriteResult(
                        collection
                                .update(
                                        Filter.byId(idOf(request.rowId())),
                                        documentOf(request.values()),
                                        UpdateOptions.updateOptions(false, true))
                                .getAffectedCount());
            default:
                return new WriteResult(
                        collection.remove(Filter.byId(idOf(request.rowId())), true)
                                .getAffectedCount());
        }
    }

    /**
     * One binary cell, whole, rather than the 64 KB {@code queryPage} showed.
     *
     * <p>Read by id rather than by filter: this is the O(1) lookup the engine already has, and the
     * row the client is looking at is one it has an {@code _id} for by definition.
     */
    @Override
    public BlobChunk fetchBlob(BlobRequest request) {
        Document document = resolve(request.store()).getById(idOf(request.rowId()));
        if (document == null) {
            return null;
        }

        Object value = document.get(request.column());
        if (value == null) {
            return null;
        }
        if (!(value instanceof byte[])) {
            // A client asking for the bytes of a field that is not bytes has a stale schema, and a
            // toString() handed back as a file is a fabricated download rather than a helpful one.
            throw new BridgeException(
                    BridgeErrorKind.BAD_REQUEST,
                    "\"" + request.column() + "\" is not a binary field",
                    "it is a " + value.getClass().getSimpleName());
        }
        return BlobChunk.slice((byte[]) value, request);
    }

    /** The document a write carries, with {@code _id} turned back into an identity. */
    private static Document documentOf(Map<String, Object> values) {
        Document document = Document.createDocument();
        for (Map.Entry<String, Object> value : values.entrySet()) {
            document.put(
                    value.getKey(),
                    Constants.DOC_ID.equals(value.getKey())
                            ? idOf(value.getValue())
                            : value.getValue());
        }
        return document;
    }

    /**
     * Accepts an id in the rendering a page carried — {@code [1755…]NO₂}, which is what {@link
     * NitriteId#toString()} produces and therefore what a client echoes back — and the bare number
     * underneath it, which is what a person types.
     */
    private static NitriteId idOf(Object rowId) {
        String text = String.valueOf(rowId);
        int open = text.indexOf(Constants.ID_PREFIX);
        int close = text.indexOf(Constants.ID_SUFFIX);
        String digits = open == 0 && close > open ? text.substring(1, close) : text;
        try {
            return NitriteId.createId(Long.parseLong(digits));
        } catch (NumberFormatException notAnId) {
            throw new BridgeException(
                    BridgeErrorKind.BAD_REQUEST,
                    "rowId is not a Nitrite _id",
                    "an _id is the value the store reported in that column");
        }
    }

    @Override
    public Runnable watch(String store, ChangeListener onChange) {
        NitriteCollection collection = resolve(store);
        // Nitrite's five event names are the protocol's five: insert, update, remove, indexStart,
        // indexEnd. No mapping table needed, and none invented — the coarser wire events exist for
        // the engines that need them. Only the initial letter differs, because the Java enum
        // constants are capitalised and the wire names are not.
        String subscription =
                collection.subscribe(event -> onChange(onChange, event.getEventType().name()));
        // The handle is the subscription id, and holding it is what makes `unwatch` and a dropped
        // socket both leave the developer's application with no listener of ours in it.
        return () -> collection.unsubscribe(subscription);
    }

    private static void onChange(ChangeListener listener, String eventType) {
        listener.onChange(
                Character.toLowerCase(eventType.charAt(0)) + eventType.substring(1));
    }

    /**
     * Turns a client-supplied store name into one this adapter reported.
     *
     * <p>This is an allow-list, and it is load-bearing for a reason particular to Nitrite:
     * {@link Nitrite#getCollection(String)} <b>creates</b> a collection that does not exist.
     * Passing an unchecked name through would let a paired client litter the developer's database
     * with empty collections.
     */
    private NitriteCollection resolve(String store) {
        for (ObjectRepository<?> repository : repositories) {
            NitriteCollection collection = repository.getDocumentCollection();
            if (collection.getName().equals(store)) {
                if (transaction == null) {
                    return collection;
                }
                // By type, because that is the only handle Nitrite opens a repository by — and it
                // has to be the transaction's own, or a read would miss the writes staged beside
                // it and a write would land outside the transaction entirely.
                //
                // The keyed overload is not the same call with a null key: it looks the repository
                // up under `entityName+key` and refuses when that name does not exist, so an
                // unkeyed repository has to go through the one-argument form.
                String key = keyOf(collection.getName());
                ObjectRepository<?> scoped =
                        key == null
                                ? transaction.getRepository(repository.getType())
                                : transaction.getRepository(repository.getType(), key);
                return scoped.getDocumentCollection();
            }
        }
        if (db.listCollectionNames().contains(store)) {
            return transaction == null
                    ? db.getCollection(store)
                    : transaction.getCollection(store);
        }
        throw new BridgeException(BridgeErrorKind.BAD_REQUEST, "unknown store");
    }

    /**
     * A keyed repository is stored under {@code entityName+key}; the key is reported beside the
     * name so the client can label it, while the name stays the one addressable identity the
     * protocol's {@code store} parameter carries.
     */
    private static String keyOf(String collectionName) {
        int separator = collectionName.indexOf(Constants.KEY_OBJ_SEPARATOR);
        return separator < 0 ? null : collectionName.substring(separator + 1);
    }

    private static Set<String> columnNames(StoreSchema schema) {
        Set<String> names = new LinkedHashSet<>();
        for (StoreSchema.Column column : schema.columns()) {
            names.add(column.name());
        }
        return names;
    }

    /** Everything dangerous is off unless the embedding developer opted in (threat model rule 5). */
    public static final class Builder {
        private final Nitrite db;
        private final String id;
        private final String displayName;
        private List<ObjectRepository<?>> repositories = Collections.emptyList();
        private int sampleSize = DEFAULT_SAMPLE_SIZE;
        private boolean allowRegex;
        private boolean allowWrite;
        private boolean allowSnapshot;

        private Builder(Nitrite db, String id, String displayName) {
            this.db = db;
            this.id = id;
            this.displayName = displayName;
        }

        /** The repositories to expose; see the class javadoc for why they are not discovered. */
        public Builder repositories(ObjectRepository<?>... repositories) {
            this.repositories = Arrays.asList(repositories);
            return this;
        }

        public Builder sampleSize(int sampleSize) {
            this.sampleSize = sampleSize;
            return this;
        }

        /** Threat model F10 / criterion 9: {@code regex} is absent from {@code filterOps} until this. */
        public Builder allowRegex(boolean allowRegex) {
            this.allowRegex = allowRegex;
            return this;
        }

        public Builder allowWrite(boolean allowWrite) {
            this.allowWrite = allowWrite;
            return this;
        }

        public Builder allowSnapshot(boolean allowSnapshot) {
            this.allowSnapshot = allowSnapshot;
            return this;
        }

        public NitriteAdapter build() {
            return new NitriteAdapter(this);
        }
    }
}
