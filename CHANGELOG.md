## Unreleased

Ten changes from [@brettwooldridge](https://github.com/brettwooldridge), most of them found on a production system. Four are data-integrity fixes, three of which can end with a store that will not reopen or a query that quietly returns the wrong rows.

### Issue Fixes

- **MVStore's chunk retention and versions-to-keep are left at H2's defaults** ([#1301](https://github.com/nitrite/nitrite-java/pull/1301), [#1303](https://github.com/nitrite/nitrite-java/pull/1303))
  - `MVStoreUtils.openOrCreate` forced `setRetentionTime(0)` and `setVersionsToKeep(0)` on every store since 2020. With both at 0, H2 may reuse a chunk's blocks while the chunk map it writes at close still lists that chunk, and the file then refuses to open at all - read-only or not - with `MVStoreException: Double mark: 394/5 ... at FreeSpaceBitSet.markUsed`. Only H2's recovery mode gets past it. ([h2database/h2database#2752](https://github.com/h2database/h2database/issues/2752), [#4083](https://github.com/h2database/h2database/issues/4083), both open.)
  - A 24-thread soak of a document workload with a close every 20 seconds reproduced it on **every** run at 0/0, with and without close-time compaction, on h2-mvstore 2.4.240 and on current H2 master - and on **none** of the runs with either setting at its H2 default (45 s / 5), including a 1 s retention window. H2's own javadoc notes the retention window is what lets readers finish traversing a map.
  - New `MVStoreModuleBuilder.retentionTime(ms)` and `versionsToKeep(n)`. Both are `null` by default, which leaves H2's values in place. Passing `0` restores the old behaviour, at the cost described above.
  - **The file no longer shrinks the instant a chunk goes dead.** Reclamation now waits out the retention window, as H2 intends. `close()` still compacts synchronously.

- **A read no longer hands out anything the store holds** ([#1294](https://github.com/nitrite/nitrite-java/pull/1294))
  - A stored document on MVStore *is* the live object in the page, and MVStore serializes pages on a background thread that any write can start through `tryCommit`. Whatever a read hands back must therefore share no mutable state with it, or a caller's in-place edit is written straight into the store, bypasses the indexes, and can race the serialization into a `ConcurrentModificationException` and a store panic.
  - The cursor has cloned each document it yields since 4.x, but two gaps remained. `Document.clone()` copied the top-level map and embedded documents only, so a `List`, `Set`, `Map`, array, `byte[]`, `Date` or `Calendar` inside the copy was still the instance in the store and `found.get("tags").add(x)` reached the page. And `NitriteCollection.getById()` returned the stored instance itself.
  - `clone()` is now a deep copy: containers and arrays are copied recursively, preserving the concrete collection class where it has a public no-arg constructor and the comparator of sorted sets and maps; `Date`s and `Calendar`s are cloned; immutable values are shared, and so are values of a type the copy does not know, which the javadoc now states. `getById()` hands out a clone as `find()` does, and returns `null` for a missing id instead of passing `null` through the processor chain.
  - **The cost is a structural copy per result and per write**, since the write path clones too. That buys the guarantee that no caller can reach into the store by accident.

- **The store catalog copies its stored name set instead of mutating it in place** ([#1296](https://github.com/nitrite/nitrite-java/pull/1296))
  - `StoreCatalog.writeCollectionEntry` wrapped the catalog document in `MapMetaData`, which took the `Set` straight out of it and added the new name to that instance. On MVStore that instance is the one held in the catalog page. Creating collections in a burst interleaved with writes - which is what a data migration does - put the serializer's `HashSet.writeObject` and the catalog's `HashSet.add` on the same set at the same time, ending in `MVStoreException: Could not serialize {mapNames=[...]}` and `MVStore.panic`, after which the store is closed and every later operation throws.
  - Observed on the first start after a Xodus-to-Nitrite migration that created eleven collections in forty milliseconds. `MapMetaData` now copies, so a write always puts a *new* set and the instance a serializer may be reading is never touched - the rule `WriteOperations.update` already followed by cloning a document before merging into it.

- **An index scan skips documents removed between the lookup and the fetch** ([#1302](https://github.com/nitrite/nitrite-java/pull/1302))
  - An index scan is two steps that are not atomic under concurrent writes: the index yields the matching ids, then each document is fetched. A document removed in between came back as an `(id, null)` row. With a residual filter that row reached `Filter.apply` and threw `NullPointerException` from the filter's `document.get()`; without one the cursor handed the caller a `null` element. The by-id fast path had the same window between `containsKey` and `get`.
  - `IndexedStream` now prefetches and drops ids whose document is gone, `FilteredStream` treats a row without a document as a non-match, and the by-id path does one `get`. `skip()` still walks ids without fetching them, so under a concurrent remove a page boundary can still count an id whose document is gone - which is the same indeterminacy the removal itself introduces.

- **`clear()` and `dropIndex()` reach every layout map an index occupies** ([#1295](https://github.com/nitrite/nitrite-java/pull/1295))
  - `IndexManager.close()`, `clearAll()` and `dropIndexDescriptor()` acted only on the map name recorded in `IndexMeta`, which is the classic one. That already missed the composite map of a non-unique index: after `collection.clear()` its rows survived, and a query on that index returned the ids of the cleared documents alongside the new ones - two live documents, four results. It also broke `ChangeIdField`, whose `createIndex` found the previous index map still populated and rebuilt over it.

- **A unique index no longer rejects a document over a key that document already holds** ([#1295](https://github.com/nitrite/nitrite-java/pull/1295))
  - `addNitriteIds` treated any existing id under the key as a violation, so it counted the writer's own id against it. Another document under the key is a violation; the same document again is not - which is what a unique index over an array field with a repeated element does, and what an index rebuild or a replayed write does.

- **One collection's long write no longer stalls `getCollection` for every other collection** ([#1292](https://github.com/nitrite/nitrite-java/pull/1292))
  - `CollectionFactory.getCollection` held one lock for the whole factory and, while holding it, called `isDropped()` and `isOpen()` on the registered collection. Both take that collection's read lock. While a long write held a collection's write lock - an index rebuild, a large `remove(filter)`, an update on a big document - the one caller asking for that collection blocked *inside the factory lock*, and from then on every `getCollection` call for every other collection queued behind it.
  - Observed on a production system: one thread rebuilding an index inside `update()` for over three hours, and 349 other threads parked in `CollectionFactory.getCollection`, most of them wanting unrelated collections.
  - The registry is now read under the factory read lock, the usability check runs with no factory lock held, and the factory write lock is taken only to create or replace an entry. Callers of the busy collection still wait on it, as they should; callers of other collections no longer wait at all.

- **`MVStoreModuleBuilder.pageSplitSize` defaults to 16 KB, not 16 bytes** ([#1293](https://github.com/nitrite/nitrite-java/pull/1293))
  - It is documented as "16 KB" and is passed straight to `MVStore.Builder.pageSplitSize`, which takes **bytes**. Its value was `16`, the same literal used for `cacheSize` (megabytes) and `cacheConcurrency` (a count), so every leaf page split as soon as it held more than one entry.
  - Measured by rebuilding a 146 MB store of 46,926 entries at each setting: **93,781 pages at depth 13** with `16`; **36,096 pages at depth 12** with MVStore's own persistent-store default of 16 KB; **13,327 pages at depth 6** with 64 KB, the largest value that survives H2's `(cacheSize / cacheConcurrency) >> 4` clamp.
  - Existing files are unaffected until their pages are rewritten; there is nothing to migrate.

### Performance

- **An update that leaves an indexed value unchanged no longer rewrites the index** ([#1297](https://github.com/nitrite/nitrite-java/pull/1297))
  - `DocumentIndexWriter.updateIndexEntry` treated an index as affected whenever the update document *carried* the indexed field, and then removed and rewrote the entry. An update that writes the whole document back - the common upsert shape - carries every indexed field with its old value, so every index was rewritten on every update for nothing. On the pre-4.4.0 list layout that was a copy of the whole per-key id list twice per index per write.
  - The old and new values of each affected index are now compared, deeply so that arrays and embedded values count as equal when their contents are, and the index is skipped when they match. A dirty index is not skipped: its rebuild still has to happen on the first write.

- **A unique index stores one id per key instead of a one-element list** ([#1295](https://github.com/nitrite/nitrite-java/pull/1295))
  - A unique index kept the classic `value -> [id]` layout: a `CopyOnWriteArrayList` per key that never held more than one element, allocated and copied on every write, plus a size check standing in for the uniqueness test. It now stores the id itself (`value -> id`) in a map of its own, named with a `|unique` suffix, and enforces uniqueness by comparing the stored id with the writer's.
  - **An index still in the list layout is migrated the first time it is accessed** and the legacy map dropped, the way the composite layout already migrates a non-unique index. `IndexMap` exposes the single-id map to the scanner and the filters as one-element lists, so the read path is unchanged. The map has its own name because the RocksDB adapter decodes values by the declared type of the map they live in.

- **Equality and range index scans stream instead of materializing every id** ([#1298](https://github.com/nitrite/nitrite-java/pull/1298))
  - `NitriteIndexer.findByFilter` returns a `LinkedHashSet` of every matching id, so `find(k = v).firstOrNull()` built the whole match set before handing back one row, and a bounded page paid for the entire result. On a non-unique index over a low-cardinality field that set is a large fraction of the collection on every lookup.
  - The composite layout already keeps its rows in key order, so the two plan shapes that map onto one bounded walk of it - an equality on the indexed field, and a two-sided range on it - are now served by a lazy iterator that starts at the first key inside the bounds and stops at the first key outside them. It honours the plan's reverse scan order, skips entries removed in an open transaction, and returns a document indexed under several keys once.
  - New default methods `NitriteIndex.findNitriteIdStream` and `NitriteIndexer.findByFilterStream` return `null`, so every other index type, plugin indexer and plan shape keeps the materialized path unchanged. The covered-count shortcut that lets `size()` answer without fetching documents is kept by counting the streamed ids on demand.

- **Same-type numbers compare without going through `BigDecimal`** ([#1299](https://github.com/nitrite/nitrite-java/pull/1299))
  - `Numbers.compare` converted both operands to `BigDecimal` on every call, two allocations per comparison, and every index key comparison lands there. On a production store with numeric index keys the conversion was the hottest frame in a multi-hour index rebuild.
  - Any two integral primitives now compare as `long`s; two doubles or two floats compare as themselves once NaN and the infinities have taken the existing special-case path, with `-0.0` and `0.0` equal as `BigDecimal` treats them; two `BigDecimal`s or two `BigInteger`s use their own `compareTo`. **Every mixed pairing keeps the exact `BigDecimal` conversion**, so cross-type equality such as `Integer 1` against `Float 1.0f` is unchanged.


## Release 5.3.0 - Aug 31, 2026

### Issue Fixes

- **MVStore no longer grows without bound under repeated updates** ([#1284](https://github.com/nitrite/nitrite-java/issues/1284))
  - Updating a document leaves its old page obsolete, so a store written to far more often than it grows accumulates chunks that are mostly dead. `autoCompactFillRate(0)` had disabled reclamation since [#41](https://github.com/nitrite/nitrite-java/issues/41), and the file climbed while the live data stayed small - a reported ~800MB around 100 live documents.
  - [#41](https://github.com/nitrite/nitrite-java/issues/41) was a cursor reading a chunk that compaction had already reclaimed (`Chunk 162 no longer exists`), which is why compaction was turned off rather than fixed. Every iterator and cursor now registers an MVStore version for its lifetime, so chunks it is reading cannot be collected underneath it, and compaction is safe to enable again. Abandoned iterators are released by a `Cleaner` and by `close()`/`drop()` on the map.
  - New `MVStoreModuleBuilder.autoCompact`, **defaulting to `true`**. Set it `false` for the previous behaviour.
  - **Reclamation on close is guaranteed; reclamation while running is not.** `close()` compacts synchronously and always shrinks the file. MVStore's background compaction runs on a one-second tick and attempts its work under a *try*-lock, so a writer holding the store lock makes it skip that round: how much a long-running application gets depends on its idle time and on how many cores are available to schedule the thread on. An application that holds the database open for months should not rely on the background half alone.
  - Measured over 25 live documents and 100k updates: the file held at ~213KB against 299KB and climbing, and `close()` left 57KB against 299KB.
  - `close(-1)` is pinned to a single compaction thread for now, working around [h2database/h2database#4286](https://github.com/h2database/h2database/issues/4286), where parallel compaction races on page references in 2.4.240. It is guarded by a private lock rather than the `System.getProperties()` monitor, which would have stalled every `System.getProperty` call in the JVM for the length of a compaction.

- **Index keys normalize to double only where the conversion is exact** ([#1282](https://github.com/nitrite/nitrite-java/pull/1282))
  - `DBValue` folded every number to a double before using it as an index key. That is what makes `Integer(5)` and `Double(5.0)` the same key ([#178](https://github.com/nitrite/nitrite-java/issues/178)), which matters on stores that compare the encoded key rather than calling `compareTo` - the RocksDB adapter is one.
  - A double stops stepping by one above 2^53. Around 8.7e17 the nearest doubles are 128 apart, so ids closer than that folded onto one key: a unique index rejected an id it had never seen, and a non-unique lookup returned rows belonging to a different id. Snowflake ids, TSIDs and ULID-style identifiers all live there.
  - The fold now applies only where the value survives it. `Integer`, `Short`, `Byte` and `Float` always do; `Long` is checked by casting the double back, guarded by a range test, because `Long.MAX_VALUE`'s double rounds up to 2^63 and the cast back saturates onto `MAX_VALUE` again. `BigInteger` and `BigDecimal` are compared against the exact value of the double they produce.
  - Cross-type equality is unchanged for every value a double holds, which is the range [#178](https://github.com/nitrite/nitrite-java/issues/178) is about.
  - **Behaviour change:** a `Long` and a `BigInteger` holding the same value above 2^53 are no longer the same key on a byte-comparing store. They only matched before because both had been rounded onto the same double - the same rounding that would equally have matched a *different* id. An index built by an earlier version over such values holds the folded keys, those entries were already colliding, and it should be rebuilt.

### Improvements

- Paging coverage for the plan shapes a source-level skip has to decline - an `or` plan, a collection with removals, and an indexed filter ordered by that same index. From [#1283](https://github.com/nitrite/nitrite-java/pull/1283), which proposed pushing the offset down per plan; the skip had already landed by capability in 5.1.0/5.2.0, but those shapes are worth holding because getting them wrong returns the wrong rows rather than merely slowly.


## Release 5.2.0 - Aug 30, 2026

### New Features

- **`nitrite-bridge` — inspect a running Nitrite database from a desktop client.** The Nitrite adapter for the `dbinspect` wire protocol: collections and handed-in repositories as browsable stores, schema inferred by sampling documents and always flagged as inferred, paging over `FindOptions` skip/limit/orderBy, the JSON filter DSL, and watch over Nitrite's collection subscription. The engine-neutral core it plugs into is `org.dizitart:dbinspect-bridge`, which has no database in its dependency tree at all.
  - **Row editing is behind `allowWrite`, and whole-store `snapshot` behind `allowSnapshot`** — both `false` unless the embedding application asks, and absent from the reported capabilities while they are. A row is addressed by `_id`; an update is partial; `changes: 0` means the row was not there. `_id` inside an update's `values` is refused, because Nitrite merges an update document and it would rewrite the identity of the row it just matched.
  - **`regex` is off unless `allowRegex` is set**, and length-capped with a nested-quantifier check when it is on. `java.util.regex` backtracks and a match cannot be interrupted, so the default is the mitigation that matters.
  - Every client-supplied store name is resolved against the set the adapter reported. `Nitrite.getCollection` creates a collection that does not exist, so an unchecked name would let a paired client make one; writes go through the same allow-list.
  - The module is deliberately **absent from the reactor `<modules>`**: `dbinspect-bridge` is not on Maven Central until the dbinspect release, so a build of a clean checkout would fail on it. Build it on its own, with `dbinspect-bridge` installed locally.

- **The bridge can open a transaction** (`docs/PROTOCOL.md` §3.1). A transaction is a second adapter over the same database rather than a mode on the first, so one connection's uncommitted documents can never reach another connection's reads. Nitrite's transaction lives above the storage engine — a `TransactionStore` buffers the writes and a journal replays them on commit — so this works identically on MVStore, RocksDB and in memory.
  - `capabilities.transactions` follows `allowWrite`: that flag is the permission, this reports what the engine can undo. The transactional twin reports it `false`, because Nitrite does not nest one.
  - `listStores` counts through the transaction, since a total that left out the rows the person just staged is not read-your-own-writes.

### Performance

- Paging a collection no longer costs every row before the page - `find(filter, skipBy(n).limit(m))` now advances the storage iterator without decoding what it passes over.
  - `BoundedStream` skipped by calling `next()` on the iterator beneath it, and that iterator is below the document layer: it deserialised **every skipped document** in order to throw it away. Page latency was therefore linear in the page number - about 9 µs per skipped row on both persistent stores - so a 50 000-row collection paged at 200 rows took 3 ms for the first page and 350 ms for the two-hundredth. It is an ordinary browse, not an edge case.
  - New `SkippableIterator`, implemented by the storage-level iterators that can do better and by nothing else. `BoundedStream` uses it when it is there and falls back to the loop when it is not, so a stream that has to look at a document to know whether to skip it - a collection-scan filter, a blocking sort - is unaffected and still correct.
  - **MVStore descends the tree by index.** An MVStore page records how many entries sit beneath it, so `MVMap.getKey(n)` is a O(log n) descent rather than a scan; the skip seeks with it and continues from a cursor. Measured over 50 000 rows, 200 to a page: p95 **583.9 ms → 5.8 ms**, and the first and last pages now cost the same 0.1 ms.
  - **RocksDB steps the native iterator.** There is no seek-by-index to use, but the decode is what a skipped row actually cost; advancing past one is now a memcmp inside the block the iterator is already on. p95 **3.8 ms** over the same 50 000 rows.
  - An index scan skips its own id set the same way, without the map lookup and decode `next()` would have paid for each row.

### Issue Fixes

- Fix the index dirty marker not being persisted ([#1281](https://github.com/nitrite/nitrite-java/pull/1281))
  - `markDirty()` flipped `isDirty` on the `IndexMeta` that `get()` returned and never put it back, so nothing told the store the entry had changed and the new value was not guaranteed to be written. Both `beginIndexing()` and `endIndexing()` go through it, so the marker was unreliable in both directions: an index left half-built by a crash could come back reading as clean, and a completed one could stay marked dirty and be rebuilt on the first write after every open.


## Release 5.1.0 - Aug 22, 2026

### Improvements

- A sorted, limited `find` no longer fetches the whole collection when the sort field is indexed
  - `find(ALL, orderBy("createdAt", Descending).limit(20))` asked for 20 rows and cost what draining every stored document costs. `SortedDocumentStream` collects the entire result set before `BoundedStream` gets to drop 99% of it — and the cost is the decode, not the comparison, so it scales with document *size* as well as count. An index on the sort field bought nothing: the index was only ever used to *filter*, never to order, and page 50 cost exactly what page 1 cost because the work finished before the skip applied.
  - When the query has no filter, one sort field, a limit, and a simple unique or non-unique index on exactly that field, the sort keys are now read from that index — which already stores them — and only the documents actually returned are fetched. Measured over 2000 rows each carrying a 150-element list, a `limit(20)` page went from ~125 ms to ~1 ms, and stopped growing with the collection.
  - The index is used only when it holds exactly one entry per stored document. A multi-valued field is indexed once per element, which is detected by a duplicate-id check and an entry-count check, and falls back to the blocking sort. Ordering — including where nulls sort and how ties break — is identical either way: the same comparator runs over keys taken from the index instead of from the documents.
  - Where documents are small and cheap to read, the index walk replaces a decode that was nearly free, so a sorted page over lean rows can cost a few hundred microseconds more than before. The trade is deliberate: the loss is bounded and sub-millisecond, the win grows without bound with document size.
  - New API, all additive: `FindPlan.getSortIndexDescriptor()`, `NitriteIndex.readSortKeys(long)` and `NitriteIndexer.readSortKeys(IndexDescriptor, NitriteConfig, long)` (both `default`-implemented to return `null`, so existing indexer plugins are unaffected), and `DocumentSorter.compareValues(Object, Object, Collator)`.

## Release 5.0.0 - Aug 7, 2026

### Breaking Changes

- Removed the `distinct` find option - `FindOptions.withDistinct()` (both overloads), `FindOptions.distinct()` and `FindPlan.distinct`. It had no effect on the result set. A find never returns the same document twice, and the `or` sub-plan union has always deduplicated unconditionally, so the flag was written by the planner and never read. Callers passing `FindOptions.withDistinct()` can drop it with no change in results.

## Release 4.5.0 - Aug 7, 2026

### New Features

- Add an `exists` filter - `where("field").exists()` matches the documents which have the field, irrespective of its value; `where("field").exists().not()` matches those which do not.
  - A field explicitly set to `null` is present and matches. This is the case no existing filter could express: `eq(null)` and `notEq(null)` cannot tell a missing field apart from one holding `null`, so "has this document been given a value for this field at all" was not answerable.
  - The filter deliberately does not extend `ComparableFilter` and so always runs as a collection scan. A missing field and a field holding `null` are stored under the same null key in an index, so an index scan could not tell them apart and would disagree with a collection scan.
  - Embedded fields are addressed by their dotted path (`where("address.city").exists()`), the same way `Document.containsField` resolves them.

### Issue Fixes

- Fix JVM crash (SIGSEGV) in the RocksDB backend when a query pages past the end of a collection
  - `EntrySet`, `KeySet` and `ValueSet` close the native `RocksIterator` the first time `hasNext()` answers `false`, but `hasNext()` is idempotent by contract and a second call reached `isValid()` on the freed handle. `BoundedStream`'s skip loop exhausts the iterator and then the caller asks once more, so any `find(..., skipBy(n))` whose skip exceeds the number of remaining records hit it — an ordinary paging request, not an edge case. The existing `catch (AssertionError)` only covered the case where assertions are enabled (`-ea`), which is how the tests ran but not how an application runs: without assertions the same call is a `SIGSEGV` in `Java_org_rocksdb_RocksIterator_isValid0Jni` that takes the whole process down.
  - `hasNext()` now returns `false` when the iterator no longer owns its handle, so it never touches a closed iterator and stays idempotent.

## Release 4.4.3 - Jul 20, 2026

### Issue Fixes

- Fix `field.eq(x)` / `field.in(..)` on an array (list) field silently matching nothing when the filter runs as a collection scan
  - Array membership is matched element-wise on the index path (arrays are indexed per element) but was matched by whole-value equality (`deepEquals`) on the collection-scan path, so a query's results depended on whether an index existed or was chosen by the planner. Combined with the 4.4.2 planner change (#1266) — which correctly relegates the non-winning-index filter to a collection scan — an AND of an indexed array `eq` and a bounded range on a second indexed field left the array `eq` running as a collection scan, where it matched no documents.
  - `EqualsFilter` and `InFilter` now match an array/`Iterable` field by element containment on the collection-scan path, mirroring `applyOnIndex`, so results are the same regardless of index presence.

## Release 4.4.2 - Jul 8, 2026

### Issue Fixes

- Fix `ClassCastException` when an AND filter combines an equality (or other comparable) filter on one indexed field with a bounded range on a second, differently-typed indexed field #1266
  - The query planner picked the best-matching index per field independently while scanning candidate indexes, but accumulated filters from every candidate it visited into one shared set instead of keeping only the winning index's filters. The resulting index scan filter set could carry filters from two unrelated indexes (e.g. a `String`-valued `eq` filter alongside a `Long`-valued range pair), which were then all applied against whichever single index the planner picked, comparing a value of the wrong type against that index's keys.
  - The planner now selects a single best-matching index descriptor and only keeps that index's own filters for the index scan; filters on any other field fall back to a post-filter (collection scan) step as before.

## Release 4.4.1 - Jul 2, 2026

### Security Fixes

- Fix unfiltered Java deserialization in the legacy v1 database migration path (CWE-502, GHSA-9297-g93h-86gg, CVSS 9.8)
  - Opening a file-based store runs `MVStoreUtils.testForMigration()`, which for a legacy v1-format file deserialized stored values through `ObjectInputStream.readObject()` with no class restriction. Any `Serializable` class on the embedding application's classpath could be instantiated, so a suitable gadget chain (e.g. from commons-collections) made a malicious `.db` file a remote-code-execution vector.
  - The v1-compat deserializer now enforces a JEP 290 allowlist filter that only permits Nitrite's own types and standard JDK types; any other class is rejected before its `readObject`/`readResolve` callbacks can run. Applications that open Nitrite database files from untrusted sources (e.g. "import"/"restore backup" features) should upgrade.

### Issue Fixes

- Fix intermittent `ConcurrentModificationException` and spurious `UniqueConstraintException` from unique and full-text indexes on the MVStore backend (regression introduced by the #1260 index rework in 4.4.0)
  - Unique and full-text indexes still store a `List<NitriteId>` value per key. 4.4.0 switched that list from `CopyOnWriteArrayList` to a plain `ArrayList`, which is mutated in place after being written to the map. MVStore serializes dirty page values on a background thread, so that in-place mutation races with the serializer and threw `ConcurrentModificationException` (and could corrupt the id list, surfacing later as a false unique-key violation) — even under single-threaded use.
  - These index value lists are `CopyOnWriteArrayList` again, so each mutation swaps the backing array atomically and the background serializer always sees a stable snapshot. The composite-key layout for non-unique indexes (the actual #1260 optimization) is unchanged.

## Release 4.4.0 - Jul 2, 2026

### Upgrade Notes

- Non-unique single-field indexes now use a composite-key on-disk layout (one `(value, id)` row per entry) instead of a single growing id list per value #1260
  - The public API is unchanged. Existing databases are upgraded automatically: a legacy array-format non-unique index is rebuilt into the new layout the first time it is opened, and the old index map is dropped.
  - The storage format change is forward-only — once a database has been opened by this version, it can no longer be read by an earlier version of Nitrite. Back up before upgrading if you may need to roll back.

### Performance Improvements

- Fixed performance degradation when inserting thousands of documents that share the same non-unique index key #1260
  - The old layout re-wrote (and, on persistent stores, re-serialized) an ever-growing id list on every insert, making bulk inserts O(n²). The composite-key layout makes each insert and removal an O(log n) point operation across all backends (in-memory, MVStore and RocksDB).
  - RocksDB orders keys by their serialized bytes, so the composite key uses an order-preserving encoding (correct ranges over negative numbers, variable-length strings, dates and booleans).
- Made `in` filter index scans look up each value directly instead of scanning every index entry, so `in` queries on large indexed collections are now as fast as `eq` instead of degrading to a full index walk #1258

### Issue Fixes

- Fix `DocumentSorter` violating the `Comparator` contract when two documents both have a null sort key, which caused intermittent `IllegalArgumentException: Comparison method violates its general contract!` from `orderBy` on fields with multiple null values #1261
- Fix indexed `lt`/`lte` filters returning an empty result when the indexed field contains any null value; the forward index scan now starts from the first non-null key #1262
- Fix descending indexed `lt`/`lte` filters leaking null-valued documents into the result on reopened persistent stores (MVStore and RocksDB); stored null index keys are now normalized to the `DBNull` sentinel in every index navigation
- Fix the RocksDB adapter failing to round-trip the null index key through Kryo, and decoding scanned keys with the wrong type when a range scan probe and the stored key have different classes
- Fix `in`/`notIn` filters on the `_id` field not matching legacy String ids written by pre-4.4 databases, while `eq`/`getById` on the same rows matched #1263
- Fix `eq`/`notEq` filters on the `_id` field for legacy String ids: `eq('_id', "3")` or `eq('_id', 3)` threw `ClassCastException` in the byId fast path, and `notEq`/negated `eq` failed to exclude legacy rows during collection scans

## Release 4.3.3 - Jun 26, 2026

### New Changes

- Support for interface entity types with EntityDecorator #1183
- Fixed NearFilter to support geodesic distance for geographic coordinates #1185
  - Added GeoPoint class for explicit geographic coordinate support
  - Created GeoNearFilter for geodesic distance queries
  - Implements two-pass query execution to eliminate false positives from bounding box approximation
  - Added comprehensive test suite for geographic coordinate support
- Upgraded Jackson to version 3 #1221
- Build now targets JUnit 6 and requires Java 17 to build/test, while keeping Java 11 bytecode compatibility for the published artifacts #1179

### Performance Improvements

- Optimized index scans for multi-bound range queries (e.g. `gt` combined with `lt` on the same field)
- Added covered-count optimization so `size()`/`count()` is answered directly from index scans and plain full scans without fetching and deserializing every matching document

### Issue Fixes

- Fix `in`/`notIn` filters not using the index while querying a collection #1258
- Fix record ID match for legacy string keys in OR clause #1246
- Fix OR filters returning duplicate documents when using multiple indexes #1184
- Fix inconsistent numeric filtering across types with indexes #1175
- Fix elemMatch queries to use array field indexes #1174
- Fix native-image build: initialize JUnit MethodSegmentResolver at runtime #1189

### Maintenance

- Bumped production and development dependencies across the project (grouped Dependabot updates)

## Release 4.3.2 - Sep 25, 2025

### Issue Fixes

- Fix for small safety/cleanup in Nitrite interface (map lookups, closed check, name trim) #1161
- Fix for updating to 4.3.1 causes existing databases to not open correctly #1162

## Release 4.3.1 - Sep 23, 2025

### New Changes

- GraalVM support for nitrite-mvstore-adapter #995
- Event subscription api changes

### Issue Fixes

- Fix for `Document.getFields()` not returning iterable fields
- Fix for failing tests on systems with non-ENGLISH locale #994
- Fix for NPE in `DefaultTransactionalRepository` #1032
- Fix for JPMS issue #1035
- Fix for RocksDB adapter issue #1093

## Release 4.3.0 - Jul 1, 2024

### New Changes

- Nitrite now supports JPMS. It is now modular and can be used in Java 9 or above.
- Version upgrade for several dependencies
- Repository type validation can be disabled in `NitriteBuilder` as a fix for #966

### Issue Fixes

- Fix for #935
- Fix for #948
- Fix for #961
- Fix for #966
- Fix for #977
- Fix for #990

## Release 4.2.2 - Mar 5, 2024

### Issue Fixes

- Fix for #917
- Fix for #916
- Fix for #911
- Version upgrade for several dependencies

## Release 4.2.1 - Feb 19, 2024

### Issue Fixes

- Fix for #901
- Fix for #902
- Version upgrade for several dependencies

## Release 4.2.0 - Jan 6, 2024

### New Changes

- Nitrite API has been re-written from ground up. It is now more stable and performant. But there are breaking changes. Please read the [guide](https://nitrite.dizitart.com/) for more details.
- Nitrite now requires Java 11 or above.
- Nitrite is now modular. It has been now divided into several modules. You can use only the modules you need.
- Modular storage adapters are now available. You can use only the storage adapter you need.
- MVStore version upgraded to 2.2.224
- RocksDB has been introduced as a new storage adapter.
- Nitrite now supports transaction.
- Nitrite now supports schema migration.
- Nitrite now supports spatial indexing and search
- Nitrite now supports compound indexes.
- Nitrite now support import/export of data in JSON format.
- Build system has been migrated to Maven.
- Nitrite DataGate has been deprecated.
- Nitrite Explorer has been deprecated.

## Release 3.4.4 - Mar 23, 2022

### Issue Fixes

- Emergency fix for #697

## Release 3.4.3 - Dec 12, 2020

### Issue Fixes

- Random crashes with exception "Fatal Exception: java.lang.IllegalStateException: Chunk 55267 not found" #386
- Null pointer on updating full text index #366
- Breton list is actually Brazilian Portuguese #251

## Release 3.4.2 - Jun 2, 2020

### Issue Fixes

- Fix for NoClassDefFoundError in isObjectStore #220
- Fix for Full text index is not updated field update #222

## Release 3.4.1 - Mar 25, 2020

### Issue Fixes

- Fix for Deadlock in latest 3.4.0 #212

## Release 3.4.0 - Mar 24, 2020

### Issue Fixes

- Fix for UniqueConstraintException when upserting #193
- Fix for several NPEs under certain edge case conditions #203
- Fix for Off-Heap store memory utilization issues #211

## Release 3.3.0 - Oct 19, 2019

### New Changes

- Upgrade MVStore version to 1.4.200
- Add Support for Off-Heap Memory #160
- Offer close und update methods for TextIndexingService #176
- Allow to access collection of IDs from find result #165
- Sorting with accents #144

### Issue Fixes

- Null pointer exception when querying data #185
- Documentation : support for querying embedded objects #157
- Documentation: minSdkVersion should be 19 #167
- Index not removed for fulltext-indexed field when using a third-party TextIndexingService #174
- Performance enhancements for InFilter() #173
- Filtering on indexed fields with multiple Number only retrieves same type as given Comparable #178
- Unique constraints apparently not checked when updating document #151

## Release 3.2.0 - Mar 16, 2019

### New Changes

- Upgrade MVStore version to 1.4.198 #134
- Improve `Mappable` performance using constructor cache #133
- Make `ObjectRepository` and `NitriteCollection` implements `Closeable` #108

### Issue Fixes

- Database file remains locked after failed connection #116
- Exception when removing a document on a text indexed collection #114
- NitriteBuilder openOrCreate returns silently null #112

## Release 3.1.0 - Sep 1, 2018

### New Changes

- Keyed `ObjectRepository` support #78
- Podam version upgraded to resolve missing JAX-WS dependency in Java 9 #90
- MVStore upgraded to latest release #69
- Introduced a utility method to register jackson modules in `NitriteBuilder` #94
- Null order support during sort #98
- `@InheritIndices` now works for fields with any modifier #101

### Issue Fixes

- Fixed documentation for MapperFacade #100
- Added documentation for @NitriteId annotation #102
- Changes to text index not saved correctly #105
- Closing the database recreates dropped collections #106

## Release 3.0.2 - Aug 2, 2018

### Issue Fixes

- Recover should return success/failure #89
- Reopening issue #72, with variation of failing scenario still broken in 3.0.1 #93

## Release 3.0.1 - Jul 21, 2018

### New Changes

- Jackson modules are auto discoverable #68
- Refactoring of NitriteMapper #74
- Make runtime shutdown hook optional #84

### Issue Fixes

- Fix for order by using a nullable columns #72
- Fix for DataGate server for Windows #71
- Intermittent NPE in remove #76
- Fix for NPE in indexing #77
- Documentation for POJO annotation #81

## Release 3.0.0 - Apr 8, 2018

### New Changes

- `KNO2JacksonMapper` is now extendable
- Support for `NitriteId` as id field of an object
- Object's property can be updated with null
- Support for `java.time` & it's backport
- Change in update operation behavior (breaking changes)

### Issue Fixes

- ConcurrentModificationException in `NitriteEventBus` - #52
- Duplicate `@Id` in concurrent modification - #55
- Fixed a race condition while updating the index entries - #58
- Fix for sort operation - #62
- Version upgraded for several dependencies - #64

## Release 2.1.1 - Feb 4, 2018

### New Changes

- Kotlin version upgrade to 1.2.20
- Data import export extension added in potassium-nitrite

### Issue Fixes

- Fixes concurrency problem while compacting database - #41
- Lucene example fixed for update and lucene version upgraded - #44
- Fixed collection registry and repository registry - #42
- Readme updated with potassium-nitrite - #49

## Release 2.1.0 - Dec 7, 2017

### New Changes

- Introduced potassium-nitrite - kotlin extension library for nitrite
- Multi-language text tokenizer support - #36
- Cursor join - #33
- Inherit `@Id`, `@Index` annotations from super class - #37
- Default executor behaves like `CachedThreadPool` executor - #32

### Issue Fixes

- Put a check on object if it is serializable - #31

## Release 2.0.1 - Oct 24, 2017

### Issue Fixes

- Fix for SOE - #29
- Fix for sync issue - #25
- Detailed log added in `JacksonMapper`

## Release 2.0.0 - Aug 13, 2017

### New Changes

- Introduced `Mappable` interface to speed up pojo to document conversion in Android - #18 

### Breaking Changes

- `NitriteMapper` and `JacksonMapper` moved from package `org.dizitart.no2.internals` to `org.dizitart.no2.mapper`

### Issue Fixes

- Fix for `ObjectFilters.ALL` - #14
- Fix for `dropIndex()` - #22 
- Documentation added - #12, #20 

## Release 1.0.1 - Jun 1, 2017

- Minor bug fixes for DataGate server - #6 , #7 , #8 
- File parameter added while opening a database - #5 
- Documentation updated - #3 , #8 

## First Release - Apr 25, 2017

- Initial release

