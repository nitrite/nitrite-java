package org.dizitart.no2.bridge;

import org.dizitart.dbinspect.AdapterCapabilities;
import org.dizitart.dbinspect.BridgeAdapter;
import org.dizitart.dbinspect.BridgeErrorKind;
import org.dizitart.dbinspect.BridgeException;
import org.dizitart.dbinspect.PageRequest;
import org.dizitart.dbinspect.QueryPage;
import org.dizitart.dbinspect.StoreInfo;
import org.dizitart.dbinspect.StoreSchema;
import org.dizitart.dbinspect.Values;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.FindOptions;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.Constants;
import org.dizitart.no2.common.SortOrder;
import org.dizitart.no2.common.tuples.Pair;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.repository.ObjectRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
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

    private NitriteAdapter(Builder builder) {
        this.db = builder.db;
        this.id = builder.id;
        this.displayName = builder.displayName;
        this.repositories = Collections.unmodifiableList(new ArrayList<>(builder.repositories));
        this.sampleSize = builder.sampleSize;
        this.allowRegex = builder.allowRegex;

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
                        .snapshot(builder.allowSnapshot)
                        .filterOps(filterOps)
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

    @Override
    public List<StoreInfo> listStores() {
        List<StoreInfo> stores = new ArrayList<>();
        for (String name : db.listCollectionNames()) {
            stores.add(new StoreInfo(name, "collection", db.getCollection(name).size()));
        }
        for (ObjectRepository<?> repository : repositories) {
            NitriteCollection collection = repository.getDocumentCollection();
            stores.add(
                    new StoreInfo(
                            collection.getName(),
                            "repository",
                            collection.size(),
                            keyOf(collection.getName())));
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
                String type = typeOf(entry.getSecond());
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
                row.put(cell.getFirst(), encode(cell.getSecond()));
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
                return collection;
            }
        }
        if (db.listCollectionNames().contains(store)) {
            return db.getCollection(store);
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

    /**
     * {@link Values#encode} is engine-neutral and so knows nothing of {@link Document}, which is
     * neither a {@code Map} nor a {@code Collection} and would degrade to its {@code toString}. A
     * repository row with a nested entity in it is the common case, not an exotic one, so the
     * adapter unwraps its own type and hands the rest to the core.
     */
    private static Object encode(Object value) {
        if (value instanceof Document) {
            Map<String, Object> nested = new LinkedHashMap<>();
            for (Pair<String, Object> field : (Document) value) {
                nested.put(field.getFirst(), encode(field.getSecond()));
            }
            return nested;
        }
        if (value instanceof Iterable) {
            List<Object> encoded = new ArrayList<>();
            for (Object element : (Iterable<?>) value) {
                encoded.add(encode(element));
            }
            return encoded;
        }
        return Values.encode(value);
    }

    private static Set<String> columnNames(StoreSchema schema) {
        Set<String> names = new LinkedHashSet<>();
        for (StoreSchema.Column column : schema.columns()) {
            names.add(column.name());
        }
        return names;
    }

    /**
     * A closed set, so the client has a known set of renderings rather than whatever
     * {@code getClass().getSimpleName()} prints. Null means this document said nothing about the
     * field's type.
     */
    private static String typeOf(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean) {
            return "bool";
        }
        if (value instanceof Integer
                || value instanceof Long
                || value instanceof Short
                || value instanceof Byte) {
            return "int";
        }
        if (value instanceof Number) {
            return "real";
        }
        if (value instanceof CharSequence) {
            return "text";
        }
        if (value instanceof Date || value instanceof java.time.temporal.Temporal) {
            return "date";
        }
        if (value instanceof byte[]) {
            return "blob";
        }
        // Before the Iterable branch on purpose: Document is itself an Iterable of its fields, and
        // a nested document reported as a list is a wrong rendering rather than a vague one.
        if (value instanceof Document || value instanceof Map) {
            return "document";
        }
        if (value instanceof Iterable || value.getClass().isArray()) {
            return "list";
        }
        return "text";
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
