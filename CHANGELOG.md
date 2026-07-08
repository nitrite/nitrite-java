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

